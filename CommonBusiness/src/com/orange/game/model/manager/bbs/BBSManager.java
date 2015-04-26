package com.orange.game.model.manager.bbs;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.common.utils.DateUtil;
import com.orange.common.utils.StringUtil;
import com.orange.game.api.service.ElasticsearchService;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.common.CommonMongoIdListManager;
import com.orange.game.model.common.MongoUtils;
import com.orange.game.model.dao.App;
import com.orange.game.model.dao.User;
import com.orange.game.model.dao.bbs.*;
import com.orange.game.model.manager.*;
import com.orange.game.model.manager.group.GroupManager;
import com.orange.game.model.manager.group.index.GroupTopicIndexManager;
import com.orange.game.model.manager.group.index.HotTopicIndexManager;
import com.orange.game.model.manager.stat.ShareStatManager;
import com.orange.game.model.service.DBService;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;

import java.util.*;

public class BBSManager extends CommonManager {

    public static final int RangeTypeNew = 0;
    public static final int RangeTypeHot = 1;
    public static final int MODE_BBS = 0;
    public static final int MODE_GROUP = 1;
    private static final Logger log = Logger.getLogger(OpusManager.class
            .getName());
    private static final String IMAGE_DIR = "image/";
    private static final String DRAW_IMAGE_DIR = "draw_image/";
    // public static
    static int[] boardPostListStatus = {BBSPost.StatusNormal,
            BBSPost.StatusMark};
    // create post methods
    static DBObject inBoardPostListStatus = new BasicDBObject("$in",
            boardPostListStatus);

    private static List<BBSBoard> createTestBoardList(
            MongoDBClient mongoClient, int count) {

        List<BBSBoard> sectionList = new ArrayList<BBSBoard>(3);

        List<BBSBoard> boardList = new ArrayList<BBSBoard>(count * 4);

        BBSBoard board = new BBSBoard(BBSBoard.BBSBoardTypeParent, "猜猜画画",
                "http://58.215.160.100:8080/bbs/icon/s_draw.png", null, 0,
                "猜猜画画区");

        sectionList.add(board);

        board = new BBSBoard(BBSBoard.BBSBoardTypeParent, "欢乐大话骰",
                "http://58.215.160.100:8080/bbs/icon/s_dice.png", null, 100,
                "欢乐大话骰区");
        sectionList.add(board);
        board = new BBSBoard(BBSBoard.BBSBoardTypeParent, "炸金花",
                "http://58.215.160.100:8080/bbs/icon/s_zjh.png", null, 200,
                "炸金花区");
        sectionList.add(board);

        boardList.addAll(sectionList);

        for (BBSBoard pBoard : sectionList) {
            board = new BBSBoard(BBSBoard.BBSBoardTypeSub, "公告区",
                    "http://58.215.160.100:8080/bbs/icon/b_announce.png",
                    pBoard.getBoardId(), 0, null);
            boardList.add(board);
            board = new BBSBoard(BBSBoard.BBSBoardTypeSub, "灌水区",
                    "http://58.215.160.100:8080/bbs/icon/b_water.png", pBoard
                    .getBoardId(), 100, null);
            boardList.add(board);
            board = new BBSBoard(BBSBoard.BBSBoardTypeSub, "交流区",
                    "http://58.215.160.100:8080/bbs/icon/b_communication.png",
                    pBoard.getBoardId(), 200, null);
            boardList.add(board);
        }
        for (BBSBoard board2 : boardList) {
            mongoClient.insert(DBConstants.T_BBS_BOARD, board2.getDbObject());
        }
        return boardList;
    }

    private static DBObject getIncCountObject(List<String> fields) {
        DBObject incObject = new BasicDBObject();
        if (fields != null && !fields.isEmpty()) {
            for (String field : fields) {
                incObject.put(field, 1);
            }
            return incObject;
        }
        return incObject;
    }

    // increase post and topic count, and set the last post content
    private static void updateBoardInfo(MongoDBClient mongoClient,
                                        String boardId, BBSPost post, int mode) {

        DBObject incDbObject = new BasicDBObject(DBConstants.F_POST_COUNT, 1);
        DBObject setDbObject = new BasicDBObject(DBConstants.F_LAST_POST, post.getDbObject());
        DBObject update = new BasicDBObject();
        update.put("$inc", incDbObject);
        if (mode == MODE_BBS) {
            //set last post.
            update.put("$set", setDbObject);
        }
        DBObject query = new BasicDBObject("_id", new ObjectId(boardId));
        mongoClient.updateOne(boardTableForMode(mode), query, update);
    }

    private static List<BBSPost> getBBSPostList(MongoDBClient mongoClient,
                                                DBObject query, DBObject order, int offset, int limit, int mode) {

        log.info("<getBBSPostList>: query = " + query + ", order = " + order);

        DBCursor cursor = mongoClient.find(postTableForMode(mode), query,
                order, offset, limit);
        if (cursor != null) {
            List<BBSPost> pList = new ArrayList<BBSPost>();
            log.info("<getBBSPostList>start to parse db data");
            while (cursor.hasNext()) {
                DBObject object = cursor.next();
                BBSPost post = new BBSPost(object);
                pList.add(post);
            }
            cursor.close();
            log.info("<getBBSPostList>end to parse db data, pList count = "
                    + pList.size());

            return pList;
        }
        return Collections.emptyList();
    }

    private static void pushMessageAndUpdateUserNewActionCount(
            MongoDBClient mongoClient, BBSUser createUser, BBSContent content,
            BBSActionSource source, String appId, int actionType, int mode) {
        List<Object> inList = new ArrayList<Object>(2);
        String cUid = createUser.getUserId();
        if (StringUtil.isEmpty(cUid)) {
            return;
        }

        String pUid = source.getPostUid();
        String aUid = source.getActionUid();
        if (aUid != null && aUid.equalsIgnoreCase(pUid)) {
            pUid = null;
        }
        if (!StringUtil.isEmpty(pUid) && !pUid.equalsIgnoreCase(cUid)) {
            inList.add(new ObjectId(pUid));
            if (mode == MODE_BBS) {
                DrawGamePushManager.newBBSAction(pUid, appId, createUser, source,
                        content, actionType, false);
            }
        }
        if (!StringUtil.isEmpty(aUid) && !aUid.equalsIgnoreCase(cUid)) {
            inList.add(new ObjectId(aUid));
            if (mode == MODE_BBS) {
                DrawGamePushManager.newBBSAction(aUid, appId, createUser, source,
                        content, actionType, true);
            }
        }
        if (mode == MODE_GROUP) {
            UserManager.inNewGroupActionCount(mongoClient, inList);
        } else {
            UserManager.inNewBBSActionCount(mongoClient, inList);
        }
    }

