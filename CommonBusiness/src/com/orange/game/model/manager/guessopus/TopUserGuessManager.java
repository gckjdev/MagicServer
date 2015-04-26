package com.orange.game.model.manager.guessopus;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.common.CommonZSetIndexManager;
import com.orange.game.model.dao.opus.UserGuess;
import com.orange.game.model.dao.opus.UserGuessAchievement;
import com.orange.game.model.service.DBService;
import com.orange.network.game.protocol.model.OpusProtos;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created with IntelliJ IDEA.
 * User: Linruin
 * Date: 13-7-27
 * Time: 下午12:14
 * To change this template use File | Settings | File Templates.
 */
public class TopUserGuessManager extends CommonZSetIndexManager {

    private String achievementField;
    private String userGuessInfoTableName;
    private String userAchievementTableName;
    private int guessMode;
    private int rankType;
    private final String timeTag;

    public static final String ALL_TIME_TOP_PREFIX = "all_time_top_";
    public static final String HOT_TOP_PREFIX = "hot_top_";
    public static final String TOP_PREFIX = "top_";

    private static String getGuessModeName(int guessMode){
        switch (guessMode){
            case OpusProtos.PBUserGuessMode.GUESS_MODE_HAPPY_VALUE:
                return "happy";
            case OpusProtos.PBUserGuessMode.GUESS_MODE_GENIUS_VALUE:
                return "genius";
            case OpusProtos.PBUserGuessMode.GUESS_MODE_CONTEST_VALUE:
                return "contest";
        }
        return null;
    }

    public static String getContestRedisTableName(String contestId){
        return getRedisTableName(null, OpusProtos.PBUserGuessMode.GUESS_MODE_CONTEST_VALUE, contestId, 0, null);
    }

    private static String getRedisTableName(String category, int guessMode, String contestId, int rankMode, String timeTag){

        if (guessMode == OpusProtos.PBUserGuessMode.GUESS_MODE_CONTEST_VALUE){
            return TOP_PREFIX + UserGuessConstants.USER_GUESS_PREFIX + getGuessModeName(guessMode) + "_" + contestId;
        }else{

            if (rankMode == OpusProtos.PBRankType.HOT_RANK_VALUE){
                return HOT_TOP_PREFIX + UserGuessConstants.USER_GUESS_PREFIX + category.toLowerCase() + "_" + getGuessModeName(guessMode) + "_" + timeTag;
            }else if(rankMode == OpusProtos.PBRankType.ALL_TIME_RANK_VALUE){
                return ALL_TIME_TOP_PREFIX + UserGuessConstants.USER_GUESS_PREFIX + category.toLowerCase() + "_" + getGuessModeName(guessMode);
            }
        }
        return null;
    }

    private static String getMongoDBTableName(String category){
        return (DBConstants.T_USER_GUESS_ACHIEVEMENT + "_" + category);
    }

    protected String getAchievementField(int guessMode, String contestId, int rankMode, String timeTag){

        if (guessMode == OpusProtos.PBUserGuessMode.GUESS_MODE_CONTEST_VALUE){
            return getGuessModeName(guessMode) + "_" + contestId;
        }else{
            if (rankMode == OpusProtos.PBRankType.HOT_RANK_VALUE){
                return HOT_TOP_PREFIX + getGuessModeName(guessMode) + "_" + timeTag;        // 日榜，增加时间戳
            }else if(rankMode == OpusProtos.PBRankType.ALL_TIME_RANK_VALUE){
                return ALL_TIME_TOP_PREFIX + getGuessModeName(guessMode);
            }
        }
        return null;
    }

    protected String getUserGuessInfoTableName(String category, int guessMode, String contestId){

        return UserGuessUtil.getUserGuessInfoTableName(category, guessMode, contestId);
    }

    public TopUserGuessManager(String category,
                               int userGuessMode,
                               String contestId,
                               int guessRankType,
                               String timeTag){

        super(getRedisTableName(category, userGuessMode, contestId, guessRankType, timeTag),
                getMongoDBTableName(category),
                UserGuessAchievement.class);

        this.timeTag = timeTag;
        guessMode = userGuessMode;
        rankType = guessRankType;

        achievementField = getAchievementField(userGuessMode, contestId, guessRankType, timeTag);
        userGuessInfoTableName = getUserGuessInfoTableName(category, userGuessMode, contestId);
        userAchievementTableName = getMongoDBTableName(category);
        setMongoIdFieldName(DBConstants.F_FOREIGN_USER_ID);
    }



    public String getAchievementKey(){
        return  achievementField;
    }

