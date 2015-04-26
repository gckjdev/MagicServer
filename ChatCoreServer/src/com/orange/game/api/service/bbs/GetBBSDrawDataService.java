package com.orange.game.api.service.bbs;

import javax.servlet.http.HttpServletRequest;

import com.orange.common.utils.StringUtil;
import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.manager.bbs.BBSManager;

public class GetBBSDrawDataService extends CommonGameService {
	int deviceType; // required

	String postId;

	String actionId;
    private int mode;

    @Override
	public void handleData() {
		if (!StringUtil.isEmpty(postId)) {
			byteData = BBSManager.getBBSPostDrawData(mongoClient, postId, mode);
		} else if (!StringUtil.isEmpty(actionId)) {
			byteData = BBSManager.getBBSActionDrawData(mongoClient, actionId, mode);
		} else {
			// ERROR
			resultCode = ErrorCode.ERROR_PARAMETER_POSTID_NULL;
			byteData = CommonServiceUtils
					.protocolBufferErrorNoData(ErrorCode.ERROR_PARAMETER_POSTID_NULL);
			return;
		}
		byteData = CommonServiceUtils.bbsDrawDataToProto(byteData);
	}

	@Override
	public boolean setDataFromRequest(HttpServletRequest request) {
        if (!setAndCheckUserIdAndAppId(request)) {
            return false;
        }
        mode = getIntValueFromRequest(request, ServiceConstant.PARA_MODE, BBSManager.MODE_BBS);

        deviceType = getIntValueFromRequest(request,
                ServiceConstant.PARA_DEVICETYPE, 0);


		// post info
		postId = request.getParameter(ServiceConstant.PARA_POSTID);

		actionId = request.getParameter(ServiceConstant.PARA_ACTIONID);

		if (StringUtil.isEmpty(actionId) && StringUtil.isEmpty(postId)) {
			resultCode = ErrorCode.ERROR_GET_BBS_DRAWDATA;
		}
		return true;
	}

}
