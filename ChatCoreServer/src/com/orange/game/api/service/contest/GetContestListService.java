package com.orange.game.api.service.contest;

import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.orange.common.utils.StringUtil;

import com.orange.common.api.service.CommonParameter;
import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.DBConstants;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.Contest;
import com.orange.game.model.manager.ContestManager;
import com.orange.network.game.protocol.constants.GameConstantsProtos;

public class GetContestListService extends CommonGameService {

	String appId;
	String userId;
	int language;
	int type;
	int offset;
	int limit;
    int category;

    boolean groupOnly = false;
    String  groupId;
	
	private static final int CONTEST_TYPE_ALL = 0;
	private static final int CONTEST_TYPE_PASSED = 1;
	private static final int CONTEST_TYPE_CURRENT = 2;
	private static final int CONTEST_TYPE_PENDING = 3;
    private static final int CONTEST_TYPE_ALL_GROUP = 4;
    private static final int CONTEST_TYPE_BY_GROUP = 5;

    private static final int CONTEST_TYPE_BY_GROUP_NEW = 4;
    private static final int CONTEST_TYPE_BY_GROUP_FOLLOW = 6;
    private static final int CONTEST_TYPE_BY_GROUP_POP = 7;
    private static final int CONTEST_TYPE_BY_GROUP_AWARD = 8;

	@Override
	public void handleData() {

		List<Contest> list = Collections.emptyList();

        switch (type) {
            case CONTEST_TYPE_BY_GROUP:
                list = ContestManager.getContestListByGroupId(mongoClient, offset, limit, groupId, category);
                break;

            case CONTEST_TYPE_ALL_GROUP:
                list = ContestManager.getAllGroupContestList(mongoClient, offset, limit, category);
                break;

            case CONTEST_TYPE_ALL:
                list = ContestManager.getAllContestList(mongoClient, offset, limit, language, category);
                break;

            case CONTEST_TYPE_CURRENT:
                list = ContestManager.getCurrentContestList(mongoClient, offset, limit, language, category);
//                list = ContestManager.getCurrentContestList(mongoClient, offset, limit, language);
                break;

            case CONTEST_TYPE_PASSED:
                list = ContestManager.getPassedContestList(mongoClient, offset, limit, language, category);
                break;

            case CONTEST_TYPE_PENDING:
                list = ContestManager.getPendingContestList(mongoClient, offset, limit, language, category);
                break;

            // for group
            case CONTEST_TYPE_BY_GROUP_FOLLOW:
                list = ContestManager.getFollowGroupContestList(mongoClient, offset, limit, userId, category);
                break;

            case CONTEST_TYPE_BY_GROUP_POP:
                list = ContestManager.getOngoingGroupContestList(mongoClient, offset, limit, category, DBConstants.F_OPUS_COUNT);
                break;

            case CONTEST_TYPE_BY_GROUP_AWARD:
                list = ContestManager.getOngoingGroupContestList(mongoClient, offset, limit, category, DBConstants.F_TOTAL_AWARD);
                break;

            default:
                break;
            }

        if (format !=null && format.equalsIgnoreCase(CommonParameter.JSON)){
		    resultData = CommonServiceUtils.contestListToJSON(list);
        }
        else{
            byteData = CommonServiceUtils.contestListToPB(list, userId);
        }
	}
	
	@Override
	public String toString() {
		return "GetContestListService [appId=" + appId + ", language="
				+ language + ", limit=" + limit + ", offset=" + offset
				+ ", type=" + type + ", userId=" + userId + "]";
	}

	@Override
	public boolean setDataFromRequest(HttpServletRequest request) {
		type = getIntValueFromRequest(request,
                ServiceConstant.PARA_TYPE, CONTEST_TYPE_ALL);
		
		language = getIntValueFromRequest(request,
                ServiceConstant.PARA_LANGUAGE, 0);

		appId = request.getParameter(ServiceConstant.PARA_APPID);
		userId = request.getParameter(ServiceConstant.PARA_USERID);
		
		if (!check(userId, ErrorCode.ERROR_PARAMETER_USERID_EMPTY,
				ErrorCode.ERROR_PARAMETER_USERID_NULL)) {
			return false;
		}
		
		offset = getIntValueFromRequest(request, ServiceConstant.PARA_OFFSET, 0);
		limit = getIntValueFromRequest(request, ServiceConstant.PARA_COUNT, ServiceConstant.CONST_DEFAULT_PAGE_COUNT);

        groupOnly = getBoolValueFromRequest(request, ServiceConstant.PARA_IS_GROUP, false);
        groupId = getStringValueFromRequeset(request, ServiceConstant.PARA_GROUPID, null);

        category = xiaoji.getCategoryType(); //getIntValueFromRequest(request, ServiceConstant.PARA_CATEGORY, GameConstantsProtos.PBOpusCategoryType.DRAW_CATEGORY_VALUE);

		return true;
	}

}