    private static void increasePostActionCountAndSetLastInfo(
            MongoDBClient mongoClient, String postId, int actionType, int mode) {
        List<String> fields = getIncCountFieldList(actionType);
        if (fields != null && postId != null) {
            DBObject inc = getIncCountObject(fields);
            DBObject update = new BasicDBObject("$inc", inc);
            // update modify date
            DBObject set = new BasicDBObject(DBConstants.F_MODIFY_DATE,
                    new Date());
            DBObject query = new BasicDBObject("_id", new ObjectId(postId));
            update.put("$set", set);

            DBObject object = mongoClient.findAndModify(postTableForMode(mode),
                    query, update);

            // increase Board Action Count
            if (object != null) {
                BBSPost post = new BBSPost(object);
                ObjectId boardId = new ObjectId(post.getBoardId());
                incCount(mongoClient, boardTableForMode(mode), boardId,
                        DBConstants.F_ACTION_COUNT, 1);

                if (mode == MODE_BBS && post.getStatus() == BBSPost.StatusNormal) {
                    BBSBoardPostManager bbsBoardPostManager = BBSBoardPostManager.managerForBoard(post.getBoardId());
                    bbsBoardPostManager.updatePostModefyDate(postId, post.getModifyDate());
                }
            }
        }
    }

    private static List<String> getIncCountFieldList(int actionType) {
        List<String> list = new ArrayList<String>(2);
        list.add(DBConstants.F_ACTION_COUNT);
        switch (actionType) {
            case BBSAction.ActionTypeSupport:
                list.add(DBConstants.F_SUPPROT_COUNT);
                return list;
            case BBSAction.ActionTypeComment:
                list.add(DBConstants.F_REPLY_COUNT);
                return list;
            default:
                list = null;
                return list;
        }
    }

    private static void incCount(MongoDBClient mongoClient, String tableName,
                                 ObjectId _id, String fieldName, int value) {
        if (_id != null) {
            DBObject query = new BasicDBObject("_id", _id);
            DBObject incDbObject = new BasicDBObject(fieldName, value);
            DBObject update = new BasicDBObject("$inc", incDbObject);
            mongoClient.updateOne(tableName, query, update);
        }
    }

    private static void incPostFieldCount(MongoDBClient mongoClient,
                                          String postId, String fieldName, int value, int mode) {
        if (!StringUtil.isEmpty(postId)) {
            incCount(mongoClient, postTableForMode(mode), new ObjectId(postId),
                    fieldName, value);
        }
    }

    private static byte[] getBBSDrawData(MongoDBClient mongoClient, String id,
                                         String tableName) {
        DBObject fields = new BasicDBObject(DBConstants.F_CONTENT, 1);
        DBObject object = mongoClient.findOneByObjectId(tableName, id, fields);
        if (object != null) {
            BBSPost post = new BBSPost(object);
            BBSContent content = post.getContent();
            if (content != null
                    && content.getType() == BBSContent.ContentTypeDraw) {
                return ((BBSDrawContent) content).getDrawData();
            }
        }
        return null;
    }

    private static void updatePostReward(MongoDBClient mongoClient,
                                         String postId, String actionId, String actionUid,
                                         String actionAvatar, String actionGender, String actionNick,
                                         BBSReward reward, int mode) {
        reward.setActionId(actionId);
        reward.setWinner(getUser(actionUid, actionNick, actionAvatar,
                actionGender));
        reward.setAwardDate(new Date());
        DBObject set = new BasicDBObject(DBConstants.F_REWARD, reward
                .getDbObject());
        mongoClient.updateOne(postTableForMode(mode), new BasicDBObject("_id",
                new ObjectId(postId)), new BasicDBObject("$set", set));
    }

    public static BBSBoard getBBSBoardById(MongoDBClient mongoClient,
                                                 String boardId) {

        DBObject obj = mongoClient.findOneByObjectId(DBConstants.T_BBS_BOARD, boardId);
        if (obj == null){
            return null;
        }
        else{
            return new BBSBoard(obj);
        }

    }


        // create post methods

    public static List<BBSBoard> getBBSBoardList(MongoDBClient mongoClient,
                                                 String userId, String appId, String gameId) {
        DBCursor cursor = null;
        DBObject orderBy = new BasicDBObject(DBConstants.F_INDEX, 1);
        if (gameId == null) {
            cursor = mongoClient.findAll(DBConstants.T_BBS_BOARD, orderBy);
        } else {
            DBObject query = new BasicDBObject(DBConstants.F_GAMEID, gameId);
            cursor = mongoClient.find(DBConstants.T_BBS_BOARD, query, orderBy,
                    0, 0);
        }

        if (cursor != null) {
            List<BBSBoard> boardList = new ArrayList<BBSBoard>();
            while (cursor.hasNext()) {
                BBSBoard board = new BBSBoard(cursor.next());
                boardList.add(board);
            }
            cursor.close();
            if (!boardList.isEmpty()) {
                updateBoardAdminList(mongoClient, boardList);
                return boardList;
            }
        }
        return Collections.emptyList();
//        return createTestBoardList(mongoClient, 3);

    }

    private static void updateBoardAdminList(MongoDBClient mongoClient,
                                             List<BBSBoard> boardList) {
        // get all the board admin list;
        HashSet<ObjectId> uidSet = new HashSet<ObjectId>();
        for (BBSBoard board : boardList) {
            Set<ObjectId> temp = board.getAdminUidSet();
            if (temp != null && !temp.isEmpty()) {
                uidSet.addAll(temp);
            }
        }
        // find user avatar and nick
        if (!uidSet.isEmpty()) {
            DBObject returnFields = new BasicDBObject();
            returnFields.put(DBConstants.F_NICKNAME, 1);
            returnFields.put(DBConstants.F_AVATAR, 1);
            returnFields.put(DBConstants.F_GENDER, 1);
            HashMap<ObjectId, User> map = UserManager.getUserMapByUserIdList(
                    mongoClient, uidSet, returnFields);
            uidSet = null;

            for (BBSBoard board : boardList) {
                Set<ObjectId> temp = board.getAdminUidSet();
                if (temp != null && !temp.isEmpty()) {
                    HashSet<BBSUser> userSet = new HashSet<BBSUser>(temp.size());
                    for (ObjectId oId : temp) {
                        User u = map.get(oId);
                        if (u != null) {
                            BBSUser user = new BBSUser(u);
                            userSet.add(user);
                        }
                    }
                    board.setAdminUserList(userSet);
                }
            }
        }
    }

