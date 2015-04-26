package com.orange.game.model.service.opus;

import java.text.SimpleDateFormat;
import java.util.*;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mongodb.DBObject;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.common.redis.RedisClient;
import com.orange.common.scheduler.ScheduleService;
import com.orange.common.upload.ImageManager;
import com.orange.game.constants.ErrorCode;
import com.orange.game.model.dao.*;
import com.orange.game.model.manager.*;
import com.orange.game.model.manager.group.GroupUserTimelineManager;
import com.orange.game.model.manager.guessopus.UserGuessUtil;
import com.orange.game.model.manager.opus.HotTopOpusManager;
import com.orange.game.model.manager.opus.contest.AllContestOpusManager;
import com.orange.game.model.manager.opusclass.OpusClassService;
import com.orange.game.model.service.CreateDataFileService;
import com.orange.network.game.protocol.model.DrawProtos;
import com.orange.network.game.protocol.model.GameBasicProtos;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.orange.common.utils.StringUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.opus.Opus;
import com.orange.game.model.manager.feed.AllFeedManager;
import com.orange.game.model.service.DBService;
import com.orange.game.model.xiaoji.AbstractXiaoji;
import com.orange.network.game.protocol.model.OpusProtos.PBOpus;

public class OpusService {

    public static final Logger log = Logger.getLogger(OpusService.class.getName());

	// thread-safe singleton implementation
	private static OpusService service = new OpusService();
    private static MongoDBClient mongoClient = DBService.getInstance().getMongoDBClient();
	private OpusService(){		
		super();
	} 	    	
	public static OpusService getInstance() { 
		return service; 
	}
	
	public Opus createOpus(AbstractXiaoji xiaoji, String userId, String appId, String gameId,
			PBOpus pbOpus, String localDataUrl, int dataLen, String localImageUrl,
			String localThumbImageUrl) {


		Opus opus = new Opus(userId, pbOpus, localDataUrl, dataLen, localImageUrl, localThumbImageUrl, xiaoji);

        User user = UserManager.findPublicUserInfoByUserId(userId);
        if (user != null){
            opus.setUserVip(user.isVip() ? user.getVip() : 0);
        }

		boolean result = DBService.getInstance().getMongoDBClient().insert(DBConstants.T_OPUS, opus.getDbObject());
        log.debug("<createOpus> opus="+opus.toString());
        if (!result){
            return null;
        }


//        OpusNotificationService.getInstance().notifyOpusCreate(userId, opus);

        String contestId = pbOpus.getContestId();
        Contest contest = null;
        boolean isContest = !StringUtil.isEmpty(contestId);
        boolean isAnonymousContest = false;
        boolean isGroupContest = false;
        boolean isOpusToUser = (pbOpus.getTargetUser() != null && !StringUtil.isEmpty(pbOpus.getTargetUser().getUserId()));
        if (isContest){
            // add user and the opus to the contest.
            ContestManager.addUserAndOpusToContest(mongoClient, userId, opus.getActionId(), contestId);

            // insert contest latest
            xiaoji.contestLatestOpusManager(contestId).updateContestLatestIndex(opus.getActionId());

            // insert user contest
            xiaoji.myContestOpusManager(contestId).insertIndex(opus.getCreateUserId(), opus.getActionId());

            // insert contest top
            xiaoji.contestTopOpusManager(contestId).updateOpusTopScore(opus.getActionId(), opus.getContestScore());

            // insert all contest opus
            AllContestOpusManager.getInstance().insert(opus.getActionId(), contestId);

            contest = ContestManager.getContestById(DBService.getInstance().getMongoDBClient(), contestId);
            if (contest != null){
                isAnonymousContest = contest.getIsAnonymous();
                isGroupContest = contest.getIsGroup();
            }

        } else if (isOpusToUser) {

            // send push
            String targetUserId = pbOpus.getTargetUser().getUserId();
            DrawGamePushManager.sendMessage(opus, targetUserId, appId);
//            UserManager.incDrawToMeCount(mongoClient, targetUserId, xiaoji);

        }

        // for anonymous contest, don't insert feed into DB until contest is finished
        if (!isAnonymousContest){

            AllFeedManager.getInstance().addActionToFans(opus, appId);  // TODO check inside


            // insert user opus
            xiaoji.userOpusManager().insertIndex(opus.getCreateUserId(), opus.getActionId());

        }

        if (!isAnonymousContest || isGroupContest){
            // insert latest opus index
            xiaoji.latestOpusManager(appId, opus.getLanguage()).insertLatestIndex(userId, opus.getActionId(), true, xiaoji.isOneUserOneOpusForLatest());
            if (appId.equalsIgnoreCase(DBConstants.APPID_LITTLEGEE)){
                xiaoji.latestOpusManager(DBConstants.APPID_DRAW, opus.getLanguage()).insertLatestIndex(userId, opus.getActionId(), true, xiaoji.isOneUserOneOpusForLatest());
            }
        }

        if (isGroupContest){
            // insert into all group user opus timeline
        }

        //insert draw to user
        if (isOpusToUser){
            xiaoji.drawToUserOpusManager().insertIndex(opus.getTargetUserId(), opus.getActionId());
        }

        if (!isContest){
            xiaoji.userGuessOpusHappyManager.createRedisIndex(opus.getActionId(), userId, opus);
            xiaoji.userGuessOpusGeniusManager.createRedisIndex(opus.getActionId(), userId, opus);
            xiaoji.userGuessOpusContestManager(UserGuessUtil.getTodayContestId(xiaoji.getCategoryName())).createRedisIndex(opus.getActionId(), userId, opus);
            xiaoji.opusForMatchManager.updateOpusForMatchScore(opus.getActionId(), true, false, false);
        }

        // insert user group timeline
        if (user != null){
            ObjectId groupId = user.getFirstGroupId();
            if (groupId != null && isAnonymousContest == false){
                // insert group timeline
                GroupUserTimelineManager.getAllTimelineManager().insertOpus(groupId.toString(), opus.getActionId());
                GroupUserTimelineManager.getTimelineManager(opus.getCategory()).insertOpus(groupId.toString(), opus.getActionId());
            }

            if (user.isVip()){
                // insert vip opus timeline
                xiaoji.vipUserOpusManager().insertVipOpus(opus.getActionId());
            }
        }


        return opus;

	}

