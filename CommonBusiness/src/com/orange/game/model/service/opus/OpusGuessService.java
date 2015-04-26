package com.orange.game.model.service.opus;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.manager.guessopus.TopUserGuessManager;
import com.orange.game.model.manager.guessopus.UserGuessOpusPlayManager;
import com.orange.game.model.service.DBService;
import com.orange.game.model.xiaoji.AbstractXiaoji;
import com.orange.network.game.protocol.model.OpusProtos;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Linruin
 * Date: 13-7-17
 * Time: 上午10:36
 * To change this template use File | Settings | File Templates.
 */
public class OpusGuessService {

    private static OpusGuessService ourInstance = new OpusGuessService();

    public static OpusGuessService getInstance() {
        return ourInstance;
    }

    private OpusGuessService() {
    }

    public static final Logger log = Logger.getLogger(OpusGuessService.class.getName());


    public void guessOpus(AbstractXiaoji xiaoji,
                          String appId,
                          UserGuessOpusPlayManager userGuessOpusPlayManager,
                          int mode,
                          String contestId,
                          String userId,
                          String opusId,
                          Set<String> guessWords,
                          boolean correct,
                          Date startDate,
                          Date endDate,
                          String opusCreatorUid,
                          String avatar,
                          String nickName,
                          String gender) {

        log.info("<guessOpus> opusId="+opusId);

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

                // TODO award painter

                // change by Benson
//                int guessAwardCoint = score;
//                if (guessAwardCoint <= 0){
//                    guessAwardCoint = DBConstants.C_DEFAULT_WORD_SCORE;
//                }
//                log.info("<guessOpus> award user "+opusCreatorUid+" with coins ="+guessAwardCoint);
//
//                // award draw user
//                UserManager.chargeAccount(mongoClient, opusCreatorUid, guessAwardCoint,
//                        DBConstants.C_CHARGE_SOURCE_GUESS_REWARD, null, null);

//				// award guess user coins
//				UserManager.chargeAccount(mongoClient, uid, score,
//						DBConstants.C_CHARGE_SOURCE_GUESS_REWARD, null, null);
//
//				// award guess user exp
//				UserManager.increaseExperience(mongoClient, uid, DBConstants.GAME_ID_DRAW, 2);		// exp is 2 for guess
            }
            update.put("$inc", inc);
            DBService.getInstance().getMongoDBClient().updateAll(DBConstants.T_OPUS, query, update);
        }

        // don't update opus score
        // UserAction opus = updateOpusScore(mongoClient, opusId, true);

        // TODO start date and end date not used
        // add a guess action
        UserAction guessAction = xiaoji.getOpusActionGuessManager().insertGuessAction(appId, opusCreatorUid, userId,
                opusId, avatar, gender, nickName, guessWords, correct);

//        UserAction userAction = new UserAction(new BasicDBObject());
//        userAction.setActionId(new ObjectId());
//        userAction.setAppId(appId);
//        userAction.setOpusCreatorUid(opusCreatorUid);
//        userAction.addRelatedOpusId(opusCreatorUid);
//        userAction.addRelatedOpusId(userId);
//        userAction.setOpusId(opusId);
//        userAction.setCreateUserId(userId);
//        userAction.setNickName(nickName);
//        userAction.setAvatar(avatar);
//        userAction.setGender(gender);
//        userAction.setCreateDate(new Date());
//        userAction.setType(UserAction.TYPE_GUESS);
//        userAction.setGuessWordList(guessWords);
//        userAction.setCorrect(correct);
//        userAction.setHasWords(hasWords);
//        userAction.setOpusStatus(UserAction.STATUS_NORMAL);
//        mongoClient.insert(DBConstants.T_ACTION, userAction.getDbObject());

        // don't show in user fans feed/timeline
        // AllFeedManager.getInstance().addActionToFans(userAction,appId);

//        if (opus != null) {
//            DrawGamePushManager.sendMessage(opus, userAction, opus.getAppId());
//        }

        //insert time line opus guess indexs
        // don't show in user guess timeline, add by Benson 2013-09-30
//        xiaoji.userGuessTimelineManager().insertIndex(guessAction.getCreateUserId(), guessAction.getActionId());
        xiaoji.getOpusActionGuessManager().insertIndex(opusId, guessAction.getObjectId().toString());

        // update play guess info
        userGuessOpusPlayManager.updateOpusGuessInfo(userId, opusId, guessWords, correct, startDate, endDate);

        // update hot top user guess score
        if (mode == OpusProtos.PBUserGuessMode.GUESS_MODE_GENIUS_VALUE ||
                mode == OpusProtos.PBUserGuessMode.GUESS_MODE_HAPPY_VALUE){

            TopUserGuessManager hotTopUserGuessManager = xiaoji.getHotTopUserGuessManager(mode);
            if (hotTopUserGuessManager != null){
                hotTopUserGuessManager.updateTopScore(userId);
            }

            // update all time top user guess score
            TopUserGuessManager allTimeTopUserGuessManager = xiaoji.getAllTimeTopUserGuessManager(mode);
            if (allTimeTopUserGuessManager != null){
                allTimeTopUserGuessManager.updateTopScore(userId);
            }
        }else{
            // update contest top user guess score
            TopUserGuessManager topUserGuessManager = xiaoji.getContestTopGuessManager(contestId);
            if (topUserGuessManager != null){
                topUserGuessManager.updateTopScore(userId);
            }
        }
    }
}
