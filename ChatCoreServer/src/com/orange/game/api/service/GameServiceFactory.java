package com.orange.game.api.service;

import com.orange.common.api.service.CommonService;
import com.orange.common.api.service.CommonServiceFactory;
import com.orange.common.log.ServerLog;
import com.orange.game.api.service.bbs.*;
import com.orange.game.api.service.board.*;
import com.orange.game.api.service.bulletin.GetBulletinService;
import com.orange.game.api.barrage.common.CommonDataRequestService;
import com.orange.game.api.service.contest.*;
import com.orange.game.api.service.game.ClearUserGameStatusService;
import com.orange.game.api.service.group.group.*;
import com.orange.game.api.service.group.notice.*;
import com.orange.game.api.service.group.topic.*;
import com.orange.game.api.service.group.user.*;
import com.orange.game.api.service.guessopus.*;

import com.orange.game.api.service.learndraw.*;
import com.orange.game.api.service.message.*;
import com.orange.game.api.service.opus.*;
import com.orange.game.api.service.push.BoardcastPushMessageService;
import com.orange.game.api.service.room.*;
import com.orange.game.api.service.song.RandomGetSongService;
import com.orange.game.api.service.song.SearchSongService;
import com.orange.game.api.service.statistics.GetStatisticsService;
import com.orange.game.api.service.tutorial.UserTutorialActionService;
import com.orange.game.api.service.wall.CreateUserWallService;
import com.orange.game.api.service.wall.GetUserWallListService;
import com.orange.game.api.service.wall.GetUserWallService;
import com.orange.game.api.service.wall.UpdateUserWallService;
import com.orange.game.constants.ServiceConstant;

import java.util.HashMap;
import java.util.Map;


public class GameServiceFactory extends CommonServiceFactory {

    @Override
    public CommonService createServiceObjectByMethod(String methodName) {

        if (methodName == null)
            return null;

        return TrafficAPIService.getService(methodName);
    }