    public static BBSPost createPost(MongoDBClient mongoClient, String boardId,
                                     String appId, int deviceType, BBSUser user, BBSContent content,
                                     int bonus, int mode, boolean isPrivate) {

        ShareStatManager.getInstance().incStat(postStatKeyForMode(mode));

        if (mode == MODE_GROUP){

        }

        BBSReward reward = null;
        if (bonus > 0) {
            reward = new BBSReward(bonus);
        }
        BBSPost post = new BBSPost(boardId, appId, deviceType, user, content,
                reward);
        post.setMode(mode);
        post.setPrivate(isPrivate);
        mongoClient.insert(postTableForMode(mode), post.getDbObject());

        BBSUserPostManager.getInstance(mode).insertIndex(user.getUserId(), post.getPostId());
        updateBoardInfo(mongoClient, boardId, post, mode);
        ElasticsearchService.addOrUpdateIndex(post, mongoClient);

        if (mode == MODE_BBS) {
            BBSBoardPostManager bbsBoardPostManager = BBSBoardPostManager.managerForBoard(boardId);
            bbsBoardPostManager.updatePostModefyDate(post.getPostId(), post.getModifyDate());
        } else if (mode == MODE_GROUP) {
            GroupTopicIndexManager.getInstanceForLatest().insertIndex(boardId, post.getPostId());
            GroupManager.postNewTopicToRelatedUsers(mongoClient, boardId, post.getPostId());
        }
        return post;
    }

    public static BBSPost  createPost(MongoDBClient mongoClient, String boardId,
                                     String appId, int deviceType, BBSUser user, BBSContent content,
                                     int bonus, int mode, boolean isPrivate,boolean forTutorial) {

        ShareStatManager.getInstance().incStat(postStatKeyForMode(mode));

        if (mode == MODE_GROUP){

        }

        BBSReward reward = null;
        if (bonus > 0) {
            reward = new BBSReward(bonus);
        }
        BBSPost post = new BBSPost(boardId, appId, deviceType, user, content,
                reward);
        post.setMode(mode);
        post.setPrivate(isPrivate);
        post.setForTutorial(forTutorial);

        mongoClient.insert(postTableForMode(mode), post.getDbObject());

        BBSUserPostManager.getInstance(mode).insertIndex(user.getUserId(), post.getPostId());
        updateBoardInfo(mongoClient, boardId, post, mode);
        ElasticsearchService.addOrUpdateIndex(post, mongoClient);

        if (mode == MODE_BBS) {
            BBSBoardPostManager bbsBoardPostManager = BBSBoardPostManager.managerForBoard(boardId);
            bbsBoardPostManager.updatePostModefyDate(post.getPostId(), post.getModifyDate());
        } else if (mode == MODE_GROUP) {
            GroupTopicIndexManager.getInstanceForLatest().insertIndex(boardId, post.getPostId());
            GroupManager.postNewTopicToRelatedUsers(mongoClient, boardId, post.getPostId());
        }
        return post;
    }

    public static String getImageUploadLocalDir() {
        String dir = System.getProperty("upload.local.bbs");
        return (dir == null ? IMAGE_DIR : dir + IMAGE_DIR);
    }

    public static String getImageUploadRemoteDir() {
        String dir = System.getProperty("upload.remote.bbs");
        return (dir == null ? IMAGE_DIR : dir + IMAGE_DIR);
    }

    public static String getDrawImageUploadLocalDir() {
        String dir = System.getProperty("upload.local.bbs");
        return (dir == null ? DRAW_IMAGE_DIR : dir + DRAW_IMAGE_DIR);
    }

    public static String getDrawImageUploadRemoteDir() {
        String dir = System.getProperty("upload.remote.bbs");
        return (dir == null ? DRAW_IMAGE_DIR : dir + DRAW_IMAGE_DIR);
    }

    public static List<BBSPost> getBBSPostListByBoardId(
            MongoDBClient mongoClient, String userId, String appId,
            String boardId, int rangeType, int offset, int limit) {

        DBObject query = new BasicDBObject(DBConstants.F_BOARD_ID,
                new ObjectId(boardId));
        query.put(DBConstants.F_STATUS, inBoardPostListStatus);
        // DBObject in = new BasicDBObject()
        // updateStatusQuery(query, BBSPost.StatusDelete, false);
        DBObject order = new BasicDBObject();
        if (rangeType == RangeTypeNew) {
            order.put(DBConstants.F_MODIFY_DATE, -1);
        } else {
            order.put(DBConstants.F_ACTION_COUNT, -1);
        }
        List<BBSPost> postList = getBBSPostList(mongoClient, query, order,
                offset, limit, MODE_BBS);
        return postList;
    }

    public static List<BBSPost> getBBSPostListByTargetUid(
            MongoDBClient mongoClient, String userId, String appId,
            String targetUid, int offset, int limit, int mode) {

        log.info("<getBBSPostListByTargetUid> target id = " + targetUid);
        DBObject query = new BasicDBObject(DBConstants.F_CREATE_USER + "."
                + DBConstants.F_UID, new ObjectId(targetUid));
        updateStatusQuery(query, BBSPost.StatusDelete, false);

        DBObject order = new BasicDBObject("_id", -1);
        List<BBSPost> postList = getBBSPostList(mongoClient, query, order,
                offset, limit, mode);
        /*
        if (offset == 0) {
            UserManager.resetCount(mongoClient, targetUid,
                    DBConstants.F_NEW_BBSACTION_COUNT);
        }
        */
        return postList;
    }

    // create action methods

