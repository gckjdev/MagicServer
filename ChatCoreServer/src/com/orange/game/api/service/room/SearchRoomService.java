package com.orange.game.api.service.room;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.orange.common.api.service.CommonService;
import com.orange.common.utils.StringUtil;
import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.DBConstants;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.Room;
import com.orange.game.model.manager.RoomManager;

public class SearchRoomService extends CommonGameService {

	// map the user nick and the room name

	String key;
	int offset;
	int limit;

	@Override
	public void handleData() {
		List<Room> list = RoomManager.searchRoomByKeyWord(mongoClient, key,
				offset, limit);
		if (list != null) {
			resultData = CommonServiceUtils.roomListToJSON(list);
		}
	}

	@Override
	public boolean setDataFromRequest(HttpServletRequest request) {
		key = request.getParameter(ServiceConstant.PARA_KEYWORD);

		key = request.getParameter(ServiceConstant.PARA_KEYWORD);
		if (!check(key, ErrorCode.ERROR_PARAMETER_KEYWORD_NULL,
				ErrorCode.ERROR_PARAMETER_KEYWORD_EMPTY)) {
			return false;
		}

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
