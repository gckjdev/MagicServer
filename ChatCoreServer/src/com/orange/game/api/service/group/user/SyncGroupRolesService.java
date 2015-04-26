package com.orange.game.api.service.group.user;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.model.dao.group.GroupRelation;
import com.orange.game.model.manager.group.GroupRoleManager;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-12-21
 * Time: 上午10:27
 * To change this template use File | Settings | File Templates.
 */
public class SyncGroupRolesService extends CommonGameService {
    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {
        if (!setAndCheckUserIdAndAppId(request)){
            return false;
        }
        return true;
    }

    @Override
    public void handleData() {
        Collection<GroupRelation> roles = GroupRoleManager.getAllRoles(mongoClient, userId);
        byteData = CommonServiceUtils.toPBList(roles);
    }
}