    public static BBSContent createContent(int contentType, String text,
                                           String drawThumbUrl, String drawImageUrl, byte[] drawData,
                                           String thumbUrl, String imageUrl, String opusId, int opusCategory) {
        switch (contentType) {
            case BBSContent.ContentTypeText:
                return new BBSContent(text);

            case BBSContent.ContentTypeImage:
                return new BBSImageContent(text, thumbUrl, imageUrl);

            case BBSContent.ContentTypeDraw:
                return new BBSDrawContent(text, drawThumbUrl, drawImageUrl, drawData);

            case BBSContent.ContentTypeOpusDraw:
            case BBSContent.ContentTypeOpusSing:
                return new BBSContent(text, thumbUrl, imageUrl, opusId, opusCategory, contentType);

            case BBSContent.ContentTypeNo:
            default:
                break;
        }
        return null;
    }

    public static BBSAction createAction(final MongoDBClient mongoClient,
                                         final String appId, final int deviceType, final BBSUser createUser, final int actionType,
                                         final BBSContent content, final BBSActionSource source, final int mode) {

        ShareStatManager.getInstance().incStat(actionStatKeyForMode(mode));

        BBSAction action = new BBSAction(appId, deviceType, createUser,
                actionType, content, source);
        mongoClient.insert(actionTableForMode(mode), action.getDbObject());

        // put into backgroudn to reduce latency
        DBService.getInstance().executeDBRequest(3, new Runnable() {
            @Override
            public void run() {
                increasePostActionCountAndSetLastInfo(mongoClient, source.getPostId(),
                        actionType,mode);

                // update user new bbs action count
                pushMessageAndUpdateUserNewActionCount(mongoClient, createUser,
                        content, source, appId, actionType, mode);
            }
        });

        // create user action index.
        insertBBSActionIndex(mongoClient, action.getActionId(), appId, createUser
                .getUserId(), source.getPostUid(), source.getActionUid(), mode);

        //create post_action index
        if (actionType == BBSAction.ActionTypeComment) {
            BBSActionManager.commentManagerInstance(mode).insertIndex(source.getPostId(), action.getActionId());
        } else if (actionType == BBSAction.ActionTypeSupport) {
            BBSActionManager.supportManagerInstance(mode).insertIndex(source.getPostId(), action.getActionId());
        }

        //update post hot score.
        if (mode == MODE_GROUP) {
            HotTopicIndexManager.getInstance().updateTopicScore(source.getPostId());
        }

        return action;
    }

    private static void insertBBSActionIndex(MongoDBClient mongoClient,
                                             String actionId, String appId, String createUid, String postUid,
                                             String actionUid, int mode) {
        List<String> relatedUid = new ArrayList<String>(2);
        if (actionUid != null && !actionUid.equalsIgnoreCase(createUid)) {
            relatedUid.add(actionUid);
        }
        if (postUid != null && !postUid.equalsIgnoreCase(createUid)) {
            relatedUid.add(postUid);
        }

        for (String uid : relatedUid) {
            BBSUserActionManager.getInstance(mode).insertIndex(uid, actionId);
        }
    }

    //only used in bbs.
    public static List<BBSAction> getBBSActionList(MongoDBClient mongoClient, String postId, String targetUid, int actionType,
                                                   int offset, int limit) {
        DBObject query = new BasicDBObject();

        // if no type return all type actions
        if (actionType != BBSAction.ActionTypeNO) {
            query.put(DBConstants.F_TYPE, actionType);
        }
        if (!StringUtil.isEmpty(postId)) {
            query.put(DBConstants.F_ACTION_SOURCE + "." + DBConstants.F_POSTID,
                    new ObjectId(postId));
            updateStatusQuery(query, BBSPost.StatusDelete, false);
        }

        if (!StringUtil.isEmpty(targetUid)) {
            updateGetUserActionListQuery(mongoClient, query, null, targetUid,
                    offset, limit);
            /*if (offset == 0) {
                UserManager.resetCount(mongoClient, targetUid,
                        DBConstants.F_NEW_BBSACTION_COUNT);
            }
            */
            offset = 0;
        }

        log.info("<getBBSActionList> query = " + query);

        // order
        DBObject orderBy = new BasicDBObject("_id", -1);
        DBCursor cursor = mongoClient.find(DBConstants.T_BBS_ACTION, query,
                orderBy, offset, limit);
        if (cursor != null) {

            List<BBSAction> retList = new ArrayList<BBSAction>();
            while (cursor.hasNext()) {
                DBObject object = cursor.next();
                if (object != null) {
                    BBSAction action = new BBSAction(object);
                    retList.add(action);
                }
            }
            cursor.close();
            return retList;
        }
        return Collections.emptyList();
    }

    private static void updateGetUserActionListQuery(MongoDBClient mongoClient,
                                                     DBObject query, String appId, String targetUid, int offset,
                                                     int limit) {
        ObjectId tId = new ObjectId(targetUid);
        DBObject subQuery = new BasicDBObject(DBConstants.F_RELATED_UID, tId);

//		subQuery.put(DBConstants.F_APPID, appId);

        if (appId != null) {
            String gameId = App.getGameIdByAppId(appId);
            subQuery.put(DBConstants.F_GAME_ID, gameId);
        }

        DBObject returnField = new BasicDBObject("_id", 1);
        DBCursor cursor = mongoClient.find(DBConstants.T_BBS_ACTION_INDEX,
                subQuery, returnField, new BasicDBObject("_id", -1), offset,
                limit);
        if (cursor != null) {
            List<ObjectId> list = new ArrayList<ObjectId>();
            while (cursor.hasNext()) {
                DBObject object = cursor.next();
                list.add((ObjectId) object.get("_id"));
            }
            cursor.close();
            DBObject in = new BasicDBObject("$in", list);
            query.put("_id", in);
        }
    }




