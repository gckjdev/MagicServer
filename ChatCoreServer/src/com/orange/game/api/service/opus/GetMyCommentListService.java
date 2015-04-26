package com.orange.game.api.service.opus;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.manager.ContestManager;

public class GetMyCommentListService extends CommonGameService {

	String uid;
	String appId;
	int offset;
	int limit;

	@Override
	public void handleData() {
		/*List<UserAction> commentList = OpusManager.getMyCommentList(
				mongoClient, uid, offset, limit);*/
		List<UserAction> commentList = null;
		commentList = xiaoji.commentTimelineManager().getList(uid, offset, limit);
		if (commentList != null && commentList.size() != 0) {

            Set<String> ongoingAnouymousContestIds = Collections.emptySet();
            ongoingAnouymousContestIds = ContestManager.getOngoingAnouymousContestIds();

			byteData = CommonServiceUtils.feedListToProtocolBuffer(commentList, 0, true, ongoingAnouymousContestIds);
		}
		if (byteData == null) {
			byteData = CommonServiceUtils.protocolBufferNoData();
		}
	}

	@Override
	public String toString() {
		return "GetMyCommentListService [appId=" + appId + ", limit=" + limit
				+ ", offset=" + offset + ", uid=" + uid + "]";
	}

	@Override
	public boolean setDataFromRequest(HttpServletRequest request) {
		uid = request.getParameter(ServiceConstant.PARA_USERID);
		if (!check(uid, ErrorCode.ERROR_PARAMETER_USERID_EMPTY,
				ErrorCode.ERROR_PARAMETER_USERID_NULL))
			return false;
		appId = request.getParameter(ServiceConstant.PARA_APPID);

		offset = getIntValueFromRequest(request, ServiceConstant.PARA_OFFSET,
                0);
		limit = getIntValueFromRequest(request, ServiceConstant.PARA_COUNT,
                ServiceConstant.CONST_DEFAULT_PAGE_COUNT);
		return true;
	}

}
