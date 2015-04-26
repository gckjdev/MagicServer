package com.orange.game.api.service.room;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.orange.common.api.service.CommonService;
import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.manager.RoomManager;

public class InviteUserService extends CommonGameService {

	String userId;
	String roomId;
	String roomPassword;
	Set<String> userIdList;

	@Override
	public void handleData() {
		
		boolean flag = RoomManager.inviteUsers(mongoClient, roomId, userId,
				roomPassword, userIdList);
		if (!flag) {
			resultCode = ErrorCode.ERROR_INVITE_USER;
		}else{
			
		}
	}

	@Override
	public boolean setDataFromRequest(HttpServletRequest request) {
		userId = request.getParameter(ServiceConstant.PARA_USERID);
		roomId = request.getParameter(ServiceConstant.PARA_ROOM_ID);
		roomPassword = request.getParameter(ServiceConstant.PARA_PASSWORD);
		
		String userList = request.getParameter(ServiceConstant.PARA_USERID_LIST);
		userIdList = CommonServiceUtils.parseTextList(userList);
		if (!check(userId, ErrorCode.ERROR_PARAMETER_USERID_EMPTY,
				ErrorCode.ERROR_PARAMETER_USERID_NULL))
			return false;

		if (!check(roomPassword, ErrorCode.ERROR_PARAMETER_PASSWORD_EMPTY,
				ErrorCode.ERROR_PARAMETER_PASSWORD_NULL))
			return false;
		
		if (!check(roomId, ErrorCode.ERROR_PARAMETER_ROOMID_EMPTY,
				ErrorCode.ERROR_PARAMETER_ROOMID_NULL))
			return false;
		
		return true;

	}

}
