package com.orange.game.api.service.opus;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.constants.DBConstants;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.manager.OpusManager;

public class GetOpustCountService extends CommonGameService {

	String userId;
	String appId;
	String targetUid;

	@Override
	public void handleData() {
		long count = xiaoji.userOpusManager().getTotalCount(targetUid);
		JSONObject object = new JSONObject();
		object.put(DBConstants.F_COUNT, count);
		resultData = object;
	}

	@Override
	public boolean setDataFromRequest(HttpServletRequest request) {
		appId = request.getParameter(ServiceConstant.PARA_APPID);
		if (!check(appId, ErrorCode.ERROR_PARAMETER_APPID_EMPTY,
				ErrorCode.ERROR_PARAMETER_APPID_NULL)) {
			return false;
		}

		userId = request.getParameter(ServiceConstant.PARA_USERID);
		if (!check(userId, ErrorCode.ERROR_PARAMETER_USERID_EMPTY,
				ErrorCode.ERROR_PARAMETER_USERID_NULL)) {
			return false;
		}

		targetUid = request.getParameter(ServiceConstant.PARA_TARGETUSERID);
		if (!check(targetUid, ErrorCode.ERROR_PARAMETER_TARGET_USERID_EMPTY,
				ErrorCode.ERROR_PARAMETER_TARGET_USERID_NULL)) {
			return false;
		}

		return true;
	}

}
