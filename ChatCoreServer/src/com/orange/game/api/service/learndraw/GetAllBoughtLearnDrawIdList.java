package com.orange.game.api.service.learndraw;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.bson.types.ObjectId;

import com.orange.common.utils.StringUtil;
import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.DBConstants;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.manager.LearnDrawManager;

public class GetAllBoughtLearnDrawIdList extends CommonGameService {
	String userId;
	String appId;
	int sellContentType = DBConstants.C_SELL_CONTENT_TYPE_LEARN_DRAW;
	

	@Override
	public void handleData() {
		List<ObjectId> list = LearnDrawManager.getDrawIdListByUserId(
				mongoClient, userId, sellContentType);
		byteData = CommonServiceUtils.objectIdListToPBData(list);
	}
	
	@Override
	public String toString() {
		return "GetAllBoughtLearnDrawIdList [userId=" + userId + ", appId="
				+ appId + ", sellContentType=" + sellContentType + "]";
	}



	@Override
	public boolean setDataFromRequest(HttpServletRequest request) {
		userId = request.getParameter(ServiceConstant.PARA_USERID);
		String sellContentTypeString = request.getParameter(ServiceConstant.PARA_SELL_CONTENT_TYPE);
		if (!StringUtil.isEmpty(sellContentTypeString)){
			sellContentType = Integer.parseInt(sellContentTypeString);
		}

		if (!check(userId, ErrorCode.ERROR_PARAMETER_USERID_EMPTY,
				ErrorCode.ERROR_PARAMETER_USERID_NULL))
			return false;

		return true;
	}

}
