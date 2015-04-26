package com.orange.game.api.service.room;



import javax.servlet.http.HttpServletRequest;

import com.orange.common.api.service.CommonService;
import com.orange.game.api.service.CommonGameService;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.manager.RoomManager;

public class NewUserJoinRoomService extends CommonGameService {

	String userId;
	String userNickName;
	String userAvatar;
	String roomId;
	String roomPassword;
	String gender;
	
	@Override
	public void handleData() {
		boolean flag = RoomManager.isPassWordCorrect(mongoClient, roomId, roomPassword);
		if (!flag) {
			resultCode = ErrorCode.ERROR_PASSWORD_NOT_MATCH;
		}else{
			
			//user should change the status into joined, when the use quit the game.
			RoomManager.addRoomUser(mongoClient, roomId, userId, gender, userNickName, userAvatar);
		}
	}

	@Override
	public boolean setDataFromRequest(HttpServletRequest request) {
		userId = request.getParameter(ServiceConstant.PARA_USERID);
		roomId = request.getParameter(ServiceConstant.PARA_ROOM_ID);
		roomPassword = request.getParameter(ServiceConstant.PARA_PASSWORD);
		userNickName = request.getParameter(ServiceConstant.PARA_NICKNAME);
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
