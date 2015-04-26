package com.orange.game.api.service.group.group;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.model.manager.group.FollowGroupManager;
import org.bson.types.ObjectId;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-11-21
 * Time: 下午3:56
 * To change this template use File | Settings | File Templates.
 */
public class SyncFollowGroupIdsService extends CommonGameService {
    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {
        if (!setAndCheckUserIdAndAppId(request)){
            return false;
        }
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void handleData() {        
        List<ObjectId> list = FollowGroupManager.getAllFollowGroupIds(mongoClient, userId);
        log.info("<SyncFollowGroupIdsService> userId = " + userId + ", result size = "+ list.size());        
        byteData = CommonServiceUtils.objectIdListToPBData(list);
    }
}
