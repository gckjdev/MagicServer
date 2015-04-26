package com.orange.game.model.manager;

import com.mongodb.BasicDBObject;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.Contest;
import com.orange.game.model.manager.opus.contest.ContestTopOpusManager;
import org.apache.log4j.Logger;

import com.orange.common.utils.StringUtil;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.dao.opus.Opus;
import com.orange.game.model.manager.feed.HotFeedManagerFactory;
import com.orange.game.model.xiaoji.AbstractXiaoji;
import com.orange.game.model.xiaoji.XiaojiFactory;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

public class ScoreManager extends CommonManager {
    private static final double M_SECOND_PER_UINIT = 3600000 * 2;

    private static final double LOG_RADIUS = 1.1;

    private static final double CORRECT_COEFFICIENT = 2.0;
    private static final double SING_CORRECT_COEFFICIENT = 2.0;

    private static final double DATA_LENGTH_COEFFICIENT = 0.0007;
    //    private static final double SING_DATA_LENGTH_COEFFICIENT = 0.00009 * 0.01;
    private static final double SING_QUALITY_SCORE_COEFFICIENT = 0.3; //1;

    private static final double DATA_LENGTH_COEFFICIENT_FOR_HOT = 0.00009; //0.00007;
//    private static final double SING_DATA_LENGTH_COEFFICIENT_FOR_HOT = 0.00009 * 0.01; //0.00007;

    private static final double COMMENT_COEFFICIENT_FOR_HOT = 0.1;
    private static final double SING_COMMENT_COEFFICIENT_FOR_HOT = 0.1;


    private static final double RANK_COEFFICIENT = 3.0;
    private static final double SING_RANK_COEFFICIENT = 3.0;

    public static final double FLOWER_CONTEST_COEFFICIENT = 3.0;
    public static final double JUDGE_CONTEST_COEFFICIENT = FLOWER_CONTEST_COEFFICIENT * 20;

    private static final double CONTEST_DATA_LENGTH_COEFFICIENT = 1.0 / 1000.0;
    private static final double CONTEST_SAVE_COEFFICIENT = 2.0;

    private static final double SAVE_COEFFICIENT = 2.5;
    private static final double SAVE_COEFFICIENT_FOR_HOT = 5.0;
    private static final double SING_SAVE_COEFFICIENT = 2.5;
    private static final double SING_SAVE_COEFFICIENT_FOR_HOT = 5.0;

    private static final double DEFAULT_DATA_LENGTH = 1200;

    private static final double MAX_DATA_LENGTH = 30000;

    // add by Benson 2013-10-03, reduce data len coefficient
    public static double calculateHotDrawScore(UserAction action) {
        int guessTimes = action.getGuessTimes();
        int correctTimes = action.getCorrectTimes();
        int commentTimes = action.getCommentTimes();
        int drawDataLen = action.getDataLength();
        int flowerTimes = action.getFlowerTimes();
        int tomatoTimes = action.getTomatoTimes();
        int saveTimes = action.getSaveTimes();

        drawDataLen /= 2;

        double lenWeight = (drawDataLen != 0) ? (drawDataLen) : DEFAULT_DATA_LENGTH;
        lenWeight *= DATA_LENGTH_COEFFICIENT_FOR_HOT;

        double rank = (flowerTimes - tomatoTimes) * RANK_COEFFICIENT + saveTimes * SAVE_COEFFICIENT_FOR_HOT;

        double p = correctTimes * CORRECT_COEFFICIENT + guessTimes + commentTimes * COMMENT_COEFFICIENT_FOR_HOT + lenWeight + rank;

        log.info("<calculateHotScore> opusId=" + action.getOpusId() + ", nick=" + action.getNickName() + ", len=" + lenWeight + ", flower=" + flowerTimes * RANK_COEFFICIENT +
                ", save=" + saveTimes * SAVE_COEFFICIENT_FOR_HOT + ", correct=" + correctTimes * CORRECT_COEFFICIENT + ", guess=" + guessTimes + ", comment=" + commentTimes * COMMENT_COEFFICIENT_FOR_HOT);

        return p;
    }

