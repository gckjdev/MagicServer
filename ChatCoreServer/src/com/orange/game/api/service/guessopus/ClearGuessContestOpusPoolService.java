package com.orange.game.api.service.guessopus;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.manager.guessopus.GuessContestGeneratorManager;

import javax.servlet.http.HttpServletRequest;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 13-9-12
 * Time: 下午10:16
 * To change this template use File | Settings | File Templates.
 */
public class ClearGuessContestOpusPoolService extends CommonGameService {

    int type;


    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {

        type = getIntValueFromRequest(request, ServiceConstant.PARA_TYPE, 1);
        return true;
    }

    @Override
    public void handleData() {
        if (type == 1){
            GuessContestGeneratorManager.getInstance().clearContestOpusPoolForDraw();
        }
        else if (type == 2){
            GuessContestGeneratorManager.getInstance().clearGeniusOpusPoolForDraw();
        }
        else if (type == 3){
            GuessContestGeneratorManager.getInstance().clearHappyOpusPoolForDraw();
        }
        else if (type == 100){
            GuessContestGeneratorManager.getInstance().clearContestOpusPoolForDraw();
            GuessContestGeneratorManager.getInstance().clearGeniusOpusPoolForDraw();
            GuessContestGeneratorManager.getInstance().clearHappyOpusPoolForDraw();
        }
    }
}
