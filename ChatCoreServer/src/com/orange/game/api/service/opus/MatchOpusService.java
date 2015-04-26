package com.orange.game.api.service.opus;

import javax.servlet.http.HttpServletRequest;

import com.orange.common.api.service.CommonParameter;
import com.orange.common.utils.RandomUtil;
import com.orange.common.utils.StringUtil;
import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.manager.OpusManager;

public class MatchOpusService extends CommonGameService {

	String uid;
	String gender;
	int language;
	int actionType;
	boolean returnDataByUrl = false;	
	boolean isReturnCompressedData = true;		// TODO set from data request

	boolean isMatchFromHot(){
		// increase the possibility of guess hot draw opus
		if (RandomUtil.random(100) > 10)
			return true;
		else
			return false;
	}
	
	@Override
	public void handleData() {

            UserAction userAction = null;
//            if (actionType == UserAction.TYPE_DRAW) {
//                if (userAction == null){
//                    userAction = OpusManager.matchDraw(mongoClient, uid, gender, language);
//                }
//            }
//            else{
                userAction = OpusManager.matchOpus(xiaoji, uid);
//            }

			if (userAction != null) {
				byteData = CommonServiceUtils.userDrawActionToProtocolBuffer(mongoClient, userAction, returnDataByUrl, isReturnCompressedData);
			}
		if (byteData == null) {
			log.warn("<warning> matchDrawService, but no byte data for return???");
			byteData = CommonServiceUtils.protocolBufferNoData();
		}

	}

	@Override
	public boolean setDataFromRequest(HttpServletRequest request) {
		uid = request.getParameter(ServiceConstant.PARA_USERID);
		gender = request.getParameter(ServiceConstant.PARA_GENDER);
		language = this
				.getIntValueFromRequest(request,
                        ServiceConstant.PARA_LANGUAGE,
                        CommonParameter.LANGUAGE_CHINESE);
		String type = request.getParameter(ServiceConstant.PARA_TYPE);
		
		String returnDataMethodString = request.getParameter(ServiceConstant.PARA_RETURN_DATA_METHOD);
		if (!StringUtil.isEmpty(returnDataMethodString)){
			returnDataByUrl = (Integer.parseInt(returnDataMethodString)!=0);
		}

		if (!check(type, ErrorCode.ERROR_PARAMETER_ACTIONTYPE_EMPTY,
				ErrorCode.ERROR_PARAMETER_ACTIONTYPE_NULL)) {
			return false;
		}
		
		String isDataCompressedString = request.getParameter(ServiceConstant.PARA_RETURN_COMPRESSED_DATA);
		if (!StringUtil.isEmpty(isDataCompressedString)){
			isReturnCompressedData = (Integer.parseInt(isDataCompressedString) != 0);
		}

		actionType = Integer.valueOf(type);

		if (!check(uid, ErrorCode.ERROR_PARAMETER_USERID_EMPTY,
				ErrorCode.ERROR_PARAMETER_USERID_NULL))
			return false;
		return true;
	}

}
