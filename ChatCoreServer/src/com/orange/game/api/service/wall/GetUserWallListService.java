package com.orange.game.api.service.wall;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.wall.UserWall;
import com.orange.game.model.manager.wall.UserWallManager;

public class GetUserWallListService extends CommonGameService {

	String  userId;
	
	@Override
	public boolean setDataFromRequest(HttpServletRequest request) {
		userId = request.getParameter(ServiceConstant.PARA_USERID);

		if (!check(userId, ErrorCode.ERROR_PARAMETER_USERID_EMPTY,
				ErrorCode.ERROR_PARAMETER_USERID_NULL))
			return false;

		return true;	
	}

	@Override
	public void handleData() {
		List<UserWall> userWallList = UserWallManager.getWallListByUser(mongoClient, userId);
		log.info("<GetUserWallListService> total "+userWallList.size()+ " found for user "+userId);
		
		byteData = CommonServiceUtils.userWallListToPB(userWallList);			
		return;		
	}

}
