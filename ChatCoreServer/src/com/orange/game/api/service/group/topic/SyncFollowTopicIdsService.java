package com.orange.game.api.service.group.topic;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.model.manager.group.index.UserTopicIndexManager;
import org.bson.types.ObjectId;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-12-31
 * Time: 下午4:14
 * To change this template use File | Settings | File Templates.
 */
public class SyncFollowTopicIdsService extends CommonGameService {
    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {
        if (!setAndCheckUserIdAndAppId(request)) {
            return false;
        }
        return true;
    }

    @Override
    public void handleData() {
        List<ObjectId> list = UserTopicIndexManager.getManagerForFollow().getAllIdList(userId);
        log.info("<SyncFollowTopicIdsService> list count = " + list.size() + ", userId = " + userId);
        byteData = CommonServiceUtils.objectIdListToPBData(list);
    }
}
