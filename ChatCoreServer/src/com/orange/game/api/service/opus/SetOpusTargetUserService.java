package com.orange.game.api.service.opus;

import com.orange.common.upload.UploadManager;
import com.orange.common.utils.StringUtil;
import com.orange.game.api.service.CommonGameService;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.manager.OpusManager;
import com.orange.game.model.service.DataService;

import javax.servlet.http.HttpServletRequest;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 14-2-15
 * Time: 下午11:28
 * To change this template use File | Settings | File Templates.
 */
public class SetOpusTargetUserService extends CommonGameService {

    String opusId;
    String targetUserId;

    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {

        log.info("request = " + request);

        opusId = request.getParameter(ServiceConstant.PARA_OPUS_ID);

        targetUserId = request.getParameter(ServiceConstant.PARA_TARGETUSERID);

        if (!check(userId, ErrorCode.ERROR_PARAMETER_USERID_EMPTY, ErrorCode.ERROR_PARAMETER_USERID_NULL))
            return false;

        if (!check(opusId, ErrorCode.ERROR_PARAMETER_OPUSID_EMPTY, ErrorCode.ERROR_PARAMETER_OPUSID_NULL))
            return false;

        if (!check(targetUserId, ErrorCode.ERROR_PARAMETER_TARGET_USERID_EMPTY, ErrorCode.ERROR_PARAMETER_TARGET_USERID_NULL))
            return false;

        // add black user check
        if (checkIsBlackByTargetUser(userId, targetUserId)){
            resultCode = ErrorCode.ERROR_USER_IS_BLACK_FRIEND;
            return false;
        }


        return true;
    }

    @Override
    public void handleData() {
        resultCode = OpusManager.changeOpusTargetUser(mongoClient, opusId, targetUserId);
    }

}
