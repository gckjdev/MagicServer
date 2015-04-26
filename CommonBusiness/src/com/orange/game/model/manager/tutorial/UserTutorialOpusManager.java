package com.orange.game.model.manager.tutorial;

import com.mongodb.BasicDBObject;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.manager.CommonManager;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 14-7-21
 * Time: 下午9:41
 * To change this template use File | Settings | File Templates.
 */
public class UserTutorialOpusManager extends CommonManager {
    private static UserTutorialOpusManager ourInstance = new UserTutorialOpusManager();

    public static UserTutorialOpusManager getInstance() {
        return ourInstance;
    }

    private UserTutorialOpusManager() {
    }

    public void createTutorialOpus(String uid, String opusId, String tutorialId, String stageId, int stageIndex, int chapterIndex, String remoteUserTutorialId, String localUserTutorialId, String chapterOpusId, int stageScore) {

        BasicDBObject obj = new BasicDBObject();
        obj.put("_id", opusId);
        obj.put(DBConstants.F_UID, uid);
        obj.put(DBConstants.F_STAGE_ID, stageId);
        obj.put(DBConstants.F_TUTORIAL_ID, tutorialId);
        obj.put(DBConstants.F_STAGE_INDEX, stageIndex);
        obj.put(DBConstants.F_SCORE, stageScore);
        obj.put(DBConstants.F_CHAPTER_INDEX, chapterIndex);
        obj.put(DBConstants.F_CREATE_DATE, new Date());
        obj.put(DBConstants.F_REMOTE_USER_TUTORIAL_ID, remoteUserTutorialId);
        obj.put(DBConstants.F_LOCAL_USER_TUTORIAL_ID, localUserTutorialId);
        obj.put(DBConstants.F_CHAPTER_OPUS_ID, chapterOpusId);

        log.info("<createTutorialOpus> data="+obj.toString());
        mongoClient.insert(DBConstants.T_TUTORIAL_OPUS_INFO, obj);
    }
}
