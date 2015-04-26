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
import com.orange.game.model.manager.OpusManager;
import com.orange.game.model.manager.opusaction.OpusActionManager;

public class GetFeedCommentList extends CommonGameService {

	String opusId;
	String appId;
	boolean returnItem = false; // return the flower and tomato
	int offset;
	int limit;

	int type;

	@Override
	public void handleData() {
		List<UserAction> commentList = null;
		if (type == UserAction.COMMENT_TYPE_ALL) {
			commentList = OpusManager.getCommentList(mongoClient, opusId,
					appId, returnItem, offset, limit);
			//commentList = xiaoji.opusActionManager(type).getList(appId, type, opusId, offset, limit);
			
		} else {
			/*commentList = OpusManager.getCommentList(mongoClient, opusId,
					appId, type, offset, limit);*/

			OpusActionManager opusActionManager = xiaoji.opusActionManager(type);
			if(opusActionManager!= null){
				commentList =  opusActionManager.getList(opusId, offset, limit);
			}
		}

        Set<String> ongoingAnouymousContestIds = Collections.emptySet();
        if (type == UserAction.COMMENT_TYPE_COMMENT || type == UserAction.COMMENT_TYPE_CONTEST_COMMENT){
            ongoingAnouymousContestIds = ContestManager.getOngoingAnouymousContestIds();
        }

		if (commentList != null && commentList.size() != 0) {
			byteData = CommonServiceUtils.feedListToProtocolBuffer(commentList, 0, false, ongoingAnouymousContestIds);
		}

		if (byteData == null) {
			byteData = CommonServiceUtils.protocolBufferNoData();
		}

	}

	@Override
	public boolean setDataFromRequest(HttpServletRequest request) {
		opusId = request.getParameter(ServiceConstant.PARA_OPUS_ID);
		if (!check(opusId, ErrorCode.ERROR_PARAMETER_OPUSID_EMPTY,
				ErrorCode.ERROR_PARAMETER_OPUSID_NULL))
			return false;

		offset = getIntValueFromRequest(request, ServiceConstant.PARA_OFFSET,
                0);
		limit = getIntValueFromRequest(request, ServiceConstant.PARA_COUNT,
                ServiceConstant.CONST_DEFAULT_PAGE_COUNT);

		type = getIntValueFromRequest(request, ServiceConstant.PARA_TYPE,
                UserAction.COMMENT_TYPE_ALL);

		if (type == UserAction.COMMENT_TYPE_ALL) {
			int returnItemValue = getIntValueFromRequest(request,
                    ServiceConstant.PARA_RETURN_ITEM, 0);
			if (returnItemValue != 0) {
				returnItem = true;
			}			
		}
		return true;
	}

}
