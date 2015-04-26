package com.orange.game.api.service.room;

import javax.servlet.http.HttpServletRequest;

import com.orange.common.api.service.CommonService;
import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.Room;
import com.orange.game.model.manager.RoomManager;

public class CreateRoomService extends CommonGameService {
	String userId;
	
	String userNickName;
	String roomPassword;
	String roomName;
	String userAvatar;
	String gender;

	@Override
	public void handleData() {
		// TODO Auto-generated method stub
		Room room = RoomManager.createRoom(mongoClient,userId,userAvatar,
				userNickName,roomPassword,roomName,gender);
		if (room == null) {
			resultCode = ErrorCode.ERROR_DATABASE_SAVE;
		}else{
//			JSONObject object = new JSONObject();
//			object.put(ServiceConstant.PARA_ROOM_ID, roomId);
			resultData = CommonServiceUtils.roomToJSON(room);
		}
		
	}

	@Override
	public String toString() {
		return "CreateRoomService [gender=" + gender + ", roomName=" + roomName
				+ ", roomPassword=" + roomPassword + ", userAvatar="
				+ userAvatar + ", userId=" + userId + ", userNickName="
				+ userNickName + "]";
	}

	@Override
	public boolean setDataFromRequest(HttpServletRequest request) {
		userId = request.getParameter(ServiceConstant.PARA_USERID);
		roomPassword = request.getParameter(ServiceConstant.PARA_PASSWORD);
		userNickName = request.getParameter(ServiceConstant.PARA_NICKNAME);
		roomName = request.getParameter(ServiceConstant.PARA_ROOM_NAME);
		userAvatar = request.getParameter(ServiceConstant.PARA_AVATAR);
		gender = request.getParameter(ServiceConstant.PARA_GENDER);
		
		if (!check(userId, ErrorCode.ERROR_PARAMETER_USERID_EMPTY,
				ErrorCode.ERROR_PARAMETER_USERID_NULL))
			return false;

		if (!check(roomPassword, ErrorCode.ERROR_PARAMETER_PASSWORD_EMPTY,
				ErrorCode.ERROR_PARAMETER_PASSWORD_NULL))
			return false;
		
		return true;

	}

}
