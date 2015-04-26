package com.orange.game.api.service.room;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.orange.common.api.service.CommonService;
import com.orange.common.utils.StringUtil;
import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.Room;
import com.orange.game.model.manager.RoomManager;


// get the room list relate to the user.
public class FindRoomByUserService extends CommonGameService {

	String userId;
	private int offset;
	private int limit;  
	
	@Override
	public void handleData() {
		List<Room>list = RoomManager.findRoomByUser(mongoClient, userId,offset,limit);
		if(list != null)
		{
			resultData = CommonServiceUtils.roomListToJSON(list);
		}
	}

	@Override
	public boolean setDataFromRequest(HttpServletRequest request) {
		userId = request.getParameter(ServiceConstant.PARA_USERID);
		if (!check(userId, ErrorCode.ERROR_PARAMETER_USERID_EMPTY,
				ErrorCode.ERROR_PARAMETER_USERID_NULL))
			return false;
		
		String start = request.getParameter(ServiceConstant.PARA_OFFSET);
		String count = request.getParameter(ServiceConstant.PARA_COUNT);
		offset = StringUtil.intFromString(start);
		limit = StringUtil.intFromString(count);
		if (limit == 0) {
			limit = ServiceConstant.CONST_DEFAULT_PAGE_COUNT;
		}

		
		return true;
	}

}
