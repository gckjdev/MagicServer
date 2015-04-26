package com.orange.game.api.service.opus;

import javax.servlet.http.HttpServletRequest;

import com.orange.common.api.service.CommonService;
import com.orange.common.utils.StringUtil;
import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.Contest;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.manager.ContestManager;
import com.orange.game.model.manager.OpusManager;
import com.orange.game.model.service.DBService;
import com.orange.game.model.service.opus.OpusService;

public class GetDrawOpusByIdService extends CommonGameService {

	String uid;
	String feedId;
	String appId;
	boolean returnDataByUrl = false;
	
	boolean isReturnCompressedData = true;		// TODO set from data request

	@Override
	public void handleData() {

		UserAction userAction = OpusManager.getDrawById(mongoClient, uid, feedId);

        boolean anounymousNick = false;
        if (!isVersionAbove70() && userAction.isContestDraw()){ //
            Contest contest = null;
            String contestId = userAction.getContestId();
            if (!StringUtil.isEmpty(contestId)){
                contest = ContestManager.getContestById(DBService.getInstance().getMongoDBClient(), contestId);
                if (contest == null){
                    log.info("<GetDrawOpusByIdService> but contest not found for "+contestId);
                    resultCode = ErrorCode.ERROR_CONTEST_NOT_FOUND;
                    return;
                }
            }

            if (contest != null && contest.canVote() && contest.getIsAnonymous()){
                log.info("<GetDrawOpusByIdService> old version, return anounymous user nick");
                anounymousNick = true;
            }
        }

		if (userAction != null) {
            if (userAction.isDeleted()){
                log.info("<GetDrawOpusByIdService> but action "+userAction.getActionId()+" "+userAction.getWord()+" is deleted");
                byteData = CommonServiceUtils.protocolBufferErrorNoData(ErrorCode.ERROR_USER_ACTION_INVALID);
            }
            else{
			    byteData = CommonServiceUtils.userDrawActionToProtocolBuffer(mongoClient, userAction, returnDataByUrl, isReturnCompressedData, anounymousNick);
            }

            OpusService.getInstance().updateOpusMissingInfo(userAction);

		}

		if (byteData == null) {
			log.warn("<warning> GetDrawOpusByIdService, but no byte data for return???");
			byteData = CommonServiceUtils.protocolBufferNoData();
		}
		else{
			log.info("<GetDrawOpusByIdService> return data, data len="+byteData.length);
		}

	}


	@Override
	public boolean setDataFromRequest(HttpServletRequest request) {
		appId = request.getParameter(ServiceConstant.PARA_APPID);
		uid = request.getParameter(ServiceConstant.PARA_USERID);
		feedId = request.getParameter(ServiceConstant.PARA_FEED_ID);

		String returnDataMethodString = request.getParameter(ServiceConstant.PARA_RETURN_DATA_METHOD);
		if (!StringUtil.isEmpty(returnDataMethodString)){
			returnDataByUrl = (Integer.parseInt(returnDataMethodString)!=0);
		}
		
		String isDataCompressedString = request.getParameter(ServiceConstant.PARA_RETURN_COMPRESSED_DATA);
		if (!StringUtil.isEmpty(isDataCompressedString)){
			isReturnCompressedData = (Integer.parseInt(isDataCompressedString) != 0);
		}

		if (!check(uid, ErrorCode.ERROR_PARAMETER_USERID_EMPTY,
				ErrorCode.ERROR_PARAMETER_USERID_NULL))
			return false;
		if (!check(feedId, ErrorCode.ERROR_PARAMETER_OPUSID_EMPTY,
				ErrorCode.ERROR_PARAMETER_OPUSID_NULL))
			return false;
		if (!check(appId, ErrorCode.ERROR_PARAMETER_APPID_EMPTY,
				ErrorCode.ERROR_PARAMETER_APPID_NULL))
			return false;

		return true;
	}

}
