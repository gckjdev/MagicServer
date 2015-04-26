package com.orange.game.model.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.orange.game.model.common.MongoGetIdListUtils;
import com.orange.game.model.manager.bbs.BBSPrivilegeManager;
import com.orange.game.model.manager.group.GroupManager;
import com.orange.game.model.manager.group.GroupUserTimelineManager;
import com.orange.game.model.manager.guessopus.UserGuessUtil;
import com.orange.game.model.dao.*;
import com.orange.game.model.manager.opus.OpusUtils;
import com.orange.game.model.manager.opus.StageOpusManager;
import com.orange.game.model.manager.opus.StageTopOpusManager;
import com.orange.game.model.manager.opus.VipUserOpusManager;
import com.orange.game.model.manager.opus.contest.AllContestOpusManager;
import com.orange.game.model.manager.opus.contest.ContestTopOpusManager;
import com.orange.game.model.manager.opusclass.OpusClassService;
import com.orange.game.model.manager.stat.ShareStatManager;
import com.orange.game.model.manager.timeline.ContestCommentTimelineManager;
import com.orange.game.model.manager.tutorial.TutorialStatManager;
import com.orange.game.model.manager.tutorial.UserTutorialManager;
import com.orange.game.model.manager.tutorial.UserTutorialOpusManager;
import com.orange.game.model.manager.useropus.DrawToUserOpusManager;
import com.orange.game.model.manager.useropus.UserStageOpusManager;
import com.orange.game.model.service.opus.OpusService;
import com.orange.network.game.protocol.constants.GameConstantsProtos;
import com.orange.network.game.protocol.model.OpusProtos;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.common.utils.RandomUtil;
import com.orange.common.utils.StringUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.constants.ErrorCode;
import com.orange.game.model.dao.CommentInfo;
import com.orange.game.model.dao.User;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.dao.Draw;
import com.orange.game.model.manager.feed.CommentManager;
import com.orange.game.model.manager.feed.AllFeedManager;
import com.orange.game.model.manager.feed.ContestFeedManager;
import com.orange.game.model.manager.feed.HotFeedManagerFactory;
import com.orange.game.model.manager.feed.RecommendFeedManager;
import com.orange.game.model.manager.opusaction.OpusActionManager;
import com.orange.game.model.manager.useropus.UserFavoriteOpusManager;
import com.orange.game.model.manager.useropus.UserOpusManager;
import com.orange.game.model.service.DBService;
import com.orange.game.model.xiaoji.AbstractXiaoji;
import com.orange.game.model.xiaoji.XiaojiFactory;

public class OpusManager extends CommonManager {
    public static final Logger log = Logger.getLogger(OpusManager.class
            .getName());

    public static final int MIN_DRAW_DATA_LEN_FOR_HOME_DISPLAY = 2500;
    static final MongoGetIdListUtils<UserAction> idListUtils = new MongoGetIdListUtils<UserAction>();


//	private static final int GUESS_CORRECT_COIN = 1;

    private static DBObject getHotFeedQuery(int language) {
        DBObject query = new BasicDBObject();
        query.put(DBConstants.F_OPUS_STATUS, UserAction.STATUS_NORMAL);
        query.put(DBConstants.F_TYPE, getOpusTypeObject(false));

        // add date for query, by Benson 2013-02-12, not tested yet
        BasicDBObject dateRange = new BasicDBObject();
        long now = System.currentTimeMillis();
        dateRange.put("$gt", new Date(now - 1000 * 60 * 60 * 24 * 7));        // only in 7 days
//		query.put(DBConstants.F_CREATE_DATE, dateRange);

        if (language != UserAction.LANGUAGE_UNKNOW) {
            query.put(DBConstants.F_LANGUAGE, language);
        }
        return query;
    }

    private static void addUserToAction(MongoDBClient mongoClient,
                                        String actionId, String uid) {
        DBObject object = new BasicDBObject();
        object.put(DBConstants.F_OBJECT_ID, new ObjectId(actionId));
        DBObject userObject = new BasicDBObject();
        userObject.put(DBConstants.F_USERID_LIST, uid);
        DBObject update = new BasicDBObject();
        update.put("$addToSet", userObject);
        BasicDBObject inc = new BasicDBObject();
        inc.put(DBConstants.F_MATCH_TIMES, 1);
        update.put("$inc", inc);
        mongoClient.updateOne(DBConstants.T_OPUS, object, update);

    }

    private static void increaseFieldTimes(MongoDBClient mongoClient,
                                           String opusId, String fieldName, int value) {
        DBObject query = new BasicDBObject();
        query.put(DBConstants.F_OBJECT_ID, new ObjectId(opusId));
        DBObject update = new BasicDBObject();
        BasicDBObject inc = new BasicDBObject();
        inc.put(fieldName, value);
        update.put("$inc", inc);
        mongoClient.updateOne(DBConstants.T_OPUS, query, update);

    }

    private static void increaseCommentTimes(MongoDBClient mongoClient,
                                             String opusId, int value) {
        increaseFieldTimes(mongoClient, opusId, DBConstants.F_COMMENT_TIMES,
                value);
        updateOpusScore(mongoClient, opusId, false);
    }

    private static void increaseContestCommentTimes(MongoDBClient mongoClient,
                                                    String opusId, int value) {
        increaseFieldTimes(mongoClient, opusId, DBConstants.F_CONTEST_COMMENT_TIMES,
                value);
    }


    private static void increaseTomatoTimes(MongoDBClient mongoClient,
                                            String opusId, int value) {
        increaseFieldTimes(mongoClient, opusId, DBConstants.F_TOMATO_TIMES,
                value);
        updateOpusScore(mongoClient, opusId, false);
    }

    private static void increaseGuessTimes(MongoDBClient mongoClient,
                                           String opusId, int value) {
        increaseFieldTimes(mongoClient, opusId, DBConstants.F_GUESS_TIMES,
                value);
        updateOpusScore(mongoClient, opusId, false);
    }

    private static void increaseCorrectTimes(MongoDBClient mongoClient,
                                             String opusId, int value) {
        increaseFieldTimes(mongoClient, opusId, DBConstants.F_CORRECT_TIMES,
                value);
        updateOpusScore(mongoClient, opusId, false);
    }

    private static void increaseFlowerTimes(MongoDBClient mongoClient,
                                            String opusId, int value, Contest contest) {
        increaseFieldTimes(mongoClient, opusId, DBConstants.F_FLOWER_TIMES,
                value);
        updateOpusScore(mongoClient, opusId, false, contest);
    }

    private static void increaseActionTimes(MongoDBClient mongoClient,
                                            String opusId, String actionName, int value) {
        increaseFieldTimes(mongoClient, opusId, actionName, value);
        updateOpusScore(mongoClient, opusId, false);
    }

    private static boolean needFindDrawData(int type) {
        return type == UserAction.TYPE_GUESS;
    }

    private static List<UserAction> getFeedList(MongoDBClient mongoClient,
                                                DBObject query, int offset, int limit, boolean returnImage, String tableName) {
        DBObject orderBy = new BasicDBObject();
        orderBy.put("_id", -1);
        return getFeedList(mongoClient, query, orderBy, offset, limit,
                returnImage, tableName);
    }

    private static List<UserAction> getFeedListDescending(MongoDBClient mongoClient,
                                                          DBObject query, int offset, int limit, boolean returnImage) {
        DBObject orderBy = null; //new BasicDBObject();
        //orderBy.put("_id", 1);
        return getFeedList(mongoClient, query, orderBy, offset, limit,
                returnImage, DBConstants.T_OPUS);
    }

    private static List<UserAction> getOpusActionList(MongoDBClient mongoClient,
                                                      DBObject query, DBObject orderBy, int offset, int limit,
                                                      boolean returnImage) {
        return getFeedList(mongoClient, query, orderBy, offset, limit, returnImage, DBConstants.T_OPUS_ACTION);
    }

    private static List<UserAction> getOpusList(MongoDBClient mongoClient,
                                                DBObject query, DBObject orderBy, int offset, int limit,
                                                boolean returnImage) {
        return getFeedList(mongoClient, query, orderBy, offset, limit, returnImage, DBConstants.T_OPUS);
    }

    private static List<UserAction> getFeedList(MongoDBClient mongoClient,
                                                DBObject query, DBObject orderBy, int offset, int limit,
                                                boolean returnImage, String tableName) {


        DBObject fields = new BasicDBObject();
        fields.put(DBConstants.F_GUESS_WORD_LIST, 0);
        fields.put(DBConstants.F_USERID_LIST, 0);
        fields.put(DBConstants.F_OPUS_RELATED_USER_ID, 0);
        fields.put(DBConstants.F_DRAW_DATA, 0);

        log.info("<GetFeedList>:query = " + query.toString() + " orderBy = "
                + orderBy + " fields=" + fields.toString() + ", limit=" + limit + ", offset = " + offset);

        DBCursor cursor = mongoClient.find(tableName, query, fields,
                orderBy, offset, limit);

        log.info("<GetFeedList> query done");

        List<UserAction> feedList = new ArrayList<UserAction>(limit);
        List<Object> opusObjectIds = new ArrayList<Object>();

        // get the feed list.
        if (cursor != null) {
            log.info("<GetFeedList> iterate cursor starts, query = "
                    + query.toString() + " orderBy = " + orderBy + ", limit="
                    + limit + ", offset = " + offset);
            int index = 0;
            while (cursor.hasNext()) {
                UserAction userAction = new UserAction(cursor.next());

                // skip the delete draw.
                if (userAction.isDrawType()
                        && userAction.getOpusStatus() == UserAction.STATUS_DELETE) {
                    continue;
                }
                if (needFindDrawData(userAction.getType())) {
                    String oid = userAction.getOpusId();
                    if (!StringUtil.isEmpty(oid)) {
                        opusObjectIds.add(new ObjectId(oid));
                    }
                } else if (userAction.isDrawType()) {
                    if (returnImage && userAction.getOpusImageUrl() != null) {
                        userAction.setDrawData(null);
                    }
                    userAction.setOpusWord(userAction.getWord());
                }
                feedList.add(userAction);
                index++;
                log.debug("<GetFeedList> add " + index + " feed");
            }
            cursor.close();

            log.info("<GetFeedList> iterate cursor end, query = "
                    + query.toString() + " orderBy = " + orderBy + ", limit="
                    + limit + ", offset = " + offset);

            Map<String, UserAction> map = new HashMap<String, UserAction>();

            if (opusObjectIds.size() != 0) {
                // find the target opuses for once searching.

                cursor = mongoClient.findByFieldInValues(DBConstants.T_OPUS,
                        DBConstants.F_OBJECT_ID, opusObjectIds, fields);
                // cursor = mongoClient.findByIds(DBConstants.T_ACTION,
                // DBConstants.F_OBJECT_ID, opusObjectIds);

                if (cursor != null) {
                    while (cursor.hasNext()) {
                        DBObject object = cursor.next();
                        UserAction action = new UserAction(object);
                        if (action != null) {
                            String opid = action.getActionId();
                            if (opid != null) {
                                map.put(opid, action);
                            }
                        }
                    }
                    cursor.close();
                }
                log.info("<GetFeedList> iterate draw data query done");

                // find the target opus information, and set the information.
                for (UserAction uAction : feedList) {
                    UserAction opus = map.get(uAction.getOpusId());
                    if (opus != null) {
                        uAction.setCorrectTimes(opus.getCorrectTimes());
                        uAction.setMatchTimes(opus.getMatchTimes());
                        uAction.setCommentTimes(opus.getCommentTimes());
                        uAction.setGuessTimes(opus.getGuessTimes());
                        uAction.setOpusStatus(opus.getOpusStatus());

                        uAction.setTomatoTimes(opus.getTomatoTimes());
                        uAction.setFlowerTimes(opus.getFlowerTimes());
                        uAction.setSaveTimes(opus.getSaveTimes());

                        uAction.setOpusWord(opus.getWord());
                        uAction.setOpusCreatorNickName(opus.getNickName());
                        uAction.setOpusCreatorAvatar(opus.getAvatar());
                        uAction.setOpusCreatorGender(opus.getGender());

                        if (returnImage) {
                            String imageUrl = opus.createOpusThumbImageUrl();
                            String thumbUrl = opus.createOpusThumbImageUrl();
                            if (imageUrl == null || thumbUrl == null) {
                                uAction.setDrawData(opus.readDrawData(true));
                            } else {
                                uAction.setOpusImageUrl(imageUrl);
                                uAction.setOpusThumbImageUrl(thumbUrl);
                            }
                        } else {
                            // old version
                            uAction.setDrawData(opus.readDrawData(true));
                        }

                        // log.info("opus detail = " + opus.toString());

                        // log.info("action detail = " + uAction.toString());
                    }
                }
                log.info("<GetFeedList> data generation ok");
            }

            return feedList;
        }
        return Collections.emptyList();
    }

