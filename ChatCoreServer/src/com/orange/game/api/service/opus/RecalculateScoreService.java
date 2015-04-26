package com.orange.game.api.service.opus;

import com.mongodb.BasicDBObject;
import com.orange.common.utils.StringUtil;
import com.orange.game.api.service.CommonGameService;
import com.orange.game.constants.DBConstants;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.Contest;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.manager.ContestManager;
import com.orange.game.model.manager.OpusManager;
import com.orange.game.model.manager.ScoreManager;
import com.orange.game.model.xiaoji.XiaojiFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 13-10-3
 * Time: 下午1:30
 * To change this template use File | Settings | File Templates.
 */
public class RecalculateScoreService extends CommonGameService {

    static final int TYPE_HOT = 1;
    static final int TYPE_CONTEST_HOT = 2;

    int type;
    String contestId;

    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {

        type = getIntValueFromRequest(request, ServiceConstant.PARA_TYPE, TYPE_HOT);
        contestId = request.getParameter(ServiceConstant.PARA_CONTESTID);

        return true;

    }

    @Override
    public void handleData() {

        if (type == TYPE_HOT){
            List<UserAction> opusList = XiaojiFactory.getInstance().getDraw().hotTopOpusManager(DBConstants.C_LANGUAGE_CHINESE).getTopList(0, 1000);
            for (UserAction opus : opusList){
                double score = ScoreManager.calculateScore(opus);
                mongoClient.updateAllByDBObject(DBConstants.T_OPUS, opus.getObjectId(), new BasicDBObject(DBConstants.F_HOT, score));
            }
        }
        else if (type == TYPE_CONTEST_HOT){
            if (StringUtil.isEmpty(contestId)){
                resultCode = ErrorCode.ERROR_CONTEST_NOT_FOUND;
                return;
            }

            Contest contest = ContestManager.getContestByIdWithAllData(mongoClient, contestId);
            if (contest == null){
                resultCode = ErrorCode.ERROR_CONTEST_NOT_FOUND;
                return;
            }

            List<UserAction> opusList = XiaojiFactory.getInstance().getDraw().contestLatestOpusManager(contestId).getList(0, 3000);
            for (UserAction opus : opusList){

                // set score
                double score = ScoreManager.calculateContestScore(opus, contest);
                BasicDBObject updateValue = new BasicDBObject(DBConstants.F_CONTEST_SCORE, score);

                // set special score
                ScoreManager.calculateContestSpecialScore(opus, updateValue);

                // update redis
                XiaojiFactory.getInstance().getDraw().contestTopOpusManager(contestId).updateOpusTopScore(opus.getActionId(), score);

                // update Mongo DB
                mongoClient.updateAllByDBObject(DBConstants.T_OPUS, opus.getObjectId(), updateValue);
            }
        }
    }
}
