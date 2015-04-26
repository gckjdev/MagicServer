package com.orange.game.api.service.bbs;

import javax.servlet.http.HttpServletRequest;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.manager.bbs.BBSManager;

public class PayBBSRewardService extends CommonGameService {
	int deviceType; // required

	String postId;
	String actionId;

	// action user info
	String actionUid;
	String actionNick;
	String actionGender;
	String actionAvatar;
    private int mode;

    @Override
	public void handleData() {
		BBSManager.payRewardAction(mongoClient, postId, userId, actionId,
				actionUid, actionAvatar, actionGender, actionNick, appId, mode);
	}

	@Override
	public boolean setDataFromRequest(HttpServletRequest request) {
        if (!setAndCheckUserIdAndAppId(request)) {
            return false;
        }
        mode = getIntValueFromRequest(request, ServiceConstant.PARA_MODE, BBSManager.MODE_BBS);

		deviceType = getIntValueFromRequest(request,
                ServiceConstant.PARA_DEVICETYPE, 0);

		// post info
		postId = request.getParameter(ServiceConstant.PARA_POSTID);

		// action info
		actionId = request.getParameter(ServiceConstant.PARA_ACTIONID);
		actionUid = request.getParameter(ServiceConstant.PARA_ACTION_UID);
		actionNick = request.getParameter(ServiceConstant.PARA_NICKNAME);
		actionAvatar = request.getParameter(ServiceConstant.PARA_AVATAR);
		actionGender = request.getParameter(ServiceConstant.PARA_GENDER);

		if (!check(postId, ErrorCode.ERROR_PARAMETER_POSTID_EMPTY,
				ErrorCode.ERROR_PARAMETER_POSTID_NULL)) {
			return false;
		}
		if (!check(actionId, ErrorCode.ERROR_PARAMETER_BBSACTIONID_EMPTY,
				ErrorCode.ERROR_PARAMETER_BBSACTIONID_NULL)) {
			return false;
		}

		if (!check(actionUid, ErrorCode.ERROR_PARAMETER_BBSACTIONUID_EMPTY,
				ErrorCode.ERROR_PARAMETER_BBSACTIONUID_NULL)) {
			return false;
		}

		return true;
	}

}
