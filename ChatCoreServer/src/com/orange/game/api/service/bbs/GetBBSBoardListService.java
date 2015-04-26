package com.orange.game.api.service.bbs;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.bbs.BBSBoard;
import com.orange.game.model.manager.bbs.BBSManager;

public class GetBBSBoardListService extends CommonGameService {

	int deviceType;
	String gameId;
	
	@Override
	public void handleData() {
		List<BBSBoard> boardList = BBSManager.getBBSBoardList(mongoClient,
				userId, appId, gameId);
		log.debug("board list = " + boardList);
		byteData = CommonServiceUtils.bbsBoardListToProto(boardList);
	}

	@Override
	public boolean setDataFromRequest(HttpServletRequest request) {
        if (!setAndCheckUserIdAndAppId(request)){
            return false;
        };
		deviceType = getIntValueFromRequest(request,
                ServiceConstant.PARA_DEVICETYPE, 0);
		gameId = request.getParameter(ServiceConstant.PARA_GAME_ID);
		
		return true;
	}

}
