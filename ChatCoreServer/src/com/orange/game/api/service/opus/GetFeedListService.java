package com.orange.game.api.service.opus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.mongodb.BasicDBObject;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.game.api.service.CommonGameService;
import com.orange.game.api.service.CommonServiceUtils;
import com.orange.game.constants.DBConstants;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.common.MongoGetIdListUtils;
import com.orange.game.model.dao.Contest;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.manager.ContestManager;
import com.orange.game.model.manager.OpusManager;
import com.orange.game.model.manager.group.GroupUserTimelineManager;
import com.orange.game.model.manager.opus.OpusUtils;
import com.orange.game.model.manager.opus.StageTopOpusManager;
import com.orange.game.model.manager.opusclass.OpusClassService;
import com.orange.game.model.manager.timeline.ContestCommentTimelineManager;
import com.orange.game.model.manager.timeline.OpusTimelineManager;
import com.orange.game.model.manager.timeline.TutorialTimelineManager;
import com.orange.game.model.manager.useropus.UserStageOpusManager;
import com.orange.game.model.service.DBService;
import com.orange.game.model.service.opus.OpusService;
import com.orange.game.model.service.tutorial.TutorialService;
import com.orange.game.model.xiaoji.XiaojiFactory;
import org.bson.types.ObjectId;

public class GetFeedListService extends CommonGameService {

	String uid;
	int offset;
	int limit;
	int feedType;
	int language;
	boolean image;
	String appId;
    String contestId;
    String className;
    String tutorialId;
    String stageId;

    int returnTotalCount;

    List<ObjectId> opusIdIdList = Collections.emptyList();
	
	private final static int FEEDLILST_TYPE_UNKNOW = 0;
	private final static int FEEDLIST_TYPE_MY = 1;
	private final static int FEEDLIST_TYPE_ALL = 2;
	private final static int FEEDLIST_TYPE_HOT = 3;
	private final static int FEEDLIST_TYPE_USER = 4;
	private final static int FEEDLIST_TYPE_USER_OPUS = 5;
	private static final int FEEDLIST_TYPE_LATEST = 6;
	
	private final static int FEEDLIST_TYPE_DRAWTO_ME = 7;
	private final static int FEEDLIST_TYPE_COMMENT= 8;
	private final static int FEEDLIST_TYPE_HISTORY_RANK = 9;
	private final static int FEEDLIST_TYPE_RECOMMEND = 11;
    private final static int FEEDLIST_TYPE_TIMELINE_OPUS = 12;
    private final static int FEEDLIST_TYPE_TIMELINE_GUESS = 13;

    private final static int FEEDLIST_TYPE_CONTEST_COMMENT = 20;
    private static final int FEEDLILST_TYPE_OPUS_ID_LIST = 21;

    private final static int FEEDLIST_TYPE_TIMELINE_GROUP = 30;
    private final static int FEEDLIST_TYPE_TIMELINE_GROUP_ALL = 31;

    private final static int FEEDLIST_TYPE_TIMELINE_CONQUER_DRAW = 32;

    private final static int FEEDLIST_TYPE_VIP_OPUS = 41;
    private final static int FEEDLIST_TYPE_USER_CONQUER_DRAW = 42;
    private final static int FEEDLIST_TYPE_CONQUER_DRAW_STAGE_TOP = 43;

    private final static int FEEDLIST_TYPE_CLASS_HOT_TOP = 51;
    private final static int FEEDLIST_TYPE_CLASS_ALLTIME_TOP = 52;
    private final static int FEEDLIST_TYPE_CLASS_LATEST = 53;
    private final static int FEEDLIST_TYPE_CLASS_FEATURE = 54;
    private final static int FEEDLIST_TYPE_CLASS_ORIGINAL = 55;

    private static final int FEEDLILST_TYPE_USER_FAVORITE = 100;

	boolean isReturnCompressedData = true;		// TODO set from data request
	boolean isReturnUserOpus = false;           // 闯关作品排行榜，是否返回用户自己的排名以及作品
	
