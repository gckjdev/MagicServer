package com.orange.game.api.service.guessopus;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.ServiceConstant;

import javax.servlet.http.HttpServletRequest;

/**
 * Created with IntelliJ IDEA.
 * User: Linruin
 * Date: 13-7-24
 * Time: 下午4:15
 * To change this template use File | Settings | File Templates.
 */
public class GetGuessContestListInfo extends CommonGameService {

    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {
        return true;
    }

    @Override
    public void handleData() {

        byteData = CommonServiceUtils.guessTodayContestListToPB(xiaoji);
    }
}
