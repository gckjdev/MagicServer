package com.orange.game.model.xiaoji;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import com.orange.common.utils.DateUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.dao.opus.Opus;
import com.orange.game.model.manager.bbs.BBSBoardPostManager;
import com.orange.game.model.manager.guessopus.TopUserGuessManager;
import com.orange.game.model.manager.guessopus.UserGuessConstants;
import com.orange.game.model.manager.guessopus.UserGuessOpusPlayManager;
import com.orange.game.model.manager.guessopus.UserGuessUtil;
import com.orange.game.model.manager.opus.*;
import com.orange.game.model.manager.opus.contest.ContestLatestOpusManager;
import com.orange.game.model.manager.opus.contest.ContestTopOpusManager;
import com.orange.game.model.manager.opus.contest.MyContestOpusManager;
import com.orange.game.model.manager.opusaction.OpusActionManager;
import com.orange.game.model.manager.timeline.*;
import com.orange.game.model.manager.user.NewStarUserManager;
import com.orange.game.model.manager.user.PopUserManager;
import com.orange.game.model.manager.user.TopUserManager;
import com.orange.game.model.manager.useropus.DrawToUserOpusManager;
import com.orange.game.model.manager.useropus.UserFavoriteOpusManager;
import com.orange.game.model.manager.useropus.UserGuessTimelineManager;
import com.orange.game.model.manager.useropus.UserOpusManager;
import com.orange.game.model.manager.OpusManager;
import com.orange.game.model.manager.utils.ImageUploadManager;
import com.orange.game.model.manager.xiaojinumber.FreePoolManager;
import com.orange.game.model.service.DataService;
import com.orange.game.model.service.contest.ContestService;
import com.orange.network.game.protocol.model.DrawProtos;
import com.orange.network.game.protocol.model.OpusProtos;
import com.orange.network.game.protocol.model.OpusProtos.PBOpus;
import org.apache.log4j.Logger;

import static com.orange.network.game.protocol.model.OpusProtos.*;

public abstract class AbstractXiaoji {

    public static final Logger log = Logger.getLogger(AbstractXiaoji.class.getName());

	final static String EN_SUFFIX = "_en";
	final static String CN_SUFFIX = "_cn";
	final static String ACTION_FLOWER = "flower";
	final static String ACTION_GUESS = "guess";
	final static String ACTION_COMMENT = "comment";
	final static String ACTION_CORRECT = "correct";
	final static String ACTION_ALL = "all";
    final static String ACTION_CONTEST_COMMENT = "contest_comment";
	final TopUserManager topUserManager =  new TopUserManager(getCategoryName());
    final NewStarUserManager newStarUserManager = new NewStarUserManager(getCategoryName());
    final PopUserManager popUserManager = new PopUserManager(getCategoryName());
    public final OpusForMatchManager opusForMatchManager = new OpusForMatchManager(getCategoryName());

	final AllTimeTopOpusManager allTimeTopOpusCnManager = new AllTimeTopOpusManager(getCategoryName()+CN_SUFFIX);
	final AllTimeTopOpusManager allTimeTopOpusEnManager = new AllTimeTopOpusManager(getCategoryName()+EN_SUFFIX);
	final HotTopOpusManager hotTopOpusCnManager = new HotTopOpusManager(getCategoryName()+CN_SUFFIX);
	final HotTopOpusManager hotTopOpusEnManager = new HotTopOpusManager(getCategoryName()+EN_SUFFIX);
	final FeatureOpusManager featureOpusCnManager = new FeatureOpusManager(getCategoryName()+CN_SUFFIX);
	final FeatureOpusManager featureOpusEnManager = new FeatureOpusManager(getCategoryName()+EN_SUFFIX);

    final VipUserOpusManager vipOpusManager = new VipUserOpusManager(getCategoryName());