    private static DBObject getFeedTypeObject() {
        DBObject object = new BasicDBObject();
        object.put("$in", UserAction.feedTypes);
        return object;
    }

    private static DBObject getOpusTypeObject(boolean includeContestOpus) {
        DBObject object = new BasicDBObject();
        if (includeContestOpus) {
            object.put("$in", UserAction.allOpusTypes);
        } else {
            object.put("$in", UserAction.normalOpusTypes);
        }

        return object;
    }

    private static DBObject getOpusTypeObject(String category, boolean includeContestOpus) {
        DBObject object = new BasicDBObject();
        if (XiaojiFactory.getInstance().isDraw(category)){
            if (includeContestOpus) {
                object.put("$in", UserAction.allDrawOpusTypes);
            } else {
                object.put("$in", UserAction.normalDrawOpusTypes);
            }
        }
        else{
            if (includeContestOpus) {
                object.put("$in", UserAction.allSingOpusTypes);
            } else {
                object.put("$in", UserAction.normalSingOpusTypes);
            }
        }

        return object;
    }

    private static UserAction updateOpusScore(MongoDBClient mongoClient,
                                              String opusId, boolean needReturn) {

        return updateOpusScore(mongoClient, opusId, needReturn, null);
    }

    public static UserAction updateOpusScore(MongoDBClient mongoClient,
                                             String opusId, boolean needReturn, Contest contest) {

        DBObject fields = OpusUtils.createReturnFields();

        DBObject result = mongoClient.findOneByObjectId(DBConstants.T_OPUS,
                opusId, fields);

        if (result != null) {
            UserAction action = new UserAction(result);

            BasicDBObject update = new BasicDBObject();
            BasicDBObject set = new BasicDBObject();
            BasicDBObject query = new BasicDBObject();
            query.put(DBConstants.F_OBJECT_ID, new ObjectId(opusId));

            double historyScore = ScoreManager.calculateAndSetHistoryScore(action);
            set.put(DBConstants.F_HISTORY_SCORE, historyScore);

            if (action.isContestDraw() && !StringUtil.isEmpty(action.getContestId())) {
                double score = ScoreManager.calculateContestScore(action, contest);
                set.put(DBConstants.F_CONTEST_SCORE, score);

                // insert contest
                ContestTopOpusManager contestTopOpusManager = XiaojiFactory.getInstance().getDraw().contestTopOpusManager(action.getContestId());
                if (contestTopOpusManager != null) {
                    contestTopOpusManager.updateOpusTopScore(opusId, score);
                }

                ScoreManager.calculateContestSpecialScore(action, set);
                set.put(DBConstants.F_CONTEST_SCORE, score);

                // insert contest
                /*
                ContestTopOpusManager contestTopOpusManager = XiaojiFactory.getInstance().getDraw().contestTopOpusManager(action.getContestId());
                if (contestTopOpusManager != null){
                    contestTopOpusManager.updateOpusTopScore(opusId, score);
                }
                */

            }
//            else {
                double hot = ScoreManager.calculateScore(action);
                set.put(DBConstants.F_HOT, hot);
//            }

            update.put("$set", set);
            mongoClient.updateOne(DBConstants.T_OPUS, query, update);

            // update opus class top
            OpusClassService.getInstance().updateOpusClassScore(action);

            return action;
        }
        return null;
    }

    public static List<Draw> findRecentDraw(MongoDBClient mongoClient,
                                            int minDataLen, int maxCount) {
        if (mongoClient == null)
            return Collections.emptyList();

        BasicDBObject query = new BasicDBObject();

        // add data len as condition
        BasicDBObject gte = new BasicDBObject();
        gte.put("$gte", minDataLen);
        query.put(DBConstants.F_DRAW_DATA_LEN, gte);

        // add avatar as condition
        // BasicDBObject ne = new BasicDBObject();
        // ne.put("$ne", null);
        // query.put(DBConstants.F_DRAW_AVATAR, ne);

        // sort by create date
        BasicDBObject orderBy = new BasicDBObject();
        orderBy.put(DBConstants.F_DRAW_CREATE_DATE, -1);

        DBCursor cursor = mongoClient.find(DBConstants.T_DRAW, query, orderBy,
                0, maxCount);
        if (cursor == null) {
            return Collections.emptyList();
        }

        if (cursor.size() == 0) {
            cursor.close();
            return Collections.emptyList();
        }

        List<Draw> drawList = new ArrayList<Draw>();
        while (cursor.hasNext()) {
            DBObject obj = cursor.next();
            if (obj != null) {
                Draw draw = new Draw(obj);
                drawList.add(draw);
            }
        }

        cursor.close();
        return drawList;
    }

    public static UserAction createDrawAction(MongoDBClient mongoClient, ObjectId opusId,
                                          String uid, String nickName, String avatar, String gender, String signature,
                                          String appId, String word, int level, int language,
                                          String drawDataUrl, int drawDataLen, String targetUserId, String contestId,
                                          String deviceModel, String drawreturnImageUrl,
                                          String thumbImageUrl, int devicetype, String description, int score,
                                          Date draftCreateDate, Date draftCompleteDate, long strokes, int spendTime,
                                          int canvasWidth, int canvasHeight,
                                          List<String> classList,
                                          String drawBgImageUrl, int bgImageWidth, int bgImageHeight, String bgImageName,
                                          // for tutorial, learn draw
                                          int type, String tutorialId, String stageId, int stageIndex,
                                          int chapterIndex, String remoteUserTutorialId, String localUserTutorialId,
                                          String chapterOpusId, int stageScore) {

        ShareStatManager.getInstance().incStat(ShareStatManager.OPUS);

        log.info("create draw action, data url=" + drawDataUrl);
        AbstractXiaoji xiaoji = XiaojiFactory.getInstance().getXiaoji(appId);

        User user = UserManager.findPublicUserInfoByUserId(uid);

        UserAction userAction = new UserAction(new BasicDBObject());
        userAction.setActionId(opusId);
        userAction.setAppId(appId);
        userAction.setCreateUserId(uid);
        userAction.addRelatedOpusId(uid); // Add by Benson
        // userAction.addDirectRelatedOpusId(uid);
        userAction.setNickName(nickName);
        userAction.setAvatar(avatar);
        userAction.setGender(gender);
        userAction.setSignature(signature);
        userAction.setWord(word);
        userAction.setLevel(level);
        userAction.setWordScore(score);
        userAction.setLanguage(language);

        if (user != null) {
            userAction.setUserVip(user.isVip() ? user.getVip() : 0);
        }

        // userAction.setDrawData(drawDataUrl);
        userAction.setDrawDataUrl(drawDataUrl);

        userAction.setCreateDate(new Date());
        userAction.setMatchTimes(0);
        userAction.setCorrectTimes(0);
        userAction.setCommentTimes(0);
        userAction.setGuessTimes(0);
        userAction.setUserIdList(new HashSet<String>());
        userAction.setGuessWordList(new HashSet<String>());
        userAction.setOpusStatus(UserAction.STATUS_NORMAL);
        userAction.setDeviceType(devicetype);
        userAction.setDescription(description);
        userAction.setCategory(GameConstantsProtos.PBOpusCategoryType.DRAW_CATEGORY_VALUE);

        userAction.setFileGen(1);
        userAction.setDraftCreateDate(draftCreateDate);
        userAction.setDraftCompleteDate(draftCompleteDate);
        userAction.setStrokes(strokes);
        userAction.setSpendTime(spendTime);

        userAction.setCanvasHeight(canvasHeight);
        userAction.setCanvasWidth(canvasWidth);

        if (drawreturnImageUrl != null) {
            userAction.setOpusImageUrl(drawreturnImageUrl);
        }
        if (thumbImageUrl != null) {
            userAction.setOpusThumbImageUrl(thumbImageUrl);
        }

        if (drawBgImageUrl != null){
            userAction.setBgImageUrl(drawBgImageUrl);
            userAction.setBgImageWidth(bgImageWidth);
            userAction.setBgImageHeight(bgImageHeight);
            userAction.setBgImageName(bgImageName);
        }

        userAction.setDeviceModel(deviceModel);

        int dataLen = drawDataLen;
        userAction.setDataLength(dataLen);

        Contest contest = null;
        boolean isContest = !StringUtil.isEmpty(contestId);
        boolean isDrawToUser = !StringUtil.isEmpty(targetUserId);
        boolean isAnonymousContest = false;
        boolean isGroupContest = false;
        boolean isLearnDraw = false;
        if (isContest) {
            userAction.setType(UserAction.TYPE_DRAW_TO_CONTEST);
            userAction.setContestId(contestId);
            // add user and the opus to the contest.
            ContestManager.addUserAndOpusToContest(mongoClient, uid, userAction
                    .getActionId(), contestId);
            // set the contest score.
            userAction.setContestScore(ScoreManager.calculateContestScore(userAction, null));

            // insert contest latest
            xiaoji.contestLatestOpusManager(contestId).updateContestLatestIndex(userAction.getActionId());

            // insert user contest
            xiaoji.myContestOpusManager(contestId).insertIndex(userAction.getCreateUserId(), userAction.getActionId());

            // insert contest top
            xiaoji.contestTopOpusManager(contestId).updateOpusTopScore(userAction.getActionId(), userAction.getContestScore());

            // insert all contest opus
            AllContestOpusManager.getInstance().insert(userAction.getActionId(), contestId);

            contest = ContestManager.getContestById(DBService.getInstance().getMongoDBClient(), contestId);
            if (contest != null) {
                isAnonymousContest = contest.getIsAnonymous();
                isGroupContest = contest.getIsGroup();
            }

        } else if (isDrawToUser) {
            userAction.setType(UserAction.TYPE_DRAW_TO_USER);
            userAction.setTargetUserId(targetUserId);
            User targetUser = UserManager.findSimpleUserInfoByUserId(
                    mongoClient, targetUserId);
            userAction.addRelatedOpusId(targetUserId); // Add by Benson

            if (targetUser != null) {
                String targetNickName = targetUser.getNickName();
                userAction.setTargetNickName(targetNickName);
                DrawGamePushManager.sendMessage(userAction, targetUserId,
                        targetUser.getAppId());

//				UserManager.incDrawToMeCount(mongoClient, targetUserId, xiaoji);
            }

        } else if (type == OpusProtos.PBOpusType.DRAW_PRACTICE_VALUE ||
                type == OpusProtos.PBOpusType.DRAW_CONQUER_VALUE) {

            isLearnDraw = true;

            userAction.setType(type);
            userAction.setTutorialId(tutorialId);
            userAction.setStageId(stageId);
            userAction.setStageIndex(stageIndex);
            userAction.setStageScore(stageScore);
            userAction.setChapterIndex(chapterIndex);
            userAction.setRemoteUserTutorialId(remoteUserTutorialId);
            userAction.setLocalUserTutorialId(localUserTutorialId);
            userAction.setChapterOpusId(chapterOpusId);
        }
        else{
            userAction.setType(UserAction.TYPE_DRAW);
        }

        userAction.setHot(ScoreManager.calculateScore(userAction));
        ScoreManager.calculateAndSetHistoryScore(userAction, xiaoji);

        if (classList != null){
            userAction.setOpusClassList(classList);
        }

        mongoClient.insert(DBConstants.T_OPUS, userAction.getDbObject());

        if (isLearnDraw){
            // insert basic data
            UserTutorialOpusManager.getInstance().createTutorialOpus(
                    uid, userAction.getActionId(),
                    tutorialId, stageId, stageIndex, chapterIndex,
                    remoteUserTutorialId, localUserTutorialId,
                    chapterOpusId, stageScore);

            // insert user opus
            UserStageOpusManager.getInstance().insertUserOpus(uid, tutorialId, stageId,
                    userAction.getActionId(), stageIndex, stageScore);

            // insert stage top
            BasicDBObject retObj = StageOpusManager.getInstance().insertOpus(uid, tutorialId, stageId,
                    stageIndex, userAction.getActionId(), stageScore);

            if (retObj != null){
                userAction.getDbObject().putAll(retObj.toMap());
            }

            // report user tutorial progress
            UserTutorialManager.getInstance().reportUserTutorialStatus(uid, tutorialId, localUserTutorialId,
                    remoteUserTutorialId, stageId, stageIndex, userAction.getDeviceModel(),
                    userAction.getDeviceOs(), userAction.getDeviceType());

            // insert user friend timeline
            AllFeedManager.getInstance().addActionToFans(userAction, appId);

            TutorialStatManager.getInstance().insertTutorialAction(uid,
                    tutorialId, stageId, score, TutorialStatManager.SUBMIT_CONQUER_DRAW);
        }
        else{
            // for anonymous contest, don't insert feed into DB until contest is finished
            if (!isAnonymousContest) {

                AllFeedManager.getInstance().addActionToFans(userAction, appId);

                // insert user opus
                xiaoji.userOpusManager().insertIndex(userAction.getCreateUserId(), userAction.getActionId());
            }

            // insert latest opus index
            // group contest opus also can be in latest feed
            xiaoji.latestOpusManager(appId, language).insertLatestIndex(uid, userAction.getActionId(), true, xiaoji.isOneUserOneOpusForLatest());
            if (appId.equalsIgnoreCase(DBConstants.APPID_LITTLEGEE)) {
                xiaoji.latestOpusManager(DBConstants.APPID_DRAW, language).insertLatestIndex(uid, userAction.getActionId(), true, xiaoji.isOneUserOneOpusForLatest());
            }

            //insert draw to user
            if (isDrawToUser) {
                xiaoji.drawToUserOpusManager().insertIndex(userAction.getTargetUserId(), userAction.getActionId());
            }

            if (!isContest) {
                xiaoji.userGuessOpusHappyManager.createRedisIndex(userAction.getActionId(), uid, userAction);
                xiaoji.userGuessOpusGeniusManager.createRedisIndex(userAction.getActionId(), uid, userAction);
                xiaoji.userGuessOpusContestManager(UserGuessUtil.getTodayContestId(xiaoji.getCategoryName())).createRedisIndex(userAction.getActionId(), uid, userAction);

                xiaoji.opusForMatchManager.updateOpusForMatchScore(userAction.getActionId(), true, false, false);
            }

            if (user != null) {
                // insert user group timeline
                ObjectId groupId = user.getFirstGroupId();
                if (groupId != null && isAnonymousContest == false){
                    // insert group timeline
                    GroupUserTimelineManager.getAllTimelineManager().insertOpus(groupId.toString(), userAction.getActionId());
                    GroupUserTimelineManager.getTimelineManager(userAction.getCategory()).insertOpus(groupId.toString(), userAction.getActionId());
                }

                if (user.isVip()) {
                    // insert vip opus timeline
                    xiaoji.vipUserOpusManager().insertVipOpus(userAction.getActionId());
                }
            }

            // set class
            OpusService.getInstance().createOpusClass(userAction, classList);
        }

        // write for statistic
        final User opusUser = user;
        final UserAction newOpus = userAction;
        DBService.getInstance().executeDBRequest(0, new Runnable() {
            @Override
            public void run() {
                ShareStatManager.writeDailyNewOpus(newOpus, opusUser);
            }
        });

        return userAction;
    }