    public static double calculateHotSingScore(UserAction action) {
        int guessTimes = action.getGuessTimes();
        int correctTimes = action.getCorrectTimes();
        int commentTimes = action.getCommentTimes();
        int dataLen = action.getDataLength();
        int flowerTimes = action.getFlowerTimes();
        int tomatoTimes = action.getTomatoTimes();
        int saveTimes = action.getSaveTimes();
        int qualityScore = action.getSpendTime();

        double qualityWeight = qualityScore * SING_QUALITY_SCORE_COEFFICIENT;
        double lenWeight = 0; //dataLen * SING_DATA_LENGTH_COEFFICIENT_FOR_HOT;
        double rank = (flowerTimes - tomatoTimes) * SING_RANK_COEFFICIENT + saveTimes * SING_SAVE_COEFFICIENT_FOR_HOT;
        double p = correctTimes * SING_CORRECT_COEFFICIENT + guessTimes + commentTimes * SING_COMMENT_COEFFICIENT_FOR_HOT + lenWeight + rank + qualityWeight;

        log.info("<calculateHotSingScore> opusId=" + action.getOpusId() + ", nick=" + action.getNickName() + ", len=" + lenWeight + ", flower=" + flowerTimes * SING_RANK_COEFFICIENT +
                ", save=" + saveTimes * SING_SAVE_COEFFICIENT_FOR_HOT + ", correct=" + correctTimes * SING_CORRECT_COEFFICIENT + ", guess=" + guessTimes + ", comment=" + commentTimes * SING_COMMENT_COEFFICIENT_FOR_HOT
                + ", quality score=" + qualityScore + ", weight=" + qualityWeight);

        return p;
    }

    // add by Benson 2013-10-03, reduce data len coefficient
    private static double calculateHotScore(UserAction action, AbstractXiaoji xiaoji) {
        return xiaoji.calculateHotScore(action);
    }


//    private static double calculateHistoryScore(UserAction action) {
//        int guessTimes = action.getGuessTimes();
//        int correctTimes = action.getCorrectTimes();
//        int commentTimes = action.getCommentTimes();
//        int drawDataLen = action.getDataLength();
//        int flowerTimes = action.getFlowerTimes();
//        int tomatoTimes = action.getTomatoTimes();
//        int saveTimes = action.getSaveTimes();
//
//
//        // add by Benson 2013-03-21, to reduce half due to new uncompressed method
//        drawDataLen /= 2;
//
//        double lenWeight = (drawDataLen != 0) ? (drawDataLen) : DEFAULT_DATA_LENGTH;
//        lenWeight *= DATA_LENGTH_COEFFICIENT;
//
//        double rank = (flowerTimes - tomatoTimes) * RANK_COEFFICIENT + saveTimes * SAVE_COEFFICIENT;
//
//        double p = correctTimes * CORRECT_COEFFICIENT + guessTimes + commentTimes + lenWeight + rank;
//
//        return p;
//    }

    public static double calculateAndSetHistoryDrawScore(UserAction action) {
        int guessTimes = action.getGuessTimes();
        int correctTimes = action.getCorrectTimes();
        int commentTimes = action.getCommentTimes();
        int drawDataLen = action.getDataLength();
        int flowerTimes = action.getFlowerTimes();
        int tomatoTimes = action.getTomatoTimes();
        int saveTimes = action.getSaveTimes();

        // add by Benson 2013-03-21, to reduce half due to new uncompressed method
        drawDataLen /= 2;

        double lenWeight = (drawDataLen != 0) ? (drawDataLen) : DEFAULT_DATA_LENGTH;
        lenWeight *= DATA_LENGTH_COEFFICIENT;

        double rank = (flowerTimes - tomatoTimes) * RANK_COEFFICIENT + saveTimes * SAVE_COEFFICIENT;

        double p = correctTimes * CORRECT_COEFFICIENT + guessTimes + commentTimes + lenWeight + rank;

        action.setHistoryScore(p);
        return p;
    }