    static final ConcurrentHashMap<String, FreePoolManager> freeNumberPoolMap = new ConcurrentHashMap<String, FreePoolManager>();
    static final Object freeNumberPoolMapLock = new Object();

//    final FreePoolManager freePoolManager = new FreePoolManager(getCategoryName());
	
	static final ConcurrentHashMap<String, ContestTopOpusManager> contestTopOpusMap = new ConcurrentHashMap<String, ContestTopOpusManager>();
	static final Object contestTopOpusMapLock = new Object();

    static final ConcurrentHashMap<String, ContestTopOpusManager> contestSpecialTopOpusMap = new ConcurrentHashMap<String, ContestTopOpusManager>();
    static final Object contestSpecialTopOpusMapLock = new Object();

	static final ConcurrentHashMap<String, ContestLatestOpusManager> contestLatestOpusMap = new ConcurrentHashMap<String, ContestLatestOpusManager>();
	static final Object contestLatestOpusMapLock = new Object();
	static final ConcurrentHashMap<String, MyContestOpusManager> myContestOpusMap = new ConcurrentHashMap<String, MyContestOpusManager>();
	static final Object myContestOpusMapLock = new Object();
	
	final DrawToUserOpusManager drawToUserOpusManager = new DrawToUserOpusManager(getCategoryName());
	final UserFavoriteOpusManager userFavoriteOpusManager = new UserFavoriteOpusManager(getCategoryName());
	final UserOpusManager userOpusManager = new UserOpusManager(getCategoryName());

    final OpusTimelineByCategoryManager opusTimelineCategoryManager = new OpusTimelineByCategoryManager(getCategoryName());
    final GuessOpusTimelineByCategoryManager guessTimelineCategoryManager = new GuessOpusTimelineByCategoryManager(getCategoryName());


    static final CommentTimelineManager commentTimelineManager = new CommentTimelineManager();
	static final OpusTimelineManager opusTimelineManager = new OpusTimelineManager();
	static final GuessOpusTimelineManager guessOpusTimelineManager = new GuessOpusTimelineManager();
	
	static final OpusActionManager opusActionFlowerManager = new OpusActionManager(ACTION_FLOWER, UserAction.COMMENT_TYPE_FLOWER);
	static final OpusActionManager opusActionGuessManager = new OpusActionManager(ACTION_GUESS, UserAction.COMMENT_TYPE_GUESS);
	static final OpusActionManager opusActionCommentManager = new OpusActionManager(ACTION_COMMENT, UserAction.COMMENT_TYPE_COMMENT);
	static final OpusActionManager opusActionCorrectManager = new OpusActionManager(ACTION_CORRECT, UserAction.COMMENT_TYPE_CORRECT);
    static final OpusActionManager opusActionContestCommentManager = new OpusActionManager(ACTION_CONTEST_COMMENT, UserAction.COMMENT_TYPE_CONTEST_COMMENT);
	static final OpusActionManager opusActionAllManager = new OpusActionManager(ACTION_ALL, UserAction.COMMENT_TYPE_ALL);

	static final UserGuessTimelineManager userGuessTimelineManager = new UserGuessTimelineManager();


    public final UserGuessOpusPlayManager userGuessOpusHappyManager = new UserGuessOpusPlayManager(getCategoryName(), UserGuessConstants.MODE_HAPPY, null, UserGuessConstants.MODE_HAPPY_MIN_DATA_LEN, false);
    public final UserGuessOpusPlayManager userGuessOpusGeniusManager = new UserGuessOpusPlayManager(getCategoryName(), UserGuessConstants.MODE_GENIUS, null, UserGuessConstants.MODE_GENIUS_MIN_DATA_LEN, true);
    static final ConcurrentHashMap<String, UserGuessOpusPlayManager> userGuessOpusContestMap = new ConcurrentHashMap<String, UserGuessOpusPlayManager>();
    static final Object userGuessOpusContestLock = new Object();
//    static final Object bbsBoardPostLock = new Object();


    public static BBSBoardPostManager getBBSBoardPostManagerByBoardId(String boardId)
    {
        return BBSBoardPostManager.managerForBoard(boardId);
    }


