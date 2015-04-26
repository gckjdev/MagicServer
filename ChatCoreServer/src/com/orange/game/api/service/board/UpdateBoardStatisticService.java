package com.orange.game.api.service.board;

import javax.servlet.http.HttpServletRequest;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.manager.bbs.BoardManager;

public class UpdateBoardStatisticService extends CommonGameService {

	String boardId;
	String userId;
	String appId;
	String gameId;
	String source;
	int deviceType;
	@Override
	public void handleData() {
		BoardManager.updateBoardStatistics(mongoClient, boardId, userId, appId,
				gameId, source, deviceType);
		log.info("did updateBoardStatistics");
	}

	@Override
	public boolean setDataFromRequest(HttpServletRequest request) {

		boardId = request.getParameter(ServiceConstant.PARA_BOARDID);
		if (!check(boardId, ErrorCode.ERROR_PARAMETER_BOARDID_EMPTY,
				ErrorCode.ERROR_PARAMETER_BOARDID_NULL)) {
			return false;
		}
		appId = request.getParameter(ServiceConstant.PARA_APPID);
		if (!check(boardId, ErrorCode.ERROR_PARAMETER_APPID_EMPTY,
				ErrorCode.ERROR_PARAMETER_APPID_NULL)) {
			return false;
		}
		gameId = request.getParameter(ServiceConstant.PARA_GAME_ID);
		if (!check(boardId, ErrorCode.ERROR_PARAMETER_GAMEID_EMPTY,
				ErrorCode.ERROR_PARAMETER_GAMEID_NULL)) {
			return false;
		}
		userId = request.getParameter(ServiceConstant.PARA_USERID);
		deviceType = getIntValueFromRequest(request,
                ServiceConstant.PARA_DEVICETYPE, 0);

		source = request.getRemoteAddr();
		return true;
		// return false;
	}

}