    public static double calculateAndSetHistorySingScore(UserAction action) {
        int guessTimes = action.getGuessTimes();
        int correctTimes = action.getCorrectTimes();
        int commentTimes = action.getCommentTimes();
        int qualityScore = action.getSpendTime();
        int dataLen = action.getDataLength();
        int flowerTimes = action.getFlowerTimes();
        int tomatoTimes = action.getTomatoTimes();
        int saveTimes = action.getSaveTimes();

        double lenWeight = 0; //dataLen * SING_DATA_LENGTH_COEFFICIENT;
        double qualityWeight = qualityScore * SING_QUALITY_SCORE_COEFFICIENT;

        double rank = (flowerTimes - tomatoTimes) * SING_RANK_COEFFICIENT + saveTimes * SING_SAVE_COEFFICIENT;

        double p = correctTimes * CORRECT_COEFFICIENT + guessTimes + commentTimes + lenWeight + rank + qualityWeight;

        log.info("<calculateAndSetHistorySingScore> quality weight = " + qualityWeight);

        action.setHistoryScore(p);
        return p;
    }


    public static double calculateAndSetHistoryScore(UserAction action) {

        AbstractXiaoji xiaoji = XiaojiFactory.getInstance().getXiaoji(action.getCategory());
        if (xiaoji == null) {
            return action.getHistoryScore();
        }

        return calculateAndSetHistoryScore(action, xiaoji);
    }

    public static double calculateAndSetHistoryScore(UserAction action, AbstractXiaoji xiaoji) {

        double score = xiaoji.calculateAndSetHistoryScore(action);
        loger.info("<calculateAndSetHistoryScore> score=" + score);

        // set history score and update index
        action.setHistoryScore(score);
        if (!action.isLearnDraw()){
            xiaoji.allTimeTopOpusManager(action.getLanguage()).updateOpusHistoryTopScore(action.getActionId(), action.getHistoryScore());
        }

        return score;
    }

    final static int MAX_CONTEST_JUDGE_RANK = 5;

    public static double calculateContestScore(UserAction action, Contest contest) {

        if (contest == null) {
            loger.warn("<calculateContestScore> but contest is null");
            return action.getContestScore();
        }

        double contestFlowerWeight = contest.getFlowerRankWeight();
        double contestJudgeWeight = contest.getJudgeRankWeight();


        int flowerTimes = action.getFlowerTimes();
        double flowerRank = flowerTimes * contestFlowerWeight;
        double p = flowerRank;


        // calcuate judege scores
        BasicDBObject judgeRank = action.getOpusRank();
        if (judgeRank != null) {
            String key = getRankTypeField(DBConstants.DEFAULT_RANK_TYPE);
            BasicDBObject ranks = (BasicDBObject) judgeRank.get(key);
            if (ranks != null && ranks.values() != null) {
                Collection<Object> values = ranks.values();
                int sum = 0;
                int count = 0;
                for (Object obj : values) {
                    int value = 0;
                    if (obj instanceof Integer) {
                        value = ((Integer) obj).intValue();
                    } else if (obj instanceof Double) {
                        continue;
                    } else {
                        loger.warn("<calculateContestScore> but value " + obj + " is not integer or double");
                    }

                    if (value > 0) {
                        if (value > MAX_CONTEST_JUDGE_RANK) {
                            value = MAX_CONTEST_JUDGE_RANK;
                        }
                        sum += Math.abs(value);
                        count++;
                    }
                }

//                int avg = (count > 0) ? sum/count : 0;
                double judgeScore = sum * contestJudgeWeight;
                p += judgeScore;
                loger.info("<calculateContestScore>　flower=" + flowerRank + ", judge=" + judgeScore + ", final=" + p);
            }
        } else {
            loger.info("<calculateContestScore>　flower=" + flowerRank + ", final=" + p);
        }

        loger.info("<calculateContestScore> score=" + p);
        return p;
    }

