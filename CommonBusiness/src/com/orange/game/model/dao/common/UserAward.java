package com.orange.game.model.dao.common;

import com.mongodb.BasicDBObject;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.CommonData;
import com.orange.game.model.dao.User;
import com.orange.network.game.protocol.model.GameBasicProtos;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 13-8-10
 * Time: 下午12:13
 * To change this template use File | Settings | File Templates.
 *
 *
 *
 *
 */



public class UserAward extends CommonData {

    public UserAward(BasicDBObject obj) {
        super(obj);
    }

    public UserAward() {
        super();
    }

    public int getAwardType(){
        return getInt(DBConstants.F_TYPE);
    }

    public void setAwardType(int value){
        getDbObject().put(DBConstants.F_TYPE, value);
    }

    public int getAwardName(){
        return getInt(DBConstants.F_NAME);
    }

    public void setAwardName(String value){
        getDbObject().put(DBConstants.F_NAME, value);
    }

    public String getUserId(){
        return getString(DBConstants.F_UID);
    }

    public void setUserId(String value){
        getDbObject().put(DBConstants.F_UID, value);
    }


    public int getRank(){
        return getInt(DBConstants.F_RANK);
    }

    public void setRank(int value){
        getDbObject().put(DBConstants.F_RANK, value);
    }


    public float getScore(){
        return getFloat(DBConstants.F_SCORE);
    }

    public void setScore(double value){
        getDbObject().put(DBConstants.F_SCORE, value);
    }

    public int getCoin(){
        return getInt(DBConstants.F_AWARD_COIN);
    }

    public void setCoin(int value){
        getDbObject().put(DBConstants.F_AWARD_COIN, value);
    }

    public Date getCreateDate(){
        return getDate(DBConstants.F_CREATE_DATE);
    }

    public void setCreateDate(Date value){
        getDbObject().put(DBConstants.F_CREATE_DATE, value);
    }

    public String getContestId(){
        return getString(DBConstants.F_CONTESTID);
    }

    public void setContestId(String value){
        getDbObject().put(DBConstants.F_CONTESTID, value);
    }

    public void setOpusId(String value){
        getDbObject().put(DBConstants.F_OPUS_ID, value);
    }

    public String getOpusId(){
        return getString(DBConstants.F_OPUS_ID);
    }


    public GameBasicProtos.PBUserAward toPBUserAward(User user, IntKeyValue rankType) {

        if (user == null || rankType == null)
            return null;

        GameBasicProtos.PBIntKeyValue pbRankType = rankType.toPBIntKeyValue();
        if (pbRankType == null){
            return null;
        }

        GameBasicProtos.PBGameUser pbUser = user.toPBUser();
        if (pbUser == null){
            return null;
        }

        GameBasicProtos.PBUserAward.Builder builder = GameBasicProtos.PBUserAward.newBuilder();

        builder.setAwardType(pbRankType);
        builder.setUser(pbUser);
        builder.setRank(getRank());
        builder.setScore(getScore());
        builder.setCoins(getCoin());
        builder.setCreateDate(getIntDate(DBConstants.F_CREATE_DATE));
        if (getContestId() != null){
            builder.setContestId(getContestId());
        }
        if (getOpusId() != null){
            builder.setOpusId(getOpusId());
        }

        return builder.build();
    }
}
