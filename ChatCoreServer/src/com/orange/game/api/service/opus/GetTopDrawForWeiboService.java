/**
 * 
 */
package com.orange.game.api.service.opus;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.DBConstants;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.manager.OpusManager;
import com.orange.game.model.xiaoji.XiaojiFactory;

/**
 * @author larmbr
 * 
 */
public class GetTopDrawForWeiboService extends CommonGameService {

	int language;
	int limit;
	int offset;
	String appId;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.orange.common.api.service.CommonService#setDataFromRequest(javax.
	 * servlet.http.HttpServletRequest)
	 */
	@Override
	public boolean setDataFromRequest(HttpServletRequest request) {
		offset = getIntValueFromRequest(request, ServiceConstant.PARA_OFFSET,
                0);
		limit = getIntValueFromRequest(request, ServiceConstant.PARA_COUNT,
                ServiceConstant.CONST_DEFAULT_PAGE_COUNT);
		language = getIntValueFromRequest(request, ServiceConstant.PARA_LANGUAGE, 1);
		return true;
	}

	@Override
	public void handleData() {
//		List<UserAction> list = OpusManager.getTopOpusSimpleInfo(mongoClient,
//				language, offset, limit);

		List<UserAction> list = XiaojiFactory.getInstance().getDraw().hotTopOpusManager(DBConstants.C_LANGUAGE_CHINESE).getTopList(offset, limit);
		resultData = CommonServiceUtils.simpleFeedListToJSON(list);

	}

	@Override
	public String toString() {
		return "GetTopDrawForWeiboService [appId=" + appId + ", language="
				+ language + ", limit=" + limit + ", offset=" + offset + "]";
	}

}
