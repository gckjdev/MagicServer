package com.orange.game.model.manager.guessopus;

import com.orange.common.utils.DateUtil;
import com.orange.network.game.protocol.model.OpusProtos;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Linruin
 * Date: 13-7-18
 * Time: 下午4:17
 * To change this template use File | Settings | File Templates.
 */
public class UserGuessUtil {

    public static String getTodayContestId(String category){
        return category + "_" + DateUtil.dateToStringByFormat(new Date(), "yyyyMMdd");
    }

    public static String getContestId(String category, Date date){
        return category + "_" + DateUtil.dateToStringByFormat(date, "yyyyMMdd");
    }

    public static int getContestStartTime() {
        return (int)(DateUtil.getDateOfToday(UserGuessConstants.CONTEST_START_HOUR, 0, 0).getTime()/1000);
    }

    public static int getContestEndTime() {
        return (int)(DateUtil.getDateOfToday(UserGuessConstants.CONTEST_END_HOUR, 0, 0).getTime()/1000);
    }

    public static String getUserGuessInfoTableName(String category, int guessMode, String contestId) {

        if (guessMode == OpusProtos.PBUserGuessMode.GUESS_MODE_CONTEST_VALUE){
            return getUserGuessInfoTableName(category, UserGuessConstants.MODE_CONTEST, contestId);
        }else if(guessMode == OpusProtos.PBUserGuessMode.GUESS_MODE_GENIUS_VALUE){
            return getUserGuessInfoTableName(category, UserGuessConstants.MODE_GENIUS, null);
        }else{
            return getUserGuessInfoTableName(category, UserGuessConstants.MODE_HAPPY, null);
        }
    }


    public static String getUserGuessInfoTableName(String category, String modeName, String contestId){

        if (contestId == null || contestId.length() == 0){
            return UserGuessConstants.USER_GUESS_PREFIX + category.toLowerCase() + "_" + modeName.toLowerCase();
        }else{
            return UserGuessConstants.USER_GUESS_PREFIX + modeName.toLowerCase() + "_" + contestId;
        }
    }
}