    // Add an enum instance whenever you add a new service.
    enum TrafficAPIService {
        UPDATE_ROOM(ServiceConstant.METHOD_UPDATE_ROOM) {
            Class<? extends CommonGameService> getHandler() {
                return UpdateRoomService.class;
            }
        },
        CREATE_ROOM(ServiceConstant.METHOD_CREATE_ROOM) {
            Class<? extends CommonGameService> getHandler() {
                return CreateRoomService.class;
            }
        },
        JOIN_ROOM(ServiceConstant.METHOD_JOIN_ROOM) {
            Class<? extends CommonGameService> getHandler() {
                return JoinRoomService.class;
            }
        },
        FIND_ROOM_BY_USER(ServiceConstant.METHOD_FIND_ROOM_BY_USER) {
            Class<? extends CommonGameService> getHandler() {
                return FindRoomByUserService.class;
            }
        },
        INVITE_USER(ServiceConstant.METHOD_INVITE_USER) {
            Class<? extends CommonGameService> getHandler() {
                return InviteUserService.class;
            }
        },
        SEARCH_ROOM(ServiceConstant.METHOD_SEARCH_ROOM) {
            Class<? extends CommonGameService> getHandler() {
                return SearchRoomService.class;
            }
        },
        REOMOVE_ROOM(ServiceConstant.METHOD_REOMOVE_ROOM) {
            Class<? extends CommonGameService> getHandler() {
                return RemoveRoomService.class;
            }
        },
        NEW_JOIN_ROOM(ServiceConstant.METHOD_NEW_JOIN_ROOM) {
            Class<? extends CommonGameService> getHandler() {
                return NewUserJoinRoomService.class;
            }
        },
        CREATE_OPUS(ServiceConstant.METHOD_CREATE_OPUS) {
            Class<? extends CommonGameService> getHandler() {
                return CreateOpusService.class;
            }
        },
        CREATE_OPUS_IMAGE(ServiceConstant.METHOD_CREATE_OPUS_IMAGE) {
            Class<? extends CommonGameService> getHandler() {
                return CreateOpusWithImageService.class;
            }
        },
        MATCH_OPUS(ServiceConstant.METHOD_MATCH_OPUS) {
            Class<? extends CommonGameService> getHandler() {
                return MatchOpusService.class;
            }
        },
        GET_MY_MESSAGE(ServiceConstant.METHOD_GET_MY_MESSAGE) {
            Class<? extends CommonGameService> getHandler() {
                return GetUserMessageService.class;
            }
        },
        SEND_MESSAGE(ServiceConstant.METHOD_SEND_MESSAGE) {
            Class<? extends CommonGameService> getHandler() {
                return SendMessageService.class;
            }
        },
        USER_READ_MESSAGE(ServiceConstant.METHOD_USER_READ_MESSAGE) {
            Class<? extends CommonGameService> getHandler() {
                return UserReadMessageService.class;
            }
        },
        ACTION_ON_OPUS(ServiceConstant.METHOD_ACTION_ON_OPUS) {
            Class<? extends CommonGameService> getHandler() {
                return ActionOnOpusService.class;
            }
        },
        GET_FEED_LIST(ServiceConstant.METHOD_GET_FEED_LIST) {
            Class<? extends CommonGameService> getHandler() {
                return GetFeedListService.class;
            }
        },
        GET_OPUST_COUNT(ServiceConstant.METHOD_GET_OPUST_COUNT) {
            Class<? extends CommonGameService> getHandler() {
                return GetOpustCountService.class;
            }
        },
        GET_FEED_COMMENT_LIST(ServiceConstant.METHOD_GET_FEED_COMMENT_LIST) {
            Class<? extends CommonGameService> getHandler() {
                return GetFeedCommentList.class;
            }
        },
        GET_STATISTICS(ServiceConstant.METHOD_GET_STATISTICS) {
            Class<? extends CommonGameService> getHandler() {
                return GetStatisticsService.class;
            }
        },
        DELETE_MESSAGE(ServiceConstant.METHOD_DELETE_MESSAGE) {
            Class<? extends CommonGameService> getHandler() {
                return DeleteMessageService.class;
            }
        },
        DELETE_FEED(ServiceConstant.METHOD_DELETE_FEED) {
            Class<? extends CommonGameService> getHandler() {
                return DeleteFeedService.class;
            }
        },
        GET_OPUS_BY_ID(ServiceConstant.METHOD_GET_OPUS_BY_ID) {
            Class<? extends CommonGameService> getHandler() {
                return GetDrawOpusByIdService.class;
            }
        },
        GET_BOARD_LIST(ServiceConstant.METHOD_GET_BOARD_LIST) {
            Class<? extends CommonGameService> getHandler() {
                return GetBoardListService.class;
            }
        },
        GET_OUPS_TIMES(ServiceConstant.METHOD_GET_OPUS_TIMES) {
            Class<? extends CommonGameService> getHandler() {
                return GetOpusTimesService.class;
            }
        },
        GET_CONTEST_LIST(ServiceConstant.METHOD_GET_CONTEST_LIST) {
            Class<? extends CommonGameService> getHandler() {
                return GetContestListService.class;
            }
        },
        GET_MY_COMMENT_LIST(ServiceConstant.METHOD_GET_MY_COMMENT_LIST) {
            Class<? extends CommonGameService> getHandler() {
                return GetMyCommentListService.class;
            }
        },
        GET_CONTEST_OPUS_LIST(ServiceConstant.METHOD_GET_CONTEST_OPUS_LIST) {
            Class<? extends CommonGameService> getHandler() {
                return GetContestOpusListService.class;
            }
        },
        UPDATE_OPUS(ServiceConstant.METHOD_UPDATE_OPUS) {
            Class<? extends CommonGameService> getHandler() {
                return UpdateOpusService.class;
            }
        },
        GET_TOP_OPUS_FOR_WEIBO(ServiceConstant.METHOD_GET_TOP_OPUS_FOR_WEIBO) {
            Class<? extends CommonGameService> getHandler() {
                return GetTopDrawForWeiboService.class;
            }
        },
        UPDATE_BOARD_STATISTIC(ServiceConstant.METHOD_UPDATE_BOARD_STATISTIC) {
            Class<? extends CommonGameService> getHandler() {
                return UpdateBoardStatisticService.class;
            }
        },
        GET_MESSAGE_STAT_LIST(ServiceConstant.METHOD_GET_MESSAGE_STAT_LIST) {
            Class<? extends CommonGameService> getHandler() {
                return GetMessageStatListService.class;
            }
        },
        GET_MESSAGE_LIST(ServiceConstant.METHOD_GET_MESSAGE_LIST) {
            Class<? extends CommonGameService> getHandler() {
                return GetMessageListService.class;
            }
        },
        CLEAR_USER_GAME_STATUS(ServiceConstant.METHOD_CLEAR_USER_GAME_STATUS) {
            Class<? extends CommonGameService> getHandler() {
                return ClearUserGameStatusService.class;
            }
        },
        GET_BBS_BOARD_LIST(ServiceConstant.METHOD_GET_BBS_BOARD_LIST) {
            Class<? extends CommonGameService> getHandler() {
                return GetBBSBoardListService.class;
            }
        },
        CREATE_BBS_POST(ServiceConstant.METHOD_CREATE_BBS_POST) {
            Class<? extends CommonGameService> getHandler() {
                return CreateBBSPostService.class;
            }
        },
        CREATE_BBS_ACTION(ServiceConstant.METHOD_CREATE_BBS_ACTION) {
            Class<? extends CommonGameService> getHandler() {
                return CreateBBSActionService.class;
            }
        },
        GET_BBS_POST_LIST(ServiceConstant.METHOD_GET_BBS_POST_LIST) {
            Class<? extends CommonGameService> getHandler() {
                return GetBBSPostListService.class;
            }
        },
        GET_CONTEST_TOP_OPUS(ServiceConstant.METHOD_GET_CONTEST_TOP_OPUS) {
            Class<? extends CommonGameService> getHandler() {
                return GetContestTopDrawForWeiboService.class;
            }
        },
        GET_BBS_ACTION_LIST(ServiceConstant.METHOD_GET_BBS_ACTION_LIST) {
            Class<? extends CommonGameService> getHandler() {
                return GetBBSActionListService.class;
            }
        },
        DELETE_BBS_POST(ServiceConstant.METHOD_DELETE_BBS_POST) {
            Class<? extends CommonGameService> getHandler() {
                return DeleteBBSPostService.class;
            }
        },
        DELETE_BBS_ACTION(ServiceConstant.METHOD_DELETE_BBS_ACTION) {
            Class<? extends CommonGameService> getHandler() {
                return DeleteBBSActionService.class;
            }
        },
        GET_BBS_POST(ServiceConstant.METHOD_GET_BBS_POST) {
            Class<? extends CommonGameService> getHandler() {
                return GetBBSPostByIdService.class;
            }
        },
        GET_BBS_DRAWDATA(ServiceConstant.METHOD_GET_BBS_DRAWDATA) {
            Class<? extends CommonGameService> getHandler() {
                return GetBBSDrawDataService.class;
            }
        },
        PAY_BBS_REWARD(ServiceConstant.METHOD_PAY_BBS_REWARD) {
            Class<? extends CommonGameService> getHandler() {
                return PayBBSRewardService.class;
            }
        },
        EDIT_BBS_POST(ServiceConstant.METHOD_EDIT_BBS_POST) {
            Class<? extends CommonGameService> getHandler() {
                return EditBBSPostService.class;
            }
        },
        EDIT_BBS_POST_TEXT(ServiceConstant.METHOD_EDIT_BBS_POST_TEXT) {
            Class<? extends CommonGameService> getHandler() {
                return EditBBSPostTextService.class;
            }
        },
        CHANGE_BBS_ROLE(ServiceConstant.METHOD_CHANGE_BBS_ROLE) {
            Class<? extends CommonGameService> getHandler() {
                return ChangeBBSUserRoleService.class;
            }
        },
        MARK_POST(ServiceConstant.METHOD_MARK_POST) {
            Class<? extends CommonGameService> getHandler() {
                return MarkPostService.class;
            }
        },
        UNMARKPOST(ServiceConstant.METHOD_UNMARK_POST) {
            Class<? extends CommonGameService> getHandler() {
                return UnmarkPostService.class;
            }
        },
        GET_MARKEDPOSTS(ServiceConstant.METHOD_GET_MARKED_POSTS) {
            Class<? extends CommonGameService> getHandler() {
                return GetMarkedPostsService.class;
            }
        },
        SEARCH_POSTS(ServiceConstant.METHOD_SEARCH_POST) {
            Class<? extends CommonGameService> getHandler() {
                return FindBBSPostByKeyWords.class;
            }
        },
        GET_BBS_PRIVILEGE_LIST(ServiceConstant.METHOD_GET_BBS_PRIVILEGE_LIST) {
            Class<? extends CommonGameService> getHandler() {
                return GetBBSUserPrivilegeListService.class;
            }
        },
        GET_BULLETINS(ServiceConstant.METHOD_GET_BULLETINS) {
            Class<? extends CommonGameService> getHandler() {
                return GetBulletinService.class;
            }
        },
        CREATE_USER_WALL(ServiceConstant.METHOD_CREATE_USER_WALL) {
            Class<? extends CommonGameService> getHandler() {
                return CreateUserWallService.class;
            }
        },
        GET_USER_WALL(ServiceConstant.METHOD_GET_USER_WALL) {
            Class<? extends CommonGameService> getHandler() {
                return GetUserWallService.class;
            }
        },
        GET_USER_WALL_LIST(ServiceConstant.METHOD_GET_USER_WALL_LIST) {
            Class<? extends CommonGameService> getHandler() {
                return GetUserWallListService.class;
            }
        },
        UPDATE_USER_WALL(ServiceConstant.METHOD_UPDATE_USER_WALL) {
            Class<? extends CommonGameService> getHandler() {
                return UpdateUserWallService.class;
            }
        },
        CONSTRUCT_INDEX(ServiceConstant.METHOD_CONSTRUCT_INDEX) {
            Class<? extends CommonGameService> getHandler() {
                return ConstructIndexService.class;
            }
        },
        RECOVER_USER_OPUS(ServiceConstant.METHOD_RECOVER_USER_OPUS) {
            Class<? extends CommonGameService> getHandler() {
                return RecoverUserOpus.class;
            }
        },