    public UserGuessOpusPlayManager getUserGuessOpusManager(int mode, String contestId){
        PBUserGuessMode guessMode = PBUserGuessMode.valueOf(mode);
        switch (guessMode){
            case GUESS_MODE_HAPPY:
                return userGuessOpusHappyManager;
            case GUESS_MODE_GENIUS:
                return userGuessOpusGeniusManager;
            case GUESS_MODE_CONTEST:
                return userGuessOpusContestManager(contestId);
            default:
                break;
        }

        return null;
    }

    final static String TIME_TYPE_DAY = "day";
    final static String TIME_TYPE_WEEK = "week";

//    public final TopUserGuessManager hotTopUserGuessHappyManager = new TopUserGuessManager(getCategoryName(), PBUserGuessMode.GUESS_MODE_HAPPY_VALUE, null, PBRankType.HOT_RANK_VALUE);
//    public final TopUserGuessManager hotTopUserGuessGeniusManager = new TopUserGuessManager(getCategoryName(), PBUserGuessMode.GUESS_MODE_GENIUS_VALUE, null, PBRankType.HOT_RANK_VALUE);
    static final ConcurrentHashMap<String, TopUserGuessManager> userGuessHotTopGeniusMap = new ConcurrentHashMap<String, TopUserGuessManager>();
    static final Object userGuessHotTopGeniusMapLock = new Object();
    public TopUserGuessManager userGuessHotTopGeniusManager(Date date, String timeType) {

        final String key = timeType + "_" + DateUtil.dateToChineseStringByFormat(date, "yyyyMMdd");

        synchronized (userGuessHotTopGeniusMapLock) {
            if (userGuessHotTopGeniusMap.containsKey(key)){
                return userGuessHotTopGeniusMap.get(key);
            }
            else{
                TopUserGuessManager manager = new TopUserGuessManager(getCategoryName(), PBUserGuessMode.GUESS_MODE_GENIUS_VALUE, null, PBRankType.HOT_RANK_VALUE, key);
                userGuessHotTopGeniusMap.putIfAbsent(key, manager);
                return userGuessHotTopGeniusMap.get(key);
            }
        }
    }

    public TopUserGuessManager getHotTopUserGuessManager(int mode) {
        PBUserGuessMode guessMode = PBUserGuessMode.valueOf(mode);
        switch (guessMode){

            case GUESS_MODE_HAPPY:
                return null; // hotTopUserGuessHappyManager;
            case GUESS_MODE_GENIUS:
            {
                Date now = new Date();
                return userGuessHotTopGeniusManager(now, TIME_TYPE_DAY);
            }
            default:
                break;
        }

        return null;
    }

    public final TopUserGuessManager allTimeTopUserGuessHappyManager = new TopUserGuessManager(getCategoryName(), PBUserGuessMode.GUESS_MODE_HAPPY_VALUE, null, PBRankType.ALL_TIME_RANK_VALUE, null);
    public final TopUserGuessManager allTimeTopUserGuessGeniusManager = new TopUserGuessManager(getCategoryName(), PBUserGuessMode.GUESS_MODE_GENIUS_VALUE, null, PBRankType.ALL_TIME_RANK_VALUE, null);

    public TopUserGuessManager getAllTimeTopUserGuessManager(int mode) {
        PBUserGuessMode guessMode = PBUserGuessMode.valueOf(mode);
        switch (guessMode){
            case GUESS_MODE_HAPPY:
                return allTimeTopUserGuessHappyManager;
            case GUESS_MODE_GENIUS:
                return allTimeTopUserGuessGeniusManager;
            default:
                break;
        }

        return null;
    }

    private static final ConcurrentHashMap<String, TopUserGuessManager> ContestTopUserGuessMap = new ConcurrentHashMap<String, TopUserGuessManager>();
    private static final Object ContestTopUserGuessMapLock = new Object();

