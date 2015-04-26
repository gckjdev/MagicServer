package com.orange.game.api.service.guessopus;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.manager.guessopus.GuessContestGeneratorManager;

import javax.servlet.http.HttpServletRequest;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 13-9-11
 * Time: 上午9:21
 * To change this template use File | Settings | File Templates.
 */
public class GenerateGuessContestOpusPool extends CommonGameService {

    int limit;
    int type;

    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {

        limit = getIntValueFromRequest(request, ServiceConstant.PARA_COUNT, 1000);
        type = getIntValueFromRequest(request, ServiceConstant.PARA_TYPE, 1);

        return true;
    }

    @Override
    public void handleData() {

        // Generate opus pool

        if (type == 1){
            GuessContestGeneratorManager.getInstance().generateContestOpusPoolForDraw(limit);
        }
        else if (type == 2){
            GuessContestGeneratorManager.getInstance().generateGeniusOpusPoolForDraw(limit);
        }
        else if (type == 3){
            GuessContestGeneratorManager.getInstance().generateHappyOpusPoolForDraw(limit);
        }
        else if (type == 100){
            GuessContestGeneratorManager.getInstance().generateContestOpusPoolForDraw(limit);
            GuessContestGeneratorManager.getInstance().generateGeniusOpusPoolForDraw(limit);
            GuessContestGeneratorManager.getInstance().generateHappyOpusPoolForDraw(limit);
        }


    }
}
