package com.orange.game.api.service.learndraw;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.orange.common.utils.StringUtil;
import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.DBConstants;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.manager.LearnDrawManager;

public class GetBoughtLearnDrawOpusListService extends CommonGameService {
	String userId;
	String appId;
	int offset;
	int limit;
	int sellContentType = DBConstants.C_SELL_CONTENT_TYPE_LEARN_DRAW;
	

	@Override
	public void handleData() {
		List<UserAction> opustList = LearnDrawManager
				.getBoughtOpusListByUserId(mongoClient, userId, sellContentType, offset, limit);
		byteData = CommonServiceUtils.feedListToProtocolBufferImage(opustList,
                0, false);
	}
	
	

	@Override
	public String toString() {
		return "GetBoughtLearnDrawOpusListService [userId=" + userId
				+ ", appId=" + appId + ", offset=" + offset + ", limit="
				+ limit + ", sellContentType=" + sellContentType + "]";
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

		offset = getIntValueFromRequest(request, ServiceConstant.PARA_OFFSET,
                0);
		limit = getIntValueFromRequest(request, ServiceConstant.PARA_LIMIT,
                ServiceConstant.CONST_DEFAULT_PAGE_COUNT);

		return true;
	}

}
