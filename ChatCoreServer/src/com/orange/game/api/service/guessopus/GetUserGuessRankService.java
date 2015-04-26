package com.orange.game.api.service.guessopus;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.opus.UserGuess;
import com.orange.game.model.manager.guessopus.TopUserGuessManager;
import com.orange.game.model.manager.guessopus.UserGuessOpusPlayManager;

import javax.servlet.http.HttpServletRequest;

/**
 * Created with IntelliJ IDEA.
 * User: Linruin
 * Date: 13-7-17
 * Time: 下午1:27
 * To change this template use File | Settings | File Templates.
 */
public class GetUserGuessRankService extends CommonGameService {

    int type;
    int mode;
    String contestId;

    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {

        type = getIntValueFromRequest(request, ServiceConstant.PARA_TYPE, -1);

        mode = getIntValueFromRequest(request, ServiceConstant.PARA_MODE, -1);
        contestId = request.getParameter(ServiceConstant.PARA_CONTESTID);

        if (mode == -1){
            resultCode = ErrorCode.ERROR_USER_GUESS_MODE;
            return  false;
        }

        return true;
    }

    @Override
    public void handleData() {

        // TODO support contest later

        TopUserGuessManager manager = xiaoji.getTopUserGuessManager(mode, type, contestId);
        UserGuessOpusPlayManager userGuessOpusPlayManager = xiaoji.getUserGuessOpusManager(mode, contestId);
        UserGuess userGuess = userGuessOpusPlayManager.getUserGuessInfoById(userId);

        if (manager == null){
            resultCode = ErrorCode.ERROR_USER_GUESS_MODE;
            byteData = protocolBufferWithErrorCode(resultCode);
            return;
        }

        byteData = CommonServiceUtils.contestRankToPBUserGuessRank(manager, userGuess, userId);
    }
}
