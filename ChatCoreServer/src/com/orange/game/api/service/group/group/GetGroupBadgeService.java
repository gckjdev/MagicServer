package com.orange.game.api.service.group.group;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.manager.group.GroupNoticeManager;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-11-29
 * Time: 下午12:11
 * To change this template use File | Settings | File Templates.
 */
public class GetGroupBadgeService extends CommonGameService {

    String groupId;

    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {
        if (!setAndCheckUserIdAndAppId(request)){
            return false;
        }

        groupId = request.getParameter(ServiceConstant.PARA_GROUPID);
        return true;
    }

    @Override
    public void handleData() {
        Map<Integer, Integer> badges = GroupNoticeManager.getGroupBadges(mongoClient, groupId, userId, gameId);
        byteData = CommonServiceUtils.badgesToPB(badges);
    }
}
