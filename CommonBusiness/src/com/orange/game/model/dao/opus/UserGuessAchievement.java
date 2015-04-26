package com.orange.game.model.dao.opus;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.CommonData;
import com.orange.network.game.protocol.model.GameBasicProtos;
import org.bson.types.ObjectId;

/**
 * Created with IntelliJ IDEA.
 * User: Linruin
 * Date: 13-7-26
 * Time: 上午11:13
 * To change this template use File | Settings | File Templates.
 */
public class UserGuessAchievement extends CommonData {

//    public String achievementKey;
//
//    public UserGuessAchievement(String key){
//        achievementKey = key;
//    }

    public UserGuessAchievement(){
        super();
    }

    public int getSpendTime(String achievementKey){

        return getInt(achievementKey, DBConstants.F_SPEND_TIME);
    }

    public ObjectId getUserId(String achievementKey) {

        return getObjectId(DBConstants.F_FOREIGN_USER_ID);
    }

    public int getEarn(String achievementKey) {

        return getInt(achievementKey, DBConstants.F_EARN);
    }


    public int getPass(String achievementKey) {

        return getInt(achievementKey, DBConstants.F_CORRECT_TIMES);
    }

    public int getTotalGuessTimes(String achievementKey) {

        return  getInt(achievementKey, DBConstants.F_GUESS_TIMES);
    }

    public int getInt(String achievementKey, String field) {

        BasicDBObject achievement = (BasicDBObject) getObject(achievementKey);

        if (achievement == null){
            return 0;
        }

        if (achievement.containsField(field)){
            return achievement.getInt(field);
        }
        else{
            return 0;
        }
    }
}