    public static UserAction matchOpus(AbstractXiaoji xiaoji, String userId) {

        UserAction userAction = xiaoji.opusForMatchManager.matchOne(userId, xiaoji);
        if (userAction == null)
            return null;

        xiaoji.opusForMatchManager.updateOpusForMatchScore(userAction.getActionId(), false, true, false);
        return userAction;
    }

    public static UserAction matchDraw(MongoDBClient mongoClient, String uid,
                                       String gender, int language) {

        DBObject obj = new BasicDBObject();
        UserAction query = new UserAction(obj);
        query.setLanguage(language);
        query.setType(UserAction.TYPE_DRAW);
        query.setOpusStatus(UserAction.STATUS_NORMAL);

        // not drawer
        BasicDBObject notDrawer = new BasicDBObject();
        notDrawer.put("$ne", uid);
        query.getDbObject().put(DBConstants.F_CREATE_USERID, notDrawer);

        // not guess
        BasicDBObject notGuess = new BasicDBObject();
        notGuess.put("$ne", uid);
        query.getDbObject().put(DBConstants.F_USERID_LIST, notGuess);

        DBObject queryObject = query.getDbObject();

        DBObject orderBy = new BasicDBObject();
        orderBy.put(DBConstants.F_GUESS_TIMES, 1);
        // orderBy.put(DBConstants.F_DRAW_DATA_LEN, -1); // add by Benson, to be
        // tested

        DBCursor cursor = mongoClient.find(DBConstants.T_OPUS, queryObject,
                orderBy, 0, 1);

        log.info("match query = " + query.toString() + " sort by="
                + orderBy.toString());

        DBObject retObject = null;
        if (cursor == null){
            return null;
        }

        if (cursor.hasNext()) {
            retObject = cursor.next();
        }

        cursor.close();
        if (retObject == null) {
            return null;
        }

        UserAction userAction = new UserAction(retObject);
        addUserToAction(mongoClient, userAction.getActionId(), uid);

        AbstractXiaoji xiaoji = userAction.getXiaoji();
        xiaoji.opusForMatchManager.updateOpusForMatchScore(userAction.getActionId(), false, true, false);

        return userAction;
    }

    public static void guessOpus(MongoDBClient mongoClient, String appId,
                                 String opusCreatorUid, String opusId, String uid, String gender,
                                 String avatar, String nickName, boolean correct, int score,
                                 Set<String> guessWords, int category, int vip) {

        log.info("guess draw action, score=" + score);

        // add guess word list
        boolean hasWords = (guessWords != null && guessWords.size() != 0);
        if (hasWords) {
            DBObject query = new BasicDBObject();
            query.put(DBConstants.F_OBJECT_ID, new ObjectId(opusId));
            DBObject update = new BasicDBObject();

            DBObject guessObject = new BasicDBObject();
            DBObject each = new BasicDBObject();
            each.put("$each", guessWords);
            guessObject.put(DBConstants.F_GUESS_WORD_LIST, each);
            update.put("$addToSet", guessObject);

            BasicDBObject inc = new BasicDBObject();
            inc.put(DBConstants.F_GUESS_TIMES, 1);
            if (correct) {
                inc.put(DBConstants.F_CORRECT_TIMES, 1);

                // change by Benson
                int guessAwardCoint = score;
                if (guessAwardCoint <= 0) {
                    guessAwardCoint = DBConstants.C_DEFAULT_WORD_SCORE;
                }
                log.info("<guessOpus> award user " + opusCreatorUid + " with coins =" + guessAwardCoint);

                // award draw user
                UserManager.chargeAccount(mongoClient, opusCreatorUid, guessAwardCoint,
                        DBConstants.C_CHARGE_SOURCE_GUESS_REWARD, null, null);

//				// award guess user coins
//				UserManager.chargeAccount(mongoClient, uid, score, 
//						DBConstants.C_CHARGE_SOURCE_GUESS_REWARD, null, null);
//				
//				// award guess user exp
//				UserManager.increaseExperience(mongoClient, uid, DBConstants.GAME_ID_DRAW, 2);		// exp is 2 for guess
            }
            update.put("$inc", inc);

            log.info("<GuessOpus> update guess time and correct time opusId = "
                    + opusId);

            mongoClient.updateOne(DBConstants.T_OPUS, query, update);
        }

        // update opus score
        UserAction opus = updateOpusScore(mongoClient, opusId, true);

        // add a guess action
        UserAction userAction = new UserAction(new BasicDBObject());
        userAction.setActionId(new ObjectId());
        userAction.setAppId(appId);
        userAction.setOpusCreatorUid(opusCreatorUid);
        userAction.addRelatedOpusId(opusCreatorUid); // add by Benson
        userAction.addRelatedOpusId(uid); // add by Benson
        // userAction.addDirectRelatedOpusId(uid); // add by Benson
        userAction.setCategory(category);
        userAction.setOpusId(opusId);
        userAction.setCreateUserId(uid);
        userAction.setNickName(nickName);
        userAction.setAvatar(avatar);
        userAction.setGender(gender);
        userAction.setCreateDate(new Date());
        userAction.setType(UserAction.TYPE_GUESS);
        userAction.setGuessWordList(guessWords);
        userAction.setScore(score);
        userAction.setCorrect(correct);
        userAction.setHasWords(hasWords);
        userAction.setOpusStatus(UserAction.STATUS_NORMAL);
        userAction.setUserVip(vip);

        mongoClient.insert(DBConstants.T_OPUS_ACTION, userAction.getDbObject());
        AllFeedManager.getInstance().addActionToFans(userAction, appId);

        if (opus != null) {
            DrawGamePushManager.sendMessage(opus, userAction, opus.getAppId());
        }

        //insert time line opus guess indexs
        AbstractXiaoji xiaoji = XiaojiFactory.getInstance().getXiaoji(appId);
        xiaoji.userGuessTimelineManager().insertIndex(userAction.getCreateUserId(), userAction.getActionId());
        xiaoji.getOpusActionGuessManager().insertIndex(opusId, userAction.getObjectId().toString());

        xiaoji.opusForMatchManager.addUserGuessOpus(uid, opusId);
        xiaoji.opusForMatchManager.updateOpusForMatchScore(opusId, false, false, true);
    }