	@Override
	public void handleData() {
		List<UserAction> feeds = Collections.emptyList();
		if (xiaoji == null){
			resultCode = ErrorCode.ERROR_XIAOJI_NULL;
			byteData = CommonServiceUtils.protocolBufferErrorNoData(ErrorCode.ERROR_XIAOJI_NULL);
			return;
		}
		
		switch (feedType) {
		case FEEDLIST_TYPE_MY:
//			feeds = OpusManager.getMyFeedList(mongoClient, uid, offset, limit, image);
			break;

		case FEEDLIST_TYPE_ALL:
//			feeds = OpusManager.getAllFeedList(mongoClient, uid, offset, limit, image);
			break;
		case FEEDLIST_TYPE_HOT:
			feeds = xiaoji.hotTopOpusManager(language).getTopList(offset, limit);
			
			//feeds = OpusManager.getHotFeedList(mongoClient, uid, offset, limit, language,image);
			break;
		case FEEDLIST_TYPE_USER:
			//feeds = OpusManager.getUserFeedList(mongoClient, uid, offset, limit,image);

            // TODO enable this feature while having enough data
			feeds = xiaoji.userGuessTimelineManager().getList(uid, offset, limit);
			break;
		case FEEDLIST_TYPE_LATEST:
		{
			feeds = xiaoji.latestOpusManager(appId, language).getList(offset, limit);
			
//			if (appId != null && appId.equalsIgnoreCase(DBConstants.APPID_LITTLEGEE)){
//				feeds = xiaoji.latestOpusManager(appId, language).getList(offset, limit);
//			}
//			else{
//				feeds = OpusManager.getLatestFeedList(mongoClient, uid, offset, limit, language,image);
//			}
		}
			break;
		case FEEDLIST_TYPE_USER_OPUS:
			//feeds = OpusManager.getUserOpusList(mongoClient, uid, offset, limit,image);
			if (xiaoji != null){
				feeds = xiaoji.userOpusManager().getListAndConstructIndex(uid, offset, limit);
			}
			break;

		case FEEDLIST_TYPE_DRAWTO_ME:
			//feeds = OpusManager.getDrawToUserOpusList(mongoClient, uid, offset, limit,image);
			if (xiaoji != null){
				feeds = xiaoji.drawToUserOpusManager().getListAndConstructIndex(uid, offset, limit);
			}
			break;
			
		case FEEDLIST_TYPE_HISTORY_RANK:
			
			if (xiaoji != null){
				feeds = xiaoji.allTimeTopOpusManager(language).getTopList(offset, limit);
			}
			
			//feeds = OpusManager.getHistoryTopOpusList(mongoClient, uid,language, offset, limit,image);
			break;

		case FEEDLILST_TYPE_USER_FAVORITE: // 收藏作品
			//feeds = OpusManager.getSavedOpusList(mongoClient, uid, offset, limit);
			feeds = xiaoji.userFavoriteOpusManager().getList(uid, offset, limit); //getListAndConstructIndex(uid, offset, limit);
			break;
		
		case FEEDLIST_TYPE_RECOMMEND:	// 推荐
			//feeds = OpusManager.getRecommendOpusList(mongoClient, language, offset, limit);
			feeds = xiaoji.featureOpusManager(language).getList(offset, limit);
			break;
			
		case FEEDLIST_TYPE_TIMELINE_OPUS://timeline opus
            if (XiaojiFactory.getInstance().isDraw(xiaoji.getCategoryName())){
                // invoke this to show sing timeline in draw, import traffic to draw
    			feeds = xiaoji.opusTimelineManager().getList(uid, offset, limit);
            }
            else{
                feeds = xiaoji.opusTimelineByCategoryManager().getList(uid, offset, limit);
            }
			break;

		case FEEDLIST_TYPE_TIMELINE_GUESS://timeline guess
//			feeds = xiaoji.guessOpusTimelineManager().getList(uid, offset, limit);
            feeds = xiaoji.guessOpusTimelineByCategoryManager().getList(uid, offset, limit);
			break;

        case FEEDLIST_TYPE_CONTEST_COMMENT:
            feeds = ContestCommentTimelineManager.getInstance().getList(contestId, offset, limit);
            break;

        case FEEDLILST_TYPE_OPUS_ID_LIST:
        {
            feeds = OpusManager.getFeedsByIdList(opusIdIdList);
            break;
        }

         case FEEDLIST_TYPE_TIMELINE_GROUP:
            {
                GroupUserTimelineManager manager = GroupUserTimelineManager.getTimelineManager(xiaoji.getCategoryType());
                if (manager != null){
                    feeds = manager.getTimelineList(groupId, offset, limit);
                }
                break;
            }

         case FEEDLIST_TYPE_TIMELINE_GROUP_ALL:
            {
                feeds = GroupUserTimelineManager.getAllTimelineManager().getTimelineList(groupId, offset, limit);
                break;
            }

         case FEEDLIST_TYPE_CLASS_HOT_TOP:
            {
                feeds = OpusClassService.getInstance().getOpusHotTopList(className, offset, limit);
                break;
            }

         case FEEDLIST_TYPE_CLASS_ALLTIME_TOP:
            {
                feeds = OpusClassService.getInstance().getOpusAlltimeTopList(className, offset, limit);
                break;
            }

         case FEEDLIST_TYPE_CLASS_FEATURE:
            {
                feeds = OpusClassService.getInstance().getOpusFeatureList(className, offset, limit);
                break;
            }

         case FEEDLIST_TYPE_CLASS_LATEST:
            {
                feeds = OpusClassService.getInstance().getOpusLatestList(className, offset, limit);
                break;
            }

         case FEEDLIST_TYPE_VIP_OPUS:
            {
                feeds = xiaoji.vipUserOpusManager().getTopList(offset, limit);
                break;
            }
         case FEEDLIST_TYPE_TIMELINE_CONQUER_DRAW:
            {
                feeds = TutorialTimelineManager.getInstance().getList(userId, offset, limit);
                break;
            }
            case FEEDLIST_TYPE_USER_CONQUER_DRAW:
            {
                feeds = UserStageOpusManager.getInstance().getOpusList(userId,offset,limit);
                break;
            }

            case FEEDLIST_TYPE_CONQUER_DRAW_STAGE_TOP:
            {
                if (isReturnUserOpus){
                    limit = limit - 1;
                }

                StageTopOpusManager manager = new StageTopOpusManager(tutorialId, stageId);
                feeds = manager.getTopList(offset, limit);
                UserAction userOpus = null;
                boolean needGet = true;

                // set ranks and set need get flag
                int i=0;
                for (UserAction userAction : feeds){
                    if (userAction.getCreateUserId().equalsIgnoreCase(uid)){
                        needGet = false;
                        userOpus = userAction;
                    }

                    int rank = offset+i+1;
                    i++;
                    userAction.setStageRank(rank);
                }

                if (isReturnUserOpus && feeds != null && offset == 0){
                    if (needGet){
                        userOpus = manager.getUserOpus(tutorialId, stageId, uid);
                    }
                }

                // construct final feeds
                List<UserAction> finalFeeds = new ArrayList<UserAction>();
                if (userOpus != null){
                    finalFeeds.add(userOpus);   // add as first
                }
                finalFeeds.addAll(feeds);

                feeds = finalFeeds;

                returnTotalCount = manager.getCurrentTopCount();

                break;
            }

		default:
			break;
		}

		if (feeds != null && feeds.size() != 0) {
			if (image) {
				byteData = CommonServiceUtils.feedListToProtocolBufferImage(feeds, returnTotalCount, isReturnCompressedData);
			}else{
				// not support any more, due to we remove draw data from getFeedList return fields
				// rem by Benson 2013-03-22
				byteData = CommonServiceUtils.feedListToProtocolBuffer(feeds, returnTotalCount, false, null);
			}
		}

        OpusService.getInstance().updateOpusMissingInfo(feeds);

		if (byteData == null) {
			log.warn("<warning> GetFeedListService, but no byte data for return???");
			byteData = CommonServiceUtils.protocolBufferNoData();
		}
	}