    public static void calculateContestSpecialScore(UserAction action, BasicDBObject returnUpdateValue) {

        // calcuate special scores
        BasicDBObject judgeRank = action.getOpusRank();
        if (judgeRank == null) {
            return;
        }

        Set<String> allKeys = judgeRank.keySet();
        if (allKeys == null) {
            return;
        }

        for (String key : allKeys) {
            Integer rankType = Integer.parseInt(key);
            if (rankType == DBConstants.DEFAULT_RANK_TYPE)
                continue;

            BasicDBObject ranks = (BasicDBObject) judgeRank.get(key);
            if (ranks != null && ranks.values() != null) {
                Collection<Object> values = ranks.values();
                double sum = 0;
                int count = 0;
                for (Object obj : values) {
                    int value = 0;
                    if (obj instanceof Integer) {
                        value = ((Integer) obj).intValue();
                    } else if (obj instanceof Double) {
                        // the average score is stored in double value here so skip Double value
                        continue;
                    } else {
                        loger.warn("<calculateContestSpecialScore> but value is not integer or double");
                    }

                    if (value > 0) {
                        if (value > MAX_CONTEST_JUDGE_RANK) {
                            value = MAX_CONTEST_JUDGE_RANK;
                        }

                        sum += Math.abs(value);
                        count++;
                    }
                }

                double avgScore = (count > 0) ? sum / count : 0;
                loger.info("<calculateContestScore>　avgScore=" + avgScore);

                // put average score into return value
                returnUpdateValue.put(getOpusUserRankScoreField(rankType), avgScore);

                // update top contest model
                ContestTopOpusManager contestSpecialTopOpusManager = XiaojiFactory.getInstance().getDraw().contestSpecialTopOpusManager(action.getContestId(), rankType);
                if (contestSpecialTopOpusManager != null) {
                    contestSpecialTopOpusManager.updateOpusTopScore(action.getActionId(), avgScore);
                }
            }
        }

        return;
    }


    private static double log(double r, double v) {
        if (r < 1 || v < 1) {
            return 0;
        }
        return Math.log10(v) / Math.log10(r);
    }


    public static final Logger loger = Logger.getLogger(ScoreManager.class
            .getName());


    public static double calculateScore(double heavy, Date createDate) {
        return calculateScore(heavy, createDate, LOG_RADIUS);
    }

    public static double calculateScore(double heavy, Date createDate, double logRadius) {
        double minus = createDate.getTime() / M_SECOND_PER_UINIT;
        double log = log(logRadius, heavy);
        double hot = log + minus;
        return hot;
    }

    public static double calculateScore(UserAction action, boolean needUpdateCache) {

        AbstractXiaoji xiaoji = XiaojiFactory.getInstance().getXiaoji(action.getCategory());
        if (xiaoji == null) {
            return action.getHot();
        }

        double p = calculateHotScore(action, xiaoji); //calculateAndSetHistoryScore(action);

        double minus = action.getCreateDate().getTime() / M_SECOND_PER_UINIT;
        double log = log(LOG_RADIUS, p);
        double hot = log + minus;
        action.setHot(hot);

        if (!action.isLearnDraw()){
            xiaoji.topUserManager().updateUserScore(action.getCreateUserId(), action.getActionId(), action.getHistoryScore(), true);
//        if (!action.isContest()) {
            xiaoji.hotTopOpusManager(action.getLanguage()).updateOpusHotTopScore(action.getActionId(), action.getHot());
//        } else {
//            loger.info("<calculateScore> opus " + action.getActionId() + " type = " + action.getType() + " is contest skip insert hot top");
//        }
        }

        return hot;
    }

    public static double calculateScore(UserAction action) {
        return calculateScore(action, true);
    }

    public static double calculateAndSetScore(Opus opus, AbstractXiaoji xiaoji) {

        double p = calculateHotScore(opus, xiaoji); //calculateHistoryScore(opus);

        double minus = opus.getCreateDate().getTime() / M_SECOND_PER_UINIT;
        double log = log(LOG_RADIUS, p);
        double hot = log + minus;
        opus.setHot(hot);
        loger.info("<calculateAndSetScore> score=" + hot);
        return hot;
    }

    public static double calculateAndSetDrawScore(Opus opus) {

        double p = calculateHotDrawScore(opus); //calculateHistoryScore(opus);

        double minus = opus.getCreateDate().getTime() / M_SECOND_PER_UINIT;
        double log = log(LOG_RADIUS, p);
        double hot = log + minus;
        opus.setHot(hot);

        return hot;
    }

    public static String getOpusUserRankField(int rankType, String userId) {
        return DBConstants.F_RANK + "." + String.valueOf(rankType) + "." + userId;
    }

    public static String getOpusUserRankScoreField(int rankType) {
        return DBConstants.F_RANK + "." + String.valueOf(rankType) + "." + DBConstants.F_SCORE;
    }

    public static String getRankTypeField(int rankType) {
        return String.valueOf(rankType);
    }

}
