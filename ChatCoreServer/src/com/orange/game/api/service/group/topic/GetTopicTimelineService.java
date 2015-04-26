package com.orange.game.api.service.group.topic;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.bbs.BBSPost;
import com.orange.game.model.manager.bbs.BBSManager;
import com.orange.game.model.manager.group.GroupManager;
import com.orange.game.model.manager.group.index.UserTopicIndexManager;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-11-22
 * Time: 下午5:22
 * To change this template use File | Settings | File Templates.
 */
public class GetTopicTimelineService extends CommonGameService{

    private int offset;
    private int limit;

    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {
        if (!setAndCheckUserIdAndAppId(request)){
            return false;
        }
        offset = getIntValueFromRequest(request, ServiceConstant.PARA_OFFSET, 0);
        limit = getIntValueFromRequest(request, ServiceConstant.PARA_LIMIT, ServiceConstant.CONST_DEFAULT_PAGE_COUNT);
        return true;
    }

    @Override
    public void handleData() {

        String userGroupId = null;
        userGroupId = GroupManager.getStringGroupIdByUserId(mongoClient, userId);

        List<BBSPost> posts = UserTopicIndexManager.getTimelineInstance().getList(userId, offset, limit);
        byteData = CommonServiceUtils.bbsPostListToProto(posts, userId, userGroupId);
    }
}
