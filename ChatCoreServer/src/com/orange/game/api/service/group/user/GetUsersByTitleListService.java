package com.orange.game.api.service.group.user;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.group.GroupUsersByTitle;
import com.orange.game.model.manager.group.GroupTitleManager;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-12-10
 * Time: 下午4:28
 * To change this template use File | Settings | File Templates.
 */
public class GetUsersByTitleListService extends CommonGameService {
    private String groupId;

    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {
        if (!setAndCheckUserIdAndAppId(request)){
            return false;
        }

        groupId = request.getParameter(ServiceConstant.PARA_GROUPID);
        if (!check(groupId, ErrorCode.ERROR_PARAMETER_GROUPID_EMPTY,
                ErrorCode.ERROR_PARAMETER_GROUPID_NULL)) {
            return false;
        }

        return true;
    }

    @Override
    public void handleData() {
        List<GroupUsersByTitle> list = GroupTitleManager.getUserListByTitle(mongoClient, groupId);
        byteData = CommonServiceUtils.toPBList(list);
    }
}
