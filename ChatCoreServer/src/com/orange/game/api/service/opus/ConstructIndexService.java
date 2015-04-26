package com.orange.game.api.service.opus;

import javax.servlet.http.HttpServletRequest;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.constants.ServiceConstant;

public class ConstructIndexService extends CommonGameService{
	
	private static final int CREATE_INDEX_TYPE_USER_OPUS = 0;
	String targetUserId;
	int type;
	
	@Override
	public boolean setDataFromRequest(HttpServletRequest request) {
		
		type = getIntValueFromRequest(request, ServiceConstant.PARA_TYPE, CREATE_INDEX_TYPE_USER_OPUS);
		targetUserId = request.getParameter(ServiceConstant.PARA_TARGETUSERID);
		return true;
	}

	@Override
	public void handleData() {
		// TODO Auto-generated method stub
		switch (type){
		case CREATE_INDEX_TYPE_USER_OPUS:
			xiaoji.userOpusManager().removeIndex(targetUserId, false);
			xiaoji.userOpusManager().constructIndexFromOldData(targetUserId, null, false, true);
			break;
		}
	}

}
