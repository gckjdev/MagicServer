package com.orange.barrage.model.user;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.Message;
import com.googlecode.protobuf.format.JsonFormat;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.orange.barrage.constant.BarrageConstants;
import com.orange.common.elasticsearch.ESORMable;
import com.orange.common.utils.MapUtil;
import com.orange.common.utils.RandomUtil;
import com.orange.common.utils.StringUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.common.ProtoBufCoding;
import com.orange.game.model.dao.CommonData;
import com.orange.network.game.protocol.message.GameMessageProtos;
import com.orange.network.game.protocol.model.GameBasicProtos;
import com.orange.network.game.protocol.model.GroupProtos;
import com.orange.protocol.message.BarrageProtos;
import com.orange.protocol.message.UserProtos;
import org.bson.types.ObjectId;

import java.util.*;

/**
 * Created by pipi on 14/12/8.
 */
public class User extends CommonData implements ProtoBufCoding<UserProtos.PBUser>, ESORMable, MapUtil.MakeMapable<ObjectId, User> {

    private static final int RANDOM_PASSWORD_LENGTH = 6;

    public User(DBObject dbObject) {
        super(dbObject);
    }

    public User() {
        super();
    }

    @Override
    public Map<String, Object> getESORM() {

        if (dbObject != null && getUserId() != null){
            Map<String, Object> userBean = new HashMap<String, Object>();

            List<String> fields = fieldsForIndex();
            for (String field : fields){
                userBean.put(field, StringUtil.getEmptyStringWhenNull(getString(field)));
            }

            // Index SNS info : SINA/QQ/WEIXIN NICK
            UserProtos.PBUser pbUser = toProtoBufModel();
            if (pbUser.getSnsUsersCount() > 0){
                for (UserProtos.PBSNSUser snsUser : pbUser.getSnsUsersList()){
                    String nick = StringUtil.getEmptyStringWhenNull(snsUser.getNick());
                    switch (snsUser.getType()){
                        case BarrageConstants.C_SHARE_WEIBO:
                            userBean.put(BarrageConstants.F_SINA_NICK, nick);
                            break;

                        case BarrageConstants.C_SHARE_QQ:
                        case BarrageConstants.C_SHARE_QQ_ZONE:
                            userBean.put(BarrageConstants.F_QQ_NICK, nick);
                            break;

                        case BarrageConstants.C_SHARE_WEIXIN_SESSION:
                        case BarrageConstants.C_SHARE_WEIXIN_TIMELINE:
                            userBean.put(BarrageConstants.F_WEIXIN_NICK, nick);
                            break;

                    }
                }
            }

            return userBean;
        }
        return  null;
    }


    @Override
    public List<String> fieldsForIndex(){
        List<String> list = new ArrayList<String>();

        list.add(BarrageConstants.F_USER_ID);

        list.add(BarrageConstants.F_MOBILE);
        list.add(BarrageConstants.F_EMAIL);
        list.add(BarrageConstants.F_NICK);
        list.add(BarrageConstants.F_SIGNATURE);

        list.add(BarrageConstants.F_SINA_NICK);
        list.add(BarrageConstants.F_QQ_NICK);
        list.add(BarrageConstants.F_WEIXIN_NICK);

        list.add(BarrageConstants.F_SINA_ID);
        list.add(BarrageConstants.F_QQ_OPEN_ID);
        list.add(BarrageConstants.F_WEIXIN_ID);

        list.add(BarrageConstants.F_LOCATION);

        return list;
    }

    @Override
    public String getESIndexType() {
        return BarrageConstants.ES_INDEX_TYPE_USER;
    }

    @Override
    public String getID() {
        return getStringObjectId();
    }


    @Override
    public String getESIndexName() {
        return BarrageConstants.ES_INDEX_NAME_BARRAGE;
    }

    @Override
    public boolean canBeIndexed()
    {
        return true;
    }

    @Override
    public boolean hasFieldForSearch() {
        return hasFieldForSearch(dbObject);
    }

    public boolean hasFieldForSearch(DBObject dbObject) {

        if (dbObject == null) return false;

        List<String> fields = fieldsForIndex();
        for (String field : fields){
            if (dbObject.containsField(field)){
                return true;
            }
        }

        // check SNS fields
        BasicDBObject obj = (BasicDBObject)dbObject.get(BarrageConstants.F_SNS_USERS);
        if (obj.size() > 0){
            return true;
        }

        return false;
    }

    public ObjectId getKey() {
        return getObjectId();
    }

    public User getValue() {
        return this;
    }

    public UserProtos.PBUser toProtoBufModel() {
        UserProtos.PBUser.Builder builder = UserProtos.PBUser.newBuilder();
        return toPB(builder, null);
    }


    public void addIntoResponse(GameMessageProtos.DataQueryResponse.Builder builder) {

    }

    public Class getPBClass(){
        return UserProtos.PBUser.class;
    }

    public String getUserId() {
        return getStringObjectId();
    }

    public BasicDBObject toFriendDBObject(DBObject info) {
        BasicDBObject obj = new BasicDBObject();
        obj.put(BarrageConstants.F_FRIEND_ID, getObjectId());
        obj.putAll(info);
        return obj;
    }

    public BasicDBObject toFriendRequestDBObject(DBObject info, int direction) {
        BasicDBObject obj = new BasicDBObject();
        obj.put(BarrageConstants.F_FRIEND_ID, getObjectId());
        obj.put(BarrageConstants.F_DIRECTION, direction);
        obj.putAll(info);
        return obj;
    }

    public static BasicDBObject getPublicReturnFields() {
        BasicDBObject obj = new BasicDBObject();
        obj.put(BarrageConstants.F_NICK, 1);
        obj.put(BarrageConstants.F_USER_ID, 1);
        obj.put(BarrageConstants.F_LOCATION, 1);
        obj.put(BarrageConstants.F_AVATAR, 1);
        obj.put(BarrageConstants.F_SIGNATURE, 1);
        obj.put(BarrageConstants.F_AVATAR_BG, 1);
        obj.put(BarrageConstants.F_GENDER, 1);
        obj.put(BarrageConstants.F_STATUS_MODIFY_DATE, 1);
        return obj;
    }

    public static BasicDBObject getMinReturnFields() {
        BasicDBObject obj = new BasicDBObject();
        obj.put(BarrageConstants.F_NICK, 1);
        obj.put(BarrageConstants.F_USER_ID, 1);
        obj.put(BarrageConstants.F_AVATAR, 1);
        obj.put(BarrageConstants.F_SIGNATURE, 1);
        obj.put(BarrageConstants.F_GENDER, 1);
        obj.put(BarrageConstants.F_STATUS_MODIFY_DATE, 1);
        return obj;
    }

    public boolean checkPassword(String passwordToCompare) {

        String password = getPassword();
        if (StringUtil.isEmpty(password)){
            return false;
        }
        return password.equalsIgnoreCase(passwordToCompare);
    }

    private String getPassword() {
        return getString(BarrageConstants.F_PASSWORD);
    }

    public String createPassword() {

        return RandomUtil.randomNumberString(RANDOM_PASSWORD_LENGTH);
    }

    static final String PASSWORD_KEY = "PASSWORD_KEY_DRAW_DSAQC";     // must align with client settings

    public String encryptPassword(String password) {
        if (password == null)
            return null;

        return StringUtil.md5base64encode(password + PASSWORD_KEY);
    }

    public int getAddStatus() {
        return getInt(BarrageConstants.F_ADD_STATUS);
    }
}
