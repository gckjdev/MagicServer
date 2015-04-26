package com.orange.game.api.service.webadmin;

import com.orange.game.api.service.CommonGameService;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by chaoso on 14-6-25.
 */
public class WebAdminLoginService extends CommonGameService {
    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {
        return false;
    }

    @Override
    public void handleData() {

    }
}
