package com.orange.game.api.service.group.group;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.group.Group;
import com.orange.game.model.manager.group.FollowGroupManager;
import com.orange.game.model.manager.group.index.GroupIndexManager;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-11-11
 * Time: 上午11:52
 * To change this template use File | Settings | File Templates.
 */
public class GetGroupsService extends CommonGameService {

    private static final int GROUP_LIST_TYPE_FOLLOW = 1;
    private static final int GROUP_LIST_TYPE_NEW = 2;
    private static final int GROUP_LIST_TYPE_BALANCE = 3;
    private static final int GROUP_LIST_TYPE_ACTIVE = 4;
    private static final int GROUP_LIST_TYPE_FAME = 5;
    private int type;
    private int offset;
    private int limit;

    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {

        setUserIdAndAppId(request);
        if (!checkUserIdAndAppId()) {
            return false;
        }

        type = getIntValueFromRequest(request, ServiceConstant.PARA_TYPE, GROUP_LIST_TYPE_BALANCE);
        offset = getIntValueFromRequest(request, ServiceConstant.PARA_OFFSET, 0);
        limit = getIntValueFromRequest(request, ServiceConstant.PARA_LIMIT, ServiceConstant.CONST_DEFAULT_PAGE_COUNT);

        return true;
    }

    //茄子牛肉  姜爆鸡*3  烧鸭粉

    @Override
    public void handleData() {
        List<Group> list = null;
        switch (type) {
            case GROUP_LIST_TYPE_FOLLOW:
                list = FollowGroupManager.getFollowGroups(mongoClient, userId, gameId, offset, limit);
                break;
            case GROUP_LIST_TYPE_NEW:
                list = GroupIndexManager.newManager().getTopList(offset, limit);
                break;
            case GROUP_LIST_TYPE_ACTIVE:
                list = GroupIndexManager.activeManager().getTopList(offset, limit);
                break;
            case GROUP_LIST_TYPE_BALANCE:
                list = GroupIndexManager.balanceManager().getTopList(offset, limit);
                break;
            case GROUP_LIST_TYPE_FAME:
                list = GroupIndexManager.fameManager().getTopList(offset, limit);
                break;
        }
        byteData = CommonServiceUtils.groupListToPB(list);
    }
}
