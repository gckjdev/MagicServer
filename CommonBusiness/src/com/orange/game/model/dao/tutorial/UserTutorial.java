package com.orange.game.model.dao.tutorial;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.orange.common.elasticsearch.ESORMable;
import com.orange.common.utils.MapUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.common.ProtoBufCoding;
import com.orange.game.model.dao.CommonData;
import com.orange.network.game.protocol.message.GameMessageProtos;
import com.orange.network.game.protocol.model.TutorialProtos;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by chaoso on 14-7-11.
 */
public class UserTutorial extends CommonData implements ProtoBufCoding<TutorialProtos.PBUserTutorial>, ESORMable, MapUtil.MakeMapable<ObjectId, TutorialProtos.PBUserTutorial> {


    public UserTutorial(DBObject retObj) {
        super(retObj);
    }

    public UserTutorial() {
        super();
    }

    public Map<String, Object> getESORM() {
        return null;
    }

    public String getESIndexType() {
        return null;
    }

    public String getESIndexName() {
        return null;
    }

    public String getID() {
        return null;
    }

    @Override
    public List<String> fieldsForIndex() {
        return null;
    }

    public boolean hasFieldForSearch() {
        return false;
    }

    public boolean canBeIndexed() {
        return false;
    }

    public ObjectId getKey() {
        return null;
    }

    public TutorialProtos.PBUserTutorial getValue() {
        return null;
    }

    public TutorialProtos.PBUserTutorial toProtoBufModel() {
        TutorialProtos.PBUserTutorial.Builder builder = TutorialProtos.PBUserTutorial.newBuilder();

        builder.setUserId(getUserId());
        builder.setRemoteId(getStringObjectId());

        TutorialProtos.PBTutorial.Builder tutorialBuilder = TutorialProtos.PBTutorial.newBuilder();
        tutorialBuilder.setTutorialId(getTutorialId());
        builder.setTutorial(tutorialBuilder.build());

        return builder.build();
    }

    public void addIntoResponse(GameMessageProtos.DataQueryResponse.Builder builder) {

    }

    public String getUserId() {
        return getString(DBConstants.F_UID);
    }

    public String getTutorialId() {
        return getString(DBConstants.F_TUTORIAL_ID);
    }

    public String getRemoteUserTutorialId() {
        return getStringObjectId();
    }

    public void setUserId(String userId) {
        put(DBConstants.F_UID, userId);
    }

    public void setTutorialId(String value) {
        put(DBConstants.F_TUTORIAL_ID, value);
    }

    public void setLocalUserTutorialId(String value) {
        put(DBConstants.F_LOCAL_USER_TUTORIAL_ID, value);
    }

    public void setRemoteUserTutorialId(String value) {
        if (value != null && ObjectId.isValid(value)){
            put("_id", new ObjectId(value));
        }
        else{
            log.warn("<setRemoteUserTutorialId> but it's invalid!!!");
        }
    }

    // 1为被删除 ,0 为正常
    public  void setDeleteStatus(int value) {
        put(DBConstants.F_USER_TUTORIAL_DELETE_STATUS,value);
    }

    public void setDeviceModel(String value) {
        put(DBConstants.F_DEVICEMODEL,value);
    }
    public void setDeviceOs(String value) {
        put(DBConstants.F_DEVICEOS,value);
    }
    public void setDeviceType(int value) {
        put(DBConstants.F_DEVICE_TYPE,value);
    }

    public void setStageIndex(int stageIndex) {
        put(DBConstants.F_STAGE_INDEX, stageIndex);
    }

    public void setStageId(String stageId) {
        put(DBConstants.F_STAGE_ID, stageId);
    }

    public void setModifyDate(Date date) {
        put(DBConstants.F_MODIFY_DATE, date);
    }

    public void setCreateDate(Date date) {
        put(DBConstants.F_CREATE_DATE, date);
    }

    public String getStageId() {
        return getString(DBConstants.F_STAGE_ID);
    }

    public int getStageIndex() {
        return getInt(DBConstants.F_STAGE_INDEX);
    }

    public int getIntCreateDate() {
        return getIntDate(DBConstants.F_CREATE_DATE);
    }

    public int getIntModifyDate() {
        return getIntDate(DBConstants.F_MODIFY_DATE);
    }

}