    static final int[] DAILY_AWARDS = {2000, 1500, 1000, 1000, 1000,
            800,  800,  800,  800,   800,
            500,  500,  500,  500,   500,
            500,  500,  500,  500,   500,
    };

    public void startDailyAwardService(final AbstractXiaoji xiaoji){
        if (xiaoji == null){
            return;
        }

        final int TOP_AWARD_COUNT = DAILY_AWARDS.length;
        if (TOP_AWARD_COUNT == 0)
            return;

        ScheduleService.getInstance().scheduleEveryday(23, 59, 59, new Runnable(){

            @Override
            public void run() {

                try{
                    String message = "";
                    HotTopOpusManager topOpusManager = xiaoji.hotTopOpusManager(DBConstants.C_LANGUAGE_CHINESE);
                    List<UserAction> opusList = topOpusManager.getTopList(0, TOP_AWARD_COUNT);
                    int i = 1;
                    for (UserAction opus : opusList){

                        String opusName = opus.getWord();
                        int rank = i;
                        int awardCoins = 0;

                        if (i >= 0 && i < DAILY_AWARDS.length){
                            awardCoins = DAILY_AWARDS[i];
                        }

                        if (awardCoins <= 0){
                            continue;
                        }

                        message = "哦嘿呦，这位同学，你的作品【"+opusName+"】获得今日小吉画榜第"+ rank+"名，获得"+ awardCoins+"金币奖励。" +
                                "绵薄金币不足道，画画开心最重要，也欢迎关注新浪微博 @小吉画画 或" +
                                "腾讯微博 小吉画画 @drawlively 获取更多精彩作品!";

                        String userId = opus.getCreateUserId();
                        UserManager.chargeAccount(mongoClient, userId, awardCoins, DBConstants.C_CHARGE_SOURCE_DRAW_TOP, null, null);
                        boolean isShow = (i <= 3);
                        MessageManager.sendSystemMessage(mongoClient, opus.getCreateUserId(), message, DBConstants.APPID_DRAW, isShow);
                        insertRankToDB(opus.getCreateUserId(), rank);
                        i++;
                    }
                }
                catch(Exception e){
                    log.error("startDailyAwardService but catch exception ="+e.toString(), e);
                }


            }
        });
    }

