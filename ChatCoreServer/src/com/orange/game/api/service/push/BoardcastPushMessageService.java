package com.orange.game.api.service.push;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.service.push.BroadPushService;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by chaoso on 14-8-18.
 */
public class BoardcastPushMessageService extends CommonGameService{

    String name;            // push table name suffix
    String action;          // refer to below in DBConstants
    String xiaoji;

//    public static final int F_PUSH_MESSAGE_ACTION_REMOVE_ALL_SEND = 1;
//    public static final int F_PUSH_MESSAGE_ACTION_RESEND_ALL = 2;
//    public static final int F_PUSH_MESSAGE_ACTION_START_NEW = 3;
//    public static final int F_PUSH_MESSAGE_ACTION_PUSH = 4;

    /* usage:

    local test

        http://localhost:8100/api/i?&m1=boardPushMessage&na=20140821&ac=3

    remote test

        http://58.215.184.18:8699/api/i?&m1=boardPushMessage&na=20140821&ac=3&xn=139847858

    production

        http://58.215.184.18:8699/api/i?&m1=boardPushMessage&na=20140821&ac=3

     */

    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {
        name = request.getParameter(ServiceConstant.PARA_NAME);
        action = request.getParameter(ServiceConstant.PARA_ACTION_PUSH_MESSAGE);
        xiaoji = request.getParameter(ServiceConstant.PARA_XIAOJI_NUMBER);
        return true;
    }

    @Override
    public void handleData() {
        //推送消息给所有符合条件的人
        BroadPushService broadPushService = BroadPushService.getInstance();
        broadPushService.pushMessageToUsersWithAction(Integer.parseInt(action), name, xiaoji);
    }
}
