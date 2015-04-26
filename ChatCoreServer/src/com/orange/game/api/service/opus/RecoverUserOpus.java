package com.orange.game.api.service.opus;

import javax.servlet.http.HttpServletRequest;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.manager.OpusManager;
import com.orange.game.model.service.DBService;

public class RecoverUserOpus extends CommonGameService {

	String opusId;
	String targetUserId;
	
	@Override
	public boolean setDataFromRequest(HttpServletRequest request) {
		opusId = request.getParameter(ServiceConstant.PARA_OPUS_ID);
//		targetUserId = request.getParameter(ServiceConstant.PARA_TARGETUSERID);
		return true;
	}

	@Override
	public void handleData() {

        UserAction opus = OpusManager.getDrawById(DBService.getInstance().getMongoDBClient(), userId, opusId);
        targetUserId = opus.getCreateUserId();
		OpusManager.recoverUserOpus(targetUserId,opusId,appId);
	}

}
