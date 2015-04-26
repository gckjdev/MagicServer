package com.orange.game.api.service.guessopus;

import com.orange.common.utils.StringUtil;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.manager.guessopus.UserGuessOpusPlayManager;
import com.orange.game.model.service.opus.OpusGuessService;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Linruin
 * Date: 13-7-17
 * Time: 上午10:20
 * To change this template use File | Settings | File Templates.
 */
public class GuessOpusService extends CommonGameService {

    String opusId;
    Set<String> guessWords;
    int mode;
    String contestId;
    boolean correct;
    Date startDate;
    Date endDate;

    // guess user info
    String gender;
    String avatar;
    String nickName;

    // opus user info
    String opusCreatorUid;


    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {

        if (!checkUserIdAndAppId()){
            return false;
        }

        opusId = request.getParameter(ServiceConstant.PARA_OPUS_ID);
        if (StringUtil.isEmpty(opusId)){
            resultCode = ErrorCode.ERROR_USER_ACTION_INVALID;
            return false;
        }

        String words = request.getParameter(ServiceConstant.PARA_WORD_LIST);
        guessWords = CommonServiceUtils.parseTextList(words);

        mode = getIntValueFromRequest(request, ServiceConstant.PARA_MODE, -1);
        contestId = request.getParameter(ServiceConstant.PARA_CONTESTID);

        if (mode == -1){
            resultCode = ErrorCode.ERROR_USER_GUESS_MODE;
            return  false;
        }

        correct = getBoolValueFromRequest(request, ServiceConstant.PARA_CORRECT, false);
        startDate = getDateValueFromRequest(request, ServiceConstant.PARA_START_DATE, null);
        endDate = getDateValueFromRequest(request, ServiceConstant.PARA_END_DATE, null);
        if (startDate == null || endDate == null){
            resultCode = ErrorCode.ERROR_PARAMETER_DATE_NULL;
            return false;
        }

        nickName = request.getParameter(ServiceConstant.PARA_NICKNAME);
        gender = request.getParameter(ServiceConstant.PARA_GENDER);
        avatar = request.getParameter(ServiceConstant.PARA_AVATAR);
        opusCreatorUid = request.getParameter(ServiceConstant.PARA_OPUS_CREATOR);

        // TODO check whether to send score or not

        if (opusId.equalsIgnoreCase("51e5084a03648b38d2d61de5")){
            log.info("<GuessOpusService> detect test opus Id("+opusId+"), skip");
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

        OpusGuessService.getInstance().guessOpus(xiaoji,
                appId,
                userGuessOpusPlayManager,
                mode,
                contestId,
                userId,
                opusId,
                guessWords,
                correct,
                startDate,
                endDate,
                opusCreatorUid,
                avatar,
                nickName,
                gender);

        resultCode = ErrorCode.ERROR_SUCCESS;
        byteData = protocolBufferWithErrorCode(resultCode);
    }

    @Override
    public String toString() {
        return "GuessOpusService{" +
                "opusId='" + opusId + '\'' +
                ", guessWords=" + guessWords +
                ", mode=" + mode +
                ", contestId='" + contestId + '\'' +
                ", correct=" + correct +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }
}
