package com.orange.game.api.service.contest;

import com.orange.common.utils.StringUtil;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.constants.DBConstants;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.Contest;
import com.orange.game.model.manager.ContestManager;
import com.orange.game.model.service.contest.ContestService;

import javax.servlet.http.HttpServletRequest;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 13-9-9
 * Time: 下午11:47
 * To change this template use File | Settings | File Templates.
 */
public class GenerateContestResultService extends CommonGameService {

    String contestId;

    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {
        contestId = request.getParameter(ServiceConstant.PARA_CONTESTID);

        if (StringUtil.isEmpty(contestId)){
            resultCode = ErrorCode.ERROR_PARAMETER_CONTESTID_EMPTY;
            return false;
        }

        return true;
    }

    @Override
    public void handleData() {

        Contest contest = ContestManager.getContestById(mongoClient, contestId);
        if (contest == null){
            resultCode = ErrorCode.ERROR_CONTEST_NOT_FOUND;
            return;
        }

        ContestService.getInstance().generateContestResult(contest);

    }
}
