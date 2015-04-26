package com.orange.game.api.service.group.notice;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.group.GroupNotice;
import com.orange.game.model.manager.group.index.GroupNoticeIndexManager;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 14-1-22
 * Time: 下午1:48
 * To change this template use File | Settings | File Templates.
 */
public class GetChargeHistoryService extends CommonGameService {
    private int offset;
    private int limit;

    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {
        if (!setAndCheckUserIdAndAppId(request)){
            return false;
        }
        offset = getIntValueFromRequest(request, ServiceConstant.PARA_OFFSET, 0);
        limit = getIntValueFromRequest(request, ServiceConstant.PARA_LIMIT, ServiceConstant.CONST_DEFAULT_PAGE_COUNT);
        groupId = request.getParameter(ServiceConstant.PARA_GROUPID);
        if (!check(groupId, ErrorCode.ERROR_PARAMETER_GROUPID_EMPTY,
                ErrorCode.ERROR_PARAMETER_GROUPID_NULL)) {
            return false;
        }

        return true;
    }

    @Override
    public void handleData() {
        List<GroupNotice> notices = GroupNoticeIndexManager.getChargeIndexManager().getList(groupId, offset, limit);
        byteData = CommonServiceUtils.toPBList(notices);
    }
}
