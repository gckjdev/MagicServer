package com.orange.game.model.manager.tutorial;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.orange.common.utils.StringUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.constants.ErrorCode;
import com.orange.game.model.common.CommonMongoIdListManager;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.dao.tutorial.UserTutorial;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.List;

/**
 * Created by chaoso on 14-7-11.
 */

public class UserTutorialManager extends CommonMongoIdListManager<UserTutorial> {




    private static UserTutorialManager ourInstance = new UserTutorialManager();

    public static UserTutorialManager getInstance() {
        return ourInstance;
    }

    private UserTutorialManager() {
        super(DBConstants.T_USER_TUTORIAL_INDEX, DBConstants.T_USER_TUTORIAL_INFO, UserTutorial.class);
        this.autoFixTotalSize = true;
    }

    public List<UserTutorial> getUserTutorialList(String userId, int offset, int limit){
        return getList(userId, offset, limit, null, null, 0);
    }

    public UserTutorial reportUserTutorialStatus(String userId, String tutorialId, String localUserTutorialId,
                                                 String remoteUserTutorialId, String stageId, int stageIndex,
                                                 String deviceModel, String deviceOs, int deviceType) {

        UserTutorial userTutorial = new UserTutorial();
        userTutorial.setUserId(userId);
        userTutorial.setTutorialId(tutorialId);
        userTutorial.setLocalUserTutorialId(localUserTutorialId);
        userTutorial.setRemoteUserTutorialId(remoteUserTutorialId);

        userTutorial.setStageIndex(stageIndex);
        userTutorial.setStageId(stageId);

        userTutorial.setDeleteStatus(0);

        userTutorial.setDeviceModel(deviceModel);
        userTutorial.setDeviceOs(deviceOs);
        userTutorial.setDeviceType(deviceType);

        userTutorial.setModifyDate(new Date());

        return UserTutorialManager.getInstance().updateOrAddUserTutorial(userTutorial);
    }

    public UserTutorial updateOrAddUserTutorial(UserTutorial userTutorial){

        if (userTutorial == null){
            return null;
        }

        String userId = userTutorial.getUserId();
        String tutorialId = userTutorial.getTutorialId();

        if (StringUtil.isEmpty(tutorialId) || StringUtil.isEmpty(userId)){
            return null;
        }

        boolean isNew = true;
        if (!StringUtil.isEmpty(userTutorial.getRemoteUserTutorialId())){
            BasicDBObject query = new BasicDBObject("_id", new ObjectId(userTutorial.getRemoteUserTutorialId()));
            DBObject foundObj = mongoDBClient.findOne(DBConstants.T_USER_TUTORIAL_INFO, query);
            if (foundObj != null){
                isNew = false;
            }
        }

        if (isNew){
            log.info("<updateOrAddUserTutorial> new user tutorial");
            return addUserTutorial(userTutorial);
        }
        else{
            BasicDBObject query = new BasicDBObject("_id", new ObjectId(userTutorial.getRemoteUserTutorialId()));
            userTutorial.getDbObject().removeField("_id");
            DBObject update = new BasicDBObject();
            update.put("$set", userTutorial.getDbObject());
            log.info("<updateOrAddUserTutorial> modify user tutorial, query="+query.toString()+", update="+update.toString());
            BasicDBObject retObj = (BasicDBObject)mongoDBClient.findAndModify(DBConstants.T_USER_TUTORIAL_INFO, query, update);
            if (retObj != null){
                return new UserTutorial(retObj);
            }
            else{
                return null;
            }
        }
    }

    //用户添加教程
    public UserTutorial addUserTutorial(UserTutorial userTutorial){

        if (userTutorial == null){
            return null;
        }

        String userId = userTutorial.getUserId();
        String tutorialId = userTutorial.getTutorialId();

        // create data in T_USER_TUTORIAL_INFO
        userTutorial.setCreateDate(new Date());
        DBObject userTutorialObj = userTutorial.getDbObject();
        mongoDBClient.insert(DBConstants.T_USER_TUTORIAL_INFO, userTutorialObj);
        log.info("<addUserTutorial> create info="+userTutorialObj.toString());

        String remoteUserTutorialId = userTutorial.getRemoteUserTutorialId();

        // create index in T_USER_TUTORIAL_INDEX
        insertId(userId, remoteUserTutorialId, 1, false, false);
        return userTutorial;
    }

    //更新UserTutorial的资料
    public void updateUserTutorialInfo(String remoteUserTutorialId, BasicDBObject updateValue){

        if (StringUtil.isEmpty(remoteUserTutorialId) ||
                updateValue == null || updateValue.size() == 0){
            log.warn("<updateUserTutorial> but input data null or empty");
            return;
        }

        BasicDBObject query = new BasicDBObject("_id", new ObjectId(remoteUserTutorialId));
        BasicDBObject update = new BasicDBObject();
        update.put("$set", updateValue);

        mongoDBClient.updateAll(DBConstants.T_USER_TUTORIAL_INFO, query, update);
    }

    //用户删除教程
    public int deleteUserTutorial(String userId,String remoteUserTutorialId){

        if (null == remoteUserTutorialId){
            return ErrorCode.ERROR_USER_REMOTEID_NULL;
        }

        // remove from index table
        removeId(userId, remoteUserTutorialId, false);

        // remove from info table
        // 1为被删除 ,0 为正常
        BasicDBObject deleteInfo = new BasicDBObject(DBConstants.F_USER_TUTORIAL_DELETE_STATUS,1);
        deleteInfo.put(DBConstants.F_MODIFY_DATE, new Date());
        updateUserTutorialInfo(remoteUserTutorialId, deleteInfo);
        return ErrorCode.ERROR_SUCCESS;
    }


    @Override
    protected String indexBeforeDate() {
        return null;
    }

    @Override
    protected List<UserTutorial> invokeOldGetList(String userId, int offset, int limit) {
        return null;
    }

    @Override
    protected List<UserTutorial> invokeOldGetListForConstruct(String key) {
        return null;
    }
}
