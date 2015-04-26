package com.orange.game.api.service.room;

import javax.servlet.http.HttpServletRequest;

import com.orange.common.api.service.CommonService;
import com.orange.game.api.service.CommonGameService;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.manager.RoomManager;

public class UpdateRoomService extends CommonGameService {

	String userId;
	String roomId;

	String userNickName;
	String roomPassword;
	String roomName;
	String userAvatar;
	String gender;

	@Override
	public void handleData() {
		boolean flag = RoomManager.updateRoom(mongoClient, userId,
				userNickName, roomId, roomPassword, roomName, userAvatar,
				gender);

		if (!flag) {
			resultCode = ErrorCode.ERROR_ROOM_UPDATE_PERMISSION;
		}

	}

	@Override
	public boolean setDataFromRequest(HttpServletRequest request) {

		userId = request.getParameter(ServiceConstant.PARA_USERID);
		roomId = request.getParameter(ServiceConstant.PARA_ROOM_ID);
		roomPassword = request.getParameter(ServiceConstant.PARA_PASSWORD);
		userNickName = request.getParameter(ServiceConstant.PARA_NICKNAME);
		roomName = request.getParameter(ServiceConstant.PARA_ROOM_NAME);
		userAvatar = request.getParameter(ServiceConstant.PARA_AVATAR);
		gender = request.getParameter(ServiceConstant.PARA_GENDER);
		
		if (!check(userId, ErrorCode.ERROR_PARAMETER_USERID_EMPTY,
				ErrorCode.ERROR_PARAMETER_USERID_NULL))
			return false;

		if (!check(roomId, ErrorCode.ERROR_PARAMETER_ROOMID_EMPTY,
				ErrorCode.ERROR_PARAMETER_ROOMID_NULL))
			return false;
		
		if (!check(roomPassword, ErrorCode.ERROR_PARAMETER_PASSWORD_EMPTY,
				ErrorCode.ERROR_PARAMETER_PASSWORD_NULL))
			return false;
		
		return true;
	}
}