        RECREATE_USER_OPUS(ServiceConstant.METHOD_RECREATE_USER_OPUS) {
            Class<? extends CommonGameService> getHandler() {
                return RecreateUserOpusService.class;
            }
        },


        // Learn Draw

        GET_LEARNDRAW_LIST(ServiceConstant.METHOD_GET_LEARNDRAW_LIST) {
            Class<? extends CommonGameService> getHandler() {
                return GetLearnDrawOpusListService.class;
            }
        },
        REMOVE_LEARNDRAW(ServiceConstant.METHOD_REMOVE_LEARNDRAW) {
            Class<? extends CommonGameService> getHandler() {
                return RemoveLearnDrawService.class;
            }
        },

        GET_USER_LEARNDRAW_LIST(ServiceConstant.METHOD_GET_USER_LEARNDRAW_LIST) {
            Class<? extends CommonGameService> getHandler() {
                return GetBoughtLearnDrawOpusListService.class;
            }
        },
        GET_USER_LEARDRAWID_LIST(
                ServiceConstant.METHOD_GET_USER_LEARNDRAWID_LIST) {
            Class<? extends CommonGameService> getHandler() {
                return GetAllBoughtLearnDrawIdList.class;
            }
        },
        BUY_LEARN_DRAW(ServiceConstant.METHOD_BUY_LEARN_DRAW) {
            Class<? extends CommonGameService> getHandler() {
                return BuyLearnDrawService.class;
            }
        },
        ADD_LEARN_DRAW(ServiceConstant.METHOD_ADD_LEARN_DRAW) {
            Class<? extends CommonGameService> getHandler() {
                return AddLearnDrawService.class;
            }
        },
        NEW_CREATE_OPUS(ServiceConstant.METHOD_SUBMIT_OPUS) {
            Class<? extends CommonGameService> getHandler() {
                return NewCreateOpusService.class;
            }
        },
        RANDOM_GET_SONGS(ServiceConstant.METHOD_RANDOM_GET_SONGS) {
            Class<? extends CommonGameService> getHandler() {
                return RandomGetSongService.class;
            }
        },

