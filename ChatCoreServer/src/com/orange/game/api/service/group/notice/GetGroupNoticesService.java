package com.orange.game.api.service.group.notice;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.group.GroupNotice;
import com.orange.game.model.manager.group.GroupNoticeManager;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-11-11
 * Time: 上午10:02
 * To change this template use File | Settings | File Templates.
 */
public class GetGroupNoticesService extends CommonGameService {

    int type;
    int offset;
    int limit;

    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {
        setUserIdAndAppId(request);
        if (!checkUserIdAndAppId()){
            return false;
        }
        type = getIntValueFromRequest(request, ServiceConstant.PARA_TYPE, GroupNoticeManager.NOTICE_TYPE_NOTICE);
        offset = getIntValueFromRequest(request, ServiceConstant.PARA_OFFSET, 0);
        limit = getIntValueFromRequest(request, ServiceConstant.PARA_LIMIT, ServiceConstant.CONST_DEFAULT_PAGE_COUNT);
        return true;
    }

    @Override
    public void handleData() {
        List<GroupNotice> list;
        if (type == GroupNoticeManager.NOTICE_TYPE_REQUEST){
            list = GroupNoticeManager.getRequestNoticeList(mongoClient, userId, offset, limit);
        }else{
            list = GroupNoticeManager.getNoticeList(mongoClient, userId, offset, limit);
        }
        byteData = CommonServiceUtils.toPBList(list);
    }
}