    private static void updateContestCommentCount(String opusCreatorUid, String uid,
                                                  UserAction comment, String appId) {


        String commentId = comment.getActionId();
        CommentInfo info = comment.getCommentInfo();

        //construct user id
        List<ObjectId> oIds = new ArrayList<ObjectId>(2);
        if (uid != null && !uid.equalsIgnoreCase(opusCreatorUid)) {
            oIds.add(new ObjectId(opusCreatorUid));
        }
        if (info != null) {
            String actionUid = info.getActionUserId();
            if (actionUid != null && !actionUid.equals(opusCreatorUid)) {
                oIds.add(new ObjectId(actionUid));
            }
        }

        // TODO insert into user comment timeline
        AbstractXiaoji xiaoji = XiaojiFactory.getInstance().getXiaoji(appId);
        CommentManager.getInstance().addCommentToUsers(commentId, oIds, uid, xiaoji);

        // TODO check push notification for contest comments
        if (uid != null && !uid.equalsIgnoreCase(opusCreatorUid)) {
            if (comment.getType() == UserAction.TYPE_CONTEST_COMMENT) {
                DrawGamePushManager.newComment(comment, appId, opusCreatorUid, false);
            }
        }
        if (info != null) {
            String actionUid = info.getActionUserId();
            if (actionUid != null && !actionUid.equals(opusCreatorUid)) {
                if (comment.getType() == UserAction.TYPE_CONTEST_COMMENT) {
                    DrawGamePushManager.newComment(comment, appId, actionUid, true);
                }
            }
        }


    }


    private static void updateCommentCount(String opusCreatorUid, String uid,
                                           UserAction comment, String appId) {


        String commentId = comment.getActionId();
        CommentInfo info = comment.getCommentInfo();

        //construct user id
        List<ObjectId> oIds = new ArrayList<ObjectId>(2);
        if (uid != null && !uid.equalsIgnoreCase(opusCreatorUid)) {
            oIds.add(new ObjectId(opusCreatorUid));
        }
        if (info != null) {
            String actionUid = info.getActionUserId();
            if (actionUid != null && !actionUid.equals(opusCreatorUid)) {
                oIds.add(new ObjectId(actionUid));
            }
        }

        // insert comment timeline
        AbstractXiaoji xiaoji = XiaojiFactory.getInstance().getXiaoji(appId);
        CommentManager.getInstance().addCommentToUsers(commentId, oIds, uid, xiaoji);


        if (uid != null && !uid.equalsIgnoreCase(opusCreatorUid)) {
            if (comment.getType() == UserAction.TYPE_COMMENT) {
                DrawGamePushManager.newComment(comment, appId, opusCreatorUid, false);
            }
        }
        if (info != null) {
            String actionUid = info.getActionUserId();
            if (actionUid != null && !actionUid.equals(opusCreatorUid)) {
                if (comment.getType() == UserAction.TYPE_COMMENT) {
                    DrawGamePushManager.newComment(comment, appId, actionUid, true);
                }
            }
        }

    }

    public static String commentOpus(MongoDBClient mongoClient, String appId,
                                     String opusCreatorUid, String opusId, String uid, String gender,
                                     String avatar, String nickName, String comment, CommentInfo info,
                                     String contestId, int category, int vip) {

        log.info("comment draw action");

        UserAction userAction = new UserAction(new BasicDBObject());
        userAction.setActionId(new ObjectId());
        userAction.setAppId(appId);
        userAction.setOpusCreatorUid(opusCreatorUid);
        userAction.setOpusId(opusId);
        userAction.setCreateUserId(uid);
        userAction.setNickName(nickName);
        userAction.setAvatar(avatar);
        userAction.setGender(gender);
        userAction.setCreateDate(new Date());
        userAction.setType(UserAction.TYPE_COMMENT);
        userAction.setComment(comment);
        userAction.setOpusStatus(UserAction.STATUS_NORMAL);
        userAction.setCommentInfo(info);
        userAction.setContestId(contestId);
        userAction.setCategory(category);
        userAction.setUserVip(vip);

        mongoClient.insert(DBConstants.T_OPUS_ACTION, userAction.getDbObject());
        increaseCommentTimes(mongoClient, opusId, 1);


        // TODO send the notification
        // updateCommentCount(opusCreatorUid, uid, userAction.getActionId(),
        // info);
        updateCommentCount(opusCreatorUid, uid, userAction, appId);

        AbstractXiaoji xiaoji = XiaojiFactory.getInstance().getXiaoji(appId);
        xiaoji.getOpusActionCommentManager().insertIndex(opusId, userAction.getObjectId().toString());

        return userAction.getActionId();
    }

    public static String contestCommentOpus(MongoDBClient mongoClient, String appId,
                                            String opusCreatorUid, String opusId, String uid, String gender,
                                            String avatar, String nickName, String comment, CommentInfo info,
                                            String contestId, int category, int vip) {

        log.info("contest comment draw action");

        UserAction userAction = new UserAction(new BasicDBObject());
        userAction.setActionId(new ObjectId());
        userAction.setAppId(appId);
        userAction.setOpusCreatorUid(opusCreatorUid);
        userAction.setOpusId(opusId);
        userAction.setCreateUserId(uid);
        userAction.setNickName(nickName);
        userAction.setAvatar(avatar);
        userAction.setGender(gender);
        userAction.setCreateDate(new Date());
        userAction.setType(UserAction.TYPE_CONTEST_COMMENT);
        userAction.setComment(comment);
        userAction.setOpusStatus(UserAction.STATUS_NORMAL);
        userAction.setCommentInfo(info);
        userAction.setContestId(contestId);
        userAction.setCategory(category);
        userAction.setUserVip(vip);

        mongoClient.insert(DBConstants.T_OPUS_ACTION, userAction.getDbObject());

        increaseContestCommentTimes(mongoClient, opusId, 1);

        // TODO check implementation inside
        updateContestCommentCount(opusCreatorUid, uid, userAction, appId);

        // insert action index
        AbstractXiaoji xiaoji = XiaojiFactory.getInstance().getXiaoji(appId);
        xiaoji.getOpusActionContestCommentManager().insertIndex(opusId, userAction.getObjectId().toString());

        // insert all contest comment timeline
        ContestCommentTimelineManager.getInstance().insertIndex(contestId, userAction.getActionId());

        return userAction.getActionId();
    }

    @Deprecated
    public static List<UserAction> getMyFeedList(MongoDBClient mongoClient,
                                                 String uid, int offset, int limit, boolean returnImage) {
        DBObject query = new BasicDBObject();
        DBObject uidObject = new BasicDBObject();
        DBObject targetObject = new BasicDBObject();

        targetObject.put(DBConstants.F_TARGET_UID, uid);
        uidObject.put(DBConstants.F_CREATE_USERID, uid);

        DBObject opusCreatorObject = new BasicDBObject();
        opusCreatorObject.put(DBConstants.F_OPUS_CREATOR_UID, uid);

		/*
         * rem by Benson BasicDBList orList = new BasicDBList();
		 * orList.add(uidObject); orList.add(opusCreatorObject);
		 * orList.add(targetObject);
		 */

        // Add By Benson
        BasicDBList relatedUserList = new BasicDBList();
        relatedUserList.add(uid);
        BasicDBObject inQuery = new BasicDBObject();
        inQuery.put("$in", relatedUserList);
        query.put(DBConstants.F_OPUS_RELATED_USER_ID, inQuery);

        query.put(DBConstants.F_OPUS_STATUS, UserAction.STATUS_NORMAL);
        query.put(DBConstants.F_TYPE, getFeedTypeObject());

        // query.put("$or", orList); // rem by Benson
        return getFeedList(mongoClient, query, offset, limit, returnImage, DBConstants.T_OPUS);
    }

    @Deprecated
    public static List<UserAction> getUserFeedList(MongoDBClient mongoClient,
                                                   String uid, int offset, int limit, boolean returnImage) {
        DBObject query = new BasicDBObject();
        query.put(DBConstants.F_TYPE, getFeedTypeObject());
        query.put(DBConstants.F_OPUS_STATUS, UserAction.STATUS_NORMAL);
        query.put(DBConstants.F_CREATE_USERID, uid);
        return getFeedList(mongoClient, query, offset, limit, returnImage, DBConstants.T_OPUS);
    }

    public static List<UserAction> getUserOpusList(MongoDBClient mongoClient, String category,
                                                   String uid, int offset, int limit, boolean returnImage) {
        DBObject query = new BasicDBObject();
        query.put(DBConstants.F_CREATE_USERID, uid);
        query.put(DBConstants.F_TYPE, getOpusTypeObject(category, true));
        query.put(DBConstants.F_OPUS_STATUS, UserAction.STATUS_NORMAL);
        return getFeedList(mongoClient, query, offset, limit, returnImage, DBConstants.T_OPUS);
    }


    public static List<UserAction> getUserOpusListDescending(MongoDBClient mongoClient, String category,
                                                             String uid, int offset, int limit, boolean returnImage) {
        DBObject query = new BasicDBObject();
        query.put(DBConstants.F_TYPE, getOpusTypeObject(category, true));
        query.put(DBConstants.F_CREATE_USERID, uid);
        query.put(DBConstants.F_OPUS_STATUS, UserAction.STATUS_NORMAL);
        List<UserAction> userActionList = getFeedListDescending(mongoClient, query, offset, limit, returnImage);
        Collections.sort(userActionList, new Comparator<UserAction>() {

            @Override
            public int compare(UserAction o1, UserAction o2) {
                return o1.getCreateDate().compareTo(o2.getCreateDate());
            }
        });
        return userActionList;
    }

    private static List<UserAction> getFeedListByFeedIdList(MongoDBClient mongoClient,
                                                            List<ObjectId> feedIdList, int offset, int limit, boolean returnImage) {

        DBObject query = null;
        if (feedIdList != null && feedIdList.size() > 0) {
            query = new BasicDBObject();
            DBObject inObject = new BasicDBObject();
            inObject.put("$in", feedIdList);
            query.put(DBConstants.F_OBJECT_ID, inObject);
        } else {
            return Collections.emptyList();
        }
        DBObject orderBy = new BasicDBObject();
        orderBy.put("_id", -1);
        return getFeedList(mongoClient, query, orderBy, 0, limit, returnImage, DBConstants.T_OPUS);
    }

    @Deprecated
    public static List<UserAction> getAllFeedList(MongoDBClient mongoClient,
                                                  String uid, int offset, int limit, boolean returnImage) {
        List<ObjectId> actionIdList = AllFeedManager.getInstance()
                .getAllFeedList(uid, offset, limit);
        if (actionIdList == null || actionIdList.size() == 0) {
            return Collections.emptyList();
        }
        DBObject query = new BasicDBObject();
        DBObject inQuery = new BasicDBObject();
        inQuery.put("$in", actionIdList);
        query.put(DBConstants.F_OBJECT_ID, inQuery);

        List<UserAction> list = getFeedList(mongoClient, query, 0, limit,
                returnImage, DBConstants.T_OPUS);
        if (offset == 0 && list != null && list.size() != 0) {
            UserAction action = list.get(0);
            Date timestamp = action.getCreateDate();
            UserManager.updateFeedTimestamp(mongoClient, uid, timestamp);
        }
        return list;
    }

    public static List<UserAction> getSimpleHotFeedList(
            MongoDBClient mongoClient, int offset, int limit, int language) {
        DBObject query = getHotFeedQuery(language);
        DBObject returnFields = new BasicDBObject();
        returnFields.put(DBConstants.F_HOT, 1);
        DBObject orderBy = new BasicDBObject();
        orderBy.put(DBConstants.F_HOT, -1);
        log.info("<getSimpleHotFeedList> query=" + query.toString() + ", orderBy=" + orderBy.toString());
        DBCursor cursor = mongoClient.find(DBConstants.T_OPUS, query,
                returnFields, orderBy, offset, limit);
        if (cursor != null) {
            List<UserAction> actionList = new ArrayList<UserAction>(limit);
            while (cursor.hasNext()) {
                UserAction action = new UserAction(cursor.next());
                actionList.add(action);
            }
            cursor.close();
            return actionList;
        }
        return Collections.emptyList();
    }