        GET_USER_GUESS_OPUS(ServiceConstant.METHOD_GET_USER_GUESS_OPUS) {
            Class<? extends CommonGameService> getHandler() {
                return GetUserGuessOpusService.class;
            }
        },
        GUESS_OPUS(ServiceConstant.METHOD_NEW_GUESS_OPUS) {
            Class<? extends CommonGameService> getHandler() {
                return GuessOpusService.class;
            }
        },
        GET_USER_GUESS_RANK(ServiceConstant.METHOD_GET_USER_GUESS_RANK) {
            Class<? extends CommonGameService> getHandler() {
                return GetUserGuessRankService.class;
            }
        },
        GET_GUESS_RANK_LIST(ServiceConstant.METHOD_GET_GUESS_RANK_LIST) {
            Class<? extends CommonGameService> getHandler() {
                return GetUserGuessRankListService.class;
            }
        },
        GET_GUESS_CONTEST_LIST(ServiceConstant.METHOD_GET_GUESS_CONTEST_LIST) {
            Class<? extends CommonGameService> getHandler() {
                return GetGuessContestListInfo.class;
            }
        },
        GET_RECENT_GUESS_CONTEST_LIST(ServiceConstant.METHOD_GET_RECENT_GUESS_CONTEST_LIST) {
            Class<? extends CommonGameService> getHandler() {
                return GetRecentGuessContestListInfo.class;
            }
        },
        DELETE_SINGLE_MESSAGE(ServiceConstant.METHOD_DELETE_SINGLE_MESSAGE) {
            Class<? extends CommonGameService> getHandler() {
                return DeleteSingleMessageService.class;
            }
        },
        NEW_GET_MESSAGE_LIST(ServiceConstant.METHOD_NEW_GET_MESSAGE_LIST) {
            Class<? extends CommonGameService> getHandler() {
                return NewGetMessageListService.class;
            }
        },
        SET_OPUS_HOT_SCORE_SERVICE(ServiceConstant.METHOD_SET_OPUS_HOT_SCORE) {
            Class<? extends CommonGameService> getHandler() {
                return SetOpusHotScoreService.class;
            }
        },
        RANK_OPUS_SERVICE(ServiceConstant.METHOD_RANK_OPUS) {
            Class<? extends CommonGameService> getHandler() {
                return RankOpusService.class;
            }
        },
        GENERATE_CONTEST_RESULT_SERVICE(ServiceConstant.METHOD_GENERATE_CONTEST_RESULT) {
            Class<? extends CommonGameService> getHandler() {
                return GenerateContestResultService.class;
            }
        },
        GENERATE_GUESS_CONTEST_OPUS_POOL_SERVICE(ServiceConstant.METHOD_GENERATE_GUESS_CONTEST_OPUS_POOL) {
            Class<? extends CommonGameService> getHandler() {
                return GenerateGuessContestOpusPool.class;
            }
        },
        CLEAR_GUESS_CONTEST_OPUS_POOL_SERVICE(ServiceConstant.METHOD_CLEAR_GUESS_CONTEST_OPUS_POOL) {
            Class<? extends CommonGameService> getHandler() {
                return ClearGuessContestOpusPoolService.class;
            }
        },
        RECALCULATE_SCORE_SERVICE(ServiceConstant.METHOD_RECALCULATE_SCORE) {
            Class<? extends CommonGameService> getHandler() {
                return RecalculateScoreService.class;
            }
        },
        SEARCH_SONG_SERVICE(ServiceConstant.METHOD_SEARCH_SONG) {
            Class<? extends CommonGameService> getHandler() {
                return SearchSongService.class;
            }
        },
        EXPORT_OPUS_SERVICE(ServiceConstant.METHOD_EXPORT_OPUS) {
            Class<? extends CommonGameService> getHandler() {
                return ExportOpusService.class;
            }
        },