    public static void recoverDeletePost(MongoDBClient mongoClient, String userId,
                                  String postId, String boardId, int mode) {

        BBSPost post = getBBSPostByPostIdWithDeleted(mongoClient, postId, mode);
        if (post == null){
            log.info("<recoverDeletePost> but postId "+postId+" not found for mode "+mode);
            return;
        }

        if (!post.isDelete()){
            log.info("<recoverDeletePost> but postId "+postId+" status is NOT deleted");
            return;
        }

        DBObject query = new BasicDBObject("_id", new ObjectId(postId));
        DBObject set = new BasicDBObject(DBConstants.F_STATUS, BBSPost.StatusNormal);

        DBObject update = new BasicDBObject("$set", set);
        post = null;
        DBObject postObj = mongoClient.findAndModify(postTableForMode(mode), query, update);
        if (postObj != null) {
            post = new BBSPost(postObj);
            post.setMode(mode);
        }

        String bid = boardId;
        if (StringUtil.isEmpty(boardId)) {
            ObjectId boid = getBoarIdByPostId(mongoClient, postId, mode);
            if (boid != null) {
                bid = boid.toString();
            }
        }
        if (bid != null) {
            if (mode == MODE_GROUP) {
                GroupTopicIndexManager.getInstanceForLatest().insertId(bid, postId, CommonMongoIdListManager.NOT_ALLOW_DUPLICATE, false, false);
            } else {
                BBSBoardPostManager.managerForBoard(bid).addPostId(postId);
            }
            increacePostCountOnBoard(mongoClient, bid, 1, mode);
            BBSUserPostManager.getInstance(mode).insertId(userId, postId, CommonMongoIdListManager.NOT_ALLOW_DUPLICATE, false, false);
            ElasticsearchService.addOrUpdateIndex(post, mongoClient);
        }
    }


    public static void deletePost(MongoDBClient mongoClient, String userId,
                                  String postId, String boardId, int mode) {
        DBObject query = new BasicDBObject("_id", new ObjectId(postId));
        // query.put(DBConstants.F_CREATE_USER + "." + DBConstants.F_UID,
        // new ObjectId(userId));

        DBObject set = new BasicDBObject(DBConstants.F_STATUS,
                BBSPost.StatusDelete);
        set.put(DBConstants.F_DELETE_DATE, new Date());
        set.put(DBConstants.F_DELETE_USER_ID, userId);

        DBObject update = new BasicDBObject("$set", set);

        BBSPost post = null;
        DBObject postObj = mongoClient.findAndModify(postTableForMode(mode), query, update);
        if (postObj != null) {
            post = new BBSPost(postObj);
            post.setMode(mode);
        }

        String bid = boardId;
        if (StringUtil.isEmpty(boardId)) {
            ObjectId boid = getBoarIdByPostId(mongoClient, postId, mode);
            if (boid != null) {
                bid = boid.toString();
            }
        }
        if (bid != null) {
            if (mode == MODE_GROUP) {
                GroupTopicIndexManager.getInstanceForLatest().removeId(bid, postId, true);
            } else {
                BBSBoardPostManager.managerForBoard(bid).removePostId(postId);
                removeAndUpdateBoardLastPost(bid, postId);
            }
            increacePostCountOnBoard(mongoClient, bid, -1, mode);
            BBSUserPostManager.getInstance(mode).removeId(userId, postId, true);
            ElasticsearchService.deleteIndex(post, mongoClient);
        }
    }

    private static void removeAndUpdateBoardLastPost(String bid, String postId) {

        if (StringUtil.isEmpty(bid) || StringUtil.isEmpty(postId))
            return;


        BasicDBObject query = new BasicDBObject();
        query.put("_id", new ObjectId(bid));
        query.put(DBConstants.F_LAST_POST+"."+"_id", new ObjectId(postId));

        BasicDBObject update = new BasicDBObject();
        List<BBSPost> postList = BBSBoardPostManager.managerForBoard(bid).getTopList(0, 1);
        if (postList.size() == 0){
            update.put("$set", new BasicDBObject(DBConstants.F_LAST_POST, null));
        }
        else{
            update.put("$set", new BasicDBObject(DBConstants.F_LAST_POST, postList.get(0).getDbObject()));
        }

        log.info("<removeAndUpdateBoardLastPost> query="+query.toString()+", update="+update.toString());
        mongoClient.updateOne(DBConstants.T_BBS_BOARD, query, update);
    }

    private static ObjectId getBoarIdByPostId(MongoDBClient mongoClient,
                                              String postId, int mode) {
        DBObject ret = mongoClient.findOneByObjectId(postTableForMode(mode),
                postId, new BasicDBObject(DBConstants.F_BOARD_ID, 1));
        if (ret != null) {
            return (ObjectId) ret.get(DBConstants.F_BOARD_ID);
        }
        return null;
    }

    private static void increacePostCountOnBoard(MongoDBClient mongoClient,
                                                 String boardId, int incValue, int mode) {
        // decrease the board count
        DBObject inc = new BasicDBObject(DBConstants.F_POST_COUNT, incValue);
        DBObject query = new BasicDBObject("_id", new ObjectId(boardId));
        mongoClient.updateOne(boardTableForMode(mode), query,
                new BasicDBObject("$inc", inc));
    }

    public static void deleteAction(MongoDBClient mongoClient, String userId,
                                    String actionId, int mode) {
        DBObject query = new BasicDBObject("_id", new ObjectId(actionId));
        DBObject set = new BasicDBObject(DBConstants.F_STATUS,
                BBSPost.StatusDelete);

        DBObject update = new BasicDBObject("$set", set);

        DBObject object = mongoClient.findAndModify(actionTableForMode(mode),
                query, update);

        if (object != null) {
            BBSAction action = new BBSAction(object);
            if (action.getType() == BBSAction.ActionTypeComment) {
                incPostFieldCount(mongoClient, action.getSourcePostId(),
                        DBConstants.F_REPLY_COUNT, -1, mode);
                BBSActionManager.commentManagerInstance(mode).removeId(action.getSourcePostId(), action.getActionId(), true);
            } else if (action.getType() == BBSAction.ActionTypeSupport) {
                incPostFieldCount(mongoClient, action.getSourcePostId(),
                        DBConstants.F_SUPPROT_COUNT, -1, mode);
                BBSActionManager.supportManagerInstance(mode).removeId(action.getSourcePostId(), action.getActionId(), true);
            }
        }
        // delete action in bbs_action_index table
        mongoClient.removeByObjectId(DBConstants.T_BBS_ACTION_INDEX, actionId);

        //TODO delete action id from bbs user action model.
    }

    public static byte[] getBBSPostDrawData(MongoDBClient mongoClient,
                                            String postId, int mode) {
        return getBBSDrawData(mongoClient, postId, postTableForMode(mode));
    }

    public static byte[] getBBSActionDrawData(MongoDBClient mongoClient,
                                              String actionId, int mode) {
        return getBBSDrawData(mongoClient, actionId, actionTableForMode(mode));
    }

