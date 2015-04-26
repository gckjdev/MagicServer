package com.orange.game.api.service.opus;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.constants.DBConstants;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.manager.OpusManager;
import com.orange.game.model.manager.ScoreManager;
import com.orange.game.model.service.DBService;

import javax.servlet.http.HttpServletRequest;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 13-8-2
 * Time: 下午4:56
 * To change this template use File | Settings | File Templates.
 */
public class SetOpusHotScoreService extends CommonGameService {

    String opusId;
    int dataLen = 0;

    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {

        opusId = request.getParameter(ServiceConstant.PARA_OPUS_ID);
        dataLen = getIntValueFromRequest(request, ServiceConstant.PARA_DATA_LEN, 0);

        if (dataLen == 0){
            resultCode = ErrorCode.ERROR_PARAMETER_DATA_LEN_EMPTY;
            return false;
        }

        return true;
    }

    @Override
    public void handleData() {

        UserAction opus = OpusManager.getOpusSimpleInfoById(opusId);
        if (opus == null){
            resultCode = ErrorCode.ERROR_USER_ACTION_INVALID;
            return;
        }

        opus.setDataLength(dataLen);
        ScoreManager.calculateScore(opus);
        OpusManager.updateOpusDataLen(mongoClient, opusId, dataLen);
    }

    @Override
    public String toString() {
        return "SetOpusHotScoreService{" +
                "opusId='" + opusId + '\'' +
                ", dataLen=" + dataLen +
                '}';
    }
}