        CREATE_GROUP_SERVICE(ServiceConstant.METHOD_CREATE_GROUP) {
            Class<? extends CommonGameService> getHandler() {
                return CreateGroupService.class;
            }
        },
        GET_GROUP_DETAIL_SERVICE(ServiceConstant.METHOD_GET_GROUP) {
            Class<? extends CommonGameService> getHandler() {
                return GetGroupDetailService.class;
            }
        },
        JOIN_GROUP_SERVICE(ServiceConstant.METHOD_JOIN_GROUP) {
            Class<? extends CommonGameService> getHandler() {
                return JoinGroupService.class;
            }
        },
        QUIT_GROUP_SERVICE(ServiceConstant.METHOD_QUIT_GROUP) {
            Class<? extends CommonGameService> getHandler() {
                return QuitGroupService.class;
            }
        },
        HANDLE_GROUPS_JOIN_REQUEST_SERVICE(ServiceConstant.METHOD_HANDLE_JOIN_REQUEST) {
            Class<? extends CommonGameService> getHandler() {
                return HandleJoinGroupRequestService.class;
            }
        },
        INVITE_GROUP_MEMBERS_SERVICE(ServiceConstant.METHOD_INVITE_GROUPMEMBERS) {
            Class<? extends CommonGameService> getHandler() {
                return InviteGroupUsersService.class;
            }
        },
        INVITE_GROUP_GUESTS_SERVICE(ServiceConstant.METHOD_INVITE_GROUPGUESTS) {
            Class<? extends CommonGameService> getHandler() {
                return InviteGroupGuestsService.class;
            }
        },
        EDIT_GROUP_SERVICE(ServiceConstant.METHOD_EDIT_GROUP) {
            Class<? extends CommonGameService> getHandler() {
                return EditGroupService.class;
            }
        },
        EXPEL_GROUPUSER_SERVICE(ServiceConstant.METHOD_EXPEL_GROUPUSER) {
            Class<? extends CommonGameService> getHandler() {
                return ExpelGroupUserService.class;
            }
        },

