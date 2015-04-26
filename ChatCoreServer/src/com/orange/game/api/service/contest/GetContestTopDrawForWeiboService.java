package com.orange.game.api.service.contest;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.Contest;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.manager.ContestManager;
import com.orange.game.model.manager.OpusManager;

public class GetContestTopDrawForWeiboService extends CommonGameService {
	int language;
	int limit;
	int offset;
	String appId;
	String contestId;

	@Override
	public void handleData() {
		Contest contest = ContestManager.getContestById(mongoClient, contestId);
		if (contest != null) {
			JSONObject contestObject = new JSONObject();
			List<UserAction> feeds = OpusManager.getContestHotOpusList(
					mongoClient, null, contestId, language, offset, limit);
			JSONArray feedList = CommonServiceUtils.simpleFeedListToJSON(feeds);
			contestObject.put(ServiceConstant.PARA_TITLE, contest.getTitle());
			contestObject.put(ServiceConstant.PARA_LIST, feedList);
			
			resultData = contestObject;
		}else {
			resultData = "contest id is null or contest is not exist";
			resultCode = ErrorCode.ERROR_CONTEST_NOT_FOUND;
		}
	}

	@Override
	public String toString() {
		return "GetContestTopDrawForWeiboService [appId=" + appId + ", language="
				+ language + ", limit=" + limit + ", offset=" + offset + "]";
	}

	@Override
	public boolean setDataFromRequest(HttpServletRequest request) {
		offset = getIntValueFromRequest(request, ServiceConstant.PARA_OFFSET,
                0);
		limit = getIntValueFromRequest(request, ServiceConstant.PARA_COUNT,
                ServiceConstant.CONST_DEFAULT_PAGE_COUNT);
		language = getIntValueFromRequest(request,
                ServiceConstant.PARA_LANGUAGE, 1);
		contestId = request.getParameter(ServiceConstant.PARA_CONTESTID);

		return true;
	}

}
