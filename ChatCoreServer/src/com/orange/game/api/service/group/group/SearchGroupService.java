package com.orange.game.api.service.group.group;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.group.Group;
import com.orange.game.model.manager.group.GroupManager;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-11-28
 * Time: 下午1:54
 * To change this template use File | Settings | File Templates.
 */
public class SearchGroupService extends CommonGameService {


    private String keyword;
    private int limit;
    private int offset;

    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {
        if (!setAndCheckUserIdAndAppId(request)) {
            return false;
        }
        keyword = request.getParameter(ServiceConstant.PARA_KEYWORD);

        limit = getIntValueFromRequest(request, ServiceConstant.PARA_LIMIT,
                ServiceConstant.CONST_DEFAULT_PAGE_COUNT);

        offset = getIntValueFromRequest(request, ServiceConstant.PARA_OFFSET, 0);
        if (!check(keyword, ErrorCode.ERROR_PARAMETER_KEYWORD_EMPTY,
                ErrorCode.ERROR_PARAMETER_KEYWORD_NULL)) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return "SearchGroupService{" +
                "keyword='" + keyword + '\'' +
                ", limit=" + limit +
                ", offset=" + offset +
                '}';
    }

    @Override
    public void handleData() {
        List<Group> list = GroupManager.searchGroup(mongoClient, keyword, offset, limit);
        log.info("<SearchGroupService> key = " + keyword + ", result count = " + list.size());
        byteData = CommonServiceUtils.groupListToPB(list);
    }
}
