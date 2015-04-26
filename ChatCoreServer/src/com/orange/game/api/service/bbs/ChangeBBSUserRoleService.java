package com.orange.game.api.service.bbs;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.bbs.BBSPrivilege;
import com.orange.game.model.manager.bbs.BBSPrivilegeManager;

public class ChangeBBSUserRoleService extends CommonGameService {

	int deviceType;

	String boardId;
	String targetUid;
	int roleType;
	Date expireDate;

	@Override
	public void handleData() {
		BBSPrivilegeManager.updateUserRole(mongoClient, targetUid, roleType,
				boardId, expireDate);
	}

	
	@Override
	public String toString() {
		return "ChangeBBSUserRoleService [appId=" + appId + ", boardId="
				+ boardId + ", deviceType=" + deviceType + ", expireDate="
				+ expireDate + ", roleType=" + roleType + ", targetUid="
				+ targetUid + ", userId=" + userId + "]";
	}


	@Override
	public boolean setDataFromRequest(HttpServletRequest request) {
        if (!setAndCheckUserIdAndAppId(request)){
            return false;
        }
		deviceType = getIntValueFromRequest(request,
                ServiceConstant.PARA_DEVICETYPE, 0);
		targetUid = request.getParameter(ServiceConstant.PARA_TARGETUSERID);
		if (!check(targetUid, ErrorCode.ERROR_PARAMETER_TARGET_USERID_EMPTY,
				ErrorCode.ERROR_PARAMETER_TARGET_USERID_NULL)) {
			return false;
		}

		boardId = request.getParameter(ServiceConstant.PARA_BOARDID);
		if (!check(boardId, ErrorCode.ERROR_PARAMETER_BOARDID_EMPTY,
				ErrorCode.ERROR_PARAMETER_BOARDID_NULL)) {
			return false;
		}
		roleType = getIntValueFromRequest(request, ServiceConstant.PARA_TYPE,
                BBSPrivilege.BBS_USER_ROLE_NORMAL);

		long dateValue = getIntValueFromRequest(request,
                ServiceConstant.PARA_EXPIRE_DATE, 0);
		if (dateValue > 0) {
			expireDate = new Date(dateValue * 1000);
		}
		if (roleType == BBSPrivilege.BBS_USER_ROLE_FORBIDED && expireDate == null) {
			resultCode = ErrorCode.ERROR_BBS_FORBIDUSER_ERROR;
			return false;
		}
		return true;
	}

}