    public static String getBoardIdByPostId(MongoDBClient mongoClient,
                                            String postId, int mode) {
        DBObject query = new BasicDBObject("_id", new ObjectId(postId));
        DBObject returnFields = new BasicDBObject(DBConstants.F_BOARD_ID, 1);
        DBObject object = mongoClient.findOne(postTableForMode(mode), query, returnFields);
        if (object != null) {
            return new BBSPost(object).getBoardId();
        }
        return null;

    }

    public static BBSPost getBBSPostByPostId(MongoDBClient mongoClient,
                                             String postId, int mode) {
        DBObject query = new BasicDBObject("_id", new ObjectId(postId));
        updateStatusQuery(query, BBSPost.StatusDelete, false);
        DBObject object = mongoClient.findOne(postTableForMode(mode), query);
        if (object != null) {
            return new BBSPost(object);
        }
        return null;
    }

    public static BBSPost getBBSPostByPostIdWithDeleted(MongoDBClient mongoClient,
                                             String postId, int mode) {
        DBObject query = new BasicDBObject("_id", new ObjectId(postId));
        DBObject object = mongoClient.findOne(postTableForMode(mode), query);
        if (object != null) {
            return new BBSPost(object);
        }
        return null;
    }

    public static void payRewardAction(MongoDBClient mongoClient,
                                       String postId, String userId, String actionId, String actionUid,
                                       String actionAvatar, String actionGender, String actionNick,
                                       String appId, int mode) {

        BBSPost post = getBBSPostByPostId(mongoClient, postId, mode);
        BBSReward reward = post.getReward();// getBBSRewardByPostId(mongoClient,
        // postId);
        if (reward != null) {
            // no winner
            if (reward.getActionId() != null) {
                return;
            }
            int bonus = reward.getBonus();
            if (bonus > 0) {

                // increase balance of action uid
                UserManager.incBalanceAndExpForAward(mongoClient, actionUid, appId, bonus, 0);

                // update post reward.
                updatePostReward(mongoClient, postId, actionId, actionUid,
                        actionAvatar, actionGender, actionNick, reward, mode);
//                BBSUser winner = new BBSUser(actionUid, actionNick,
//                        actionAvatar, actionGender);
                BBSUser winner = getUser(actionUid, actionNick, actionAvatar, actionGender);
                DrawGamePushManager.payBBSReward(post, winner, appId);
            }
        }
    }

    public static int getNewBBSActionCount(MongoDBClient mongoClient,
                                           String userId) {
        return (int) BBSUserActionManager.getInstance(MODE_BBS).getUnreadCount(userId);
    }

    //only used in bbs.
    public static void transferBBSPost(MongoDBClient mongoClient, String userId, String postId, String boardId) {
        BBSPost post = getBBSPostByPostId(mongoClient, postId, MODE_BBS);
        if (post == null) return;
        String oldBoardId = post.getBoardId();
        if (oldBoardId != null && !oldBoardId.equalsIgnoreCase(boardId)) {
            mongoClient.updateOne(DBConstants.T_BBS_POST, postId, DBConstants.F_BOARD_ID, new ObjectId(boardId));
            // increase the destination board post count
            increacePostCountOnBoard(mongoClient, boardId, 1, MODE_BBS);
            // decrease the destination board post count
            increacePostCountOnBoard(mongoClient, oldBoardId, -1, MODE_BBS);

            if (post.isMarked()) {
                BBSMarkPostManager.getInstance().removePostId(oldBoardId, postId);
                BBSMarkPostManager.getInstance().insertIndex(boardId, postId);
            } else {
                switch (post.getStatus()) {
                    case BBSPost.StatusNormal:
                        BBSBoardPostManager.managerForBoard(oldBoardId).removePostId(postId);
                        BBSBoardPostManager.managerForBoard(boardId).updatePostModefyDate(postId, post.getModifyDate());
                        break;

                    case BBSPost.StatusTop:
                        BBSTopPostManager.managerForBoard(oldBoardId).removePostId(postId);
                        BBSTopPostManager.managerForBoard(boardId).updatePostModefyDate(postId, post.getModifyDate());
                        break;
                }
            }
        }
    }

    public static void updatePostStatus(MongoDBClient mongoClient, String userId, String postId, int status, int mode) {
        BBSPost post = getBBSPostByPostId(mongoClient, postId, mode);
        if (post == null || post.getBoardId() == null) return;
        String boardId = post.getBoardId();
        int oldStatus = post.getStatus();
        if (oldStatus != status) {

            mongoClient.updateOne(postTableForMode(mode), postId, DBConstants.F_STATUS, status);

            if (mode == MODE_GROUP) {
                GroupTopicIndexManager.changeTopicStatus(boardId, postId, oldStatus, status);
            } else {
                //original status
                if (oldStatus == BBSPost.StatusNormal) {
                    BBSBoardPostManager.managerForBoard(boardId).removePostId(postId);
                } else if (oldStatus == BBSPost.StatusTop) {
                    BBSTopPostManager.managerForBoard(boardId).removePostId(postId);
                } //other status

                //new status
                if (status == BBSPost.StatusNormal) {
                    BBSBoardPostManager.managerForBoard(boardId).updatePostModefyDate(postId, post.getModifyDate());
                } else if (status == BBSPost.StatusTop) {
                    BBSTopPostManager.managerForBoard(boardId).updatePostModefyDate(postId, post.getModifyDate());
                } //other status
            }
        }
    }

    public static List<BBSPost> getBBSTopPostList(MongoDBClient mongoClient,
                                                  String boardId) {

        DBObject query = new BasicDBObject();
        updateStatusQuery(query, BBSPost.StatusTop, true);
        if (!StringUtil.isEmpty(boardId)) {
            query.put(DBConstants.F_BOARD_ID, new ObjectId(boardId));
        }
        List<BBSPost> postList = getBBSPostList(mongoClient, query, null, 0,
                0, MODE_BBS);
        return postList;
    }

    private static void updateStatusQuery(DBObject query, int value,
                                          boolean equal) {
        if (equal) {
            query.put(DBConstants.F_STATUS, value);
        } else {
            query.put(DBConstants.F_STATUS, new BasicDBObject("$ne", value));
        }
    }