        SET_USER_AS_ADMIN_SERVICE(ServiceConstant.METHOD_SET_USER_AS_ADMIN) {
            Class<? extends CommonGameService> getHandler() {
                return SetUserAsAdminService.class;
            }
        },

        REMOVE_USER_FROM_ADMIN_SERVICE(ServiceConstant.METHOD_REMOVE_USER_FROM_ADMIN) {
            Class<? extends CommonGameService> getHandler() {
                return RemoveUserFromAdminService.class;
            }
        },

        //////
        FOLLOW_GROUP_SERVICE(ServiceConstant.METHOD_FOLLOW_GROUP) {
            Class<? extends CommonGameService> getHandler() {
                return FollowGroupService.class;
            }
        },
        UNFOLLOW_GROUP_SERVICE(ServiceConstant.METHOD_UNFOLLOW_GROUP) {
            Class<? extends CommonGameService> getHandler() {
                return UnfollowGroupService.class;
            }
        },
        SYNC_FOLLOWED_GROUPIDS_SERVICE(ServiceConstant.METHOD_SYNC_FOLLOWED_GROUPIDS) {
            Class<? extends CommonGameService> getHandler() {
                return SyncFollowGroupIdsService.class;
            }
        },

        SYNC_TOPICIDS_SERVICE(ServiceConstant.METHOD_SYNC_FOLLOWED_TOPICIDS) {
            Class<? extends CommonGameService> getHandler() {
                return SyncFollowTopicIdsService.class;
            }
        },

        GET_GROUPS_SERVICE(ServiceConstant.METHOD_GET_GROUPS) {
            Class<? extends CommonGameService> getHandler() {
                return GetGroupsService.class;
            }
        },
        UPGRADE_GROUP_SERVICE(ServiceConstant.METHOD_UPGRADE_GROUP) {
            Class<? extends CommonGameService> getHandler() {
                return UpgradeGroupService.class;
            }
        },
        GET_GROUP_NOTICES_SERVICE(ServiceConstant.METHOD_GET_GROUP_NOTICES) {
            Class<? extends CommonGameService> getHandler() {
                return GetGroupNoticesService.class;
            }
        },

        IGNORE_GROUP_NOTICE_SERVICE(ServiceConstant.METHOD_IGNORE_NOTICE) {
            Class<? extends CommonGameService> getHandler() {
                return IgnoreNoticeService.class;
            }
        },
        FOLLOW_TOPIC_SERVICE(ServiceConstant.METHOD_FOLLOW_TOPIC) {
            Class<? extends CommonGameService> getHandler() {
                return FollowTopicService.class;
            }
        },
        UNFOLLOW_TOPIC_SERVICE(ServiceConstant.METHOD_UNFOLLOW_TOPIC) {
            Class<? extends CommonGameService> getHandler() {
                return UnfollowTopicService.class;
            }
        },