    public TopUserGuessManager getContestTopGuessManager(String contestId) {
        return allTimeTopUserGuessContestManager(contestId);
    }

    public TopUserGuessManager getTopUserGuessManager(int mode, int type, String contestId){

        TopUserGuessManager manager = null;
        switch (mode){
            case OpusProtos.PBUserGuessMode.GUESS_MODE_CONTEST_VALUE:
                manager = getContestTopGuessManager(contestId);
                break;

            case OpusProtos.PBUserGuessMode.GUESS_MODE_HAPPY_VALUE:
            case OpusProtos.PBUserGuessMode.GUESS_MODE_GENIUS_VALUE:

                if (type == UserGuessConstants.HOT_RANK){
                    manager = getHotTopUserGuessManager(mode);
                }else{
                    manager = getAllTimeTopUserGuessManager(mode);
                }

                break;

            default:
                break;
        }

        return manager;
    }

    final ImageUploadManager imageUploadManager = new ImageUploadManager(OpusManager.getFileUploadLocalDir(getCategoryName()), OpusManager.getFileUploadRemoteDir(getCategoryName()));
	final ImageUploadManager dataUploadManager = new ImageUploadManager(DataService.getDataFileUploadLocalDir(getCategoryName()), DataService.getDataFileUploadRemoteDir(getCategoryName()));
    static final ImageUploadManager contestImageUploadManager = new ImageUploadManager(ContestService.getImageFileUploadLocalDir(), ContestService.getImageFileUploadRemoteDir());

	public TopUserManager topUserManager() {
		return topUserManager;
	}

    public NewStarUserManager newStarUserManager() {
        return newStarUserManager;
    }

    public  PopUserManager popUserManager()
    {
        return  popUserManager;
    }

	public AllTimeTopOpusManager allTimeTopOpusManager(int language){
		if (language == DBConstants.C_LANGUAGE_ENGLISH)
			return allTimeTopOpusEnManager;
		else
			return allTimeTopOpusCnManager;
	}
	
	public HotTopOpusManager hotTopOpusManager(int language) {
		if (language == DBConstants.C_LANGUAGE_ENGLISH) 
			return hotTopOpusEnManager;
		else
			return hotTopOpusCnManager;
	}

    public VipUserOpusManager vipUserOpusManager(){
        return vipOpusManager;
    }
	
	
	public FeatureOpusManager featureOpusManager(int language) {
		if (language == DBConstants.C_LANGUAGE_ENGLISH) 
			return featureOpusEnManager;
		else
			return featureOpusCnManager;
	}
	
	
	public ContestLatestOpusManager contestLatestOpusManager(String contestId) {
		synchronized (contestLatestOpusMapLock) {
			if (contestLatestOpusMap.containsKey(contestId)){
				return contestLatestOpusMap.get(contestId);
			}
			else{
				ContestLatestOpusManager manager = new ContestLatestOpusManager(contestId);
				contestLatestOpusMap.putIfAbsent(contestId, manager);
				return contestLatestOpusMap.get(contestId);
			}
		}
	}
	
	public ContestTopOpusManager contestTopOpusManager(String contestId) {
        synchronized (contestTopOpusMapLock) {
            if (contestTopOpusMap.containsKey(contestId)){
                return contestTopOpusMap.get(contestId);
            }
            else{
                ContestTopOpusManager manager = new ContestTopOpusManager(contestId);
                contestTopOpusMap.putIfAbsent(contestId, manager);
                return contestTopOpusMap.get(contestId);
            }
        }
    }

    public ContestTopOpusManager contestSpecialTopOpusManager(String contestId, int topType) {

        if (topType == DBConstants.DEFAULT_RANK_TYPE){
            return contestTopOpusManager(contestId);
        }

        synchronized (contestSpecialTopOpusMapLock) {
            String mapKey = contestId + String.valueOf(topType);
            if (contestSpecialTopOpusMap.containsKey(mapKey)){
                return contestSpecialTopOpusMap.get(mapKey);
            }
            else{
                ContestTopOpusManager manager = new ContestTopOpusManager(contestId, topType);
                contestSpecialTopOpusMap.putIfAbsent(mapKey, manager);
                return contestSpecialTopOpusMap.get(mapKey);
            }
        }
    }

