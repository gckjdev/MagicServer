package com.orange.game.api.service.game;

import javax.servlet.http.HttpServletRequest;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.model.manager.UserGameStatusManager;

public class ClearUserGameStatusService extends CommonGameService {

	@Override
	public void handleData() {
//		UserGameStatusManager.clearAllUserGameStatus(mongoClient);
	}

	@Override
	public boolean setDataFromRequest(HttpServletRequest request) {
		// TODO Auto-generated method stub
		return false;
	}

}
