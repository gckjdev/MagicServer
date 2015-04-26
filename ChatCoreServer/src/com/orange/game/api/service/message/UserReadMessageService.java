package com.orange.game.api.service.message;

import javax.servlet.http.HttpServletRequest;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.manager.MessageManager;

public class UserReadMessageService extends CommonGameService {
	String userId;
	String friendUserId;
	boolean isGroup = false;

	@Override
	public void handleData() {
		MessageManager.readFriendMessage(mongoClient, userId, friendUserId);
	}

	@Override
	public String toString() {
		return "UserReadMessageService [friendUserId=" + friendUserId
				+ ", userId=" + userId + "]";
	}

	@Override
	public boolean setDataFromRequest(HttpServletRequest request) {
		userId = request.getParameter(ServiceConstant.PARA_USERID);
		friendUserId = request.getParameter(ServiceConstant.PARA_TARGETUSERID);
		
		if (!check(userId, ErrorCode.ERROR_PARAMETER_USERID_EMPTY,
				ErrorCode.ERROR_PARAMETER_USERID_NULL))
			return false;
		if (!check(friendUserId, ErrorCode.ERROR_PARAMETER_TARGET_USERID_EMPTY,
				ErrorCode.ERROR_PARAMETER_TARGET_USERID_NULL))
			return false;

        isGroup = getBoolValueFromRequest(request, ServiceConstant.PARA_IS_GROUP, false);
		return true;
	}

}
