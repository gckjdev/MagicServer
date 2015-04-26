package com.orange.game.model.service;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.common.upload.UploadManager;
import com.orange.common.utils.DateUtil;
import com.orange.common.utils.FileUtils;
import com.orange.common.utils.StringUtil;
import com.orange.common.utils.ZipUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.MessageStat;
import com.orange.game.model.dao.Relation;
import com.orange.game.model.dao.User;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.dao.bbs.BBSBoard;
import com.orange.game.model.dao.bbs.BBSPost;
import com.orange.game.model.dao.song.Song;
import com.orange.game.model.manager.OpusManager;
import com.orange.game.model.manager.ScoreManager;
import com.orange.game.model.manager.bbs.BBSBoardPostManager;
import com.orange.game.model.manager.bbs.BBSManager;
import com.orange.game.model.manager.bbs.BBSTopPostManager;
import com.orange.game.model.manager.friend.FriendManager;
import com.orange.game.model.manager.message.UserMessageManager;
import com.orange.game.model.manager.message.UserMessageStatisticManager;
import com.orange.game.model.manager.opus.AllTimeTopOpusManager;
import com.orange.game.model.manager.opus.FeatureOpusManager;
import com.orange.game.model.manager.opus.HotTopOpusManager;
import com.orange.game.model.manager.opus.contest.ContestLatestOpusManager;
import com.orange.game.model.manager.opus.contest.ContestTopOpusManager;
import com.orange.game.model.manager.opus.contest.MyContestOpusManager;
import com.orange.game.model.manager.useropus.UserFavoriteOpusManager;
import com.orange.game.model.xiaoji.AbstractXiaoji;
import com.orange.game.model.xiaoji.XiaojiFactory;
import com.orange.game.api.service.ElasticsearchService;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateDataFileService {

    public static final Logger log = Logger.getLogger(CreateDataFileService.class
            .getName());
    private static final int FILE_GEN_NOT_DONE = 0;
    private static final int FILE_GEN_DONE = 1;
    private static final int FILE_GEN_RESULT_SUCCESS = 0;
    private static final int FILE_GEN_RESULT_ERROR_DATA_NULL = 1;
    private static final int FILE_GEN_RESULT_ERROR_CREATE_COMPRESSED_ZIP_FILE = 2;
    private static final int FILE_GEN_RESULT_ERROR_CREATE_UNCOMPRESSED_ZIP_FILE = 3;
    // thread-safe singleton implementation
    private static CreateDataFileService service = new CreateDataFileService();
    ExecutorService executor = Executors.newSingleThreadExecutor();
    ExecutorService processExecutor = Executors.newFixedThreadPool(20);

    private CreateDataFileService() {

    }

    public static CreateDataFileService getInstance() {
        return service;
    }

    public ExecutorService getMultiExecutor(){
        return processExecutor;
    }

    public ExecutorService getSingleExecutor(){
        return executor;
    }

    private static void updateDB(MongoDBClient mongoClient, int fileGenResult, int newDataLen, UserAction userAction, String localZipFileUrl) {
        BasicDBObject updateQuery = new BasicDBObject("_id", new ObjectId(userAction.getActionId()));

        BasicDBObject updateValue = new BasicDBObject();

//		if (newDataLen > 0 && newDataLen != userAction.getDataLength()){
        if (newDataLen > 0) {

            // set new data len and recalcuate score
            userAction.setDataLength(newDataLen);
            ScoreManager.calculateScore(userAction, false);
            ScoreManager.calculateAndSetHistoryDrawScore(userAction);

            double newHot = userAction.getHot();
            double newHistoryScore = userAction.getHistoryScore();

            updateValue.put(DBConstants.F_DRAW_DATA_LEN, newDataLen);
            if (newHot > 0.0) {
                updateValue.put(DBConstants.F_HOT, newHot);
            }

            if (newHistoryScore > 0.0) {
                updateValue.put(DBConstants.F_HISTORY_SCORE, newHistoryScore);
            }

        }

        updateValue.put(DBConstants.F_FILE_GEN, FILE_GEN_DONE);
        updateValue.put(DBConstants.F_FILE_GEN_RESULT, fileGenResult);
        if (fileGenResult == FILE_GEN_RESULT_SUCCESS && localZipFileUrl != null) {
            updateValue.put(DBConstants.F_DRAW_DATA_URL, localZipFileUrl);
        }

        BasicDBObject update = new BasicDBObject();
        update.put("$set", updateValue);

        mongoClient.updateOne(DBConstants.T_OPUS, updateQuery, update);
    }

    public static String createFileAndUpdate(MongoDBClient mongoClient, UserAction userAction, boolean isCreateCompressedFile) {
        int fileGenResult = FILE_GEN_RESULT_SUCCESS;

        log.info("<createFileAndUpdate> for opus id=" + userAction.getActionId());

        // handle data here
        byte[] data = (byte[]) userAction.getDbObject().get(DBConstants.F_DRAW_DATA);
        if (data == null) {
            fileGenResult = FILE_GEN_RESULT_ERROR_DATA_NULL;
            updateDB(mongoClient, fileGenResult, 0, userAction, null);
            return null;
        } else {

            String timeDir = UploadManager.getTimeFilePath(userAction.getCreateDate());
            String dir = DataService.getDrawFileUploadLocalDir() + timeDir;
            FileUtils.createDir(dir);

            String fileName = StringUtil.randomUUID();
            String localZipFileUrl = timeDir + "/" + fileName + ".zip";

            // create compress zip file
            if (isCreateCompressedFile) {
                int oldDataLen = ZipUtil.createZipFile(DataService.getInstance().generateLocalDrawDataUrl(localZipFileUrl, true), UploadManager.DEFAULT_ZIP_FILE_NAME, data);
                if (oldDataLen <= 0) {
                    fileGenResult = FILE_GEN_RESULT_ERROR_CREATE_COMPRESSED_ZIP_FILE;
                }
            }

            // create uncompress zip file
            int newDataLen = DataService.getInstance().createUncompressDataFile(data, localZipFileUrl);
            if (newDataLen <= 0) {
                fileGenResult = FILE_GEN_RESULT_ERROR_CREATE_UNCOMPRESSED_ZIP_FILE;
            }

            // update DB
            // file gen flag, data len,
            updateDB(mongoClient, fileGenResult, newDataLen, userAction, localZipFileUrl);

            return localZipFileUrl;
        }
    }

    public void createFileAndUpdateAtBackground(final MongoDBClient mongoClient, final UserAction userAction, final boolean isCreateCompressedFile) {
        executor.execute(new Runnable() {

            @Override
            public void run() {
                createFileAndUpdate(mongoClient, userAction, isCreateCompressedFile);
            }
        });

    }

    // db.action.ensureIndex({type:1,file_gen:1})
    public List<DBObject> getFeeds(MongoDBClient mongoClient, int offset, int limit) {

        BasicDBObject query = new BasicDBObject();

        BasicDBList typeList = new BasicDBList();
        typeList.add(UserAction.TYPE_DRAW);
        typeList.add(UserAction.TYPE_DRAW_TO_CONTEST);
        typeList.add(UserAction.TYPE_DRAW_TO_USER);
        BasicDBObject in = new BasicDBObject("$in", typeList);

        query.put(DBConstants.F_TYPE, in);
        query.put(DBConstants.F_FILE_GEN, new BasicDBObject("$ne", FILE_GEN_DONE));

        BasicDBObject fields = new BasicDBObject();
        fields.put(DBConstants.F_GUESS_WORD_LIST, 0);
        fields.put(DBConstants.F_USERID_LIST, 0);
        fields.put(DBConstants.F_OPUS_RELATED_USER_ID, 0);

        log.info("<getFeeds> query=" + query.toString() + ", offset=" + offset + ", limit=" + limit);
        DBCursor cursor = mongoClient.find(DBConstants.T_OPUS, query, fields, null, offset, limit);
        if (cursor == null) {
            log.info("<getFeeds> no data, cursor null");
            return Collections.emptyList();
        }

//		if (cursor.count() == 0){
//			log.info("<getFeeds> no data, cursor count is 0");
//			cursor.close();
//			return Collections.emptyList();			
//		}

        List<DBObject> retList = new ArrayList<DBObject>();
        int count = 0;
        while (cursor.hasNext()) {
            count++;
            log.info("<process feed> read " + count + " record");
            DBObject obj = cursor.next();
            retList.add(obj);
            if (obj != null) {
                UserAction userAction = new UserAction(obj);

                int fileGenResult = FILE_GEN_RESULT_SUCCESS;

                // handle data here
                byte[] data = (byte[]) userAction.getDbObject().get(DBConstants.F_DRAW_DATA);
                if (data == null) {
                    fileGenResult = FILE_GEN_RESULT_ERROR_DATA_NULL;
                    updateDB(mongoClient, fileGenResult, 0, userAction, null);
                } else {

                    String timeDir = UploadManager.getTimeFilePath(userAction.getCreateDate());
                    String dir = DataService.getDrawFileUploadLocalDir() + timeDir;
                    FileUtils.createDir(dir);

                    String fileName = userAction.getActionId();
                    String localZipFileUrl = timeDir + "/" + fileName + ".zip";

                    // create compress zip file
                    int oldDataLen = ZipUtil.createZipFile(DataService.getInstance().generateLocalDrawDataUrl(localZipFileUrl, true), UploadManager.DEFAULT_ZIP_FILE_NAME, data);
                    if (oldDataLen <= 0) {
                        fileGenResult = FILE_GEN_RESULT_ERROR_CREATE_COMPRESSED_ZIP_FILE;
                    }

                    // create uncompress zip file
                    int newDataLen = DataService.getInstance().createUncompressDataFile(data, localZipFileUrl);
                    if (newDataLen <= 0) {
                        fileGenResult = FILE_GEN_RESULT_ERROR_CREATE_UNCOMPRESSED_ZIP_FILE;
                    }

                    // update DB
                    // file gen flag, data len,
                    updateDB(mongoClient, fileGenResult, newDataLen, userAction, localZipFileUrl);
                }


            }
        }
        cursor.close();
        log.info("<getFeeds> complete.");
        return retList;
    }

    private String getHotRedisNamespace(int language) {
        if (language == 2) {
            return DBConstants.REDIS_NS_HOT_FEED_EN;
        } else {
            return DBConstants.REDIS_NS_HOT_FEED_CN;
        }
    }

    public void moveHotDataToRedis(MongoDBClient mongoClient, int language, final int offset, final int limit) {
        log.info("<moveHotDataToRedis> offset = " + offset + " and limit = " + limit);
        log.info("<moveHotDataToRedis> start -------");
        String uid = "88888888888888888";
        HotTopOpusManager hotTopOpusManager = XiaojiFactory.getInstance().getDraw().hotTopOpusManager(language);
        List<UserAction> userActions = OpusManager.getHotFeedList(mongoClient, uid, offset, limit, language, false);
        if (userActions == null) {
            log.info("<moveHotDataToRedis> hot feed action list is null ");
            return;
        }
        log.info("<moveHotDataToRedis> hot feed action list size = " + userActions.size());
        if (userActions != null) {
            log.info("<moveHotDataToRedis> get data count = " + userActions.size());
            for (UserAction userAction : userActions) {
                double score = userAction.getHot();
                String id = userAction.getActionId();
                hotTopOpusManager.updateOpusHotTopScore(id, score);
            }
        }

    }


    //type 1 5 9 no opus id
    //

    public void moveAllTimeDataToRedis(MongoDBClient mongoClient, int language, int offset, int limit) {
        String uid = "8888888888888888";
        AllTimeTopOpusManager allTimeTopOpusManager = XiaojiFactory.getInstance().getDraw().allTimeTopOpusManager(language);
        List<UserAction> userActions = OpusManager.getHistoryTopOpusList(mongoClient, uid, language, offset, limit, false);
        if (userActions != null) {
            log.info("<moveAllTimeDataToRedis> get data count = " + userActions.size());
            for (UserAction userAction : userActions) {
                String id = userAction.getActionId();
                double score = userAction.getHistoryScore();
                allTimeTopOpusManager.updateOpusHistoryTopScore(id, score);
            }
        } else {
            log.info("<moveAllTimeDataToRedis> but data is null");
        }
    }

    public void moveFeatureDataToRedis(MongoDBClient mongoClient, int language, int offset, int limit) {

        FeatureOpusManager featureOpusManager = XiaojiFactory.getInstance().getDraw().featureOpusManager(language);


        BasicDBObject query = new BasicDBObject();
        query.put(DBConstants.F_LANGUAGE, language);

        BasicDBObject rtnField = new BasicDBObject();
        rtnField.put(DBConstants.F_OPUS_ID, 1);
        rtnField.put(DBConstants.F_BY_USER_ID, 1);
        DBObject orderBy = new BasicDBObject();
        orderBy.put(DBConstants.F_MODIFY_DATE, 1);
        DBCursor cursor = mongoClient.find(DBConstants.T_RECOMMEND_OPUS, query, rtnField, orderBy, offset, limit);
        if (cursor != null) {
            log.info("<moveFeatureDataToRedis> get data count = " + cursor.size());
            while (cursor.hasNext()) {
                DBObject dbObject = cursor.next();
                String opusId = (String) dbObject.get(DBConstants.F_OPUS_ID);
                String userId = (String) dbObject.get(DBConstants.F_BY_USER_ID);
                featureOpusManager.featureOpus(userId, opusId);
            }
            cursor.close();
        } else {
            log.info("<moveFeatureDataToRedis> but data is null");
        }


    }

    public void moveContestMyOpusToMongoIndex(MongoDBClient mongoClient, int language, int offset, int limit) {
        String uid = "88888888888888888888";
        String contestIds[] = new String[]{"888888888888888888890000",
                "888888888888888888890001",
                "888888888888888888890002",
                "888888888888888888890003",
                "888888888888888888890004",
                "888888888888888888890005",
                "888888888888888888890006"};
        for (String contestId : contestIds) {
            List<UserAction> userActions = OpusManager.getContestHotOpusList(mongoClient, uid, contestId, language, offset, limit);
            MyContestOpusManager contestOpusManager = XiaojiFactory.getInstance().getDraw().myContestOpusManager(contestId);
            if (userActions != null) {
                for (UserAction userAction : userActions) {
                    String userPhotoId = userAction.getActionId();
                    String userId = userAction.getCreateUserId();
                    contestOpusManager.insertIndex(userId, userPhotoId);
                }
            }
        }
    }

    public void moveContestTopDataToRedis(MongoDBClient mongoClient, int language, int offset, int limit) {
        String uid = "88888888888888888888";
        String contestIds[] = new String[]{"888888888888888888890000",
                "888888888888888888890001",
                "888888888888888888890002",
                "888888888888888888890003",
                "888888888888888888890004",
                "888888888888888888890005",
                "888888888888888888890006"};
        for (String contestId : contestIds) {
            List<UserAction> userActions = OpusManager.getContestHotOpusList(mongoClient, uid, contestId, language, offset, limit);
            ContestTopOpusManager contestTopOpusManager = XiaojiFactory.getInstance().getDraw().contestTopOpusManager(contestId);
            if (userActions != null) {
                for (UserAction userAction : userActions) {
                    String id = userAction.getActionId();
                    double score = userAction.getContestScore();
                    contestTopOpusManager.updateOpusTopScore(id, score);
                }
            }
        }

    }

    public void moveContestLatestDataToRedis(MongoDBClient mongoClient, int language, int offset, int limit) {
        String uid = "888888888888888888";
        String contestIds[] = new String[]{
                "888888888888888888890000",
                "888888888888888888890001",
                "888888888888888888890002",
                "888888888888888888890003",
                "888888888888888888890004",
                "888888888888888888890005",
                "888888888888888888890006"};
        for (String contestId : contestIds) {
            List<UserAction> userActions = OpusManager.getContestNewOpusList(mongoClient, uid, contestId, language, offset, limit);
            ContestLatestOpusManager contestLatestOpusManager = XiaojiFactory.getInstance().getDraw().contestLatestOpusManager(contestId);
            if (userActions != null) {
                for (UserAction userAction : userActions) {
                    String id = userAction.getActionId();
                    int createDate = DateUtil.dateToInt(userAction.getCreateDate());
                    contestLatestOpusManager.updateContestLatestIndex(id, createDate);
                }
            }
        }
    }

    public void moveUserSaveOpus(MongoDBClient mongoClient, int offset, int limit) {
        BasicDBObject query = new BasicDBObject();
        //query.put(DBConstants.F_UID, uid);

        BasicDBObject returnFields = new BasicDBObject();
        returnFields.put(DBConstants.F_OPUS_ID, 1);
        returnFields.put(DBConstants.F_UID, 1);
        BasicDBObject orderBy = new BasicDBObject(DBConstants.F_CREATE_DATE, 1);
        log.info("<getSavedOpusList> query=" + query.toString() + ", fields=" + returnFields + ", orderBy=" + orderBy.toString());
        DBCursor cursor = mongoClient.find(DBConstants.T_USER_OPUS_ACTION, query, returnFields,
                orderBy, offset, limit);


        if (cursor != null) {
            UserFavoriteOpusManager favoriteOpusManager = XiaojiFactory.getInstance().getDraw().userFavoriteOpusManager();
            while (cursor.hasNext()) {
                DBObject dbObject = cursor.next();
                String key = (String) dbObject.get(DBConstants.F_UID);
                String id = (String) dbObject.get(DBConstants.F_OPUS_ID);
                //favoriteOpusManager.constructIndex(key, id);
                favoriteOpusManager.insertIndex(key, id);
            }
            cursor.close();
        }


    }

    public boolean moveFriend(MongoDBClient mongoClient, int offset, int limit) {
        final FriendManager friendFollowManager = FriendManager.getFriendfollowmanager();
        final FriendManager friendFansManager = FriendManager.getFriendfansmanager();
        final FriendManager friendBlackManager = FriendManager.getFriendblackmanager();

        DBCursor cursor = mongoClient.find(DBConstants.T_RELATION, null,
                null, offset, limit);

        if (cursor == null || cursor.hasNext() == false) {
            return false;
        }

        if (cursor != null) {
            while (cursor.hasNext()) {
                DBObject object = cursor.next();
                final Relation relation = new Relation(object);

                java.util.concurrent.Future<Object> future = processExecutor.submit((new Callable<Object>() {

                    @Override
                    public Object call() {
                        int relationType = relation.getType();
                        if (relationType == Relation.RELATION_TYPE_FOLLOW) {
                            friendFollowManager.addFriend(relation.getUid().toString(),
                                    relation.getFid().toString(),
                                    String.valueOf(relation.getGameSessionId()),
                                    relation.getCreateDate());

                            friendFansManager.addFriend(relation.getFid().toString(),
                                    relation.getUid().toString(),
                                    String.valueOf(relation.getGameSessionId()),
                                    relation.getCreateDate());


                        } else if (relationType == Relation.RELATION_TYPE_BLACK) {
                            friendBlackManager.addFriend(relation.getUid().toString(),
                                    relation.getFid().toString(),
                                    String.valueOf(relation.getGameSessionId()),
                                    relation.getCreateDate());
                        }

                        return null;
                    }
                }));


            }
            cursor.close();


        }

        return true;

    }

    public void moveBBSBoardPostList(MongoDBClient mongoDBClient, int offset, int limit) {

        List<BBSBoard> boards = BBSManager.getBBSBoardList(mongoDBClient, null, null, "Draw");

        for (BBSBoard board : boards) {
            String boardId = board.getBoardId();
            if (board.isSubBoard()){
                BBSBoardPostManager manager = AbstractXiaoji.getBBSBoardPostManagerByBoardId(boardId);
                List<BBSPost> posts = BBSManager.getBBSPostListByBoardId(mongoDBClient, null, null, boardId, BBSManager.RangeTypeNew, offset, limit);
                int count = posts.size();
                int index = 0;
                for (BBSPost post : posts) {
                    manager.updatePostModefyDate(post.getPostId(), post.getModifyDate());
                    log.info("<insert into board-post redis> board = "+ board.getName()+ ", " + index + " / " + count);
                    index ++;
                }
            }
        }
    }

    public void recreateBBSBoardPostList(MongoDBClient mongoDBClient, String boardId) {

//        List<BBSBoard> boards = BBSManager.getBBSBoardList(mongoDBClient, null, null, "Draw");

        BBSBoard board = BBSManager.getBBSBoardById(mongoDBClient, boardId);
        if (board == null){
            return;
        }

//        for (BBSBoard board : boards) {
//            String boardId = board.getBoardId();
            if (board.isSubBoard()){
                BBSBoardPostManager manager = AbstractXiaoji.getBBSBoardPostManagerByBoardId(boardId);
                List<BBSPost> posts = BBSManager.getBBSPostListByBoardId(mongoDBClient, null, null, boardId, BBSManager.RangeTypeNew, 0, 99999999);
                int count = posts.size();
                int index = 0;
                for (BBSPost post : posts) {
                    manager.updatePostModefyDate(post.getPostId(), post.getModifyDate());
                    log.info("<insert into board-post redis> board = "+ board.getName()+ ", " + index + " / " + count);
                    index ++;
                }
            }
//        }
    }


    public void moveBBSHotPostList(MongoDBClient mongoDBClient) {

        List<BBSBoard> boards = BBSManager.getBBSBoardList(mongoDBClient, null, null, "Draw");

        for (BBSBoard board : boards) {
            String boardId = board.getBoardId();            
            if (board.isSubBoard()){
                BBSTopPostManager manager = BBSTopPostManager.managerForBoard(boardId);
                List<BBSPost> posts = BBSManager.getBBSTopPostList(mongoDBClient, boardId);

                int count = posts.size();
                int index = 0;                
                for (BBSPost post : posts) {
                    manager.updatePostModefyDate(post.getPostId(), post.getModifyDate());
                    log.info("<insert into top-post redis> board = "+ board.getName()+ ", " + index + " / " + count);                    
                    index ++;
                }
            }
        }
    }

    public void moveMessageStatList(MongoDBClient mongoClient, int offset, int limit) {


        DBCursor cursor = mongoClient.find(DBConstants.T_MESSAGE_STATISTIC,
                null, null, offset, limit);
        if (cursor != null) {

            while (cursor.hasNext()) {
                DBObject dbObject = cursor.next();
                MessageStat messageStat = new MessageStat(dbObject);
                UserMessageStatisticManager.getInstance().insertUserMessageStat(
                        messageStat.getUserId(),
                        messageStat.getFriendUserId(),
                        messageStat.getLatestMsgId(),
                        messageStat.getMessageId(),
                        messageStat.getModifyDate(),
                        messageStat.getDirection(),
                        messageStat.getType(),
                        messageStat.getNewMessageCount(),
                        messageStat.getTotalMessageCount(),
                        messageStat.getText());

            }
            cursor.close();
        }

    }

    public void moveMessage(final MongoDBClient mongoClient) {


        executor.execute(new Runnable() {
            @Override
            public void run() {
                BasicDBObject query = new BasicDBObject();
//                query.put(DBConstants.F_FOREIGN_USER_ID, new ObjectId("4fc3089a26099b2ca8c7a4ab")); // for test, read single user's message
//                query.put(DBConstants.F_RELATED_USER_ID, new ObjectId("4f86469d260958163895b958")); //

                DBCursor cursor = mongoClient.find(DBConstants.T_USER_MESSAGE, query, null, null, 0, Integer.MAX_VALUE);
                int processedCount = 0;
                int insertCount = 0;

                if (cursor != null) {


                    while (cursor.hasNext()) {

                        processedCount++;
                        if (processedCount % 100 == 0) {
                            log.info(String.format("total %d processed, %d inserted", processedCount, insertCount));
                        }

                        BasicDBObject dbObject = (BasicDBObject) cursor.next();

                        Object processedFlag = dbObject.get(DBConstants.F_INDEX);
                        if (processedFlag != null && dbObject.getBoolean(DBConstants.F_INDEX)) {
                            // already insert index, skip
                            continue;
                        }

                        insertCount++;

                        ObjectId id = (ObjectId) dbObject.get("_id");
                        String userId = dbObject.get(DBConstants.F_FOREIGN_USER_ID).toString();
                        String relatedUserId = dbObject.get(DBConstants.F_RELATED_USER_ID).toString();
                        String messageId = dbObject.get(DBConstants.F_USER_MESSAGE_ID).toString();
                        int type = dbObject.getInt(DBConstants.F_TYPE);

                        UserMessageManager.getInstance().insertUserMessage(userId, relatedUserId, messageId, type);

                        // update process flag
                        dbObject.put(DBConstants.F_INDEX, true);
                        mongoClient.save(DBConstants.T_USER_MESSAGE, dbObject);

                    }
                    cursor.close();
                }

                log.info(String.format("process completed. total %d processed, %d inserted", processedCount, insertCount));
            }
        });


    }

    public void moveMessageStat(final MongoDBClient mongoClient) {
        executor.execute(new Runnable() {

            @Override
            public void run() {
                int offset = 0;
                int maxCount = 900000;
                int limit = 20;
                while (maxCount != 0) {
                    moveMessageStatList(mongoClient, offset, limit);
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                    maxCount -= 20;
                    offset += 20;
                }


            }
        });
    }

    public void moveFriend(final MongoDBClient mongoClient) {
        executor.execute(new Runnable() {

            @Override
            public void run() {
                int offset = 0;
                int maxCount = 4000000;
                int limit = 1000;
                int completeCount = 0;
                while (maxCount != 0) {
                    if (moveFriend(mongoClient, offset, limit) == false) {
                        break;
                    }
//					try {
//						Thread.sleep(10);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//						break;
//					}
                    maxCount -= limit;
                    offset += limit;
                    completeCount += limit;

                    log.info("<moveFriend> complete " + completeCount + " records");
                }


            }
        });
    }

    public  void  moveBBSData(final MongoDBClient mongoClient){
        //move post
        moveBBSBoardPostList(mongoClient, 0, 10000);
        moveBBSHotPostList(mongoClient);
        try{
            ElasticsearchService.indexMongodbToES(DBConstants.D_GAME, DBConstants.T_BBS_POST, BBSManager.getPostSearchFields(), BBSPost.class);
        }catch(Exception e){
            log.info("<indexMongodbToES> exception = " + e);
        }
    }

    public void indexSongData(final MongoDBClient mongoDBClient){
        try{
            ElasticsearchService.indexMongodbToES(DBConstants.D_GAME, DBConstants.T_SONG, null, Song.class);
        }catch(Exception e){
            log.info("<indexSongData> exception = " + e);
        }

    }

    public void userFavorite(final MongoDBClient mongoClient) {
        executor.execute(new Runnable() {

            @Override
            public void run() {
                int offset = 0;
                int maxCount = 50000;
                int limit = 20;
                while (maxCount != 0) {
                    moveUserSaveOpus(mongoClient, offset, limit);
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                    maxCount -= 20;
                    offset += 20;
                }


            }
        });
    }

    public void myContestOpus(final MongoDBClient mongoClient, final int language) {
        executor.execute(new Runnable() {

            @Override
            public void run() {
                int offset = 0;
                int maxCount = XiaojiFactory.getInstance().getDraw().allTimeTopOpusManager(language).getZsetTopRecordCount();
                int limit = 20;
                while (maxCount != 0) {
                    moveContestMyOpusToMongoIndex(mongoClient, language, offset, limit);
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                    maxCount -= 20;
                    offset += 20;
                }


            }
        });
    }

    public void allTimeExecute(final MongoDBClient mongoClient, final int language) {
        executor.execute(new Runnable() {

            @Override
            public void run() {
                int offset = 0;
                int maxCount = XiaojiFactory.getInstance().getDraw().allTimeTopOpusManager(language).getZsetTopRecordCount();
                int limit = 20;
                while (maxCount != 0) {
                    moveAllTimeDataToRedis(mongoClient, language, offset, limit);
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                    maxCount -= 20;
                    offset += 20;
                }


            }
        });
    }

    public void hotExecute(final MongoDBClient mongoClient, final int language) {
        executor.execute(new Runnable() {

            @Override
            public void run() {
                int offset = 0;
                int maxCount = XiaojiFactory.getInstance().getDraw().hotTopOpusManager(language).getZsetTopRecordCount();
                int limit = 20;
                while (maxCount != 0) {
                    moveHotDataToRedis(mongoClient, language, offset, limit);


                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                    maxCount -= 20;
                    offset += 20;
                }


            }
        });
    }

    public void featureExcute(final MongoDBClient mongoClient, final int language) {
        executor.execute(new Runnable() {

            @Override
            public void run() {
                int offset = 0;
                int maxCount = XiaojiFactory.getInstance().getDraw().featureOpusManager(language).getZsetTopRecordCount();
                int limit = 20;
                while (maxCount != 0) {
                    moveFeatureDataToRedis(mongoClient, language, offset, limit);


                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                    maxCount -= 20;
                    offset += 20;

                }
            }
        });
    }

    public void contestHotExecute(final MongoDBClient mongoClient, final int language) {
        executor.execute(new Runnable() {

            @Override
            public void run() {
                int offset = 0;
                int maxCount = XiaojiFactory.getInstance().getDraw().contestTopOpusManager("").getZsetTopRecordCount();
                int limit = 20;
                while (maxCount != 0) {
                    moveContestTopDataToRedis(mongoClient, language, offset, limit);
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                    maxCount -= 20;
                    offset += 20;
                }
            }
        });
    }

    public void contestLatestExecute(final MongoDBClient mongoClient, final int language) {
        executor.execute(new Runnable() {

            @Override
            public void run() {
                int offset = 0;
                int maxCount = XiaojiFactory.getInstance().getDraw().contestLatestOpusManager("").getZsetTopRecordCount();
                int limit = 20;
                while (maxCount != 0) {
                    moveContestLatestDataToRedis(mongoClient, language, offset, limit);
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                    maxCount -= 20;
                    offset += 20;
                }
            }
        });
    }

//	MongoDBClient mongoClient = new MongoDBClient(DBConstants.D_GAME);

    public void execute(final MongoDBClient mongoClient) {

        executor.execute(new Runnable() {

            @Override
            public void run() {
                int offset = 0;
                int limit = 20;
                while (true) {
                    List list = getFeeds(mongoClient, offset, limit);
                    if (offset == 0 && list.size() == 0) {
                        log.info("NO MORE DATA, CREATE FILE FINISH");
                        break;
                    } else if (list.size() == 0) {
                        offset = 0;
                    }
//					else{
//						offset += limit;
//					}

                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }
        });
    }

    public void indexUserData(MongoDBClient mongoClient) {
        try{
            ElasticsearchService.indexMongodbToES(DBConstants.D_GAME, DBConstants.T_USER, null, User.class);
        }catch(Exception e){
            log.info("<indexUserData> exception = " + e);
        }

    }
}
