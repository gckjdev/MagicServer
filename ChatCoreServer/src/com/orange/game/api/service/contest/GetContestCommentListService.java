package com.orange.game.api.service.contest;

import com.orange.common.utils.StringUtil;
import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.manager.timeline.ContestCommentTimelineManager;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 13-8-5
 * Time: 下午10:10
 * To change this template use File | Settings | File Templates.
 */
public class GetContestCommentListService extends CommonGameService {

    String contestId;
    int offset;
    int limit;

    @Override
    public boolean setDataFromRequest(HttpServletRequest request) {
        contestId = request.getParameter(ServiceConstant.PARA_CONTESTID);
        offset = getIntValueFromRequest(request, ServiceConstant.PARA_OFFSET, 0);
        limit = getIntValueFromRequest(request, ServiceConstant.PARA_COUNT, ServiceConstant.CONST_DEFAULT_PAGE_COUNT);

        if (StringUtil.isEmpty(contestId)){
            resultCode = ErrorCode.ERROR_PARAMETER_CONTESTID_EMPTY;
            return false;
        }

        return true;
    }

    @Override
    public void handleData() {

        List<UserAction> commentList =  ContestCommentTimelineManager.getInstance().getList(contestId, offset, limit);
        if (commentList.size() > 0) {
            byteData = CommonServiceUtils.feedListToProtocolBuffer(commentList, 0, false, null);
        }

        if (byteData == null) {
            byteData = CommonServiceUtils.protocolBufferNoData();
        }
    }
}
