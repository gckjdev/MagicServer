package com.orange.game.api.service.guessopus;

import com.orange.common.utils.DateUtil;
import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.opus.Opus;
import com.orange.game.model.dao.opus.UserGuess;
import com.orange.game.model.manager.guessopus.UserGuessOpusPlayManager;
import com.orange.network.game.protocol.model.OpusProtos;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Linruin
 * Date: 13-7-16
 * Time: 上午9:39
 * To change this template use File | Settings | File Templates.
 */
public class GetUserGuessOpusService extends CommonGameService {

    int mode;
    String contestId;
    int offset;
    int limit;
    boolean isStartNew;

    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {

        mode = getIntValueFromRequest(request, ServiceConstant.PARA_MODE, -1);
        contestId = request.getParameter(ServiceConstant.PARA_CONTESTID);

        offset = getIntValueFromRequest(request, ServiceConstant.PARA_OFFSET, 0);
        limit = getIntValueFromRequest(request, ServiceConstant.PARA_COUNT, 20);
        isStartNew = getBoolValueFromRequest(request, ServiceConstant.PARA_IS_START_NEW, true);

        if (xiaoji == null){
            resultCode = ErrorCode.ERROR_XIAOJI_NULL;
            return false;
        }

        return true;
    }

    @Override
    public void handleData() {

        UserGuessOpusPlayManager userGuessOpusPlayManager = xiaoji.getUserGuessOpusManager(mode, contestId);
        if (userGuessOpusPlayManager == null){
            resultCode = ErrorCode.ERROR_USER_GUESS_MODE;
            byteData = protocolBufferWithErrorCode(resultCode);
            return;
        }

        if (isStartNew && mode != OpusProtos.PBUserGuessMode.GUESS_MODE_CONTEST_VALUE){
            userGuessOpusPlayManager.clearList(userId);
        }

//        UserGuess userGuess = userGuessOpusPlayManager.getUserGuessInfoById(userId);
//        if (userGuess != null && userGuess.isExpired(mode)){
//            userGuessOpusPlayManager.clearList(userId);
//        }

        List<Opus> opusList = userGuessOpusPlayManager.getList(userId, offset, limit);
        byteData = CommonServiceUtils.userGuessOpusListToPB(opusList, xiaoji);

    }

    @Override
    public String toString() {
        return "GetUserGuessOpusService{" +
                "mode=" + mode +
                ", offset=" + offset +
                ", limit=" + limit +
                ", isStartNew=" + isStartNew +
                '}';
    }
}
