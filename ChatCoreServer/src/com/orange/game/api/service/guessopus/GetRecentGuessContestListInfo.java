package com.orange.game.api.service.guessopus;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * Created with IntelliJ IDEA.
 * User: Linruin
 * Date: 13-8-7
 * Time: 下午12:21
 * To change this template use File | Settings | File Templates.
 */
public class GetRecentGuessContestListInfo extends CommonGameService {
    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {
        return true;
    }

    @Override
    public void handleData() {
        byteData = CommonServiceUtils.guessRecentContestListToPB(xiaoji);
    }
}
