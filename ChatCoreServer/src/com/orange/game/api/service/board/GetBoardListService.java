package com.orange.game.api.service.board;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.Board;
import com.orange.game.model.manager.bbs.BoardManager;

public class GetBoardListService extends CommonGameService {

	int deviceType;
	String appId;
	String gameId;

	@Override
	public void handleData() {
		List<Board> boardList = BoardManager.getBoardList(mongoClient, appId, gameId,
				deviceType);
		if (boardList == null || boardList.isEmpty()) {
//			resultCode = ErrorCode.ERROR_BOARD_ERROR;
			log.error("<GetBoardListService>: board list is null.");
		} else {
			resultData = CommonServiceUtils.boardListToJSON(boardList);
			log.info(resultData);
		}
	}

	@Override
	public String toString() {
		return "GetBoardListService [appId=" + appId + ", deviceType="
				+ deviceType + "]";
	}

	@Override
	public boolean setDataFromRequest(HttpServletRequest request) {
		deviceType = getIntValueFromRequest(request,
                ServiceConstant.PARA_DEVICETYPE, 0);
		if (deviceType == 0) {
			resultCode = ErrorCode.ERROR_DEVICE_TYPE_ERROR;
			return false;
		}
		appId = request.getParameter(ServiceConstant.PARA_APPID);
		gameId = request.getParameter(ServiceConstant.PARA_GAME_ID);
		
		if (!check(gameId, ErrorCode.ERROR_PARAMETER_GAMEID_EMPTY,
				ErrorCode.ERROR_PARAMETER_GAMEID_NULL)) {
			return false;
		}
		return true;
	}

}
