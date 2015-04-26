package com.orange.barrage.model.user;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.orange.barrage.common.CommonModelManager;
import com.orange.barrage.constant.BarrageConstants;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.common.utils.DateUtil;
import com.orange.common.utils.StringUtil;
import com.orange.game.api.service.ElasticsearchService;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.common.MongoGetIdListUtils;
import com.orange.game.model.manager.RelationManager;
import com.orange.protocol.message.MessageProtos;
import com.orange.protocol.message.UserProtos;
import org.bson.types.ObjectId;

import java.util.*;

/**
 * Created by pipi on 14/12/2.
 */
public class UserManager extends CommonModelManager<User> {
    private static UserManager ourInstance = new UserManager();

    public static UserManager getInstance() {
        return ourInstance;
    }

    private UserManager() {
    }


    public static UserProtos.PBUser findUser(String fieldName, String value) {
        DBCursor cursor = mongoDBClient.find(BarrageConstants.T_USER, fieldName, value);
        return null;
    }

    public User findUserById(String friendId) {
        return findObjectById(friendId);
    }

    public User findUserByEmail(String email) {
        return findObjectByField(BarrageConstants.F_EMAIL, email);
    }

    public User findUserByMobile(String mobile) {
        return findObjectByField(BarrageConstants.F_MOBILE, mobile);
    }

    public User findUserBySnsId(String snsFieldName, String snsId) {
        return findObjectByField(snsFieldName, snsId);
    }

    public User findUserByQQ(String qqOpenId) {
        return findObjectByField(BarrageConstants.F_QQ_OPEN_ID, qqOpenId);
    }

    public User findUserBySina(String sinaId) {
        return findObjectByField(BarrageConstants.F_SINA_ID, sinaId);
    }

    public User findUserByWeixin(String weixinId) {
        return findObjectByField(BarrageConstants.F_WEIXIN_ID, weixinId);
    }


    @Override
    public String getTableName() {
        return BarrageConstants.T_USER;
    }

    @Override
    public Class getClazz() {
        return User.class;
    }

    public int updateUser(String userId, UserProtos.PBUser pbUser, MessageProtos.PBUpdateUserInfoResponse.Builder builder) {

        BasicDBObject query = new BasicDBObject();
        query.put("_id", new ObjectId(userId));

        DBObject updateObj = User.pbToDBObject(pbUser);
        updateObj.removeField(BarrageConstants.F_USER_ID);

        // TODO maybe need to be more accurate, compare to data in DB
        if (pbUser.getSignature() != null || pbUser.getNick() != null || pbUser.getAvatar() != null ){
            updateObj.put(BarrageConstants.F_STATUS_MODIFY_DATE, DateUtil.getCurrentSeconds());
        }

        BasicDBObject update = new BasicDBObject();
        update.put("$set", updateObj);
        if (updateObj.keySet().size() == 0){
            log.info("<updateUser> but no data to update");
            return 0;
        }

        log.info("<updateUser> query="+query.toString()+",update="+update.toString());
        DBObject obj = mongoDBClient.findAndModify(BarrageConstants.T_USER, query, update);
        if (obj != null){
            User user = new User(obj);
            UserProtos.PBUser retPBUser = user.toProtoBufModel();
            builder.setUser(retPBUser);

            // update elastic search user index
            ElasticsearchService.addOrUpdateIndex(user, mongoDBClient);
        }

        return 0;
    }

    public User createNewUser(UserProtos.PBUser pbUser) {

        UserProtos.PBUser.Builder userBuilder = UserProtos.PBUser.newBuilder(pbUser);

        // set some auto creation data here
        userBuilder.setRegDate((int)(System.currentTimeMillis()/1000));
        userBuilder.setVisitDate((int)(System.currentTimeMillis()/1000));

        DBObject obj = User.pbToDBObject(userBuilder.build(), true, BarrageConstants.F_USER_ID);

        log.info("create user = "+obj.toString());
        mongoDBClient.insert(BarrageConstants.T_USER, obj);

        User retUser = new User(obj);

        // index use in Elastic Search
        ElasticsearchService.addOrUpdateIndex(retUser, mongoDBClient);
        return retUser;
    }

    public static List<User> findPublicUserInfoByUserIdList(String userId,
                                                            List<ObjectId> targetUserIdList,
                                                            boolean isReturnRelation) {

        BasicDBObject returnFields = User.getPublicReturnFields();
        MongoGetIdListUtils<User> utils = new MongoGetIdListUtils<User>();
        return utils.getList(mongoDBClient,
                BarrageConstants.T_USER,
                BarrageConstants.F_ID,
                null,
                0,
                targetUserIdList,
                returnFields,
                User.class);
    }
}
