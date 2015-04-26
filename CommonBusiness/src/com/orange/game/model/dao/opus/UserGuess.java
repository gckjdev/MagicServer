package com.orange.game.model.dao.opus;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.orange.common.utils.DateUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.CommonData;
import com.orange.game.model.manager.guessopus.UserGuessOpusPlayManager;
import com.orange.network.game.protocol.model.GameBasicProtos;
import com.orange.network.game.protocol.model.OpusProtos;
import org.bson.types.ObjectId;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Linruin
 * Date: 13-7-17
 * Time: 下午1:40
 * To change this template use File | Settings | File Templates.
 */
public class UserGuess extends CommonData {

    private static final double M_SECOND_PER_DAY = 3600 * 24 * 1000;
    private static final double LOG_RADIUS = 1.1;

    public UserGuess() {
        super();
    }

    public UserGuess(DBObject obj) {
        super(obj);
    }

    public int getRank() {
        return getInt(DBConstants.F_RANK);
    }

    public ObjectId getUserId() {
        return getObjectId(DBConstants.F_OWNER);
    }

    public GameBasicProtos.PBGameCurrency getEarnCurrency() {
        return GameBasicProtos.PBGameCurrency.Coin;
    }

    public int getEarn(int guessMode) {
        return getInt(DBConstants.F_AWARD_COIN);
//        switch (guessMode){
//            case OpusProtos.PBUserGuessMode.GUESS_MODE_HAPPY_VALUE:
//                return getHappyEarn();
//            case OpusProtos.PBUserGuessMode.GUESS_MODE_GENIUS_VALUE:
//                return getGeniusEarn();
//            case OpusProtos.PBUserGuessMode.GUESS_MODE_CONTEST_VALUE:
//                return getContestEarn();
//        }
//        return 0;
    }

    private int getAwardCoins() {

        return getInt(DBConstants.F_AWARD_COIN);
    }

    private int getContestEarn() {

        return getAwardCoins();
    }

    private int getGeniusEarn() {

        int pass = getPass();

        int count = pass / 10;
        int earn = 0;
        for (int i = 0; i < count; i ++){

            earn += (1 + ((float)i / 10.0)) * 1000;
        }

        return earn;
    }

    private int getHappyEarn() {
        return getPass() / 10 * 100;  //To change body of created methods use File | Settings | File Templates.
    }

    public int getPass() {

        BasicDBList list = (BasicDBList)getObject(DBConstants.F_ID_LIST);
        if (list == null || list.size() == 0)
            return 0;

        int size = list.size();
        int pass = 0;
        for (int i=0; i<size; i++){
            BasicDBObject obj = (BasicDBObject )list.get(i);
            boolean correct = obj.getBoolean(DBConstants.F_CORRECT);
            if (correct){
                pass ++;
            }
        }

        return pass;
    }

    public long getCreateTime() {
        Date date = getDate(DBConstants.F_CREATE_DATE);
        if (date == null){
            return 0;
        }else{
            return date.getTime();
        }
    }

    public int getStartTime() {
        return 0;  //TODO
    }

    public int getEndTime() {
        return 0;  //TODO
    }

    public double calculateScore(int guessMode, int rankType){
        if (guessMode == OpusProtos.PBUserGuessMode.GUESS_MODE_CONTEST_VALUE){
            return calculateAllTimeScore();
        }

        if (rankType == OpusProtos.PBRankType.HOT_RANK_VALUE){
          return calculateHotScore();
        } else if(rankType == OpusProtos.PBRankType.ALL_TIME_RANK_VALUE){
          return calculateAllTimeScore();
        }
        return 0;
    }

    private double calculateAllTimeScore() {
        BasicDBList list = (BasicDBList)getObject(DBConstants.F_ID_LIST);
        if (list == null || list.size() == 0)
            return 0;

        int size = list.size();
        long pass = 0;
        long spendTime = 0;
        for (int i=0; i<size; i++){
            BasicDBObject obj = (BasicDBObject )list.get(i);

            boolean correct = obj.getBoolean(DBConstants.F_CORRECT);
            if (correct){

                pass ++;
                Object singleSpendTimeObj =  obj.get(DBConstants.F_SPEND_TIME);
                if (singleSpendTimeObj != null){
                    long singleSpendTime = ((Long)singleSpendTimeObj).longValue();
                    spendTime += singleSpendTime;
                }
            }
        }

        spendTime /= 1000;


        double score = (pass * UserGuessOpusPlayManager.SPEND_TIME_MULTIPLIER) +
                UserGuessOpusPlayManager.SPEND_TIME_MULTIPLIER - spendTime;
        return score;
    }

    private double calculateHotScore(){

        double p = calculateAllTimeScore();
        return p;

//        double days = getCreateTime() / M_SECOND_PER_DAY;
//        double log = Math.log10(p);
//
//        double hot = log + days;
//        return hot;
    }

    private static double log(double r, double v) {
        if (r < 1 || v < 1) {
            return 0;
        }
        return Math.log10(v) / Math.log10(r);
    }

    public long getGuessTimes() {

        BasicDBList list = (BasicDBList)getObject(DBConstants.F_ID_LIST);
        if (list == null || list.size() == 0)
            return 0;

        int size = list.size();
        long totalGuessTimes = 0;
        for (int i=0; i<size; i++){
            BasicDBObject obj = (BasicDBObject )list.get(i);
            long guessTimes = obj.getLong(DBConstants.F_GUESS_TIMES);
            totalGuessTimes += guessTimes;
        }

        return totalGuessTimes;
    }

    public long getSpendTime() {

        BasicDBList list = (BasicDBList)getObject(DBConstants.F_ID_LIST);
        if (list == null || list.size() == 0)
            return 0;

        int size = list.size();
        long totalSpendTime = 0;
        for (int i=0; i<size; i++){
            BasicDBObject obj = (BasicDBObject )list.get(i);
            boolean correct = obj.getBoolean(DBConstants.F_CORRECT);
            if (correct){
                long spendTimes = obj.getLong(DBConstants.F_SPEND_TIME);
                totalSpendTime += spendTimes;
            }
        }

        return totalSpendTime;
    }

    public boolean isExpired(int mode){
        if (mode == OpusProtos.PBUserGuessMode.GUESS_MODE_GENIUS_VALUE){

            BasicDBList list = getList(DBConstants.F_ID_LIST);
            if (list.size() <= 0){
                return false;
            }

            BasicDBObject dbObject = (BasicDBObject) list.get(0);
            Date createDate = (Date)dbObject.get(DBConstants.F_CREATE_DATE);
            long duration = DateUtil.getCurrentTime() - createDate.getTime();

            if (duration > 12 * 3600 * 1000){
                return true;
            }else {
                return false;
            }
        }
        return false;
    }
}
