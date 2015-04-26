package com.orange.game.api.service.opus;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.orange.common.api.service.CommonService;
import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.manager.OpusManager;

public class GetOpusTimesService extends CommonGameService {

	String opusId;
	String appId;
	@Override
	public void handleData() {
		// TODO Auto-generated method stub
		UserAction feed =  OpusManager.getOpusTimes(mongoClient, opusId);
		resultData = CommonServiceUtils.feedTimesToJSON(feed);
	}

	@Override
	public String toString() {
		return "GetOpusTimesService [appId=" + appId + ", opusId=" + opusId + "]";
	}

	@Override
	public boolean setDataFromRequest(HttpServletRequest request) {
		opusId = request.getParameter(ServiceConstant.PARA_OPUS_ID);
		if (!check(opusId, ErrorCode.ERROR_PARAMETER_OPUSID_EMPTY,
				ErrorCode.ERROR_PARAMETER_OPUSID_NULL))
			return false;
		appId = request.getParameter(ServiceConstant.PARA_APPID);
		return true;
	}

}