    public static List<UserAction> getHotFeedList(MongoDBClient mongoClient,
                                                  String uid, int offset, int limit, int language, boolean returnImage) {
        DBObject query = null;

        // language 1 for chinese, 2 for english
        List<ObjectId> list = HotFeedManagerFactory.getHotFeedManager().getFeedIds(language,
                offset, limit);

        if (list != null) {
            query = new BasicDBObject();
            DBObject inObject = new BasicDBObject();
            inObject.put("$in", list);
            query.put(DBConstants.F_OBJECT_ID, inObject);
        } else {
            return null;
            // log.info("warning: <getHotFeedList> use old query condition.");
            // query = getHotFeedQuery(language);
        }
        DBObject orderBy = new BasicDBObject();
        orderBy.put(DBConstants.F_HOT, -1);
        List<UserAction> retList = getFeedList(mongoClient, query, orderBy, 0,
                limit, returnImage, DBConstants.T_OPUS);
        if (offset == 0) {
            updateOpusTop3Image(retList);
        }
        return retList;
    }

    private static boolean setCommentQueryObject(DBObject query, int type) {
        if (type == UserAction.COMMENT_TYPE_COMMENT
                || type == UserAction.COMMENT_TYPE_FLOWER
                || type == UserAction.COMMENT_TYPE_TOMATO
                || type == UserAction.COMMENT_TYPE_GUESS) {
            query.put(DBConstants.F_TYPE, type);
            return true;
        } else if (type == UserAction.COMMENT_TYPE_TRY) {
            query.put(DBConstants.F_CORRECT, false);
            query.put(DBConstants.F_HAS_WORDS, true);
            return true;
        } else if (type == UserAction.COMMENT_TYPE_CORRECT) {
            query.put(DBConstants.F_TYPE, UserAction.TYPE_GUESS);
            query.put(DBConstants.F_CORRECT, true);
            return true;
        }
        return false;
    }

    public static List<UserAction> getCommentList(MongoDBClient mongoClient,
                                                  String opusId, String appId, int type, int offset, int limit) {
        BasicDBObject query = new BasicDBObject();

        query.put(DBConstants.F_OPUS_ID, opusId);
        query.put(DBConstants.F_OPUS_STATUS, UserAction.STATUS_NORMAL);
        boolean flag = setCommentQueryObject(query, type);
        if (flag) {
            return getCommentList(mongoClient, query, offset, limit);
        }
        return Collections.emptyList();
    }


    public static List<UserAction> getCommentListDisorder(MongoDBClient mongoClient,
                                                          String opusId, String appId, int type, int offset, int limit) {
        BasicDBObject query = new BasicDBObject();

        query.put(DBConstants.F_OPUS_ID, opusId);
        query.put(DBConstants.F_OPUS_STATUS, UserAction.STATUS_NORMAL);
        boolean flag = setCommentQueryObject(query, type);
        if (flag) {
            return getCommentListDisorder(mongoClient, query, offset, limit);
        }
        return Collections.emptyList();
    }


    public static List<UserAction> getCommentList(MongoDBClient mongoClient,
                                                  String opusId, String appId, boolean returnItem, int offset,
                                                  int limit) {
        BasicDBObject query = new BasicDBObject();

        query.put(DBConstants.F_OPUS_ID, opusId);
        query.put(DBConstants.F_OPUS_STATUS, UserAction.STATUS_NORMAL);

        BasicDBList orList = new BasicDBList();
        BasicDBObject commentObject = new BasicDBObject();
        BasicDBObject guessObject = new BasicDBObject();

        commentObject.put(DBConstants.F_TYPE, UserAction.TYPE_COMMENT);
        guessObject.put(DBConstants.F_TYPE, UserAction.TYPE_GUESS);
        guessObject.put(DBConstants.F_HAS_WORDS, true);
        orList.add(commentObject);
        orList.add(guessObject);

        if (returnItem) {
            BasicDBObject flowerObject = new BasicDBObject();
            BasicDBObject tomatoObject = new BasicDBObject();
            flowerObject.put(DBConstants.F_TYPE, UserAction.TYPE_FLOWER);
            tomatoObject.put(DBConstants.F_TYPE, UserAction.TYPE_TOMATO);
            orList.add(flowerObject);
            orList.add(tomatoObject);
        }

        query.put("$or", orList);

        return getCommentList(mongoClient, query, offset, limit);
    }

    private static List<UserAction> getCommentList(MongoDBClient mongoClient,
                                                   BasicDBObject query, int offset, int limit) {

        DBObject orderBy = new BasicDBObject();
        orderBy.put("_id", -1);

        log.info("<GetCommentList>:query = " + query.toString() + " orderBy = "
                + orderBy + ", limit=" + limit + ", offset = " + offset);
        DBCursor cursor = mongoClient.find(DBConstants.T_OPUS_ACTION, query,
                orderBy, offset, limit);

        log.info("<GetCommentList> query done");

        List<UserAction> feedList = new ArrayList<UserAction>(limit);

        // get the feed list.
        if (cursor != null) {
            log.info("<GetCommentList> iterate cursor starts, query = "
                    + query.toString() + " orderBy = " + orderBy + ", limit="
                    + limit + ", offset = " + offset);
            while (cursor.hasNext()) {
                UserAction userAction = new UserAction(cursor.next());

                // skip the delete draw.
                if (userAction.isDrawType()
                        || userAction.getOpusStatus() == UserAction.STATUS_DELETE) {
                    continue;
                }
                feedList.add(userAction);
            }
            cursor.close();

            return feedList;
        }
        return Collections.emptyList();
    }


    private static List<UserAction> getCommentListDisorder(MongoDBClient mongoClient,
                                                           BasicDBObject query, int offset, int limit) {

        DBObject orderBy = new BasicDBObject();
        //orderBy.put("_id", -1);

        log.info("<GetCommentList>:query = " + query.toString() + " orderBy = "
                + orderBy + ", limit=" + limit + ", offset = " + offset);
        DBCursor cursor = mongoClient.find(DBConstants.T_OPUS_ACTION, query,
                orderBy, offset, limit);

        log.info("<GetCommentList> query done");

        List<UserAction> feedList = new ArrayList<UserAction>(limit);

        // get the feed list.
        if (cursor != null) {
            log.info("<GetCommentList> iterate cursor starts, query = "
                    + query.toString() + " orderBy = " + orderBy + ", limit="
                    + limit + ", offset = " + offset);
            while (cursor.hasNext()) {
                UserAction userAction = new UserAction(cursor.next());

                // skip the delete draw.
                if (userAction.isDrawType()
                        || userAction.getOpusStatus() == UserAction.STATUS_DELETE) {
                    continue;
                }
                feedList.add(userAction);
            }
            cursor.close();

            return feedList;
        }
        return Collections.emptyList();
    }

    public static long getNewFeedCount(MongoDBClient mongoClient, String userId) {
        return AllFeedManager.getInstance()
                .getNewFeedCount(mongoClient, userId);

    }

    public static int deleteOpus(MongoDBClient mongoClient, String feedId,
                                 String userId, String appId) {
        return deleteFeed(mongoClient, feedId, userId, appId, DBConstants.T_OPUS);
    }

    public static int deleteOpusAction(MongoDBClient mongoClient, String feedId,
                                       String userId, String appId) {
        return deleteFeed(mongoClient, feedId, userId, appId, DBConstants.T_OPUS_ACTION);
    }

    public static int deleteFeed(MongoDBClient mongoClient,
                                 String feedId,
                                 String userId,
                                 String appId,
                                 String tableName) {

        int resultCode = ErrorCode.ERROR_SUCCESS;

        DBObject query = new BasicDBObject();

        query.put(DBConstants.F_OBJECT_ID, new ObjectId(feedId));

        DBObject returnField = new BasicDBObject();
        returnField.put(DBConstants.F_TYPE, 1);
        returnField.put(DBConstants.F_CREATE_USERID, 1);
        returnField.put(DBConstants.F_OPUS_ID, 1);
        returnField.put(DBConstants.F_CORRECT, 1);
        returnField.put(DBConstants.F_OPUS_CREATOR_UID, 1);
        returnField.put(DBConstants.F_COMMENT_INFO, 1);
        returnField.put(DBConstants.F_LANGUAGE, 1);
        DBObject retObject = mongoClient.findOne(tableName, query,
                returnField);

        if (retObject == null) {
            return ErrorCode.ERROR_USER_ACTION_INVALID;
        }

        UserAction target = new UserAction(retObject);

        if (!target.getCreateUserId().equals(userId) &&
                !(target.getOpusCreatorUid() != null && target.getOpusCreatorUid().equals(userId))) {
            if (!BBSPrivilegeManager.isSuperAdmin(mongoClient, userId)) {
                log.warn("try to delete user but user " + userId + " is not super admin or create user");
                resultCode = ErrorCode.ERROR_BBS_NO_PRIVILIGE;
                return resultCode;
            }
        } else {
            // user cannot delete his/her contest opus
            if (target.isContestDraw()) {
                return ErrorCode.ERROR_DELETE_CONTEST_OPUS;
            }
        }

        // if the feed exists
        DBObject update = new BasicDBObject();
        DBObject set = new BasicDBObject();
        set.put(DBConstants.F_OPUS_STATUS, UserAction.STATUS_DELETE);
        update.put("$set", set);
        mongoClient.updateAll(tableName, query, update);
        UserAction action = new UserAction(retObject);
        boolean flag = isFeedType(action.getType());

        /* disaable delete action from fans to reduce server pressure
        if (flag) {
            AllFeedManager.getInstance().deleteActionFromFans(action, appId);
            HotFeedManagerFactory.getHotFeedManager().deleteAction(action.getActionId(),
                    action.getLanguage());
        }
        */

        String opusId = action.getOpusId();
        if (opusId != null) {
            int type = action.getType();
            // decrease the times
            if (type == UserAction.TYPE_COMMENT) {
                increaseCommentTimes(mongoClient, opusId, -1);
            } else if (type == UserAction.TYPE_CONTEST_COMMENT) {
                increaseContestCommentTimes(mongoClient, opusId, -1);
            } else if (type == UserAction.TYPE_FLOWER) {
                increaseFlowerTimes(mongoClient, opusId, -1, null);    // TODO this is not that good
            } else if (type == UserAction.TYPE_TOMATO) {
                increaseTomatoTimes(mongoClient, opusId, -1);
            } else if (type == UserAction.TYPE_GUESS) {
                increaseGuessTimes(mongoClient, opusId, -1);
                if (action.isCorrect()) {
                    increaseCorrectTimes(mongoClient, opusId, -1);
                }
            }
        }

        CommentInfo info = action.getCommentInfo();
        if (info != null) {
            List<ObjectId> userIds = new ArrayList<ObjectId>(2);
            String actionUid = info.getActionUserId();
            if (actionUid != null) {
                userIds.add(new ObjectId(actionUid));
            }
            String author = action.getOpusCreatorUid();
            if (author != null && !author.equals(actionUid)) {
                userIds.add(new ObjectId(author));
            }
            CommentManager.getInstance().removeCommentFromUsers(
                    action.getActionId(), userIds);
        }

        //remove opus action index
        AbstractXiaoji xiaoji = XiaojiFactory.getInstance().getXiaoji(appId);
        OpusActionManager opusActionManager = xiaoji.opusActionManager(action.getType());
        if (opusActionManager != null) {
            opusActionManager.removeIndex(action.getOpusId(), action.getObjectId().toString());
        }
        if (isOpus(action)) {
            UserOpusManager opusManager = xiaoji.userOpusManager();
            opusManager.removeId(userId, feedId, false);

            // remove from hot top, add 2013-08-09
            xiaoji.hotTopOpusManager(action.getLanguage()).deleteIndex(action.getActionId(), false);

            xiaoji.opusForMatchManager.deleteIndex(action.getActionId(), true);
        }

        OpusClassService.getInstance().clearOpusClass(action);

        return resultCode;
    }