    public static void addUserToBoardAdminList(MongoDBClient mongoClient,
                                               String userId, String boardId) {
        DBObject query = new BasicDBObject(DBConstants.F_OBJECT_ID,
                new ObjectId(boardId));
        DBObject addToSet = new BasicDBObject(DBConstants.F_ADMINUID_LIST,
                new ObjectId(userId));
        DBObject update = new BasicDBObject("$addToSet", addToSet);
        mongoClient.updateOne(DBConstants.T_BBS_BOARD, query, update);
    }

    public static void pullUserOutOfBoardAdminList(MongoDBClient mongoClient,
                                                   String userId, String boardId) {
        DBObject query = new BasicDBObject(DBConstants.F_OBJECT_ID,
                new ObjectId(boardId));
        DBObject pull = new BasicDBObject(DBConstants.F_ADMINUID_LIST,
                new ObjectId(userId));
        DBObject update = new BasicDBObject("$pull", pull);
        mongoClient.updateOne(DBConstants.T_BBS_BOARD, query, update);
    }

    public static void markPost(MongoDBClient mongoClient, String userId, String boardId, String postId, int mode) {
        //TODO check permission

        mongoClient.updateOne(postTableForMode(mode), postId, DBConstants.F_ISMARKED, true);

        if (mode == MODE_GROUP) {
            GroupTopicIndexManager.getInstanceForMarked().insertIndex(boardId, postId);
        } else {
            BBSMarkPostManager.getInstance().insertIndex(boardId, postId);
        }
    }

    public static void unmarkPost(MongoDBClient mongoClient, String userId, String boardId, String postId, int mode) {
        mongoClient.updateOne(postTableForMode(mode), postId, DBConstants.F_ISMARKED, false);
        if (mode == MODE_GROUP) {
            GroupTopicIndexManager.getInstanceForMarked().removeId(boardId, postId, true);
        } else {
            BBSMarkPostManager.getInstance().removePostId(boardId, postId);
        }
    }

    public static List<BBSPost> getBBSMarkedPostList(MongoDBClient mongoClient, String boardId, int offset, int limit) {
        BasicDBObject query = new BasicDBObject();
        query.put(DBConstants.F_BOARD_ID, new ObjectId(boardId));

        BasicDBObject orderBy = new BasicDBObject(DBConstants.F_CREATE_DATE, -1);
        DBCursor cursor = mongoClient.find(DBConstants.T_BBS_MARKEDPOST, query, orderBy, offset, limit);
        if (cursor == null) {
            return Collections.emptyList();
        }
        List<ObjectId> postIdList = new ArrayList<ObjectId>();
        while (cursor.hasNext()) {
            DBObject obj = cursor.next();
            ObjectId pid = (ObjectId) obj.get(DBConstants.F_POSTID);
            if (pid != null) {
                postIdList.add(pid);
            }
        }
        cursor.close();
        return getBBSPostListByPostIdList(mongoClient, postIdList, true, MODE_BBS);

    }

    private static List<BBSPost> getBBSPostListByPostIdList(MongoDBClient mongoClient, List<ObjectId> postIdList, boolean keepOrder, int mode) {


        if (postIdList.isEmpty()) {
            return Collections.emptyList();
        }

        DBObject query = new BasicDBObject();
        Set<ObjectId> postIdSet = new HashSet<ObjectId>();
        postIdSet.addAll(postIdList);

        DBObject in = new BasicDBObject("$in", postIdSet);
        query.put("_id", in);

        DBObject orderBy = new BasicDBObject();
        List<BBSPost> postList = getBBSPostList(mongoClient, query, null,
                0, 0, mode);

        if (keepOrder) {
            Map<ObjectId, BBSPost> map = new HashMap<ObjectId, BBSPost>(postIdList.size());
            for (BBSPost post : postList) {
                map.put(post.getObjectId(), post);
            }
            postList.clear();
            for (ObjectId oid : postIdList) {
                BBSPost post = map.get(oid);
                if (post != null) {
                    postList.add(post);
                }
            }
        }
        return postList;
    }

    // 在ElasticSearch中搜索用户，然后用搜索结果取得的userId在mongodb中查询结果并返回。
    public static List<BBSPost> searchPostFromES(MongoDBClient mongoClient, String keyString, String groupId, int offset, int limit, int mode) {
        List<String> candidateFields = new ArrayList<String>(2);
        candidateFields.add(DBConstants.ES_CONTENT);
        candidateFields.add(DBConstants.ES_NICK_NAME);
        String indexType = (mode == MODE_GROUP) ? DBConstants.ES_INDEX_TYPE_GROUP_TOPIC : DBConstants.ES_INDEX_TYPE_POST;
        log.info("<searchPostFromES> indexType = " + indexType);
        List<ObjectId> pidList = null;
        if (MODE_GROUP == mode) {
            pidList = ElasticsearchService.search(keyString, candidateFields, DBConstants.ES_POST_ID, DBConstants.ES_GROUP_ID, groupId, offset, limit);
        } else {
            pidList = ElasticsearchService.search(keyString, candidateFields, DBConstants.ES_POST_ID, offset, limit, indexType);
        }

        if (!pidList.isEmpty()) {
            return BBSManager.getBBSPostListByPostIdList(mongoClient, pidList, true, mode);
        }
        return Collections.emptyList();
    }

    public static DBObject getPostSearchFields() {
        DBObject dbObject = new BasicDBObject();
        dbObject.put(DBConstants.F_CONTENT + "." + DBConstants.F_TEXT_CONTENT, 1);
        dbObject.put(DBConstants.F_CREATE_USER + "." + DBConstants.F_NICKNAME, 1);
        dbObject.put(DBConstants.F_STATUS, 1);
        return dbObject;
    }

    ///get table name for mode.
    private static String boardTableForMode(int mode) {
        if (mode == MODE_GROUP)
            return DBConstants.T_GROUP;
        return DBConstants.T_BBS_BOARD;
    }

    private static String postTableForMode(int mode) {
        if (mode == MODE_GROUP)
            return DBConstants.T_GROUP_TOPIC;
        return DBConstants.T_BBS_POST;
    }

    private static String actionTableForMode(int mode) {
        if (mode == MODE_GROUP)
            return DBConstants.T_GROUP_ACTION;
        return DBConstants.T_BBS_ACTION;
    }

    private static String postStatKeyForMode(int mode) {
        if (mode == MODE_GROUP)
            return ShareStatManager.GROUP_TOPIC;
        return ShareStatManager.POST;
    }