    public UserGuess getUserGuessInfo(String userId) {

        ObjectId objectId= new ObjectId(userId);
        DBCursor cursor = DBService.getInstance().getMongoDBClient().find(userGuessInfoTableName, DBConstants.F_OWNER, objectId);

        UserGuess userGuess = null;
        if (cursor != null) {
            while (cursor.hasNext()) {
                // only fetch one record
                BasicDBObject dbObject = (BasicDBObject) cursor.next();
                userGuess= new UserGuess();
                userGuess.setDbObject(dbObject);
                break;
            }
            cursor.close();
        }

        if (userGuess != null){
            log.debug("<getUserGuessInfo> data found, return data = "+userGuess.getDbObject().toString());
        }
        else{
            log.info("<getUserGuessInfo> data not found for objectId="+objectId.toString());
        }


        return userGuess;
    }

    public UserGuessAchievement getAchievement(String userId){

        BasicDBObject returnField = new BasicDBObject();
        returnField.put(achievementField, 1);

        UserGuessAchievement achievement = (UserGuessAchievement) getSingle(userId, returnField);
        return achievement;
    }

    public void updateTopScore(final String userId) {

        final UserGuess userGuess = getUserGuessInfo(userId);

        if (userGuess == null){
            log.info("<TopUserGuessManager> updateTopScore but user guess object not found for userId "+userId);
            return;
        }

        double score = userGuess.calculateScore(guessMode, rankType);

        updateTopScore(userId, score, new Callable(){

            @Override
            public Object call() throws Exception {

                // update or insert the higher record into mongo db
                UserGuessAchievement achievement = getAchievementFromUserGuess(userGuess);
                insertArchievement(userId, achievement);
                return null;

            }

            private UserGuessAchievement getAchievementFromUserGuess(UserGuess userGuess) {
                UserGuessAchievement achievement = new UserGuessAchievement();
                achievement.put(DBConstants.F_GUESS_TIMES, userGuess.getGuessTimes());
                achievement.put(DBConstants.F_CORRECT_TIMES, userGuess.getPass());
                achievement.put(DBConstants.F_SPEND_TIME, userGuess.getSpendTime());
                achievement.put(DBConstants.F_EARN, userGuess.getEarn(guessMode));
                return achievement;
            }

        }, true, true);
    }

    public List<UserGuessAchievement> getAchievementList(int offset, int limit) {

        return getTopList(offset, limit, null, 0, null);
    }

    public void insertArchievement(String userId, UserGuessAchievement data){

        BasicDBObject query = new BasicDBObject();
        query.put(DBConstants.F_FOREIGN_USER_ID, new ObjectId(userId));

        BasicDBObject updateValue = new BasicDBObject();
        updateValue.put(achievementField, data.getDbObject());

        BasicDBObject update = new BasicDBObject();
        update.put("$set", updateValue);

        DBService.getInstance().getMongoDBClient().upsertAll(userAchievementTableName, query, update);
    }


    public void updateArchievement(String userId, String field, int data){

        BasicDBObject query = new BasicDBObject();
        query.put(DBConstants.F_FOREIGN_USER_ID, new ObjectId(userId));

        BasicDBObject updateValue = new BasicDBObject();

        updateValue.put(achievementField+"."+field, data);

        BasicDBObject update = new BasicDBObject();
        update.put("$set", updateValue);

        DBService.getInstance().getMongoDBClient().upsertAll(userAchievementTableName, query, update);
    }


//    public void updateArchievement(String userId, UserGuessAchievement data){
//
//        BasicDBObject query = new BasicDBObject();
//        query.put(DBConstants.F_FOREIGN_USER_ID, new ObjectId(userId));
//
//        BasicDBObject updateValue = new BasicDBObject();
//
//        updateValue.put(achievementField+"."+DBConstants.F_EARN, data.getEarn(achievementField));
//        updateValue.put(achievementField+"."+DBConstants.F_GUESS_TIMES, data.getTotalGuessTimes(achievementField));
//        updateValue.put(achievementField+"."+DBConstants.F_CORRECT_TIMES, data.getPass(achievementField));
//        updateValue.put(achievementField+"."+DBConstants.F_SPEND_TIME, data.getSpendTime(achievementField));
//
//        BasicDBObject update = new BasicDBObject();
//        update.put("$set", updateValue);
//
//        DBService.getInstance().getMongoDBClient().upsertAll(userAchievementTableName, query, update);
//    }

    public int getUserRank(String userId){

        return getZsetMemberRevRank(userId) + 1;
    }
}
