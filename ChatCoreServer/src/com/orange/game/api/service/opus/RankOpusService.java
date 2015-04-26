package com.orange.game.api.service.opus;

import com.orange.common.utils.StringUtil;
import com.orange.game.api.service.CommonGameService;
import com.orange.game.constants.DBConstants;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.manager.OpusManager;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 13-8-6
 * Time: 上午11:58
 * To change this template use File | Settings | File Templates.
 */
public class RankOpusService extends CommonGameService {

    private static final String SEP = "$";
    String opusId;
    String contestId;

    List<Integer> rankTypes = Collections.emptyList();
    List<Integer> rankValues = Collections.emptyList();

    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {

        opusId = request.getParameter(ServiceConstant.PARA_OPUS_ID);
        contestId = request.getParameter(ServiceConstant.PARA_CONTESTID);

        String rankTypeString = request.getParameter(ServiceConstant.PARA_TYPE);
        String rankValueString = request.getParameter(ServiceConstant.PARA_VALUE);

        if (StringUtil.isEmpty(rankTypeString) || StringUtil.isEmpty(rankValueString)){
            resultCode = ErrorCode.ERROR_PARAMETER_RANK_TYPE_EMPTY;
            return false;
        }

        String rankTypeStringList[] = rankTypeString.split("\\" + SEP);
        String rankValueStringList[] = rankValueString.split("\\" + SEP);

        if (rankTypeStringList == null || rankTypeStringList.length == 0 || rankValueStringList == null || rankValueStringList.length == 0){
            resultCode = ErrorCode.ERROR_PARAMETER_RANK_TYPE_EMPTY;
            return false;
        }

        rankTypes = StringUtil.stringArrayToIntList(rankTypeStringList);
        rankValues = StringUtil.stringArrayToIntList(rankValueStringList);

        if (rankTypes.size() != rankValues.size()){
            resultCode = ErrorCode.ERROR_PARAMETER_RANK_TYPE_VALUE_NOT_MATCH;
            return false;
        }

        if (!checkUserIdAndAppId()){
            return false;
        }

        if (StringUtil.isEmpty(opusId)){
            resultCode = ErrorCode.ERROR_USER_ACTION_INVALID;
            return false;
        }

        return true;
    }

    @Override
    public void handleData() {

        resultCode = OpusManager.rankOpus(userId, opusId, contestId, rankTypes, rankValues);

    }

    @Override
    public String toString() {
        return "RankOpusService{" +
                "opusId='" + opusId + '\'' +
                ", contestId='" + contestId + '\'' +
                ", rankTypes=" + rankTypes +
                ", rankValues=" + rankValues +
                '}';
    }
}
