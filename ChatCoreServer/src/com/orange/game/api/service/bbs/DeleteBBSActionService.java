package com.orange.game.api.service.bbs;

import javax.servlet.http.HttpServletRequest;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.manager.bbs.BBSManager;

public class DeleteBBSActionService extends CommonGameService {

	int deviceType; // required

	String actionId;


    int mode;

	@Override
	public void handleData() {
		BBSManager.deleteAction(mongoClient, userId, actionId, mode);
	}

	@Override
	public boolean setDataFromRequest(HttpServletRequest request) {
        if (!setAndCheckUserIdAndAppId(request)){
            return false;
        }
		// required system parameters
		deviceType = getIntValueFromRequest(request,
                ServiceConstant.PARA_DEVICETYPE, 0);

		// action info
		actionId = request.getParameter(ServiceConstant.PARA_ACTIONID);
		
		if (!check(actionId, ErrorCode.ERROR_PARAMETER_BBSACTIONID_EMPTY,
				ErrorCode.ERROR_PARAMETER_BBSACTIONID_NULL)) {
			return false;
		}

        mode = getIntValueFromRequest(request, ServiceConstant.PARA_MODE, BBSManager.MODE_BBS);

        return true;
	}

}