    private static boolean isOpus(UserAction action) {
        for (int type : UserAction.allOpusTypes) {
            if (action.getType() == type)
                return true;
        }
        return false;
    }


    private static boolean isFeedType(int type) {
        if (type == UserAction.TYPE_DRAW
                || type == UserAction.TYPE_DRAW_TO_USER
                || type == UserAction.TYPE_GUESS) {
            return true;
        }
        return false;
    }

    public static void throwItemToOpus(MongoDBClient mongoClient, String appId,
                                       String opusCreatorUid, String opusId, String uid, String gender,
                                       String avatar, String nickName, int itemType, String contestId,
                                       Contest contest, int category, int vip) {

        log.info(uid + " thrwo item(type = " + itemType + ") to " + opusId);

        UserAction userAction = new UserAction(new BasicDBObject());
        userAction.setActionId(new ObjectId());
        userAction.setAppId(appId);
        userAction.setOpusCreatorUid(opusCreatorUid);
        userAction.setOpusId(opusId);
        userAction.setCreateUserId(uid);
        userAction.setNickName(nickName);
        userAction.setAvatar(avatar);
        userAction.setGender(gender);
        userAction.setCreateDate(new Date());
        userAction.setType(itemType);
        userAction.setCategory(category);
        userAction.setOpusStatus(UserAction.STATUS_NORMAL);
        userAction.setUserVip(vip);

        if (itemType == UserAction.TYPE_FLOWER) {
            increaseFlowerTimes(mongoClient, opusId, 1, contest);
        } else if (itemType == UserAction.TYPE_TOMATO) {
            increaseTomatoTimes(mongoClient, opusId, 1);
        }

        DrawGamePushManager.newItemToOpus(userAction, appId, opusCreatorUid);

        UserAction feed = getSimpleFeedByFeedId(mongoClient, opusId);
        CommentInfo info = null;
        if (feed != null) {
            info = new CommentInfo();
            info.setActionId(opusId);
            info.setActionNickName(feed.getNickName());
            info.setActionSummary(feed.getWord());
            info.setType(feed.getType());
            info.setActionUserId(feed.getCreateUserId());
            userAction.setCommentInfo(info);
        }
        updateCommentCount(opusCreatorUid, uid, userAction, appId);
        mongoClient.insert(DBConstants.T_OPUS_ACTION, userAction.getDbObject());


        if (itemType == UserAction.TYPE_FLOWER) {
            //insert opus action in index
            AbstractXiaoji xiaoji = XiaojiFactory.getInstance().getXiaoji(appId);
            xiaoji.getOpusActionFlowerManager().insertIndex(opusId, userAction.getObjectId().toString());

            //update contest score

        }

    }

    private static UserAction getSimpleFeedByFeedId(MongoDBClient mongoClient,
                                                    String opusId) {
        DBObject returnFields = new BasicDBObject();
        returnFields.put(DBConstants.F_DRAW_DATA, 0);
        returnFields.put(DBConstants.F_GUESS_WORD_LIST, 0);
        DBObject object = mongoClient.findOneByObjectId(DBConstants.T_OPUS,
                opusId, returnFields);
        if (object != null) {
            return new UserAction(object);
        }
        return null;
    }

    public static void actionPlayOpus(MongoDBClient mongoClient, String appId,
                                      String opusId, String userId, String actionName, int category) {
        if (StringUtil.isEmpty(actionName))
            return;

        log.info("action play on opus =" + opusId + ",actionName=" + actionName);
        increaseActionTimes(mongoClient, opusId, actionName, 1);
    }

    public static void actionSaveOpus(MongoDBClient mongoClient, String appId,
                                      String opusId, String userId, String actionName, int category) {
        if (StringUtil.isEmpty(actionName))
            return;

        AbstractXiaoji xiaoji = XiaojiFactory.getInstance().getXiaoji(appId);
        if (xiaoji.userFavoriteOpusManager().isIdExistInList(userId, opusId)) {
            log.info(" action save on opus =" + opusId + ", but opusId already saved by user");
            return;
        }

        log.info(" action save on opus =" + opusId + ",actionName=" + actionName);
        increaseActionTimes(mongoClient, opusId, actionName, 1);
        if (actionName.equals(DBConstants.F_SAVE_TIMES)) {
            updateUserOpusCollection(mongoClient, opusId, userId);

            //insert user favorite
            xiaoji.userFavoriteOpusManager().insertIndex(userId, opusId);
        }
    }

    // 更新用户收藏作品表
    private static void updateUserOpusCollection(MongoDBClient mongoClient,
                                                 String opusId, String userId) {

        DBObject query = new BasicDBObject();
        query.put(DBConstants.F_UID, userId);
        query.put(DBConstants.F_OPUS_ID, opusId);

        DBObject update = new BasicDBObject();
        update.put(DBConstants.F_UID, userId);
        update.put(DBConstants.F_OPUS_ID, opusId);
        update.put(DBConstants.F_CREATE_DATE, new Date());
        update.put(DBConstants.F_ACTION_TYPE, 0);

        mongoClient.updateOrInsert(DBConstants.T_USER_OPUS_ACTION, query, update);


    }

    public static UserAction getOpusSimpleInfoById(String opusId){

        if (StringUtil.isEmpty(opusId)){
            return null;
        }

        DBObject fields = new BasicDBObject();
        fields.put(DBConstants.F_GUESS_WORD_LIST, 0);
        fields.put(DBConstants.F_USERID_LIST, 0);
        fields.put(DBConstants.F_OPUS_RELATED_USER_ID, 0);
        fields.put(DBConstants.F_DRAW_DATA, 0);
        DBObject object = DBService.getInstance().getMongoDBClient().findOneByObjectId(DBConstants.T_OPUS, opusId, fields);
        if (object != null) {
            UserAction action = new UserAction(object);
            return action;
        }
        return null;
    }

    public static UserAction getDrawById(MongoDBClient mongoClient, String uid,
                                         String opusId) {

        if (StringUtil.isEmpty(opusId)){
            return null;
        }

        DBObject fields = new BasicDBObject();
        fields.put(DBConstants.F_GUESS_WORD_LIST, 0);
        fields.put(DBConstants.F_USERID_LIST, 0);
        fields.put(DBConstants.F_OPUS_RELATED_USER_ID, 0);
        DBObject object = mongoClient.findOneByObjectId(DBConstants.T_OPUS, opusId, fields);
        if (object != null) {
            UserAction action = new UserAction(object);
            return action;
        }
        return null;
    }

    public static UserAction getOpusTimes(MongoDBClient mongoClient,
                                          String opusId) {
        DBObject fields = new BasicDBObject();
        fields.put(DBConstants.F_COMMENT_TIMES, 1);
        fields.put(DBConstants.F_GUESS_TIMES, 1);
        fields.put(DBConstants.F_CORRECT_TIMES, 1);
        fields.put(DBConstants.F_FLOWER_TIMES, 1);
        fields.put(DBConstants.F_TOMATO_TIMES, 1);
        fields.put(DBConstants.F_SAVE_TIMES, 1);
        DBObject object = mongoClient.findOneByObjectId(DBConstants.T_OPUS,
                opusId, fields);
        if (object != null) {
            return new UserAction(object);
        }
        return null;
    }

    public static List<UserAction> getDrawToUserOpusList(
            MongoDBClient mongoClient, String uid, int offset, int limit,
            boolean image) {
        DBObject query = new BasicDBObject();
        query.put(DBConstants.F_TARGET_UID, uid);
        query.put(DBConstants.F_OPUS_STATUS, UserAction.STATUS_NORMAL);
        query.put(DBConstants.F_TYPE, UserAction.TYPE_DRAW_TO_USER);
        List<UserAction> list = getFeedList(mongoClient, query, offset, limit,
                image, DBConstants.T_OPUS);
//		updateDrawToMeCount(mongoClient, uid, 0);
        return list;
    }


    public static List<UserAction> getDrawToUserOpusListDescending(
            MongoDBClient mongoClient, String uid, int offset, int limit,
            boolean image) {
        DBObject query = new BasicDBObject();
        query.put(DBConstants.F_TARGET_UID, uid);
        query.put(DBConstants.F_OPUS_STATUS, UserAction.STATUS_NORMAL);
        query.put(DBConstants.F_TYPE, UserAction.TYPE_DRAW_TO_USER);
        List<UserAction> list = getFeedListDescending(mongoClient, query, offset, limit,
                image);
        Collections.sort(list, new Comparator<UserAction>() {

            @Override
            public int compare(UserAction o1, UserAction o2) {
                return o1.getCreateDate().compareTo(o2.getCreateDate());
            }
        });
//		updateDrawToMeCount(mongoClient, uid, 0);
        return list;
    }

    public static List<UserAction> getHistoryTopOpusList(
            MongoDBClient mongoClient, String uid, int language, int offset,
            int limit, boolean image) {
        log.info("<getHistoryTopOpusList> begin, uid =" + uid);

        DBObject query = new BasicDBObject();
        query.put(DBConstants.F_TYPE, getOpusTypeObject(false));
        query.put(DBConstants.F_LANGUAGE, language);
        query.put(DBConstants.F_OPUS_STATUS, UserAction.STATUS_NORMAL);

        DBObject orderBy = new BasicDBObject();
        orderBy.put(DBConstants.F_HISTORY_SCORE, -1);
        List<UserAction> list = getFeedList(mongoClient, query, orderBy,
                offset, limit, image, DBConstants.T_OPUS);
        updateOpusTop3Image(list);
        log.info("<getHistoryTopOpusList> end, uid =" + uid);
        return list;
    }

//	public static long getNewDrawToMeCount(MongoDBClient mongoClient,
//			String userId) {
//		DBObject fields = new BasicDBObject();
//		fields.put(DBConstants.F_DRAWTOME_COUNT, 1);
//		DBObject obj = mongoClient.findOneByObjectId(DBConstants.T_USER,
//				userId, fields);
//		if (obj != null) {
//			User user = new User(obj);
//			return user.getDrawToMeCount();
//		}
//		return 0;
//	}

    private static void updateDrawToMeCount(MongoDBClient mongoClient,
                                            String userId, int count, AbstractXiaoji xiaoji) {

        if (xiaoji == null) {
            return;
        }

        DBObject query = new BasicDBObject();
        query.put(DBConstants.F_USERID, new ObjectId(userId));
        DBObject update = new BasicDBObject();
        // update.put(, arg1)
        DBObject set = new BasicDBObject();
        set.put(xiaoji.getDrawToMeField(), count);
        update.put("$set", set);

        mongoClient.updateOne(DBConstants.T_USER, query, update);
    }

    public static List<UserAction> getMyCommentList(MongoDBClient mongoClient,
                                                    String uid, int offset, int limit) {
        List<ObjectId> actionIdList = CommentManager.getInstance()
                .getCommentList(uid, offset, limit);

        if (actionIdList == null || actionIdList.size() == 0) {
            return Collections.emptyList();
        }
        BasicDBObject query = new BasicDBObject();
        DBObject inQuery = new BasicDBObject();
        inQuery.put("$in", actionIdList);
        query.put(DBConstants.F_OBJECT_ID, inQuery);
        log.info("<getMyCommentList>, query = " + query);
        query.put(DBConstants.F_OPUS_STATUS, UserAction.STATUS_NORMAL);

        List<UserAction> list = getCommentList(mongoClient, query, 0, limit);
        return list;

    }

