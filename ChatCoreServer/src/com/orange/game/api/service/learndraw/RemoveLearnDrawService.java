package com.orange.game.api.service.learndraw;

import javax.servlet.http.HttpServletRequest;

import com.orange.common.utils.StringUtil;
import com.orange.game.api.service.CommonGameService;
import com.orange.game.constants.DBConstants;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.manager.LearnDrawManager;

public class RemoveLearnDrawService extends CommonGameService {
	String userId;
	String appId;
	String drawId;
	int sellContentType = DBConstants.C_SELL_CONTENT_TYPE_LEARN_DRAW;
	

	@Override
	public String toString() {
		return "RemoveLearnDrawService [userId=" + userId + ", appId=" + appId
				+ ", drawId=" + drawId + ", sellContentType=" + sellContentType
				+ "]";
	}

	@Override
	public void handleData() {
		LearnDrawManager.removeDraw(mongoClient, drawId, sellContentType);
	}

	@Override
	public boolean setDataFromRequest(HttpServletRequest request) {
		userId = request.getParameter(ServiceConstant.PARA_USERID);
		drawId = request.getParameter(ServiceConstant.PARA_OPUS_ID);
		String sellContentTypeString = request.getParameter(ServiceConstant.PARA_SELL_CONTENT_TYPE);
		if (!StringUtil.isEmpty(sellContentTypeString)){
			sellContentType = Integer.parseInt(sellContentTypeString);
		}
		

		if (!check(drawId, ErrorCode.ERROR_PARAMETER_OPUSID_EMPTY,
				ErrorCode.ERROR_PARAMETER_OPUSID_NULL))
			return false;

		if (!check(userId, ErrorCode.ERROR_PARAMETER_USERID_EMPTY,
				ErrorCode.ERROR_PARAMETER_USERID_NULL))
			return false;

		return true;
	}

}