	@Override
	public boolean setDataFromRequest(HttpServletRequest request) {
		uid = request.getParameter(ServiceConstant.PARA_USERID);
		appId = request.getParameter(ServiceConstant.PARA_APPID);
		if (!check(uid, ErrorCode.ERROR_PARAMETER_USERID_EMPTY,
				ErrorCode.ERROR_PARAMETER_USERID_NULL))
			return false;
		language = this
		.getIntValueFromRequest(request,
                ServiceConstant.PARA_LANGUAGE,
                UserAction.LANGUAGE_UNKNOW);

        contestId = request.getParameter(ServiceConstant.PARA_CONTESTID);

		feedType = getIntValueFromRequest(request,
                ServiceConstant.PARA_TYPE, FEEDLILST_TYPE_UNKNOW);

		offset = getIntValueFromRequest(request, ServiceConstant.PARA_OFFSET,
                0);
		limit = getIntValueFromRequest(request, ServiceConstant.PARA_COUNT,
                ServiceConstant.CONST_DEFAULT_PAGE_COUNT);
		
		int imageInt = getIntValueFromRequest(request, ServiceConstant.PARA_IMAGE, 0);
		image = (imageInt != 0);
        if (feedType >= FEEDLIST_TYPE_CONTEST_COMMENT){
            image = true;
        }

        String opusIdString = request.getParameter(ServiceConstant.PARA_OPUS_ID_LIST);
        if (opusIdString != null){
            String[] opusIdStringArray = opusIdString.split("\\" + ServiceConstant.USERID_SEPERATOR);
            if (opusIdStringArray != null){
                opusIdIdList = new ArrayList<ObjectId>(opusIdStringArray.length);
                for ( String opusId : opusIdStringArray ) {
                    if (opusId != null && ObjectId.isValid(opusId)){
                        opusIdIdList.add(new ObjectId(opusId));
                    }
                    else{
                        log.warn("<GetFeedList> parse opusId list but opusId invalid "+opusId);
                    }
                }
            }
        }

        className = request.getParameter(ServiceConstant.PARA_CLASS);
        tutorialId = request.getParameter(ServiceConstant.PARA_TUTORIAL_ID);
        stageId = request.getParameter(ServiceConstant.PARA_STAGE_ID);
        isReturnUserOpus = getBoolValueFromRequest(request, ServiceConstant.PARA_RETURN_USER_OPUS, true);

		return true;
	}

}
