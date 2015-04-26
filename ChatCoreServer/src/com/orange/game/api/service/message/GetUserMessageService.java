package com.orange.game.api.service.message;

import java.util.List;

import javax.servlet.http.HttpServletRequest;


import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.Message;
import com.orange.game.model.dao.MessageStat;
import com.orange.game.model.manager.MessageManager;

@Deprecated
// use GetMessageStatListService and GetMessageListService replace it. use below v5.0
public class GetUserMessageService extends CommonGameService {

	String userId;
	String friendUserId; // optional
	int offset;
	int limit;

	@Override
	public String toString() {
		return "GetUserMessageService [friendUserId=" + friendUserId
				+ ", limit=" + limit + ", offset=" + offset + ", userId="
				+ userId + "]";
	}

	@Override
	public void handleData() {
		if (friendUserId == null || friendUserId.isEmpty()) {
			List<MessageStat> messageStatList = MessageManager
					.getMessageStatList(mongoClient, userId, null, offset,
							limit);
			byteData = CommonServiceUtils
					.userMessageStatListToProtocolBuffer(userId,messageStatList);
		} else {
			List<Message> messageList = MessageManager.getUserMessage(
					mongoClient, userId, friendUserId, offset, limit);
			byteData = CommonServiceUtils
					.userMessageListToProtocolBuffer(messageList, false);
		}

	}

	@Override
	public boolean setDataFromRequest(HttpServletRequest request) {
		userId = request.getParameter(ServiceConstant.PARA_USERID);
		friendUserId = request.getParameter(ServiceConstant.PARA_TO_USERID);
		String startOffsetString = request
				.getParameter(ServiceConstant.PARA_MESSAGETEXT);
		String maxCountString = request
				.getParameter(ServiceConstant.PARA_MAX_COUNT);

		if (!check(userId, ErrorCode.ERROR_PARAMETER_USERID_EMPTY,
				ErrorCode.ERROR_PARAMETER_USERID_NULL))
			return false;

		if (startOffsetString != null && !startOffsetString.isEmpty()) {
			offset = Integer.valueOf(startOffsetString);
		}

		if (maxCountString != null && !maxCountString.isEmpty()) {
			limit = Integer.valueOf(maxCountString);
		}

		return true;
	}

}