    /*
    public TopUserGuessManager hotTopUserGuessContestManager(String contestId) {
        synchronized (hotTopUserGuessMapLock) {

            if (contestId == null || contestId.length() == 0){
                contestId = UserGuessUtil.getTodayContestId(getCategoryName());
            }

            if (hotTopUserGuessMap.containsKey(contestId)){
                return hotTopUserGuessMap.get(contestId);
            }
            else{
                TopUserGuessManager model = new TopUserGuessManager(getCategoryName(), PBUserGuessMode.GUESS_MODE_CONTEST_VALUE, contestId, PBRankType.HOT_RANK_VALUE);
                hotTopUserGuessMap.putIfAbsent(contestId, model);
                return hotTopUserGuessMap.get(contestId);
            }
        }
    }
    */


    public TopUserGuessManager allTimeTopUserGuessContestManager(String contestId) {
        synchronized (ContestTopUserGuessMapLock) {

            if (contestId == null || contestId.length() == 0){
                contestId = UserGuessUtil.getTodayContestId(getCategoryName());
            }

            if (ContestTopUserGuessMap.containsKey(contestId)){
                return ContestTopUserGuessMap.get(contestId);
            }
            else{
                TopUserGuessManager manager = new TopUserGuessManager(getCategoryName(), PBUserGuessMode.GUESS_MODE_CONTEST_VALUE, contestId, PBRankType.ALL_TIME_RANK_VALUE, null);
                ContestTopUserGuessMap.putIfAbsent(contestId, manager);
                return ContestTopUserGuessMap.get(contestId);
            }
        }
    }

    public UserGuessOpusPlayManager userGuessOpusContestManager(String contestId) {
        synchronized (userGuessOpusContestLock) {

            if (contestId == null || contestId.length() == 0){
                contestId = UserGuessUtil.getTodayContestId(getCategoryName());
            }

            if (userGuessOpusContestMap.containsKey(contestId)){
                return userGuessOpusContestMap.get(contestId);
            }
            else{
                UserGuessOpusPlayManager manager = new UserGuessOpusPlayManager(getCategoryName(), UserGuessConstants.MODE_CONTEST, contestId, UserGuessConstants.MODE_CONTEST_MIN_DATA_LEN, true);
                userGuessOpusContestMap.putIfAbsent(contestId, manager);
                return userGuessOpusContestMap.get(contestId);
            }
        }
    }
	
	public  MyContestOpusManager myContestOpusManager(String contestId) {
		synchronized (myContestOpusMapLock) {
			if (myContestOpusMap.containsKey(contestId)){
				return myContestOpusMap.get(contestId);
			}
			else{
				MyContestOpusManager manager = new MyContestOpusManager(contestId);
				myContestOpusMap.putIfAbsent(contestId, manager);
				return myContestOpusMap.get(contestId);
			}
		}
	}


    public OpusTimelineByCategoryManager opusTimelineByCategoryManager() {
        return opusTimelineCategoryManager;
    }

    public GuessOpusTimelineByCategoryManager guessOpusTimelineByCategoryManager() {
        return guessTimelineCategoryManager;
    }


    public DrawToUserOpusManager drawToUserOpusManager() {
		return drawToUserOpusManager;
	}
	
	public UserFavoriteOpusManager userFavoriteOpusManager() {
		return userFavoriteOpusManager;
	}
	
	public UserOpusManager userOpusManager() {
		return userOpusManager;
	}
	
	
	public CommentTimelineManager commentTimelineManager() {
		return commentTimelineManager;
	}
	
	public OpusTimelineManager opusTimelineManager() {
		return opusTimelineManager;
	}
	
