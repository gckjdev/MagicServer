package com.orange.game.api.service.learndraw;

import javax.servlet.http.HttpServletRequest;

import com.orange.common.utils.StringUtil;
import com.orange.game.api.service.CommonGameService;
import com.orange.game.constants.DBConstants;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.LearnDraw;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.manager.bbs.BBSPrivilegeManager;
import com.orange.game.model.manager.LearnDrawManager;
import com.orange.game.model.manager.OpusManager;
import com.orange.game.model.service.CreateDataFileService;

// Administrator method, add the draw to the on cell pool...

//com.orange.game.api.service.CommonGameService

public class AddLearnDrawService extends CommonGameService {

	String userId;
	String appId;
	String drawId;
	int price;
	int type;
	int sellContentType = DBConstants.C_SELL_CONTENT_TYPE_LEARN_DRAW;

	@Override
	public void handleData() {
		
		
		
		LearnDrawManager.addDraw(mongoClient, drawId, price, type, sellContentType);

		// create compress file if needed
		UserAction userAction = OpusManager.getOpusSimpleInfoById(drawId);
		if (userAction != null && userAction.isLocalDataFileExist(false) == false){
			CreateDataFileService.getInstance().createFileAndUpdateAtBackground(mongoClient, userAction, true);
		}

	}


	@Override
	public String toString() {
		return "AddLearnDrawService [userId=" + userId + ", appId=" + appId
				+ ", drawId=" + drawId + ", price=" + price + ", type=" + type
				+ ", sellContentType=" + sellContentType + "]";
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

		if (!BBSPrivilegeManager.isSuperAdmin(mongoClient, userId)) {
			resultCode = ErrorCode.ERROR_PARAMETER_PERMISSION_NOT_ENOUGH;
			return false;
		}
		
		price = getIntValueFromRequest(request, ServiceConstant.PARA_PRICE, 1);

		type = getIntValueFromRequest(request, ServiceConstant.PARA_TYPE,
                LearnDraw.LearnDrawTypeOther);

		return true;
	}

}