        GET_TOPIC_TIMELINE_SERVICE(ServiceConstant.METHOD_GET_TOPIC_TIMELINE) {
            Class<? extends CommonGameService> getHandler() {
                return GetTopicTimelineService.class;
            }
        },
        GET_FOLLOWED_TOPICS_SERVICE(ServiceConstant.METHOD_GET_FOLLOWED_TOPIC) {
            Class<? extends CommonGameService> getHandler() {
                return GetFollowedTopicsService.class;
            }
        },
        SEARCH_GROUP_SERVICE(ServiceConstant.METHOD_SEARCH_GROUP) {
            Class<? extends CommonGameService> getHandler() {
                return SearchGroupService.class;
            }
        },
        GET_GROUP_BADGES_SERVICE(ServiceConstant.METHOD_GET_GROUP_BADGES) {
            Class<? extends CommonGameService> getHandler() {
                return GetGroupBadgeService.class;
            }
        },
        GET_POSTACTION_BYUSER_SERVICE(ServiceConstant.METHOD_GET_POST_ACTION_BY_USER) {
            Class<? extends CommonGameService> getHandler() {
                return GetPostActionByUserService.class;
            }
        },
        CHANGE_USER_TITLE_SERVICE(ServiceConstant.METHOD_CHANGE_USER_TITLE) {
            Class<? extends CommonGameService> getHandler() {
                return ChangeUserTitleService.class;
            }
        },
        DELETE_GROUP_TITLE(ServiceConstant.METHOD_DELETE_GROUP_TITLE) {
            Class<? extends CommonGameService> getHandler() {
                return DeleteGroupTitleService.class;
            }
        },
        GET_USERS_BYTITLE_SERVICE(ServiceConstant.METHOD_GET_USERS_BYTITLE) {
            Class<? extends CommonGameService> getHandler() {
                return GetUsersByTitleListService.class;
            }
        },
        CREATE_GROUP_TITLE_SERVICE(ServiceConstant.METHOD_CREATE_GROUP_TITLE) {
            Class<? extends CommonGameService> getHandler() {
                return CreateGroupTitleService.class;
            }
        },
        UPDATE_GROUP_TITLE_SERVICE(ServiceConstant.METHOD_UPDATE_GROUP_TITLE) {
            Class<? extends CommonGameService> getHandler() {
                return UpdateTitleNameService.class;
            }
        },
        ACCEPT_INVITATION_SERVICE(ServiceConstant.METHOD_ACCEPT_INVITATION) {
            Class<? extends CommonGameService> getHandler() {
                return AcceptInvitationService.class;
            }
        },
        REJECT_INVITATION_SERVICE(ServiceConstant.METHOD_REJECT_INVITATION) {
            Class<? extends CommonGameService> getHandler() {
                return RejectInvitationService.class;
            }
        },
        UPDATE_GROUP_ICON_SERVICE(ServiceConstant.METHOD_UPDATE_GROUP_ICON) {
            Class<? extends CommonGameService> getHandler() {
                return UpdateGroupIconService.class;
            }
        },
        SYNC_GROUP_ROLES(ServiceConstant.METHOD_SYNC_GROUP_ROLES) {
            Class<? extends CommonGameService> getHandler() {
                return SyncGroupRolesService.class;
            }
        },
        UPDATE_GROUP_BG_SERVICE(ServiceConstant.METHOD_UPDATE_GROUP_BG) {
            Class<? extends CommonGameService> getHandler() {
                return UpdateGroupBGImage.class;
            }
        },
        DISMISSAL_GROUP_SERVICE(ServiceConstant.METHOD_DISMISSAL_GROUP) {
            Class<? extends CommonGameService> getHandler() {
                return DismissalGroupService.class;
            }
        },
        CREATE_CONTEST_SERVICE(ServiceConstant.METHOD_CREATE_CONTEST) {
            Class<? extends CommonGameService> getHandler() {
                return CreateContestService.class;
            }
        },
        UPDATE_CONTEST_SERVICE(ServiceConstant.METHOD_UPDATE_CONTEST) {
            Class<? extends CommonGameService> getHandler() {
                return UpdateContestService.class;
            }
        },
        GET_SIMPLE_GROUP_SERVICE(ServiceConstant.METHOD_GET_SIMPLE_GROUP){
            Class<? extends CommonGameService> getHandler() {
                return GetSimpleGroupService.class;
            }
        },
        CHARGE_GROUP_BALANCE_SERVICE(ServiceConstant.METHOD_CHARGE_GROUP){
            Class<? extends CommonGameService> getHandler() {
                return ChargeGroupBalance.class;
            }
        },
        GET_CHARGE_GROUP_HISTORY(ServiceConstant.METHOD_GET_GROUP_CHARGE_HISTORIES){
            Class<? extends CommonGameService> getHandler() {
                return GetChargeHistoryService.class;
            }
        },
        IGNORE_REQUEST_SERVICE(ServiceConstant.METHOD_IGNORE_ALL_REQUEST_NOTICE){
            Class<? extends CommonGameService> getHandler() {
                return IgnoreAllRequestService.class;
            }
        }
        ,
        SET_USER_OFF_GROUP_SERVICE(ServiceConstant.METHOD_SET_USER_OFF_GROUP){
            Class<? extends CommonGameService> getHandler() {
                return SetUserGroupMessageNoticeService.class;
            }
        },
        GET_TOPICS_SERVICE(ServiceConstant.METHOD_GET_TOPICS){
            Class<? extends CommonGameService> getHandler() {
                return GetTopicsService.class;
            }
        },
        SET_OPUS_TARGET_USER_SERVICE(ServiceConstant.METHOD_SET_OPUS_TARGET_USER){
            Class<? extends CommonGameService> getHandler() {
                return SetOpusTargetUserService.class;
            }
        },
        CREATE_BBS_BOARD_SERVICE(ServiceConstant.METHOD_CREATE_BOARD){
            Class<? extends CommonGameService> getHandler() {
                return CreateBoardService.class;
            }
        },
        UPDATE_BBS_BOARD_SERVICE(ServiceConstant.METHOD_UPDATE_BOARD){
            Class<? extends CommonGameService> getHandler() {
                return UpdateBoardService.class;
            }
        },
        DELETE_BBS_BOARD_SERVICE(ServiceConstant.METHOD_DELETE_BOARD){
            Class<? extends CommonGameService> getHandler() {
                return DeleteBoardService.class;
            }
        },
        FORBID_USER_BOARD_SERVICE(ServiceConstant.METHOD_FORBID_USER_BOARD){
            Class<? extends CommonGameService> getHandler() {
                return ForbidUserBoardService.class;
            }
        },
        USER_TUTORIAL_ACTION_SERVICE(ServiceConstant.METHOD_USER_TUTORIAL_ACTION){
            Class<? extends CommonGameService> getHandler() {
                return UserTutorialActionService.class;
            }
        }
        ,
        SET_OPUS_CLASS_SERVICE(ServiceConstant.METHOD_SET_OPUS_CLASS){
            Class<? extends CommonGameService> getHandler() {
                return SetOpusClassService.class;
            }
        }
        ,
        GET_OPUS_LIST_FOR_ADMIN_SERVICE(ServiceConstant.METHOD_GET_OPUS_LIST_FOR_ADMIN){
            Class<? extends CommonGameService> getHandler() {
                return GetOpusListForAdminService.class;
            }
        },
        BOARDCAST_PUSH_MESSAGE_SERVICE(ServiceConstant.METHOD_PUSH_MESSAGE){
            Class<? extends CommonGameService> getHandler() {
                return BoardcastPushMessageService.class;
            }
        },

