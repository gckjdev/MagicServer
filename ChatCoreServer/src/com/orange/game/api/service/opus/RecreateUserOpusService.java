package com.orange.game.api.service.opus;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.manager.OpusManager;
import com.orange.game.model.manager.opus.OpusUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 13-9-23
 * Time: 上午11:44
 * To change this template use File | Settings | File Templates.
 */
public class RecreateUserOpusService extends CommonGameService {

    String targetUserId;

    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {
        targetUserId = request.getParameter(ServiceConstant.PARA_TARGETUSERID);
        return true;
    }

    @Override
    public void handleData() {

        // remove old index
        xiaoji.userOpusManager().removeIndex(targetUserId, false);

        // get list to invoke old one
        xiaoji.userOpusManager().getList(targetUserId, 0, 1, OpusUtils.createReturnFields(), null, 0);
    }
}
