package com.orange.game.api.service.message;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.manager.MessageManager;
import com.orange.game.model.manager.message.UserMessageStatisticManager;

public class DeleteMessageService extends CommonGameService {
	String appId;
	String userId;
	String targetUserId;
	List<String> messageIdList;

	@Override
	public void handleData() {

		if (targetUserId != null
				&& !targetUserId.isEmpty() 
				&& messageIdList == null) {
			MessageManager.deleteMessageStat(mongoClient, userId, targetUserId);
		}


        // this is so dirty for interface definition
        // new client version will use DeleteSingleMessage to delete message
		if (messageIdList != null
				&& !messageIdList.isEmpty() 
				&& (targetUserId == null || targetUserId.isEmpty())) {
			MessageManager.deleteMessage(mongoClient, userId, targetUserId, messageIdList);


		}

	}

	
	
	@Override
	public String toString() {
		return "DeleteMessageService [appId=" + appId + ", messageIdList="
				+ messageIdList + ", targetUserId=" + targetUserId
				+ ", userId=" + userId + "]";
	}

	@Override
	public boolean setDataFromRequest(HttpServletRequest request) {
		appId = request.getParameter(ServiceConstant.PARA_APPID);
		userId = request.getParameter(ServiceConstant.PARA_USERID);
		targetUserId = request.getParameter(ServiceConstant.PARA_TARGETUSERID);
		String messageIdListString = request.getParameter(ServiceConstant.PARA_TARGET_MESSAGE_ID);
		
		if (!check(appId, ErrorCode.ERROR_PARAMETER_APPID_EMPTY,
 				ErrorCode.ERROR_PARAMETER_APPID_NULL))
 			return false;
		
		if (!check(userId, ErrorCode.ERROR_PARAMETER_USERID_EMPTY,
 				ErrorCode.ERROR_PARAMETER_USERID_NULL))
 			return false;
		
		if (messageIdListString != null && !messageIdListString.isEmpty()) {
			String[] messageIdStringList = messageIdListString.split("\\"+ServiceConstant.MESSAGEID_SEPERATOR);
			messageIdList = new ArrayList<String>();
			for (int i = 0; i < messageIdStringList.length; i++) {
				if (messageIdStringList[i].length() >0) {
					messageIdList.add(messageIdStringList[i]);
				}
			}
		}
		
		return true;
	}

}
