package com.orange.game.api.service.group.topic;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.bbs.BBSPost;
import com.orange.game.model.manager.bbs.BBSManager;
import com.orange.game.model.manager.group.GroupManager;
import com.orange.game.model.manager.group.index.HotTopicIndexManager;
import com.orange.game.model.manager.group.index.UserTopicIndexManager;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created by gamy on 14-2-9.
 */
public class GetTopicsService extends CommonGameService {


    private int offset;
    private int limit;
    private int type;

    static final int GetTopicListTypeHot = 11;
    static final int GetTopicListTypeNew = 12;
    static final int GetTopicListTypeMine = 14;

    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {
        if (!setAndCheckUserIdAndAppId(request)) {
            return false;
        }
        offset = getIntValueFromRequest(request, ServiceConstant.PARA_OFFSET, 0);
        limit = getIntValueFromRequest(request, ServiceConstant.PARA_LIMIT, ServiceConstant.CONST_DEFAULT_PAGE_COUNT);
        type = getIntValueFromRequest(request, ServiceConstant.PARA_TYPE, GetTopicsService.GetTopicListTypeHot);

        return true;
    }

    @Override
    public void handleData() {
        List<BBSPost> posts = null;
        switch (type) {
            case GetTopicListTypeHot:
                posts = HotTopicIndexManager.getInstance().getTopList(offset, limit);
                break;
            case GetTopicListTypeMine:
                posts = UserTopicIndexManager.getManagerForMine().getList(userId, offset, limit);
                break;
            case GetTopicListTypeNew:
                posts = BBSManager.getLatestGroupTopics(mongoClient, offset, limit);
                break;
        }

        String userGroupId = null;
        userGroupId = GroupManager.getStringGroupIdByUserId(mongoClient, userId);

        byteData = CommonServiceUtils.bbsPostListToProto(posts, userId, userGroupId);
    }

}