    public void insertRankToDB(String userId, int rank) {

        Date date = new Date(System.currentTimeMillis()-7200000);// 因为在12点后发的微博，所以减去2小时以表示前一天
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        DBObject query = new BasicDBObject(DBConstants.F_USERID, new ObjectId(userId));

        DBObject dailyRankValue = new BasicDBObject();
        dailyRankValue.put("rank", rank);
        dailyRankValue.put("date", dateFormat.format(date));

        DBObject dailyRank = new BasicDBObject();
        dailyRank.put("daily_rank", dailyRankValue);

        DBObject update = new BasicDBObject();
        update.put("$push", dailyRank);

        mongoClient.updateOne(DBConstants.T_USER, query, update);

    }



    final static int TOP_RANK_BULLETIN_COUNT = 0;

    public void startDailyTopRankService(final AbstractXiaoji xiaoji, final String displayName){
        if (xiaoji == null){
            return;
        }

        if (TOP_RANK_BULLETIN_COUNT == 0)
            return;

        ScheduleService.getInstance().scheduleEveryday(23, 59, 59, new Runnable(){

            @Override
            public void run() {

                String categoryName = displayName;

                String bulletinMessage = "今日"+categoryName+"榜单，获得前"+TOP_RANK_BULLETIN_COUNT+"名的依次是用户";
                if (TOP_RANK_BULLETIN_COUNT == 1){
                    bulletinMessage = "今日"+categoryName+"榜单，第一名获得者是";
                }

                HotTopOpusManager topOpusManager = xiaoji.hotTopOpusManager(DBConstants.C_LANGUAGE_CHINESE);
                List<UserAction> opusList = topOpusManager.getTopList(0, TOP_RANK_BULLETIN_COUNT);
                int i = 1;
                String firstUserId = "";
                for (UserAction opus : opusList){
                    String nickName = opus.getNickName();

                    if (i == 1){
                        firstUserId = opus.getCreateUserId();
                    }

                    if (i<=TOP_RANK_BULLETIN_COUNT){

                        bulletinMessage = bulletinMessage + nickName;
                        if (i == TOP_RANK_BULLETIN_COUNT){
                            bulletinMessage = bulletinMessage + "，真是顶呱呱的"+categoryName+"大触啊，赶快关注她/他啦";
                            BulletinManager.createBulletin(bulletinMessage, DBConstants.GAME_ID_DRAW, Bulletin.JumpTypeGame, Bulletin.FUNC_USER_DETAIL, firstUserId);
                        }
                        else{
                            bulletinMessage = bulletinMessage + "，";
                        }
                    }

                    i++;
                }

            }
        });
    }

    public void updateOpusMissionInfoCache(String opusId){
        BasicDBObject obj = new BasicDBObject("_id", opusId);
        Date date = new Date();
        date = DateUtils.addHours(date, 6);
        obj.put(DBConstants.F_EXPIRE_DATE, date);
        DBService.getInstance().getMongoDBClient().insert(DBConstants.T_CACHE_OPUS_MISSING, obj);
    }

    public boolean isOpusMissingInfoAdded(String opusId){
        BasicDBObject query = new BasicDBObject("_id", opusId);
        DBObject obj = DBService.getInstance().getMongoDBClient().findOne(DBConstants.T_CACHE_OPUS_MISSING, query);
        return (obj == null) ? false : true;
    }