    public static List<UserAction> getMyCommentListDisorder(MongoDBClient mongoClient,
                                                            String uid, int offset, int limit) {
        List<ObjectId> actionIdList = CommentManager.getInstance()
                .getCommentList(uid, offset, limit);

        if (actionIdList == null || actionIdList.size() == 0) {
            return Collections.emptyList();
        }
        BasicDBObject query = new BasicDBObject();
        DBObject inQuery = new BasicDBObject();
        inQuery.put("$in", actionIdList);
        query.put(DBConstants.F_OBJECT_ID, inQuery);
        log.info("<getMyCommentList>, query = " + query);
        query.put(DBConstants.F_OPUS_STATUS, UserAction.STATUS_NORMAL);

        List<UserAction> list = getCommentListDisorder(mongoClient, query, 0, limit);
        return list;

    }

    public static List<UserAction> getContestHotOpusList(
            MongoDBClient mongoClient, String uid, String contestId,
            int language, int offset, int limit) {
        DBObject orderBy = new BasicDBObject();
        orderBy.put(DBConstants.F_CONTEST_SCORE, -1);
        DBObject query = new BasicDBObject();
        query.put(DBConstants.F_CONTESTID, contestId);
        query.put(DBConstants.F_OPUS_STATUS, UserAction.STATUS_NORMAL);
        query.put(DBConstants.F_LANGUAGE, language);
        List<UserAction> list = getFeedList(mongoClient, query, orderBy,
                offset, limit, true, DBConstants.T_OPUS);
        if (offset == 0) {
            updateOpusTop3Image(list);
        }
        return list;
    }

    private static void updateOpusTop3Image(List<UserAction> list) {
        int i = 0;
        for (UserAction action : list) {
            if (action.getOpusImageUrl() != null) {
                action.setOpusThumbImageUrl(action.getOpusImageUrl());
            }
            if (++i >= 3) {
                return;
            }
        }
    }

    public static List<UserAction> getContestMyOpusList(
            MongoDBClient mongoClient, String uid, String contestId,
            int language, int offset, int limit) {

        List<ObjectId> feedIdList = ContestFeedManager.getInstance().getContestUserOpusIds(mongoClient, contestId, uid, offset, limit);
        return getFeedListByFeedIdList(mongoClient, feedIdList, offset, limit, true);
		
		/* old impl, rem by Benson
		DBObject query = new BasicDBObject();
		query.put(DBConstants.F_CONTESTID, contestId);
		query.put(DBConstants.F_CREATE_USERID, uid);
		query.put(DBConstants.F_OPUS_STATUS, UserAction.STATUS_NORMAL);
		return getFeedList(mongoClient, query, offset, limit, true);
		*/
    }

    public static List<UserAction> getContestNewOpusList(
            MongoDBClient mongoClient, String uid, String contestId,
            int language, int offset, int limit) {

        List<ObjectId> feedIdList = ContestFeedManager.getInstance().getLatestContestOpusIds(mongoClient, contestId, offset, limit);
        return getFeedListByFeedIdList(mongoClient, feedIdList, offset, limit, true);

		/*
		DBObject query = new BasicDBObject();
		query.put(DBConstants.F_CONTESTID, contestId);
		query.put(DBConstants.F_OPUS_STATUS, UserAction.STATUS_NORMAL);
		return getFeedList(mongoClient, query, offset, limit, true);
		*/
    }

    public static String getFileUploadLocalDir() {
        String dir = System.getProperty("upload.local.drawImage");
        log.info("getFileUploadLocalDir dir = " + dir);
        return (dir == null ? "" : dir);
    }

    public static String getFileUploadRemoteDir() {
        String dir = System.getProperty("upload.remote.drawImage");
        log.info("getFileUploadRemoteDir dir = " + dir);
        return (dir == null ? "" : dir);
    }

    public static String getFileUploadLocalDir(String category) {
        String para = String.format("upload.local.%sImage", category.toLowerCase());
        String dir = System.getProperty(para);
        log.info("getFileUploadLocalDir dir = " + dir);
        return (dir == null ? "" : dir);
    }

    public static String getFileUploadRemoteDir(String category) {
        String para = String.format("upload.remote.%sImage", category.toLowerCase());
        String dir = System.getProperty(para);
        log.info("getFileUploadRemoteDir dir = " + dir);
        return (dir == null ? "" : dir);
    }

    public static int updateDrawAction(MongoDBClient mongoClient,
                                       String opusId, String drawImageUrl, String thumbImageUrl,
                                       String drawDataUrl, int dataLen, String type, String targetUserId, String desc) {

        if (opusId == null) {
            return ErrorCode.ERROR_USER_ACTION_INVALID;
        }

        BasicDBObject query = new BasicDBObject();
        query.put("_id", new ObjectId(opusId));

        BasicDBObject update = new BasicDBObject();
        BasicDBObject updateValue = new BasicDBObject();

        if (thumbImageUrl != null) {
            updateValue.put(DBConstants.F_THUMB_URL, thumbImageUrl);
        }

        if (drawImageUrl != null) {
            updateValue.put(DBConstants.F_IMAGE_URL, drawImageUrl);
        }

        if (drawDataUrl != null) {
            updateValue.put(DBConstants.F_DRAW_DATA_URL, drawDataUrl);
            updateValue.put(DBConstants.F_DRAW_DATA_LEN, dataLen);
        }

        if (!StringUtil.isEmpty(type)) {
            updateValue.put(DBConstants.F_TYPE, Integer.parseInt(type));
        }

        if (targetUserId != null) {
            updateValue.put(DBConstants.F_TARGET_UID, targetUserId);
        }

        if (!StringUtil.isEmpty(desc)) {
            updateValue.put(DBConstants.F_DESC, desc);
        }

        update.put("$set", updateValue);

        log.info("<updateDrawAction> query=" + query.toString() + ", update="
                + update.toString());
        mongoClient.updateOne(DBConstants.T_OPUS, query, update);
        return ErrorCode.ERROR_SUCCESS;
    }

    public static int updateOpusDataLen(MongoDBClient mongoClient, String opusId, int dataLen) {

        if (opusId == null) {
            return ErrorCode.ERROR_USER_ACTION_INVALID;
        }

        BasicDBObject query = new BasicDBObject();
        query.put("_id", new ObjectId(opusId));

        BasicDBObject update = new BasicDBObject();
        BasicDBObject updateValue = new BasicDBObject();

        updateValue.put(DBConstants.F_DRAW_DATA_LEN, dataLen);
        update.put("$set", updateValue);

        log.info("<updateOpusDataLen> query=" + query.toString() + ", update="
                + update.toString());
        mongoClient.updateOne(DBConstants.T_OPUS, query, update);
        return ErrorCode.ERROR_SUCCESS;
    }

    public static int changeOpusTargetUser(MongoDBClient mongoClient, String opusId, String targetUserId) {

        if (StringUtil.isEmpty(opusId) || StringUtil.isEmpty(targetUserId)) {
            return ErrorCode.ERROR_USER_ACTION_INVALID;
        }

        User user = UserManager.findPublicUserInfoByUserId(targetUserId);
        if (user == null){
            return ErrorCode.ERROR_USER_NOT_FOUND;
        }

        UserAction opus = OpusManager.getOpusSimpleInfoById(opusId);
        if (opus == null){
            return ErrorCode.ERROR_USER_ACTION_INVALID;
        }

        int type = opus.getOpusToUserType(opus.getCategory());

        BasicDBObject query = new BasicDBObject();
        query.put("_id", new ObjectId(opusId));

        BasicDBObject update = new BasicDBObject();
        BasicDBObject updateValue = new BasicDBObject();

        updateValue.put(DBConstants.F_TARGET_UID, user.getUserId());
        updateValue.put(DBConstants.F_TARGET_NICK, user.getNickName());
        updateValue.put(DBConstants.F_TYPE, type);
        update.put("$set", updateValue);

        log.info("<changeOpusTargetUser> query=" + query.toString() + ", update="+ update.toString());
        mongoClient.updateOne(DBConstants.T_OPUS, query, update);

        // update draw to user model index
        DrawToUserOpusManager drawToUserOpusManager = XiaojiFactory.getInstance().getXiaoji(opus.getCategory()).drawToUserOpusManager();
        if (!drawToUserOpusManager.isIdExistInList(targetUserId, opusId)){
            log.info("<changeOpusTargetUser> update draw to user index targetUserId="+targetUserId+", opusId="+opusId);
            drawToUserOpusManager.insertAndConstructIndex(targetUserId, opusId, true);
        }

        return ErrorCode.ERROR_SUCCESS;
    }

    public static String getWordWithActionId(MongoDBClient mongoClient,
                                             String opusId) {
        if (!StringUtil.isEmpty(opusId)) {
            DBObject fields = new BasicDBObject();
            fields.put(DBConstants.F_WORD, 1);
            DBObject object = mongoClient.findOneByObjectId(
                    DBConstants.T_OPUS, opusId, fields);
            if (object != null) {
                return (String) object.get(DBConstants.F_WORD);
            }
        }
        return null;
    }

    public static List<UserAction> getTopOpusSimpleInfo(
            MongoDBClient mongoClient, int language, int offset, int limit) {
        Object actionIdList = HotFeedManagerFactory.getHotFeedManager().getFeedIds(language,
                offset, limit + 3);

        BasicDBObject query = new BasicDBObject();
        DBObject inQuery = new BasicDBObject();
        inQuery.put("$in", actionIdList);
        query.put(DBConstants.F_OBJECT_ID, inQuery);
        query.put(DBConstants.F_OPUS_STATUS, UserAction.STATUS_NORMAL);

        BasicDBObject field = new BasicDBObject();
        field.put(DBConstants.F_IMAGE_URL, 1);
        field.put(DBConstants.F_WORD, 1);
        field.put(DBConstants.F_CREATE_USERID, 1);

        DBObject orderBy = new BasicDBObject();
        orderBy.put(DBConstants.F_HOT, -1);
        DBCursor cursor = mongoClient.find(DBConstants.T_OPUS, query, field,
                orderBy, offset, limit);
        if (cursor != null) {
            List<UserAction> retList = new ArrayList<UserAction>(limit);
            while (cursor.hasNext()) {
                DBObject object = cursor.next();
                UserAction action = new UserAction(object);
                retList.add(action);
            }
            cursor.close();
            return retList;
        }
        return Collections.emptyList();
    }


    public static List<UserAction> getSavedOpusList(MongoDBClient mongoClient,
                                                    String uid, int offset, int limit) {

        BasicDBObject query = new BasicDBObject();
        query.put(DBConstants.F_UID, uid);

        BasicDBObject returnFields = new BasicDBObject();
        returnFields.put(DBConstants.F_OPUS_ID, 1);

        BasicDBObject orderBy = new BasicDBObject(DBConstants.F_CREATE_DATE, -1);

        log.info("<getSavedOpusList> query=" + query.toString() + ", fields=" + returnFields + ", orderBy=" + orderBy.toString());
        DBCursor cursor = mongoClient.find(DBConstants.T_USER_OPUS_ACTION, query, returnFields,
                orderBy, offset, limit);
        if (cursor != null) {
            List<ObjectId> retList = new ArrayList<ObjectId>(limit);
            while (cursor.hasNext()) {
                DBObject object = cursor.next();
                ObjectId objectId = new ObjectId((String) object.get(DBConstants.F_OPUS_ID));
                retList.add(objectId);
            }
            cursor.close();
            return getFeedListByFeedIdList(mongoClient, retList, offset, limit, true);
        } else {
            return Collections.emptyList();
        }

    }
	
