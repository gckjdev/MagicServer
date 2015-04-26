package com.orange.game.api.service.contest;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.orange.common.utils.StringUtil;
import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.Contest;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.manager.ContestManager;
import com.orange.game.model.service.DBService;

public class GetContestOpusListService extends CommonGameService {

	private final static int OPUS_LIST_TYPE_MY = 1;
	private final static int OPUS_LIST_TYPE_TOP = 2;
	private final static int OPUS_LIST_TYPE_NEW = 3;
    private final static int OPUS_LIST_TYPE_CONTEST_HOT = 4;

	String uid;
	String appId;
	String contestId;

	int offset;
	int limit;
	int type;
	int language;
	
	boolean isReturnCompressedData = true;		// TODO set from data request, actaully this is useless for get contest opus

	@Override
	public void handleData() {

        List<UserAction> feeds = null;
		switch (type) {
		case OPUS_LIST_TYPE_TOP:
			feeds = xiaoji.contestTopOpusManager(contestId).getTopList(offset, limit);
			break;
		case OPUS_LIST_TYPE_MY:
			feeds = xiaoji.myContestOpusManager(contestId).getList(uid, offset, limit);
			break;
		case OPUS_LIST_TYPE_NEW:
			feeds = xiaoji.contestLatestOpusManager(contestId).getList(offset, limit);
			break;

        case OPUS_LIST_TYPE_CONTEST_HOT:
//            feeds = xiaoji.contestHotOpusManager(contestId).getList(offset, limit);
            break;

		default:
			break;
		}
		try {
            boolean anounymousNick = false;
            if (!isVersionAbove70()){
                Contest contest = null;
                if (!StringUtil.isEmpty(contestId)){
                    contest = ContestManager.getContestById(DBService.getInstance().getMongoDBClient(), contestId);
                    if (contest == null){
                        log.info("<GetContestOpusListService> but contest not found for "+contestId);
                        resultCode = ErrorCode.ERROR_CONTEST_NOT_FOUND;
                        return;
                    }
                }

                if (contest != null && contest.canVote() && contest.getIsAnonymous()){
                    log.info("<GetContestOpusListService> old version, return anounymous user nick");
                    anounymousNick = true;
                }
            }

			byteData = CommonServiceUtils.feedListToProtocolBufferImage(feeds, 0, isReturnCompressedData, anounymousNick);
		} catch (Exception e) {
			log.info(e.getStackTrace());
			byteData = null;
		}
		
		if (feeds != null && byteData != null) {
			log.info("feed list count = "+ feeds.size() + "byte size = "+byteData.length);	
		}
		
		if (byteData == null) {
			log
					.warn("<warning> GetFeedListService, but no byte data for return???");
			byteData = CommonServiceUtils.protocolBufferNoData();
		}
	}

	@Override
	public boolean setDataFromRequest(HttpServletRequest request) {
		uid = request.getParameter(ServiceConstant.PARA_USERID);

		if (!check(uid, ErrorCode.ERROR_PARAMETER_USERID_EMPTY,
				ErrorCode.ERROR_PARAMETER_USERID_NULL))
			return false;

		contestId = request.getParameter(ServiceConstant.PARA_CONTESTID);
		if (!check(contestId, ErrorCode.ERROR_PARAMETER_CONTESTID_EMPTY,
				ErrorCode.ERROR_PARAMETER_CONTESTID_NULL))
			return false;

		appId = request.getParameter(ServiceConstant.PARA_APPID);

		language = this.getIntValueFromRequest(request,
                ServiceConstant.PARA_LANGUAGE, UserAction.LANGUAGE_UNKNOW);

		type = getIntValueFromRequest(request, ServiceConstant.PARA_TYPE, 0);

		offset = getIntValueFromRequest(request, ServiceConstant.PARA_OFFSET,
                0);
		limit = getIntValueFromRequest(request, ServiceConstant.PARA_COUNT,
                ServiceConstant.CONST_DEFAULT_PAGE_COUNT);

		return true;
	}

}