        GET_BBS_POST_BY_TUTORIAL_ID_SERVICE(ServiceConstant.METHOD_GET_STAGE_POST_ID) {
            Class<? extends CommonGameService> getHandler() {
                return GetBBSPostByTutorialIdService.class;}
        },

        RECOVER_DELETED_POST_SERVICE(ServiceConstant.METHOD_RECOVER_DELETED_POST){
            Class<? extends CommonGameService> getHandler() {
                return RecoverDeletedPostService.class;
            }
        },
        RECREATE_BOARD_POST(ServiceConstant.METHOD_RECREATE_POST){
            Class<? extends CommonGameService> getHandler() {
                return RecreateBBSBoardPostService.class;
            }
        },

        COMMON_DATA_REQUEST(ServiceConstant.METHOD_COMMON_DATA_REQUEST){
            Class<? extends CommonGameService> getHandler() {
                return CommonDataRequestService.class;
            }
        }



        ;
        private static Map<String, Class<? extends CommonGameService>> serviceTable = new HashMap<String, Class<? extends CommonGameService>>();
        private final String methodName;

        private TrafficAPIService(String methodName) {
            this.methodName = methodName;
        }

        static {
            ServerLog.info(0, "<GameServiceFactory> Registering all service routines...");
            for (TrafficAPIService service : TrafficAPIService.values()) {
                serviceTable.put(service.methodName.toLowerCase(), service.getHandler());
            }
        }

        private static CommonGameService getService(String methodName) {

            CommonGameService result = null;
            try {
                result = serviceTable.get(methodName.toLowerCase()).newInstance();
            } catch (InstantiationException e) {
                ServerLog.info(0, "<GameServiceFactory> Instantiating service :" + methodName
                        + " fails. Please check if you give an invalid name.");
                result = null;
            } catch (IllegalAccessException e) {
                ServerLog.info(0, "<GameServiceFactory> Instantiating service :" + methodName
                        + " fails. Maybe you have no access permission ?!");
                result = null;
            }

            return result;
        }

        // Enum-specified method, override it whenever you add an enum instance.
        abstract Class<? extends CommonGameService> getHandler();
    }

}