    public void updateOpusMissingInfo(final List<UserAction> feeds) {
        if (feeds == null){
            return;
        }

        CreateDataFileService.getInstance().getSingleExecutor().execute(new Runnable(){
            @Override
            public void run() {
                for (final UserAction userAction : feeds){
                    if (userAction.isContestDrawType() || userAction.isDrawType()){

                        boolean needUpdate = false;

                        BasicDBObject updateValue = new BasicDBObject();

                        if (userAction.getCanvasHeight() == 0 || userAction.getCanvasWidth() == 0){
                            // set canvas
                            ImageManager.ImageResult result =  ImageManager.getImageInfo(userAction.createOpusLocalImageUrl());
                            if (result.getResult() == 0 && result.getImageHeight() > 0 && result.getImageWidth() > 0){
                                updateValue.put(DBConstants.F_CANVAS_HEIGHT, result.getImageHeight());
                                updateValue.put(DBConstants.F_CANVAS_WIDTH, result.getImageWidth());
                                needUpdate = true;
                            }
                            else{
                                log.error("<updateOpusMissingInfo> read image failure, opus "+userAction.getActionId()+result.getResultMessage());
                            }
                        }

                        if (userAction.getStrokes() == 0){
                            byte[] byteData = userAction.readDrawData(false);
                            if (byteData == null){

                                if (isOpusMissingInfoAdded(userAction.getActionId())){
                                    log.info("<updateOpusMissingInfo> already done, opus " + userAction.getActionId());
                                    return;
                                }

                                // read from DB
                                UserAction newUserAction = OpusManager.getDrawById(DBService.getInstance().getMongoDBClient(),
                                        DBConstants.SYSTEM_USERID, userAction.getActionId());
                                byteData = newUserAction.readDrawData(false);
                            }

                            if (byteData == null || byteData.length == 0){
                                log.warn("<updateOpusMissingInfo> read opus data failure, opus " + userAction.getActionId());
                            }
                            else{

                                if (isOpusMissingInfoAdded(userAction.getActionId())){
                                    log.info("<updateOpusMissingInfo> already done, opus " + userAction.getActionId());
                                    return;
                                }

                                try {
                                    DrawProtos.PBDraw pbDraw = DrawProtos.PBDraw.parseFrom(byteData);
                                    List<GameBasicProtos.PBDrawAction> drawActionList = pbDraw.getDrawDataList();
                                    int totalStrokes = 0;
                                    for (GameBasicProtos.PBDrawAction drawAction : drawActionList){
                                        if (UserAction.isStroke(drawAction.getType())){
                                            // draw paints
                                            totalStrokes += 1;
                                        }
                                    }

                                    if (totalStrokes == 0 && drawActionList.size() > 0){
                                        totalStrokes = 1;
                                    }

                                    if (totalStrokes > 0){
                                        updateValue.put(DBConstants.F_STROKES, totalStrokes);
                                        needUpdate = true;
                                    }
                                } catch (Exception e) {
                                    log.error("<updateOpusMissingInfo> calc strokes error for opus "+userAction.getActionId(), e);
                                }
                            }
                        }

                        if (needUpdate && updateValue.keySet().size() > 0){
                            // update DB
                            BasicDBObject query = new BasicDBObject("_id", new ObjectId(userAction.getActionId()));
                            BasicDBObject update = new BasicDBObject("$set", updateValue);
                            DBService.getInstance().getMongoDBClient().updateOne(DBConstants.T_OPUS, query, update);

                            updateOpusMissionInfoCache(userAction.getActionId());
                        }
                    }
            }
        }

        });
    }

    public void updateOpusMissingInfo(UserAction userAction) {
        if (userAction == null){
            return;
        }

        List<UserAction> list = new ArrayList<UserAction>();
        list.add(userAction);
        updateOpusMissingInfo(list);
    }

    public int updateOpusClass(String opusId, List<String> classList) {

        if (StringUtil.isEmpty(opusId)){
            return ErrorCode.ERROR_USER_ACTION_INVALID;
        }

        if (classList == null){
            classList = Collections.emptyList();
        }

        int resultCode = 0;

        // get opus class
        UserAction opus = OpusManager.getOpusSimpleInfoById(opusId);

        List<String> currentClassList = opus.getClassList();

        List<String> classToBeRemoved = (List<String>)CollectionUtils.subtract(currentClassList, classList);
        List<String> classNew = (List<String>)CollectionUtils.subtract(classList, currentClassList);

        // update opus class
        BasicDBObject updateObject = new BasicDBObject(DBConstants.F_CLASS, classList);
        OpusManager.updateOpus(opusId, updateObject);

        // remove old index
        if (classToBeRemoved != null && classToBeRemoved.size() > 0){
            OpusClassService.getInstance().removeOpusClass(opus.getActionId(), classToBeRemoved);
        }

        // add new index
        if (classNew != null && classNew.size() > 0){
            OpusClassService.getInstance().addOpusClass(opus, classNew);
        }

        // TODO update search index

        return resultCode;
    }

    public int createOpusClass(UserAction opus, List<String> classList) {

        if (opus == null || classList == null || classList.size() == 0){
            return ErrorCode.ERROR_USER_ACTION_INVALID;
        }

        int resultCode = 0;
        List<String> classNew = classList;

        // add new index
        if (classNew != null && classNew.size() > 0){
            OpusClassService.getInstance().addOpusClass(opus, classNew);
        }

        // TODO update search index

        return resultCode;
    }

}
