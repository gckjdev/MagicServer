package com.orange.barrage.model.user;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.orange.barrage.constant.BarrageConstants;
import com.orange.game.model.common.CommonMongoIdComplexListManager;
import com.orange.game.model.service.DBService;
import com.orange.protocol.message.ErrorProtos;
import com.orange.protocol.message.UserProtos;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.List;

/**
 * Created by pipi on 15/1/20.
 */
public class UserTagManager extends CommonMongoIdComplexListManager<UserTag> {
    private static UserTagManager ourInstance = new UserTagManager();

    public static UserTagManager getInstance() {
        return ourInstance;
    }

    private UserTagManager() {
        super(BarrageConstants.T_USER_TAG, BarrageConstants.T_USER, BarrageConstants.F_TAG_ID, User.class);
    }

    @Override
    protected String indexBeforeDate() {
        return null;
    }

    @Override
    protected List invokeOldGetList(String userId, int offset, int limit) {
        return null;
    }

    @Override
    protected List invokeOldGetListForConstruct(String key) {
        return null;
    }

    public UserProtos.PBUserTagList createNewTag(String userId, UserProtos.PBUserTag pbUserTag){
        UserProtos.PBUserTag.Builder builder = UserProtos.PBUserTag.newBuilder(pbUserTag);

        // set some auto creation data here
//        builder.setRegDate((int)(System.currentTimeMillis()/1000));
//        builder.setVisitDate((int)(System.currentTimeMillis()/1000));

        DBObject retObj = null;

        DBObject obj = UserTag.pbToDBObject(builder.build());

        String keyFieldName = "_id";
        ObjectId keyObjectId = new ObjectId(userId);

        String listFieldName = BarrageConstants.F_TAG_LIST;
        String idListKeyFieldName = BarrageConstants.F_TAG_ID;
        String idListTableName = BarrageConstants.T_USER_TAG;
        String modifyDateField = BarrageConstants.F_MODIFY_DATE;

        String tagId = pbUserTag.getTid();

        // fix empty list
        if (!obj.containsField(BarrageConstants.F_USER_IDS)){
            obj.put(BarrageConstants.F_USER_IDS, new BasicDBList());
        }

        boolean found = false;
        boolean allowDuplicate = false;

        if (!allowDuplicate){
            // find the object firstly
            BasicDBObject query = new BasicDBObject();
            query.put(keyFieldName, keyObjectId);
            query.put(listFieldName+"."+idListKeyFieldName, tagId);

            BasicDBObject returnFields = new BasicDBObject("_id", 1);
            DBObject foundObj = mongoDBClient.findOne(idListTableName, query, returnFields);
            if (foundObj != null){
                log.info("<createNewTag> tag found");
                found = true;
            }
            else{
                log.info("<createNewTag> tag not found");
                found = false;
            }
        }

        if (found){
            DBObject query = new BasicDBObject();
            query.put(keyFieldName, keyObjectId);
            query.put(listFieldName+"."+idListKeyFieldName, tagId);

            DBObject update = new BasicDBObject();

            BasicDBObject updateValue = new BasicDBObject();
            for (String objKey : obj.keySet()){
                updateValue.put(listFieldName+".$."+objKey, obj.get(objKey));
            }
            updateValue.put(modifyDateField, new Date());

            update.put("$set", updateValue);
            log.info("<createNewTag> query="+query.toString()+", udpate="+update.toString());
            retObj = mongoDBClient.findAndModify(idListTableName, query, update);
        }
        else{
            DBObject query = new BasicDBObject();
            query.put(keyFieldName, keyObjectId);
            DBObject update = new BasicDBObject();

            DBObject pushUpdate = new BasicDBObject();
            pushUpdate.put(listFieldName, obj);
            update.put("$push", pushUpdate);

//            DBObject incUpdate = new BasicDBObject();
//            incUpdate.put(totalCountField, 1);

            BasicDBObject updateValue = new BasicDBObject();
            updateValue.put(modifyDateField, new Date());
//            update.put("$inc", incUpdate);
            update.put("$set", updateValue);
            log.info("<createNewTag> query="+query.toString()+", udpate="+update.toString());
            retObj = mongoDBClient.findAndModifyUpsert(idListTableName, query, update);
        }

        return getListFromObject(retObj, false);
    }

    private UserProtos.PBUserTagList getListFromObject(DBObject obj, boolean returnUserDetail){
        if (obj == null){
            // not found user
            return null;
        }

        List<DBObject> list = (List<DBObject>)(obj.get(BarrageConstants.F_TAG_LIST));
        if (list == null){
            // not found list in user
            log.info("<getUserTagList> list not found in ");
            return null;
        }

        UserProtos.PBUserTagList.Builder builder = UserProtos.PBUserTagList.newBuilder();
        for (DBObject object : list){
            UserTag userTag = new UserTag(object);
            UserProtos.PBUserTag pbUserTag = userTag.toProtoBufModel();

            if (returnUserDetail){
                // TODO, add user information
            }

            if (pbUserTag != null){
                builder.addTags(pbUserTag);
            }
        }

        UserProtos.PBUserTagList retList = builder.build();
        log.info("<getUserTagList> list="+retList.toString());
        return retList;

    }

    public UserProtos.PBUserTagList getUserTagList(String userId, boolean returnUserDetail){

        BasicDBObject query = new BasicDBObject("_id", new ObjectId(userId));
        DBObject obj = mongoDBClient.findOne(BarrageConstants.T_USER_TAG, query, null);
        return getListFromObject(obj, returnUserDetail);
    }


    public UserProtos.PBUserTagList deleteTag(String userId, UserProtos.PBUserTag userTag) {

        DBObject query = new BasicDBObject();
        query.put("_id", new ObjectId(userId));

        DBObject update = new BasicDBObject();

        DBObject pullValue = new BasicDBObject();
        pullValue.put(BarrageConstants.F_TAG_ID, userTag.getTid());
        update.put("$pull", new BasicDBObject(BarrageConstants.F_TAG_LIST, pullValue));

        log.info("<deleteTag> query="+query.toString()+", udpate="+update.toString());
        DBObject retObj = mongoDBClient.findAndModify(BarrageConstants.T_USER_TAG, query, update);

        return getListFromObject(retObj, false);
    }
}
