package com.orange.game.api.service.bbs;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.bbs.BBSPrivilege;
import com.orange.game.model.manager.bbs.BBSPrivilegeManager;

public class GetBBSUserPrivilegeListService extends CommonGameService {


	int deviceType;

	@Override
	public void handleData() {
		List<BBSPrivilege> list = BBSPrivilegeManager.getPrivilegeList(
				mongoClient, userId);
		log.info("<GetBBSPrivilege> count = " + list.size());
		byteData = CommonServiceUtils.bbsPrivilegeListToProto(list);
	}

	
	
	@Override
	public String toString() {
		return "GetBBSUserPrivilegeListService [appId=" + appId
				+ ", deviceType=" + deviceType + ", userId=" + userId + "]";
	}



	@Override
	public boolean setDataFromRequest(HttpServletRequest request) {

        if (!setAndCheckUserIdAndAppId(request)){
            return false;
        }
		deviceType = getIntValueFromRequest(request,
                ServiceConstant.PARA_DEVICETYPE, 0);

		return true;
	}

}