    private static String actionStatKeyForMode(int mode) {
        if (mode == MODE_GROUP)
            return ShareStatManager.GROUP_ACTION;
        return ShareStatManager.POST_ACTION;
    }

    public static List<BBSAction> getPostCommentListByUser(MongoDBClient mongoClient, String postId, String targetUid, int offset, int limit, int mode) {
        DBObject query = new BasicDBObject();
        query.put(DBConstants.F_ACTION_SOURCE + "." + DBConstants.F_POSTID, new ObjectId(postId));
        query.put(DBConstants.F_CREATE_USER + "." + DBConstants.F_UID, new ObjectId(targetUid));
        query.put(DBConstants.F_TYPE, BBSAction.ActionTypeComment);
        query.put(DBConstants.F_STATUS, BBSAction.StatusNormal);
        DBObject order = new BasicDBObject("_id", -1);
        return MongoUtils.findAndMakeList(mongoClient, actionTableForMode(mode), query, null, order, offset, limit, BBSAction.class);
    }

    public static void updatePostText(MongoDBClient mongoClient, String userId, String postId, String text, int mode) {
        //update the post
        //TODO check permission

        DBObject query = new BasicDBObject("_id", new ObjectId(postId));
        DBObject set = new BasicDBObject(DBConstants.F_CONTENT + "." + DBConstants.F_TEXT_CONTENT, text);
        DBObject update = new BasicDBObject("$set", set);
//        mongoClient.updateOneById(postTableForMode(mode), postId, update);
        DBObject obj = mongoClient.findAndModify(postTableForMode(mode), query, update);

        if (obj != null) {
            log.info("<updatePostText> done, update ES index.");
            BBSPost post = new BBSPost(obj);
            post.setMode(mode);
            ElasticsearchService.addOrUpdateIndex(post, mongoClient);
        }
    }

    public static BBSUser getUser(String userId, String nickName, String avatar, String gender) {
        User user = UserManager.findPublicUserInfoByUserId(userId);
        BBSUser createUser = null;
        if (user != null) {
            createUser = new BBSUser(user);
        } else {
            createUser = new BBSUser(userId, nickName, avatar, gender, 0);
        }
        return createUser;
    }

    public static List<BBSPost> getLatestGroupTopics(MongoDBClient mongoClient, int offset, int limit) {
        DBObject query = new BasicDBObject(DBConstants.F_STATUS, 0);
        DBObject orderBy = new BasicDBObject("_id", -1);
        DBCursor cursor = mongoClient.find(postTableForMode(MODE_GROUP), query, orderBy, offset, limit);
        return getDataListFromCursor(cursor, BBSPost.class);
    }

    public static void forbidUserBoard(String targetUserId, String boardId, int days) {

        if (StringUtil.isEmpty(targetUserId) || StringUtil.isEmpty(boardId)){
            log.info("<forbidUserBoard> but targetUserId null or boardId null");
            return;
        }

        BasicDBObject obj = new BasicDBObject();
        obj.put("_id", createBlackBoardUserId(boardId, targetUserId));
        obj.put(DBConstants.F_BOARD_ID, boardId);
        obj.put(DBConstants.F_UID, targetUserId);
        obj.put(DBConstants.F_CREATE_DATE, new Date());

        if (days == 0) {
            obj.put(DBConstants.F_EXPIRE_DATE, DateUtil.dateFromString("20201231000000"));
        } else {
            obj.put(DBConstants.F_EXPIRE_DATE, new Date(System.currentTimeMillis() + (long)days * 24 * 60 * 60 * 1000));
        }

        log.info("<forbidUserBoard> "+obj.toString());
        mongoClient.insert(DBConstants.T_BLACK_BOARD_USER, obj);
    }

    private static String createBlackBoardUserId(String boardId, String targetUserId) {
        return boardId + "." + targetUserId;
    }

    public static void unforbidUserBoard(String targetUserId, String boardId) {

        if (StringUtil.isEmpty(targetUserId) || StringUtil.isEmpty(boardId)){
            log.info("<unforbidUserBoard> but targetUserId null or boardId null");
            return;
        }

        String id = createBlackBoardUserId(boardId, targetUserId);
        log.info("<unforbidUserBoard> id="+id);
        mongoClient.remove(DBConstants.T_BLACK_BOARD_USER, new BasicDBObject("_id", id));
    }

    public static boolean isUserBlackInBoard(String targetUserId, String boardId){

        if (StringUtil.isEmpty(targetUserId) || StringUtil.isEmpty(boardId)){
            log.info("<isUserBlackInBoard> but targetUserId null or boardId null");
            return false;
        }

        BasicDBObject query = new BasicDBObject("_id", createBlackBoardUserId(boardId, targetUserId));
        DBObject obj = mongoClient.findOne(DBConstants.T_BLACK_BOARD_USER, query);
        if (obj == null){
            return false;
        }

        log.info("<isUserBlackInBoard> user "+targetUserId+" is black in board " +boardId);
        return true;
    }

    //取得教程的PostId
    public static String hasStagePost(String tutorialId,String stageId){

        if (StringUtil.isEmpty(tutorialId) ||
                StringUtil.isEmpty(stageId)){

            return null;
        }

        DBObject query = new BasicDBObject(DBConstants.F_TUTORIAL_ID,tutorialId);
        query.put(DBConstants.F_STAGE_ID, stageId);

        DBObject result = mongoClient.findOne(DBConstants.T_TUTORIAL_STAGE_POST,query);
        if(result==null){
            return null;
        }
        String postId  = (String)result.get(DBConstants.F_POSTID);
        if(postId == null || postId.equals("")){
           log.info("<hasStagePost> postId is empty or null");
           return null;
        }else{
            return postId;
        }
    }
    //插入字段在tutorial_stage_post表中
    public static BasicDBObject insertTutorialStagePost(String tutorialId,String stageId,String postId){
        BasicDBObject insert = new BasicDBObject();
        insert.put(DBConstants.F_TUTORIAL_ID,tutorialId);
        insert.put(DBConstants.F_STAGE_ID,stageId);
        insert.put(DBConstants.F_POSTID,postId);
        insert.put(DBConstants.F_CREATE_DATE,new Date());

        boolean result = mongoClient.insert(DBConstants.T_TUTORIAL_STAGE_POST,insert);
        return insert;
    }
}

