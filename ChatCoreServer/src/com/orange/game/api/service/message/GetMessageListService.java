package com.orange.game.api.service.message;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.Message;
import com.orange.game.model.manager.MessageManager;
import com.orange.game.model.manager.message.UserMessageManager;

public class GetMessageListService extends CommonGameService {

	String userId;
	String friendUserId; // optional

//	Date dateOffset = new Date(); //
	String offsetMessageId;
	int limit; //
	String appId;
	boolean forward;

    boolean isGroup = false;

	@Override
	public void handleData() {

        if (isGroup){
            log.info("<GetMessageListService> group not support for old interface");
            byteData = CommonServiceUtils.protocolBufferErrorNoData(ErrorCode.ERROR_GET_MESSAGE_GROUP_NOT_SUPPORT);
            resultCode = ErrorCode.ERROR_GET_MESSAGE_GROUP_NOT_SUPPORT;
            return;
        }

		List<Message> messageList = MessageManager.getMessageList(mongoClient, userId,
				friendUserId, offsetMessageId, limit, forward);

		byteData = CommonServiceUtils.userMessageListToProtocolBuffer(messageList, false);
	}

	
	
	@Override
	public String toString() {
		return "GetMessageListService [appId=" + appId + ", forward=" + forward
				+ ", friendUserId=" + friendUserId + ", limit=" + limit
				+ ", offsetMessageId=" + offsetMessageId + ", userId=" + userId
				+ "]";
	}



	@Override
	public boolean setDataFromRequest(HttpServletRequest request) {
		userId = request.getParameter(ServiceConstant.PARA_USERID);

		if (!check(userId, ErrorCode.ERROR_PARAMETER_USERID_EMPTY,
				ErrorCode.ERROR_PARAMETER_USERID_NULL))
			return false;

		appId = request.getParameter(ServiceConstant.PARA_APPID);

		if (!check(userId, ErrorCode.ERROR_PARAMETER_APPID_EMPTY,
				ErrorCode.ERROR_PARAMETER_APPID_NULL))
			return false;

		friendUserId = request.getParameter(ServiceConstant.PARA_TO_USERID);
		limit = getIntValueFromRequest(request, ServiceConstant.PARA_COUNT,
                ServiceConstant.CONST_DEFAULT_PAGE_COUNT);

		offsetMessageId = request.getParameter(ServiceConstant.PARA_MESSAGE_ID);
		
		int forwardValue = getIntValueFromRequest(request,
                ServiceConstant.PARA_FORWARD, 0);
		forward = (forwardValue != 0);

        isGroup = getBoolValueFromRequest(request, ServiceConstant.PARA_IS_GROUP, false);

		return true;
	}

}