	public GuessOpusTimelineManager guessOpusTimelineManager() {
		return guessOpusTimelineManager;
	}
	
	
	public OpusActionManager opusActionManager(int actionType) {
		if (actionType == UserAction.COMMENT_TYPE_COMMENT) {
			return opusActionCommentManager;
		}else if (actionType == UserAction.COMMENT_TYPE_FLOWER) {
			return opusActionFlowerManager;
		}else if (actionType == UserAction.COMMENT_TYPE_GUESS) {
			return opusActionGuessManager;
		}else if (actionType == UserAction.COMMENT_TYPE_CONTEST_COMMENT){
            return opusActionContestCommentManager;
        }
        else{
			return null;
		}
		
	}
	
	
	
	
	
	
	
	
	
	/*public static FriendManager getFriendfollowmanager() {
		return friendFollowManager;
	}

	public static FriendManager getFriendfansmanager() {
		return friendFansManager;
	}

	public static FriendManager getFriendblackmanager() {
		return friendBlackManager;
	}*/

	public OpusActionManager getOpusActionFlowerManager() {
		return opusActionFlowerManager;
	}

	public OpusActionManager getOpusActionGuessManager() {
		return opusActionGuessManager;
	}

	public OpusActionManager getOpusActionCommentManager() {
		return opusActionCommentManager;
	}

	public OpusActionManager getOpusActionCorrectManager() {
		return opusActionCorrectManager;
	}

    public OpusActionManager getOpusActionContestCommentManager() {
        return opusActionContestCommentManager;
    }

	public OpusActionManager getOpusActionAllManager() {
		return opusActionAllManager;
	}
	
	
	public UserGuessTimelineManager userGuessTimelineManager() {
		return userGuessTimelineManager;
	}

	public abstract LatestOpusManager latestOpusManager(String appId, int language);	
	public abstract String getCategoryName();
	public abstract int getCategoryType();

	public abstract boolean isZipUploadDataFile();
	public ImageUploadManager getImageUploadManager(){
		return imageUploadManager;
	}
	public abstract void setOpusInfo(Opus opus, PBOpus pbOpus);
	public ImageUploadManager getDataUploadManager(){
		return dataUploadManager;
	}


    public abstract void opusToPB(UserAction opus, PBOpus.Builder builder);



    public FreePoolManager getNumberFreePoolManager(int requestPoolType) {

        synchronized (freeNumberPoolMapLock) {

            String mapKey = getCategoryName()+"_"+String.valueOf(requestPoolType);

            if (freeNumberPoolMap.containsKey(mapKey)){
                return freeNumberPoolMap.get(mapKey);
            }
            else{
                FreePoolManager manager = new FreePoolManager(getCategoryName(), requestPoolType);
                freeNumberPoolMap.putIfAbsent(mapKey, manager);
                return freeNumberPoolMap.get(mapKey);
            }
        }
    }


    public FreePoolManager getNumberFreePoolManager(String number) {


        int requestPoolType = FreePoolManager.getTypeByNumber(number);
        if (requestPoolType == -1){
            return null;
        }

        synchronized (freeNumberPoolMapLock) {

            String mapKey = getCategoryName()+"_"+String.valueOf(requestPoolType);

            if (freeNumberPoolMap.containsKey(mapKey)){
                return freeNumberPoolMap.get(mapKey);
            }
            else{
                FreePoolManager manager = new FreePoolManager(getCategoryName(), requestPoolType);
                freeNumberPoolMap.putIfAbsent(mapKey, manager);
                return freeNumberPoolMap.get(mapKey);
            }
        }
    }

    public abstract double calculateAndSetHistoryScore(UserAction action);

    public abstract double calculateHotScore(UserAction action);

    public abstract boolean isOneUserOneOpusForLatest();

    public String getDrawToMeField(){
        return getCategoryName().toLowerCase()+"tome_count";
    }

    public static ImageUploadManager getContestImageUploadManager() {
        return contestImageUploadManager;
    }
}
