package com.orange.game.api.service.bulletin;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.Bulletin;
import com.orange.game.model.manager.BulletinManager;

public class GetBulletinService extends CommonGameService {

	private String appId;
	private String gameId;
	private String userId;
	private String lastBulletinId;
	private int offset;
	private int limit;
	
	@Override
	public void handleData() {
		
		List<Bulletin> bulletins = null;
		bulletins = BulletinManager.getLatestBulletins(mongoClient, appId, gameId, 
				userId, lastBulletinId, offset, limit);
		
		resultData = CommonServiceUtils.bulletinListToJSON(bulletins);
		
	}
	

	@Override
	public boolean setDataFromRequest(HttpServletRequest request) {
		
		appId = request.getParameter(ServiceConstant.PARA_APPID);
		gameId = request.getParameter(ServiceConstant.PARA_GAME_ID);
		userId = request.getParameter(ServiceConstant.PARA_USERID);
		
		if (!check(userId, ErrorCode.ERROR_PARAMETER_USERID_EMPTY,
				ErrorCode.ERROR_PARAMETER_USERID_NULL)) {
			return false;
		}

		lastBulletinId = request.getParameter(ServiceConstant.PARA_LAST_BULLETIN_ID);
		
		offset = getIntValueFromRequest(request, ServiceConstant.PARA_OFFSET, 0);
	   limit = getIntValueFromRequest(request, ServiceConstant.PARA_COUNT, ServiceConstant.CONST_DEFAULT_PAGE_COUNT);

		return true;
	}

}
