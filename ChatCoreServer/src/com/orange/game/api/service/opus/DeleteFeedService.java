package com.orange.game.api.service.opus;

import javax.servlet.http.HttpServletRequest;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.manager.OpusManager;
import com.orange.game.model.manager.opus.StageTopOpusManager;
import com.orange.game.model.manager.useropus.UserStageOpusManager;

public class DeleteFeedService extends CommonGameService {


    static final int DELETE_OPUS = 1;
    static final int DELETE_OPUS_ACTION = 2;

	String userId;
	String feedId;
	String appId;
    int type = DELETE_OPUS;

    boolean removeStageTop = true;

	@Override
	public void handleData() {
        if (type == DELETE_OPUS){
            resultCode = OpusManager.deleteOpus(mongoClient, feedId, userId, appId);
            if (ErrorCode.ERROR_USER_ACTION_INVALID == resultCode){
                resultCode = OpusManager.deleteOpusAction(mongoClient, feedId, userId, appId);
            }

            if (removeStageTop){
                UserAction opus = OpusManager.getDrawById(mongoClient, userId, feedId);
                if (!opus.isLearnDraw()){
                    return;
                }

                String tutorialId = opus.getTutorialId();
                String stageId = opus.getStageId();
                StageTopOpusManager manager = new StageTopOpusManager(tutorialId, stageId);
                if (opus != null){
                    String createUserId = opus.getCreateUserId();

                    String bestOpusId = UserStageOpusManager.getInstance().getBestOpusByUserId(createUserId, tutorialId, stageId);
                    if (bestOpusId == null){
                        return;
                    }

                    if (opus.getActionId().equalsIgnoreCase(bestOpusId)){
                        log.info("opus "+feedId+" is the best opus for user, need to remove");
                        manager.deleteIndex(createUserId, true);
                    }
                    else{
                        log.info("opus "+feedId+" is NOT the best opus for user, don't need to remove");
                    }
                }
            }

        }
        else{
            resultCode = OpusManager.deleteOpusAction(mongoClient, feedId, userId, appId);
            if (ErrorCode.ERROR_USER_ACTION_INVALID == resultCode){
                resultCode = OpusManager.deleteOpus(mongoClient, feedId, userId, appId);
            }
        }
	}

	@Override
	public boolean setDataFromRequest(HttpServletRequest request) {
		appId = request.getParameter(ServiceConstant.PARA_APPID);
		userId = request.getParameter(ServiceConstant.PARA_USERID);
		feedId = request.getParameter(ServiceConstant.PARA_FEED_ID);
        type = getIntValueFromRequest(request, ServiceConstant.PARA_TYPE, DELETE_OPUS);
		
		if (!check(userId, ErrorCode.ERROR_PARAMETER_USERID_EMPTY,
				ErrorCode.ERROR_PARAMETER_USERID_NULL))
			return false;
		
		if (!check(feedId, ErrorCode.ERROR_PARAMETER_OPUS_CREATOR_UID_EMPTY,
				ErrorCode.ERROR_PARAMETER_OPUSID_NULL))
			return false;

//        tutorialId = request.getParameter(ServiceConstant.PARA_TUTORIAL_ID);
//        stageId = request.getParameter(ServiceConstant.PARA_STAGE_ID);
        removeStageTop = getBoolValueFromRequest(request, ServiceConstant.PARA_IS_REMOVE_STAGE_TOP, true);
		
		return true;
	}

}
