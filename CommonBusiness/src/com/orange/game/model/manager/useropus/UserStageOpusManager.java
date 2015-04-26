package com.orange.game.model.manager.useropus;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.orange.common.utils.StringUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.common.CommonMongoIdComplexListManager;
import com.orange.game.model.common.CommonMongoIdListManager;
import com.orange.game.model.common.MongoGetIdListUtils;
import com.orange.game.model.dao.CommonData;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.dao.tutorial.UserTutorial;
import com.orange.game.model.manager.opus.OpusUtils;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 14-7-21
 * Time: 下午9:24
 * To change this template use File | Settings | File Templates.
 */
public class UserStageOpusManager  extends CommonMongoIdComplexListManager<UserAction> {
    private static UserStageOpusManager ourInstance = new UserStageOpusManager();

    public static UserStageOpusManager getInstance() {
        return ourInstance;
    }

    public static final String USER_STAGE_OPUS_PREFIX = "user_stage_opus_draw";
    public static final String OPUS_TABLE_NAME = DBConstants.T_OPUS;
    public static final String KEY_FIELD_NAME = DBConstants.F_OPUS_ID;

    private UserStageOpusManager() {
        super(USER_STAGE_OPUS_PREFIX, OPUS_TABLE_NAME, KEY_FIELD_NAME, UserAction.class);
        this.isListIdAllowDuplicate = NOT_ALLOW_DUPLICATE;
        this.autoFixTotalSize = true;
    }

    public List<UserAction> getOpusList(String userId, int offset, int limit){
        List<UserAction> userActions = getList(userId, offset, limit,
                OpusUtils.NORMAL_RETURN_FIELDS,
                DBConstants.F_OPUS_STATUS,
                UserAction.STATUS_DELETE);

        return userActions;
    }

    public void insertUserOpus(String userId, String tutorialId, String stageId, String opusId, int stageIndex, int score){

        BasicDBObject obj = new BasicDBObject();
        obj.put(DBConstants.F_OPUS_ID, new ObjectId(opusId));
        obj.put(DBConstants.F_STAGE_ID, stageId);
        obj.put(DBConstants.F_TUTORIAL_ID, tutorialId);
        obj.put(DBConstants.F_STAGE_INDEX, stageIndex);
        obj.put(DBConstants.F_SCORE, score);
        obj.put(DBConstants.F_CREATE_DATE, new Date());

        this.insertObject(userId, obj, false, false, false);
    }

    public void updateObject(String userId, BasicDBObject updateValue) {
        BasicDBObject query = new BasicDBObject(DBConstants.F_OWNER, new ObjectId(userId));
        BasicDBObject update = new BasicDBObject("$set", updateValue);

        mongoDBClient.updateAll(getIdListTableName(), query, update);
    }


    @Override
    protected String indexBeforeDate() {
        return null;
    }

    @Override
    protected List<UserAction> invokeOldGetList(String userId, int offset, int limit) {
        return null;
    }

    @Override
    protected List<UserAction> invokeOldGetListForConstruct(String key) {
        return null;
    }

    @Override
    protected BasicDBObject returnMongoDBFields(){
        return OpusUtils.createReturnFields();
    }

    @Override
    protected String deleteStatusFieldName(){
        return  DBConstants.F_OPUS_STATUS;
    }

    @Override
    protected int deleteStatusValue(){
        return UserAction.STATUS_DELETE;
    }

    public String getBestOpusByUserId(String userId, String tutorialId, String stageId){
        if (StringUtil.isEmpty(userId) || !ObjectId.isValid(userId)){
            return null;
        }

        BasicDBObject returnFields = new BasicDBObject(tutorialId, 1);
        returnFields.put(this.getKeyFieldName(), 1);

        List<ObjectId> list = new ArrayList<ObjectId>();
        list.add(new ObjectId(userId));

        MongoGetIdListUtils<UserTutorial> idListUtils = new MongoGetIdListUtils<UserTutorial>();
        List<UserTutorial> userStageOpusList = idListUtils.getList(mongoDBClient, this.getIdListTableName(), this.getKeyFieldName(),
                null, 0, list, returnFields, UserTutorial.class);

        // get best opus ID and put it into array
        List<ObjectId> opusIdList = new ArrayList<ObjectId>();
        for (CommonData data : userStageOpusList){
            DBObject obj = data.getDbObject();
            if (obj == null)
                continue;

            DBObject tutorial = (DBObject)obj.get(tutorialId);
            if (tutorial == null)
                continue;

            DBObject stage = (DBObject)tutorial.get(stageId);
            if (stage == null)
                continue;

            String bestOpusId = (String)stage.get(DBConstants.F_BEST_OPUS_ID);
            if (bestOpusId == null || ObjectId.isValid(bestOpusId) == false)
                continue;

            opusIdList.add(new ObjectId(bestOpusId));
        }

        log.info("<getOpusByUserIdList> best opus id list="+opusIdList.toString());
        if (opusIdList.size() == 0){
            return null;
        }

        String bestOpusId = opusIdList.get(0).toString();
        return bestOpusId;

//        MongoGetIdListUtils<UserAction> idListUtils1 = new MongoGetIdListUtils<UserAction>();
//        List<UserAction> finalOpusList = idListUtils1.getList(mongoDBClient,
//                DBConstants.T_OPUS, "_id",
//                DBConstants.F_OPUS_STATUS,
//                UserAction.STATUS_DELETE,
//                opusIdList,
//                OpusUtils.NORMAL_RETURN_FIELDS,
//                UserAction.class);
//
//        return finalOpusList;

    }

    public List<UserAction> getOpusByUserIdList(List<ObjectId> list, String tutorialId, String stageId) {

        if (list == null || list.size() == 0){
            return Collections.emptyList();
        }

        BasicDBObject returnFields = new BasicDBObject(tutorialId, 1);
        returnFields.put(this.getKeyFieldName(), 1);

        MongoGetIdListUtils<UserTutorial> idListUtils = new MongoGetIdListUtils<UserTutorial>();
        List<UserTutorial> userStageOpusList = idListUtils.getList(mongoDBClient, this.getIdListTableName(), this.getKeyFieldName(),
                null, 0, list, returnFields, UserTutorial.class);

        // get best opus ID and put it into array
        List<ObjectId> opusIdList = new ArrayList<ObjectId>();
        for (CommonData data : userStageOpusList){
            DBObject obj = data.getDbObject();
            if (obj == null)
                continue;

            DBObject tutorial = (DBObject)obj.get(tutorialId);
            if (tutorial == null)
                continue;

            DBObject stage = (DBObject)tutorial.get(stageId);
            if (stage == null)
                continue;

            String bestOpusId = (String)stage.get(DBConstants.F_BEST_OPUS_ID);
            if (bestOpusId == null || ObjectId.isValid(bestOpusId) == false)
                continue;

            opusIdList.add(new ObjectId(bestOpusId));
        }

        log.info("<getOpusByUserIdList> best opus id list="+opusIdList.toString());
        if (opusIdList.size() == 0){
            return Collections.emptyList();
        }

        MongoGetIdListUtils<UserAction> idListUtils1 = new MongoGetIdListUtils<UserAction>();
        List<UserAction> finalOpusList = idListUtils1.getList(mongoDBClient,
                DBConstants.T_OPUS, "_id",
                DBConstants.F_OPUS_STATUS,
                UserAction.STATUS_DELETE,
                opusIdList,
                OpusUtils.NORMAL_RETURN_FIELDS,
                UserAction.class);

        return finalOpusList;
    }
}
