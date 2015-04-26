package com.orange.game.model.dao.bbs;

import com.mongodb.DBObject;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.CommonData;
import com.orange.game.model.dao.User;
import org.bson.types.ObjectId;

public class BBSUser extends CommonData {

    public BBSUser(DBObject object) {
        super(object);
    }

    public BBSUser() {
        super();
    }

    public BBSUser(String userId, String nickName, String avatar, String gender, int vip) {
        super();
        setUserId(userId);
        setNickName(nickName);
        setAvatar(avatar);
        setGender(gender);
        setVip(vip);
    }

    public BBSUser(User user) {
        this(user.getUserId(), user.getNickName(), user.getAvatar(), user
                .getGender(), user.getVip());
    }

    public int getVip() {
        return getInt(DBConstants.F_VIP);
    }

    public boolean isVip() {
        return getVip() != 0;
    }

    public void setVip(int vip) {
        put(DBConstants.F_VIP, vip);
    }

    public String getUserId() {
        ObjectId objectId = (ObjectId) getObject(DBConstants.F_UID);
        if (objectId != null) {
            return objectId.toString();
        }
        return null;
    }

    public void setUserId(String userId) {
        if (userId != null) {
            put(DBConstants.F_UID, new ObjectId(userId));
        }

    }

    public String getNickName() {
        return getString(DBConstants.F_NICKNAME);
    }

    public void setNickName(String nickName) {
        put(DBConstants.F_NICKNAME, nickName);
    }

    public String getAvatar() {
        return getString(DBConstants.F_AVATAR);
    }

    public void setAvatar(String avatar) {
        put(DBConstants.F_AVATAR, avatar);
    }

    public String getGender() {
        return getString(DBConstants.F_GENDER);
    }

    public void setGender(String gender) {
        put(DBConstants.F_GENDER, gender);
    }

}
