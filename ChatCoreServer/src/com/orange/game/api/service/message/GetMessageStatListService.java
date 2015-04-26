package com.orange.game.api.service.message;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.MessageStat;
import com.orange.game.model.manager.MessageManager;
import com.orange.game.model.manager.message.UserMessageStatisticManager;

public class GetMessageStatListService extends CommonGameService {

	String userId;
	String appId;
	int offset;
	int limit;

	@Override
	public void handleData() {
		List<MessageStat> statList = MessageManager.getMessageStatList(mongoClient,userId,
				appId, offset, limit);

		if (statList != null && !statList.isEmpty()) {
			byteData = CommonServiceUtils.userMessageStatListToProtocolBuffer(userId, 
					statList);			
		}
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

		offset = getIntValueFromRequest(request, ServiceConstant.PARA_OFFSET,
                0);
		limit = getIntValueFromRequest(request, ServiceConstant.PARA_COUNT, 20);

		return true;
	}

}