	/*public static List<UserAction> getSavedOpusListDescending(MongoDBClient mongoClient,
			String uid, int offset, int limit) {
		
		BasicDBObject query = new BasicDBObject();
		query.put(DBConstants.F_UID, uid);

		BasicDBObject returnFields = new BasicDBObject();
		returnFields.put(DBConstants.F_OPUS_ID, 1);
		
		BasicDBObject orderBy = new BasicDBObject(DBConstants.F_CREATE_DATE, 1);
		
		log.info("<getSavedOpusList> query="+query.toString()+", fields="+returnFields+", orderBy="+orderBy.toString());
		DBCursor cursor = mongoClient.find(DBConstants.T_USER_OPUS_ACTION, query, returnFields,
				orderBy, offset, limit);
		if (cursor != null) {
			List<ObjectId> retList = new ArrayList<ObjectId>(limit);
			while (cursor.hasNext()) {
				DBObject object = cursor.next();
				ObjectId objectId = new ObjectId((String)object.get(DBConstants.F_OPUS_ID));
				retList.add(objectId);
			}
			cursor.close();
			return getFeedListByFeedIdList(mongoClient, retList, offset, limit, true);		
		}
		else{
			return Collections.emptyList();
		}
		
	}*/

    public static long getOpusCount(MongoDBClient mongoClient, String uid) {
        DBObject query = new BasicDBObject();
        query.put(DBConstants.F_TYPE, getOpusTypeObject(true));
        query.put(DBConstants.F_CREATE_USERID, uid);
        query.put(DBConstants.F_OPUS_STATUS, UserAction.STATUS_NORMAL);
        return mongoClient.count(DBConstants.T_OPUS, query);
    }

    public static UserAction matchDrawFromHot(MongoDBClient mongoClient,
                                              String uid, String gender, int language, AbstractXiaoji xiaoji) {

        int MAX_RANDOM = 50; //HotFeedManager.getHotFeedCachecount();
        List<ObjectId> list = xiaoji.hotTopOpusManager(language).getTopIdList(0, MAX_RANDOM); // HotFeedManagerFactory.getHotFeedManager().getFeedIds(language, 0, MAX_RANDOM);

        DBObject query = new BasicDBObject();
        if (list != null && list.size() > 0) {
            int randomOpusIndex = RandomUtil.random(list.size());
            ObjectId opusId = list.get(randomOpusIndex);
            query.put(DBConstants.F_OBJECT_ID, opusId);
        } else {
            return null;
        }

        // not drawer
        BasicDBObject notDrawer = new BasicDBObject();
        notDrawer.put("$ne", uid);
        query.put(DBConstants.F_CREATE_USERID, notDrawer);

        // not guess yet
        BasicDBObject notGuess = new BasicDBObject();
        notGuess.put("$ne", uid);
        query.put(DBConstants.F_USERID_LIST, notGuess);

        log.info("<matchDrawFromHot> query = " + query.toString());

        BasicDBObject fields = new BasicDBObject();
        fields.put(DBConstants.F_GUESS_WORD_LIST, 0);
        fields.put(DBConstants.F_USERID_LIST, 0);
        fields.put(DBConstants.F_OPUS_RELATED_USER_ID, 0);

        DBObject retObject = mongoClient.findOne(DBConstants.T_OPUS, query, fields);
        if (retObject == null)
            return null;

        UserAction userAction = new UserAction(retObject);
        addUserToAction(mongoClient, userAction.getActionId(), uid);
		
		/*
		List<UserAction> retList = getFeedList(mongoClient, query, null, 0, 1, true);
		if (retList != null && retList.size() > 0){
			UserAction userAction = retList.get(0);
			addUserToAction(mongoClient, userAction.getActionId(), uid);			
			return userAction;
		}
		*/

        return userAction;
    }

    public static void disableUserForMatch(MongoDBClient mongoClient, UserAction userAction) {

        log.info("<disableUserForMatch> opusId=" + userAction.getActionId());

        BasicDBObject query = new BasicDBObject("_id", userAction.getObjectId());
        BasicDBObject update = new BasicDBObject();
        BasicDBObject updateValue = new BasicDBObject();
        updateValue.put(DBConstants.F_GUESS_TIMES, 50);
        update.put("$set", updateValue);

        mongoClient.updateOne(DBConstants.T_OPUS, query, update);
    }

    public static void removeUserOpusFavorite(MongoDBClient mongoClient,
                                              String appId, String opusId, String uid, String actionName) {

        if (StringUtil.isEmpty(uid) || StringUtil.isEmpty(opusId)) {
            return;
        }

        DBObject query = new BasicDBObject();
        query.put(DBConstants.F_UID, uid);
        query.put(DBConstants.F_OPUS_ID, opusId);

        log.info("<removeUserOpusFavorite> query=" + query.toString());
        mongoClient.remove(DBConstants.T_USER_OPUS_ACTION, query);

        AbstractXiaoji xiaoji = XiaojiFactory.getInstance().getXiaoji(appId);
        UserFavoriteOpusManager userFavoriteOpusManager = xiaoji.userFavoriteOpusManager();
        userFavoriteOpusManager.removeIndex(uid, opusId);
    }

    public static List<UserAction> getRecommendOpusList(MongoDBClient mongoClient, int language, int offset, int limit) {
        DBObject query = null;
        List<ObjectId> list = RecommendFeedManager.getInstance().getFeedIds(
                mongoClient, language, offset, limit);
        if (list != null) {
            query = new BasicDBObject();
            DBObject inObject = new BasicDBObject();
            inObject.put("$in", list);
            query.put(DBConstants.F_OBJECT_ID, inObject);
        } else {
            return Collections.emptyList();
        }
        DBObject orderBy = new BasicDBObject();
        orderBy.put("_id", -1);

        return getFeedList(mongoClient, query, orderBy, 0, limit, true, DBConstants.T_OPUS);
    }

    public static void recoverUserOpus(String targetUserId, String opusId,
                                       String appId) {
        DBObject query = new BasicDBObject();
        query.put("_id", new ObjectId(opusId));

        BasicDBObject updateObject = new BasicDBObject();
        BasicDBObject updateValue = new BasicDBObject();

        updateValue.put(DBConstants.F_OPUS_STATUS, UserAction.STATUS_NORMAL);
        updateObject.put("$set", updateValue);

        DBService.getInstance().getMongoDBClient().updateOne(DBConstants.T_OPUS, query, updateObject);

        AbstractXiaoji xiaoji = XiaojiFactory.getInstance().getXiaoji(appId);
        if (xiaoji != null) {
            xiaoji.userOpusManager().insertIndex(targetUserId, opusId);
        }
    }


    public static int rankOpus(String userId, String opusId, String contestId, List<Integer> rankTypes, List<Integer> rankValues) {

        Contest contest = null;
        if (!StringUtil.isEmpty(contestId)) {
            contest = ContestManager.getContestById(DBService.getInstance().getMongoDBClient(), contestId);
        }

        if (contest == null) {
            return ErrorCode.ERROR_CONTEST_NOT_FOUND;
        }

        List<String> judges = contest.getJudgeList();
        if (judges == null || judges.size() == 0) {
            log.info(String.format("<rankOpus> but judge list is empty"));
            return ErrorCode.ERROR_CONTEST_NO_JUDGE;
        }

        if (userId == null || !judges.contains(userId)) {
            log.info(String.format("<rankOpus> user %s not in judge list", userId));
            return ErrorCode.ERROR_CONTEST_USER_NOT_JUDGE;
        }

        /*  check rank type can be enabled later
        if (rankType > DBConstants.DEFAULT_RANK_TYPE){
            boolean foundType = false;
            BasicDBList rankTypeList = contest.getRankTypeList();
            for (Object rankTypeObj : rankTypeList){
                int type = ((BasicDBObject)rankTypeObj).getInt(DBConstants.F_VALUE);
                if (type == rankType){
                    foundType = true;
                }
            }

            if (!foundType){
                log.info(String.format("<rankOpus> rank type (%d) not in list", rankType));
                return ErrorCode.ERROR_CONTEST_RANK_TYPE_NOT_FOUND;
            }
        }
        */

        BasicDBObject query = new BasicDBObject("_id", new ObjectId(opusId));
        BasicDBObject update = new BasicDBObject();
        BasicDBObject updateValue = new BasicDBObject();

        int i = 0;
        for (Integer rankType : rankTypes) {
            String field = ScoreManager.getOpusUserRankField(rankType, userId);
            updateValue.put(field, rankValues.get(i));
            i++;
        }
        update.put("$set", updateValue);

        DBObject obj = DBService.getInstance().getMongoDBClient().findAndModify(DBConstants.T_OPUS, query, update, OpusUtils.createReturnFields());
        if (obj != null) {
            // TODO this can be optimized later
            updateOpusScore(DBService.getInstance().getMongoDBClient(), opusId, false, contest);
        }

        return 0;
    }

    public static int rankOpus(String userId, String opusId, String contestId, int rankType, int rankValue) {

        Contest contest = null;
        if (!StringUtil.isEmpty(contestId)) {
            contest = ContestManager.getContestById(DBService.getInstance().getMongoDBClient(), contestId);
        }

        if (contest == null) {
            return ErrorCode.ERROR_CONTEST_NOT_FOUND;
        }

        List<String> judges = contest.getJudgeList();
        if (judges == null || judges.size() == 0) {
            log.info(String.format("<rankOpus> but judge list is empty"));
            return ErrorCode.ERROR_CONTEST_NO_JUDGE;
        }

        if (userId == null || !judges.contains(userId)) {
            log.info(String.format("<rankOpus> user %s not in judge list", userId));
            return ErrorCode.ERROR_CONTEST_USER_NOT_JUDGE;
        }

        /*  check rank type can be enabled later
        if (rankType > DBConstants.DEFAULT_RANK_TYPE){
            boolean foundType = false;
            BasicDBList rankTypeList = contest.getRankTypeList();
            for (Object rankTypeObj : rankTypeList){
                int type = ((BasicDBObject)rankTypeObj).getInt(DBConstants.F_VALUE);
                if (type == rankType){
                    foundType = true;
                }
            }

            if (!foundType){
                log.info(String.format("<rankOpus> rank type (%d) not in list", rankType));
                return ErrorCode.ERROR_CONTEST_RANK_TYPE_NOT_FOUND;
            }
        }
        */

        BasicDBObject query = new BasicDBObject("_id", new ObjectId(opusId));
        BasicDBObject update = new BasicDBObject();
        BasicDBObject updateValue = new BasicDBObject();

        String field = ScoreManager.getOpusUserRankField(rankType, userId);
        updateValue.put(field, rankValue);
        update.put("$set", updateValue);

        DBObject obj = DBService.getInstance().getMongoDBClient().findAndModify(DBConstants.T_OPUS, query, update, OpusUtils.createReturnFields());
        if (obj != null) {
            // TODO this can be optimized later
            updateOpusScore(DBService.getInstance().getMongoDBClient(), opusId, false);
        }

        return 0;
    }

    public static List<UserAction> getFeedsByIdList(List<ObjectId> opusIdIdList) {
        return idListUtils.getList(DBService.getInstance().getMongoDBClient(), DBConstants.T_OPUS, "_id", DBConstants.F_OPUS_STATUS, UserAction.STATUS_DELETE, opusIdIdList, OpusUtils.createReturnFields(), UserAction.class);
    }

    public static void updateOpus(String opusId, BasicDBObject updateObject) {

        if (StringUtil.isEmpty(opusId) || updateObject == null || updateObject.size() == 0){
            return;
        }

        BasicDBObject query = new BasicDBObject("_id", new ObjectId(opusId));
        BasicDBObject update = new BasicDBObject("$set", updateObject);

        DBService.getInstance().getMongoDBClient().updateOne(DBConstants.T_OPUS, query, update);

    }
}
