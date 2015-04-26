package com.orange.game.api.service;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mongodb.BasicDBObject;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.common.utils.DateUtil;
import com.orange.common.utils.StringUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.common.ProtoBufCoding;
import com.orange.game.model.dao.*;
import com.orange.game.model.dao.Contest.ContestLimit;
import com.orange.game.model.dao.bbs.*;
import com.orange.game.model.dao.common.IntKeyValue;
import com.orange.game.model.dao.common.UserAward;
import com.orange.game.model.dao.group.Group;
import com.orange.game.model.dao.group.GroupUsersByTitle;
import com.orange.game.model.dao.opus.Opus;
import com.orange.game.model.dao.opus.UserGuess;
import com.orange.game.model.dao.opus.UserGuessAchievement;
import com.orange.game.model.dao.song.Song;
import com.orange.game.model.dao.tutorial.UserTutorial;
import com.orange.game.model.dao.wall.UserWall;
import com.orange.game.model.manager.ContestManager;
import com.orange.game.model.manager.OpusManager;
import com.orange.game.model.manager.UserManager;
import com.orange.game.model.manager.bbs.BBSManager;
import com.orange.game.model.manager.group.GroupManager;
import com.orange.game.model.manager.group.GroupUserManager;
import com.orange.game.model.manager.guessopus.AwardManager;
import com.orange.game.model.manager.guessopus.TopUserGuessManager;
import com.orange.game.model.manager.guessopus.UserGuessUtil;
import com.orange.game.model.service.CreateDataFileService;
import com.orange.game.model.service.DBService;
import com.orange.game.model.service.DataService;
import com.orange.game.model.xiaoji.AbstractXiaoji;
import com.orange.game.model.xiaoji.XiaojiSing;
import com.orange.network.game.protocol.constants.GameConstantsProtos;
import com.orange.network.game.protocol.message.GameMessageProtos;
import com.orange.network.game.protocol.message.GameMessageProtos.DataQueryResponse;
import com.orange.network.game.protocol.model.*;
import com.orange.network.game.protocol.model.BBSProtos.*;
import com.orange.network.game.protocol.model.DrawProtos.*;
import com.orange.network.game.protocol.model.GameBasicProtos.PBMessage;
import com.orange.network.game.protocol.model.GameBasicProtos.PBMessageStat;
import com.orange.network.game.protocol.model.SingProtos.PBSong;
import com.orange.network.game.protocol.model.SingProtos.PBSongList;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;

import java.util.*;

public class CommonServiceUtils {

    private static Logger logger = Logger.getLogger("CommonServiceUtils");
    private static String SPLIT_STRING = ":";

    private static void safePut(JSONObject object, String key, Object value) {
        if (value == null)
            return;

        object.put(key, value);
    }

    public static Set<String> parseTextList(String textList) {
        if (StringUtil.isEmpty(textList)) {
            return null;
        }
        String[] users = textList.split(SPLIT_STRING);
        HashSet<String> set = new HashSet<String>();
        Collections.addAll(set, users);
//        for (int i = 0; i < users.length; i++) {
//            set.add(users[i]);
//        }
        return set;
    }

    public static Object roomListToJSON(List<Room> list) {
        JSONArray objList = new JSONArray();
        for (Room room : list) {
            JSONObject object = (JSONObject) roomToJSON(room);
            objList.add(object);
        }
        return objList;
    }

    public static Object roomToJSON(Room room) {
        JSONObject object = new JSONObject();
        safePut(object, ServiceConstant.PARA_ROOM_ID, room.getRoomId());
        safePut(object, ServiceConstant.PARA_ROOM_NAME, room.getRoomName());
        safePut(object, ServiceConstant.PARA_CREATE_USERID, room
                .getCreatorUserId());
        safePut(object, ServiceConstant.PARA_NICKNAME, room
                .getCreatorNickName());
        safePut(object, ServiceConstant.PARA_SERVER_ADDRESS, room
                .getServerAddress());
        safePut(object, ServiceConstant.PARA_SERVER_PORT, room.getServerPort());
        safePut(object, ServiceConstant.PARA_PASSWORD, room.getPassword());
        safePut(object, ServiceConstant.PARA_STATUS, room.getStatus());

        if (room.getCreateDate() != null) {
            safePut(object, ServiceConstant.PARA_CREATE_DATE, DateUtil
                    .dateToString(room.getCreateDate()));
        }

        if (room.getExpireDate() != null) {
            safePut(object, ServiceConstant.PARA_EXPIRE_DATE, DateUtil
                    .dateToString(room.getExpireDate()));
        }

        List<RoomUser> users = room.getRoomUsers();
        JSONArray userArray = new JSONArray();
        for (RoomUser user : users) {
            JSONObject userObj = new JSONObject();
            safePut(userObj, ServiceConstant.PARA_USERID, user.getUserId());
            safePut(userObj, ServiceConstant.PARA_NICKNAME, user.getnickName());
            safePut(userObj, ServiceConstant.PARA_GENDER, user.getGender());
            safePut(userObj, ServiceConstant.PARA_AVATAR, user.getAvatar());
            safePut(userObj, ServiceConstant.PARA_STATUS, user.getStatus());

            if (user.getLastPlayDate() != null) {
                safePut(userObj, ServiceConstant.PARA_LAST_PLAY_DATE, DateUtil
                        .dateToString(user.getLastPlayDate()));
            }

            safePut(userObj, ServiceConstant.PARA_PLAY_TIMES, user
                    .getPlayTimes());
            userArray.add(userObj);
        }
        safePut(object, ServiceConstant.PAPA_ROOM_USERS, userArray);
        return object;
    }

    public static byte[] protocolBufferErrorNoData(int errorCode) {
        DataQueryResponse response = GameMessageProtos.DataQueryResponse
                .newBuilder().setResultCode(errorCode).build();
        return response.toByteArray();
    }

    public static byte[] protocolBufferNoData() {
        DataQueryResponse response = GameMessageProtos.DataQueryResponse
                .newBuilder().setResultCode(ErrorCode.ERROR_SUCCESS).build();
        return response.toByteArray();
    }

    public static DataQueryResponse dateQueryResponseNoData() {
        DataQueryResponse response = GameMessageProtos.DataQueryResponse
                .newBuilder().setResultCode(ErrorCode.ERROR_SUCCESS).build();
        return response;
    }

    public static DataQueryResponse dateQueryResponseNoData(int errorCode) {
        DataQueryResponse response = GameMessageProtos.DataQueryResponse
                .newBuilder().setResultCode(errorCode).build();
        return response;
    }

    public static DataQueryResponse dateQueryResponseWithUserTutorial(UserTutorial userTutorial) {

        if (userTutorial == null){
            return dateQueryResponseNoData(ErrorCode.ERROR_DATA_NULL);
        }

        TutorialProtos.PBUserTutorial pbUserTutorial = userTutorial.toProtoBufModel();
        if (pbUserTutorial == null){
            return dateQueryResponseNoData(ErrorCode.ERROR_DATA_NULL);
        }

        DataQueryResponse response = GameMessageProtos.DataQueryResponse
                .newBuilder()
                .setResultCode(ErrorCode.ERROR_SUCCESS)
                .setUserTutorial(pbUserTutorial)
                .build();
        return response;
    }

    public static byte[] userTutorialListToProtocolBuffer(List<UserTutorial> userTutorialList){
        if (userTutorialList == null || userTutorialList.isEmpty()) {
            return protocolBufferNoData();
        }
        try {
            List<TutorialProtos.PBUserTutorial> list = new ArrayList<TutorialProtos.PBUserTutorial>(
                    userTutorialList.size());

            for (UserTutorial userTutorial : userTutorialList) {
                try {
                    TutorialProtos.PBUserTutorial.Builder builder = TutorialProtos.PBUserTutorial.newBuilder();

                    // required
                    builder.setUserId(userTutorial.getUserId());
                    builder.setRemoteId(userTutorial.getRemoteUserTutorialId());
                    builder.setTutorial(TutorialProtos.PBTutorial.newBuilder().setTutorialId(userTutorial.getTutorialId()).build());

                    if (userTutorial.getStageId() != null){
                        builder.setCurrentStageId(userTutorial.getStageId());
                    }
                    builder.setCurrentStageIndex(userTutorial.getStageIndex());
                    builder.setLocalId("");
                    builder.setCreateDate(userTutorial.getIntCreateDate());
                    builder.setModifyDate(userTutorial.getIntModifyDate());

                    TutorialProtos.PBUserTutorial newUserTutorial = builder.build();
                    list.add(newUserTutorial);
                } catch (Exception e) {
                    logger.error(
                            "<userTutorialListToProtocolBuffer> catch exception="
                                    + e.toString(), e);
                }
            }

            DataQueryResponse response = GameMessageProtos.DataQueryResponse
                    .newBuilder().setResultCode(ErrorCode.ERROR_SUCCESS)
                    .addAllUserTutorials(list).build();

            logger.info("<debug> user tutorial response="+response.toString());
            return response.toByteArray();
        } catch (Exception e) {
            logger.error("catch exception e=" + e.toString(), e);
            return null;
        }



    }

    public static byte[] userMessageListToProtocolBuffer(
            List<Message> messageList, boolean isGroup) {
        if (messageList == null || messageList.isEmpty()) {
            return protocolBufferNoData();
        }
        try {

            List<User> messageUserList = Collections.emptyList();
            Map<String, User> userMap = new HashMap<String, User>();

            if (isGroup) {

                // for group message, need to retrieve user infomration

                List<String> userIdList = new ArrayList<String>();
                for (Message message : messageList) {
                    String fromUserId = message.getFrom();
                    String toUserId = message.getTo();

                    if (!StringUtil.isEmpty(fromUserId) && !userIdList.contains(fromUserId)) {
                        userIdList.add(fromUserId);
                    }

                    if (!StringUtil.isEmpty(toUserId) && !userIdList.contains(toUserId)) {
                        userIdList.add(toUserId);
                    }
                }

                messageUserList = UserManager.findPublicUserInfo(userIdList);
                if (messageUserList != null) {
                    for (User user : messageUserList) {
                        userMap.put(user.getUserId(), user);
                    }
                }
            }


            List<PBMessage> list = new ArrayList<PBMessage>();
            if (messageList != null) {
                for (Message message : messageList) {

                    PBMessage.Builder builder = PBMessage.newBuilder();

                    // required
                    builder.setMessageId(message.getMessageId()).setFrom(
                            message.getFrom()).setTo(message.getTo());

                    builder.setStatus(message.getStatus());

                    // optional
                    if (message.getText() != null) {
                        builder.setText(message.getText());
                    }
                    if (message.getDrawData() != null) {
                        PBDraw pbDraw = PBDraw.parseFrom(message.getDrawData());
                        builder.addAllDrawData(pbDraw.getDrawDataList());
                        if (pbDraw.getCanvasSize() != null) {
                            builder.setCanvasSize(pbDraw.getCanvasSize());
                        }
                        builder.setDrawDataVersion(pbDraw.getVersion());
                    }

                    builder.setStatus(message.getStatus()).setCreateDate(
                            message.getCreateDateIntValue());

                    builder.setType(message.getType());

                    String reqMessageId = message.getReqMessageId();
                    if (reqMessageId != null) {
                        builder.setReqMessageId(reqMessageId);
                    }

                    builder.setReplyResult(message.getReplyResult());
                    builder.setLatitude(message.getLatitude());
                    builder.setLongitude(message.getLongitude());
                    builder.setIsGroup(isGroup);

                    if (isGroup) {
                        // set from & to user details
                        User fromUser = userMap.get(message.getFrom());
                        if (fromUser != null) {
                            builder.setFromUser(fromUser.toPBUser());
                        }

                        User toUser = userMap.get(message.getTo());
                        if (toUser != null) {
                            builder.setToUser(toUser.toPBUser());
                        }
                    }

                    String imageURL = message.getRemoteImageURL();
                    if (imageURL != null) {
                        builder.setImageURL(imageURL);
                    }

                    String thumbURL = message.getRemoteThumbImageURL();
                    if (thumbURL != null) {
                        builder.setThumbImageURL(thumbURL);
                    }

                    PBMessage newMessage = builder.build();

                    list.add(newMessage);

                }
            }
            DataQueryResponse response = GameMessageProtos.DataQueryResponse
                    .newBuilder().setResultCode(ErrorCode.ERROR_SUCCESS)
                    .addAllMessage(list).build();

//            logger.info("<messageToPB> response=" + response.toString());
            return response.toByteArray();
        } catch (Exception e) {
            logger.error("catch exception e=" + e.toString(), e);
            return null;
        }
    }

    public static byte[] userMessageStatListToProtocolBuffer(String userId,
                                                             List<MessageStat> messageStatList) {

        if (messageStatList == null || messageStatList.isEmpty()) {
            return protocolBufferNoData();
        }
        try {
            List<PBMessageStat> list = new ArrayList<PBMessageStat>(
                    messageStatList.size());

            for (MessageStat messageStat : messageStatList) {
                try {
                    PBMessageStat.Builder builder = PBMessageStat.newBuilder();

                    // required

                    // set statistic info

                    builder.setUserId(userId);
                    if (messageStat.getFromId() == null) {
                        builder.setFrom(userId);
                    } else {
                        builder.setFrom(messageStat.getFromId());
                    }
                    if (messageStat.getToId() == null) {
                        builder.setTo(userId);
                    } else {
                        builder.setTo(messageStat.getToId());
                    }
                    builder.setTotalMessageCount(messageStat
                            .getTotalMessageCount());
                    builder
                            .setNewMessageCount(messageStat
                                    .getNewMessageCount());
                    builder.setType(messageStat.getType());
                    builder.setCreateDate(messageStat.getCreateDateIntValue());
                    builder.setModifiedDate(messageStat
                            .getModifiedDateIntValue());

                    // set friend info
                    builder.setFriendUserId(messageStat.getFriendUserId());

                    builder.setFriendGender(messageStat.isFriendMale());
                    builder.setIsGroup(messageStat.isGroup());
                    builder.setIsVip(messageStat.getFriendVip());

                    if (messageStat.getFriendAvatar() != null) {
                        builder.setFriendAvatar(messageStat.getFriendAvatar());
                    }

                    if (messageStat.getFriendNickName() != null) {
                        builder.setFriendNickName(messageStat
                                .getFriendNickName());
                    } else {
                        builder.setFriendNickName("");
                    }

                    // optional
                    if (messageStat.getMessageId() != null) {
                        builder.setMessageId(messageStat.getMessageId());
                    }
                    if (messageStat.getText() != null) {
                        builder.setText(messageStat.getText());
                    }
                    builder.setNewGroupMessageCount(messageStat.getNewGroupMessageCount());

                    PBMessageStat newMessageStat = builder.build();

                    list.add(newMessageStat);
                } catch (Exception e) {
                    logger.error(
                            "<userMessageStatListToProtocolBuffer> catch exception="
                                    + e.toString(), e);
                }
            }

            DataQueryResponse response = GameMessageProtos.DataQueryResponse
                    .newBuilder().setResultCode(ErrorCode.ERROR_SUCCESS)
                    .addAllMessageStat(list).build();
            return response.toByteArray();
        } catch (Exception e) {
            logger.error("catch exception e=" + e.toString(), e);
            return null;
        }
    }

    private static PBCommentInfo createCommentInfoWithComment(UserAction comment,
                                                              boolean isCheckContest) {
        CommentInfo info = comment.getCommentInfo();

        if (info != null) {

            boolean isAnounymousAction = false;
            if (isCheckContest) {
                if (comment.getOpusCreatorUid() != null) {
                    if (comment.getOpusCreatorUid().equalsIgnoreCase(info.getActionUserId())) {
                        // 用户回复的评论的评论，被评论者同时也是作品的作者
                        isAnounymousAction = true;
                    }
                }
            }

            com.orange.network.game.protocol.model.DrawProtos.PBCommentInfo.Builder infoBuilder = PBCommentInfo
                    .newBuilder();
            if (info.getActionId() != null) {
                infoBuilder.setActionId(info.getActionId());
            }

            if (isAnounymousAction) {
                infoBuilder.setActionUserId(DBConstants.ANOUNYMOUS_USER_ID);
                infoBuilder.setActionNickName(DBConstants.ANOUNYMOUS_USER_NICK);
            } else {
                if (info.getActionUserId() != null) {
                    infoBuilder.setActionUserId(info.getActionUserId());
                }
                if (info.getActionNickName() != null) {
                    infoBuilder.setActionNickName(info.getActionNickName());
                }
            }

            if (info.getActionSummary() != null) {
                infoBuilder.setActionSummary(info.getActionSummary());
            }
            infoBuilder.setType(info.getType());
            return infoBuilder.build();
        }
        return null;

    }

    public static byte[] feedListToProtocolBuffer(List<UserAction> feeds,
                                                  int returnTotalCount, boolean returnCommentInfo, Set<String> ongoingAnouymousContestIds) {
        List<PBFeed> feedList = new ArrayList<PBFeed>();

        for (UserAction feed : feeds) {
            com.orange.network.game.protocol.model.DrawProtos.PBFeed.Builder builder = PBFeed
                    .newBuilder();

            // required
            builder.setUserId(feed.getCreateUserId());
            builder.setFeedId(feed.getActionId());
            builder.setActionType(feed.getType());
            builder.setMatchTimes(feed.getMatchTimes());
            builder.setCorrectTimes(feed.getCorrectTimes());
            builder.setGuessTimes(feed.getGuessTimes());
            builder.setCommentTimes(feed.getCommentTimes());
            builder.setOpusStatus(feed.getOpusStatus());
            builder.setDeviceType(feed.getDeviceType());
            builder.setCreateDate((int) (feed.getCreateDate().getTime() / 1000));
            String gender = feed.getGender();

            if (gender != null && gender.equalsIgnoreCase("M")) {
                builder.setGender(true);
            } else {
                builder.setGender(false);
            }
            builder.setVip(feed.getUserVip());

            // optional
            if (feed.getNickName() != null) {
                builder.setNickName(feed.getNickName());
            }

            // optional
            if (feed.getSignature() != null) {
                builder.setSignature(feed.getSignature());
            }

            if (feed.getAvatar() != null) {
                builder.setAvatar(feed.getAvatar());
            }

            if (!StringUtil.isEmpty(feed.getContestId())) {
                builder.setContestId(feed.getContestId());
            }

            // new for sing app
            if (feed.getSpendTime() != 0) {
                builder.setSpendTime(feed.getSpendTime());
            }
            builder.setCategory(GameConstantsProtos.PBOpusCategoryType.valueOf(feed.getCategory()));
            List<String> tags = feed.getTags();
            if (tags != null && tags.size() > 0) {
                builder.addAllTags(tags);
            }
            XiaojiSing.opusToPB(feed, builder);


            // rem by Benson, suppose this will not happen
            /*
			 * byte[] drawData = feed.getDrawData(); if (drawData != null) { try
			 * { PBDraw draw = PBDraw.parseFrom(drawData);
			 * builder.setDrawData(draw);
			 * 
			 * } catch (InvalidProtocolBufferException e) {
			 * logger.info("<feedListToProtocolBuffer> fail to parse draw data"
			 * ); } }
			 */

            int feedType = feed.getType();
            switch (feedType) {
                case UserAction.TYPE_DRAW:
                case OpusProtos.PBOpusType.SING_VALUE:
                    builder.setMatchTimes(feed.getMatchTimes());
                    break;
                case UserAction.TYPE_DRAW_TO_USER:
                case OpusProtos.PBOpusType.SING_TO_USER_VALUE:
                    builder.setMatchTimes(feed.getMatchTimes());
                    builder.setTargetUserId(feed.getTargetUserId());
                    if (!StringUtil.isEmpty(feed.getTargetNickName())) {
                        builder.setTargetUserNickName(feed.getTargetNickName());
                    }
                    break;

                case UserAction.TYPE_GUESS:
                    builder.setOpusId(feed.getOpusId());
                    builder.setIsCorrect(feed.isCorrect());
                    builder.setScore(feed.getScore());
                    if (feed.getGuessWordList() != null) {
                        builder.addAllGuessWords(feed.getGuessWordList());
                    }
                    break;

                case UserAction.TYPE_COMMENT:
                case UserAction.TYPE_CONTEST_COMMENT:

                    boolean isCheckContest = (feed.getContestId() != null) &&
                            (ongoingAnouymousContestIds != null) &&
                            (ongoingAnouymousContestIds.contains(feed.getContestId()));

                    // TODO rem debug log here
//                logger.info("<debug> isCheckContest is "+isCheckContest);

                    if (isCheckContest) {
                        // 检查比赛是否要匿名返回用户信息
                        if (feed.getOpusCreatorUid() != null) {
                            // 直接回复作品的评论
                            if (feed.getOpusCreatorUid().equalsIgnoreCase(feed.getCreateUserId())) {
                                // 用户评论自己的作品，需要匿名返回用户信息
                                setAnouymousFeedUserInfo(builder);
                            }
                        }
                    }

                    buildCommentPbFeed(builder, feed, isCheckContest, ongoingAnouymousContestIds);
                    break;

                case UserAction.TYPE_FLOWER:
                case UserAction.TYPE_TOMATO:
                    if (returnCommentInfo) {
                        buildCommentPbFeed(builder, feed, false, ongoingAnouymousContestIds);
                    }
                default:
                    break;
            }
            PBFeed pbFeed = builder.build();
            feedList.add(pbFeed);
        }
        if (feedList == null || feedList.size() == 0) {
            return null;
        }
        DataQueryResponse response = GameMessageProtos.DataQueryResponse
                .newBuilder().setResultCode(0).addAllFeed(feedList).build();
//        logger.info("return comment = "+response.toString());
        return response.toByteArray();
    }

    public static byte[] userDrawActionToProtocolBuffer(
            MongoDBClient mongoClient, UserAction userAction,
            boolean returnDataByUrl, boolean isReturnCompressedData) {

        return userDrawActionToProtocolBuffer(mongoClient, userAction, returnDataByUrl, isReturnCompressedData, false);
    }

    public static void setAnouymousFeedUserInfo(PBFeed.Builder builder) {

//        logger.info("<setAnouymousFeedUserInfo>");

        builder.setVip(0);
        builder.setUserId(DBConstants.ANOUNYMOUS_USER_ID);
        builder.setNickName(DBConstants.ANOUNYMOUS_USER_NICK);
        builder.setSignature(DBConstants.ANOUNYMOUS_USER_SIGNATURE);
        builder.setAvatar(DBConstants.ANOUNYMOUS_USER_AVATAR);
    }

    public static byte[] userDrawActionToProtocolBuffer(
            MongoDBClient mongoClient, UserAction userAction,
            boolean returnDataByUrl, boolean isReturnCompressedData,
            boolean returnAnounymousNick) {

        try {
            com.orange.network.game.protocol.model.DrawProtos.PBFeed.Builder builder = PBFeed
                    .newBuilder();

            // required
            builder.setUserId(userAction.getCreateUserId());
            builder.setFeedId(userAction.getActionId());
            builder.setActionType(userAction.getType());
            builder.setCreateDate((int) (userAction.getCreateDate().getTime() / 1000));

            // optional
            builder.setDeviceType(userAction.getDeviceType());

            if (userAction.getNickName() != null) {
                builder.setNickName(userAction.getNickName());
            }

            String gender = userAction.getGender();
            if (gender != null && gender.equalsIgnoreCase("M")) {
                builder.setGender(true);
            } else {
                builder.setGender(false);
            }

            if (userAction.getAvatar() != null) {
                builder.setAvatar(userAction.getAvatar());
            }

            if (userAction.getSignature() != null) {
                builder.setSignature(userAction.getSignature());
            }

            builder.setVip(userAction.getUserVip());

            String thumbURL = userAction.createOpusThumbImageUrl();
            String imageURL = userAction.createOpusImageUrl();
            String bgImageURL = userAction.createBgImageUrl();
            String bgImageName = userAction.getBgImageName();

            if (bgImageURL != null && bgImageName != null){
                logger.info("<userActionDrawPbFeed> bgImageUrl="+bgImageURL);
                builder.setBgImageUrl(bgImageURL);
                builder.setBgImageName(bgImageName);
                builder.setBgImageHeight(userAction.getBgImageHeight());
                builder.setBgImageWidth(userAction.getBgImageWidth());
            }

            if (imageURL != null) {
                builder.setOpusImage(imageURL);
            }

            if (thumbURL != null) {
                builder.setOpusThumbImage(thumbURL);
            }

            if (userAction.getOpusWord() != null) {
                builder.setOpusWord(userAction.getOpusWord());
            }

            if (userAction.getDeviceModel() != null){
                builder.setDeviceName(userAction.getDeviceModel());
            }

            setCanvasAndDescLabelInfo(userAction, builder);
            builder.setMatchTimes(userAction.getMatchTimes());

            if (userAction.isOpusToUser()) { // userAction.getType() == UserAction.TYPE_DRAW_TO_USER) {
                setFeedTargetUserInfo(builder, userAction);
            }

            if (userAction.isContestDraw()) {
                setFeedContestInfo(builder, userAction);
            }

            if (userAction.getClassList().size() > 0){
                builder.addAllOpusClass(userAction.getPBClassList());
            }

            // new for sing app
            if (userAction.getSpendTime() != 0) {
                builder.setSpendTime(userAction.getSpendTime());
            }

            if (userAction.getDraftCreateDate() != 0){
                builder.setDraftCreateDate(userAction.getDraftCreateDate());
            }

            if (userAction.getDraftCompleteDate() != 0){
                builder.setDraftCompleteDate(userAction.getDraftCompleteDate());
            }

            if (userAction.getSpendTime() != 0){
                builder.setSpendTime(userAction.getSpendTime());
            }

            if (userAction.getStrokes() != 0){
                builder.setStrokes(userAction.getStrokes());
            }

            if (userAction.getStageScore() > 0){
                builder.setStageScore(userAction.getStageScore());
                builder.setStageRank(userAction.getStageRank());
            }


            builder.setCategory(GameConstantsProtos.PBOpusCategoryType.valueOf(userAction.getCategory()));
            List<String> tags = userAction.getTags();
            if (tags != null && tags.size() > 0) {
                builder.addAllTags(tags);
            }
            XiaojiSing.opusToPB(userAction, builder);

            setFeedTimes(builder, userAction);

            // return ranking info, add by Benson 2013-08-06
            if (userAction.isContest()) {
                BasicDBObject rankData = userAction.getOpusRank();
                if (rankData != null) {
                    Set<String> keys = rankData.keySet();
                    for (String key : keys) {
                        int rankType = Integer.parseInt(key);
                        BasicDBObject rankValueData = (BasicDBObject) rankData.get(key);
                        if (rankValueData != null) {
                            Set<String> userIds = rankValueData.keySet();
                            for (String userId : userIds) {
                                int rankValue = rankValueData.getInt(userId);

                                GameBasicProtos.PBOpusRank.Builder rankBuilder = GameBasicProtos.PBOpusRank.newBuilder();
                                rankBuilder.setUserId(userId);
                                rankBuilder.setType(rankType);
                                rankBuilder.setValue(rankValue);

                                GameBasicProtos.PBOpusRank rank = rankBuilder.build();
//                                logger.info("userActionToPB add rank="+rank.toString());
                                builder.addRankInfo(rank);
                            }
                        }
                    }
                }
            }

            // 用于比赛匿名显示
            if (returnAnounymousNick) {
                setAnouymousFeedUserInfo(builder);
            }

            if (returnDataByUrl) {
                String remoteDrawDataUrl = userAction.getRemoteDrawDataUrl(isReturnCompressedData);
                boolean localFileExists = userAction.isLocalDataFileExist(isReturnCompressedData);
                if (localFileExists && remoteDrawDataUrl != null) {
                    builder.setDrawDataUrl(remoteDrawDataUrl);
                } else {

                    if (userAction.isXiaojiDraw()) {

                        // zip data file not exist, try to create one
                        // for draw app only
                        String localUrl = CreateDataFileService
                                .createFileAndUpdate(mongoClient, userAction,
                                        isReturnCompressedData);
                        remoteDrawDataUrl = DataService.getInstance()
                                .generateRemoteDrawDataUrl(localUrl,
                                        isReturnCompressedData);
                        logger
                                .info("<drawToPB> zip file not found, create from old data, return URL="
                                        + remoteDrawDataUrl);
                        if (remoteDrawDataUrl != null) {
                            builder.setDrawDataUrl(remoteDrawDataUrl);
                        } else {
                            logger
                                    .warn("<drawToPB> zip file not found, but cannot create zip file and return URL, opusId="
                                            + userAction.getActionId());
                        }
                    }

                }
            } else {

                byte[] localDrawData = userAction.readOldDrawData();
                if (localDrawData == null) {
                    // no old data, try to read from c_zip file
                    logger.info("<drawToPB> no draw data, try to read from compressed zip file, opusId="
                            + userAction.getActionId());
                    localDrawData = userAction
                            .readDrawData(isReturnCompressedData);
                }

                if (localDrawData != null) {
                    PBDraw draw = PBDraw.parseFrom(localDrawData);
                    builder.setDrawData(draw);

                    // try to create file in background thread
                    String remoteDrawDataUrl = userAction
                            .getRemoteDrawDataUrl(isReturnCompressedData);
                    boolean localFileExists = userAction
                            .isLocalDataFileExist(true);
                    if (localFileExists == false || remoteDrawDataUrl == null) {
                        logger
                                .info("<drawToPB> try to create draw data url for old data, opusId="
                                        + userAction.getActionId());
                        CreateDataFileService.getInstance()
                                .createFileAndUpdateAtBackground(mongoClient,
                                        userAction, true);
                    }

                } else {
                    logger
                            .warn("<drawToPB> required to return draw data, but draw data null and there is no compressed zip file, opusId="
                                    + userAction.getActionId());
                    OpusManager.disableUserForMatch(mongoClient, userAction);
                    return protocolBufferErrorNoData(ErrorCode.ERROR_OPUS_DRAW_DATA_ERROR);
                }
            }

            String remoteDrawDataUrl = userAction.getRemoteDrawDataUrl(isReturnCompressedData);
            if (remoteDrawDataUrl != null) {
                builder.setDrawDataUrl(remoteDrawDataUrl);
            }

            if (userAction.getDescription() != null) {
                builder.setOpusDesc(userAction.getDescription());
            }

            PBFeed pbFeed = builder.build();
            DataQueryResponse response = GameMessageProtos.DataQueryResponse
                    .newBuilder().setResultCode(0).addFeed(pbFeed).build();
            // logger.info("<test> !!!!!!!!!!!!!!!!!!!!!! opus=" + response.toString());
            return response.toByteArray();
        } catch (Exception e) {
            logger.error("<drawToPB> catch exception e=" + e.toString(), e);
            return protocolBufferErrorNoData(ErrorCode.ERROR_GENERAL_EXCEPTION);
        }
    }

    private static void setFeedContestInfo(
            com.orange.network.game.protocol.model.DrawProtos.PBFeed.Builder builder,
            UserAction userAction) {

        if (!StringUtil.isEmpty(userAction.getContestId())) {
            builder.setContestId(userAction.getContestId());
        }
    }

    private static void setFeedTargetUserInfo(
            com.orange.network.game.protocol.model.DrawProtos.PBFeed.Builder builder,
            UserAction userAction) {
        if (userAction.getTargetUserId() != null) {
            builder.setTargetUserId(userAction.getTargetUserId());
        }
        if (userAction.getTargetNickName() != null) {
            builder.setTargetUserNickName(userAction.getTargetNickName());
        }
    }

    public static Object statisticsToJSON(long chatCount, long fanCount,
                                          long feedCount, long roomCount, long commentCount,
                                          long drawToMeCount, long bbsCommentCount,
                                          long timelineGuessCount, long timelineOpusCount, long timelineConquerCount,
                                          long groupNoticeCount) {
        JSONObject object = new JSONObject();
        object.put(ServiceConstant.PARA_FEED_COUNT, feedCount);
        object.put(ServiceConstant.PARA_MESSAGE_COUNT, chatCount);
        object.put(ServiceConstant.PARA_FAN_COUNT, fanCount);
        object.put(ServiceConstant.PARA_ROOM_COUNT, roomCount);
        object.put(ServiceConstant.PARA_COMMENT_COUNT, commentCount);
        object.put(ServiceConstant.PARA_DRAWTOME_COUNT, drawToMeCount);
        object.put(ServiceConstant.PARA_BBS_ACTION_COUNT, bbsCommentCount);
        object.put(ServiceConstant.PARA_TIMELINE_GUESS_COUNT, timelineGuessCount);
        object.put(ServiceConstant.PARA_TIMELINE_OPUS_COUNT, timelineOpusCount);
        object.put(ServiceConstant.PARA_TIMELINE_CONQUER_COUNT, timelineConquerCount);
        object.put(ServiceConstant.PARA_GROUP_NOTICE_COUNT, groupNoticeCount);
        return object;
    }

    private static PBFeedTimes getPBFeedTimes(int type, int value) {
        com.orange.network.game.protocol.model.DrawProtos.PBFeedTimes.Builder times = PBFeedTimes
                .newBuilder();
        times.setType(type);
        times.setValue(value);
        return times.build();
    }

    private static com.orange.network.game.protocol.model.DrawProtos.PBFeed.Builder setFeedTimes(
            com.orange.network.game.protocol.model.DrawProtos.PBFeed.Builder builder,
            UserAction feed) {
        List<PBFeedTimes> feedTimes = new ArrayList<PBFeedTimes>();
        PBFeedTimes times = getPBFeedTimes(UserAction.TIMES_TYPE_MATCH, feed
                .getMatchTimes());
        feedTimes.add(times);
        times = getPBFeedTimes(UserAction.TIMES_TYPE_GUESS, feed
                .getGuessTimes());
        feedTimes.add(times);
        times = getPBFeedTimes(UserAction.TIMES_TYPE_CORRECT, feed
                .getCorrectTimes());
        feedTimes.add(times);

        times = getPBFeedTimes(UserAction.TIMES_TYPE_COMMENT, feed
                .getCommentTimes());
        feedTimes.add(times);

        times = getPBFeedTimes(UserAction.TIMES_TYPE_CONTEST_COMMENT, feed
                .getContestCommentTimes());
        feedTimes.add(times);

        times = getPBFeedTimes(UserAction.TIMES_TYPE_FLOWER, feed
                .getFlowerTimes());
        feedTimes.add(times);
        times = getPBFeedTimes(UserAction.TIMES_TYPE_TOMATO, feed
                .getTomatoTimes());
        feedTimes.add(times);
        times = getPBFeedTimes(UserAction.TIMES_TYPE_SAVE, feed.getSaveTimes());
        feedTimes.add(times);

        times = getPBFeedTimes(UserAction.TIMES_TYPE_PLAY, feed.getPlayTimes());
        feedTimes.add(times);

        builder.addAllFeedTimes(feedTimes);

        return builder;
    }

    @SuppressWarnings("unused")
    private static PBFeed.Builder setFeedOpusCreator(PBFeed.Builder builder, UserAction feed) {
        if (feed.getOpusCreatorUid() != null) {
            builder.setOpusCreatorUserId(feed.getOpusCreatorUid());
        }
        return builder;
    }

    private static PBFeed buildDrawPbFeed(
            PBFeed.Builder builder,
            UserAction feed, boolean isReturnCompressedData) {
        setFeedTimes(builder, feed);

        // set opus image and draw data.
        String thumbURL = feed.createOpusThumbImageUrl();
        String imageURL = feed.createOpusImageUrl();
        String bgImageURL = feed.createBgImageUrl();
        String bgImageName = feed.getBgImageName();

        if (bgImageURL != null && bgImageName != null){
            logger.info("<buildDrawPbFeed> bgImageUrl="+bgImageURL);
            builder.setBgImageUrl(bgImageURL);
            builder.setBgImageName(bgImageName);
            builder.setBgImageHeight(feed.getBgImageHeight());
            builder.setBgImageWidth(feed.getBgImageWidth());
        }

        if (thumbURL != null) {
            builder.setOpusImage(thumbURL);
        } else if (imageURL != null) {
            builder.setOpusImage(imageURL);
        } else {
            if (feed.isDrawType() || feed.isContestDrawType()) {
                logger.warn("<buildDrawPbFeed> but not image or thumb image found!!! opusId=" + feed.getOpusId());
            }
			/*
			 * byte[] drawData = feed.readDrawData(isReturnCompressedData); if
			 * (drawData != null) { try { PBDraw draw =
			 * PBDraw.parseFrom(drawData); builder.setDrawData(draw);
			 * 
			 * } catch (InvalidProtocolBufferException e) { logger
			 * .info("<feedListToProtocolBuffer> fail to parse draw data");
			 * e.printStackTrace(); } }
			 */
        }

        if (feed.getOpusWord() != null) {
            builder.setOpusWord(feed.getOpusWord());
        }

        if (feed.getDescription() != null) {
            builder.setOpusDesc(feed.getDescription());
        }

        if (feed.getDrawDataUrl() != null) {
            builder.setDrawDataUrl(feed.getRemoteDrawDataUrl(false));
        }

        if (feed.getStageScore() > 0){
            builder.setStageScore(feed.getStageScore());
            builder.setStageRank(feed.getStageRank());
        }

        builder.setCategory(GameConstantsProtos.PBOpusCategoryType.valueOf(feed.getCategory()));

        XiaojiSing.opusToPB(feed, builder);

        return builder.build();
    }

    private static PBFeed buildGuessPbFeed(
            com.orange.network.game.protocol.model.DrawProtos.PBFeed.Builder builder,
            UserAction feed, boolean isReturnCompressedData) {
        buildDrawPbFeed(builder, feed, isReturnCompressedData);
        if (feed.getOpusCreatorUid() != null) {
            builder.setOpusCreatorUserId(feed.getOpusCreatorUid());
        }

        if (feed.getOpusCreatorAvatar() != null) {
            builder.setOpusCreatorAvatar(feed.getOpusCreatorAvatar());
        }

        String gender = feed.getOpusCreatorGender();

        if (gender != null && gender.equalsIgnoreCase("M")) {
            builder.setOpusCreatorGender(true);
        } else {
            builder.setOpusCreatorGender(false);
        }

        if (feed.getOpusCreatorNickName() != null) {
            builder.setOpusCreatorNickName(feed.getOpusCreatorNickName());
        }

        builder.setOpusId(feed.getOpusId());
        builder.setIsCorrect(feed.isCorrect());
        builder.setScore(feed.getScore());
        if (feed.getGuessWordList() != null) {
            builder.addAllGuessWords(feed.getGuessWordList());
        }

        return builder.build();
    }

    private static PBFeed buildCommentPbFeed(
            PBFeed.Builder builder,
            UserAction feed,
            boolean isCheckContest,
            Set<String> anouymousContestIds) {

        buildDrawPbFeed(builder, feed, false);

        if (isCheckContest) {
            // 匿名返回作品作者信息
            builder.setOpusCreatorUserId(DBConstants.ANOUNYMOUS_USER_ID);
            builder.setOpusCreatorNickName(DBConstants.ANOUNYMOUS_USER_NICK);
            builder.setOpusCreatorAvatar(DBConstants.ANOUNYMOUS_USER_AVATAR);
        } else {
            if (feed.getOpusCreatorUid() != null) {
                builder.setOpusCreatorUserId(feed.getOpusCreatorUid());
            }

            if (feed.getOpusCreatorAvatar() != null) {
                builder.setOpusCreatorAvatar(feed.getOpusCreatorAvatar());
            }

            String gender = feed.getOpusCreatorGender();
            if (gender != null && gender.equalsIgnoreCase("M")) {
                builder.setOpusCreatorGender(true);
            } else {
                builder.setOpusCreatorGender(false);
            }

            if (feed.getOpusCreatorNickName() != null) {
                builder.setOpusCreatorNickName(feed.getOpusCreatorNickName());
            }
        }

        builder.setOpusId(feed.getOpusId());
        builder.setIsCorrect(feed.isCorrect());
        builder.setScore(feed.getScore());
        if (feed.getGuessWordList() != null) {
            builder.addAllGuessWords(feed.getGuessWordList());
        }

        if (feed.getComment() != null) {
            builder.setComment(feed.getComment());
        }
        if (feed.getOpusId() != null) {
            builder.setOpusId(feed.getOpusId());
        }
        if (feed.getOpusCreatorUid() != null) {
            builder.setOpusCreatorUserId(feed.getOpusCreatorUid());
        }

        PBCommentInfo info = createCommentInfoWithComment(feed, isCheckContest);

        if (info != null) {
            builder.setCommentInfo(info);
        }

        return builder.build();
    }

    private static PBFeed buildDrawToUserPbFeed(
            com.orange.network.game.protocol.model.DrawProtos.PBFeed.Builder builder,
            UserAction feed, boolean isReturnCompressedData) {
        buildDrawPbFeed(builder, feed, isReturnCompressedData);
        builder.setTargetUserId(feed.getTargetUserId());
        if (!StringUtil.isEmpty(feed.getTargetNickName())) {
            builder.setTargetUserNickName(feed.getTargetNickName());
        }
        return builder.build();
    }

    public static byte[] feedListToProtocolBufferImage(List<UserAction> feeds,
                                                       int returnTotalCount,
                                                       boolean isReturnCompressedData,
                                                       boolean returnAnounymousNick) {

        return feedListToDataQueryResponse(feeds, returnTotalCount, isReturnCompressedData, returnAnounymousNick, true).toByteArray();
    }

    public static byte[] feedListToProtocolBufferImage(List<UserAction> feeds,
                                                       int returnTotalCount,
                                                       boolean isReturnCompressedData) {

        return feedListToProtocolBufferImage(feeds, returnTotalCount, isReturnCompressedData, false);
    }


    public static DataQueryResponse feedListToDataQueryResponse(List<UserAction> feeds,
                                                                int returnTotalCount,
                                                                boolean isReturnCompressedData,
                                                                boolean returnAnounymousNick,
                                                                boolean returnOpusClass) {
        if (feeds == null || feeds.isEmpty()) {
            return dateQueryResponseNoData();
        }
        List<PBFeed> feedList = new ArrayList<PBFeed>();

        for (UserAction feed : feeds) {
            if (feed == null) {
                continue;
            }
            com.orange.network.game.protocol.model.DrawProtos.PBFeed.Builder builder = PBFeed
                    .newBuilder();

            String dataUrl = feed.getRemoteDrawDataUrl(isReturnCompressedData);
            if (dataUrl != null) {
                builder.setDrawDataUrl(dataUrl);
            }

            // required
            builder.setUserId(feed.getCreateUserId());
            builder.setFeedId(feed.getActionId());

            if (feed.getType() == OpusProtos.PBOpusType.SING_CONTEST_VALUE){
                builder.setActionType(OpusProtos.PBOpusType.SING_VALUE);
            }
            else if (feed.getType() == OpusProtos.PBOpusType.DRAW_CONTEST_VALUE
                    ){
                builder.setActionType(OpusProtos.PBOpusType.DRAW_VALUE);
            }
            else{
                builder.setActionType(feed.getType());
            }

            if (returnOpusClass) {
                if (feed.getClassList().size() > 0) {
                    builder.addAllOpusClass(feed.getPBClassList());
                }
            }

            if (feed.getClassList().size() > 0){
                builder.addAllOpusClassIds(feed.getClassList());
            }

            builder.setOpusStatus(feed.getOpusStatus());
            builder.setCreateDate((int) (feed.getCreateDate().getTime() / 1000));

            if (feed.getDeviceModel() != null){
                builder.setDeviceName(feed.getDeviceModel());
            }

            setCanvasAndDescLabelInfo(feed, builder);

            // new for sing app
            if (feed.getSpendTime() != 0) {
                builder.setSpendTime(feed.getSpendTime());
            }
            builder.setCategory(GameConstantsProtos.PBOpusCategoryType.valueOf(feed.getCategory()));
            List<String> tags = feed.getTags();
            if (tags != null && tags.size() > 0) {
                builder.addAllTags(tags);
            }

            String gender = feed.getGender();

            if (gender != null && gender.equalsIgnoreCase("M")) {
                builder.setGender(true);
            } else {
                builder.setGender(false);
            }
            // optional
            if (feed.getNickName() != null) {
                builder.setNickName(feed.getNickName());
            }

            if (feed.getAvatar() != null) {
                builder.setAvatar(feed.getAvatar());
            }

            if (feed.getSignature() != null) {
                builder.setSignature(feed.getSignature());
            }

            // add by Gamy, for learn draw.
            if (feed.getLearnDraw() != null) {
                PBLearnDraw learnDraw = feed.getLearnDraw().toPBLearnDraw();
                if (learnDraw != null) {
                    builder.setLearnDraw(learnDraw);
                }
            }

            builder.setVip(feed.getUserVip());

            // 用于比赛匿名显示
            if (returnAnounymousNick) {
                builder.setUserId(DBConstants.ANOUNYMOUS_USER_ID);
                builder.setNickName(DBConstants.ANOUNYMOUS_USER_NICK);
                builder.setSignature(DBConstants.ANOUNYMOUS_USER_SIGNATURE);
                builder.setAvatar(DBConstants.ANOUNYMOUS_USER_AVATAR);
                builder.setVip(0);
            }

            if (feed.getDraftCreateDate() != 0){
                builder.setDraftCreateDate(feed.getDraftCreateDate());
            }

            if (feed.getDraftCompleteDate() != 0){
                builder.setDraftCompleteDate(feed.getDraftCompleteDate());
            }

            if (feed.getSpendTime() != 0){
                builder.setSpendTime(feed.getSpendTime());
            }

            if (feed.getStrokes() != 0){
                builder.setStrokes(feed.getStrokes());
            }

            // logger.info("<getFeedList> feed signature="+feed.getSignature());

            int feedType = feed.getType();
            switch (feedType) {
                case UserAction.TYPE_DRAW:
                case UserAction.TYPE_LEARN_DRAW_CONQUER:
                case UserAction.TYPE_LEARN_DRAW_PRACTICE:
                case OpusProtos.PBOpusType.SING_VALUE:
                {
                    buildDrawPbFeed(builder, feed, isReturnCompressedData);
                    break;
                }
                case UserAction.TYPE_DRAW_TO_USER:
                case OpusProtos.PBOpusType.SING_TO_USER_VALUE: {
                    buildDrawToUserPbFeed(builder, feed, isReturnCompressedData);
                    break;
                }
                case UserAction.TYPE_DRAW_TO_CONTEST:
                case OpusProtos.PBOpusType.SING_CONTEST_VALUE: {
                    buildDrawPbFeed(builder, feed, isReturnCompressedData);
                    if (feed.getContestId() != null) {
                        builder.setContestId(feed.getContestId());
                    }
                    builder.setContestScore(feed.getContestScore());
                    break;
                }

                case UserAction.TYPE_GUESS:
                    buildGuessPbFeed(builder, feed, isReturnCompressedData);
                    break;

                case UserAction.TYPE_COMMENT:
                case UserAction.TYPE_CONTEST_COMMENT:
                    buildCommentPbFeed(builder, feed, false, null);
                    break;

                default:
                    break;
            }

            // add by Benson
            if (feed.getContestId() != null) {
                builder.setContestId(feed.getContestId());
            }

            PBFeed pbFeed = builder.build();
            feedList.add(pbFeed);
        }
        if (feedList == null || feedList.size() == 0) {
            return null;
        }
        DataQueryResponse response = GameMessageProtos.DataQueryResponse
                .newBuilder()
                .setResultCode(0)
                .addAllFeed(feedList)
                .setTotalCount(returnTotalCount)
                .build();

        //logger.info("<test> !!!!!!!!!!!!!!!!!!!!!!!!!!!! feed list="+response.toString());

        return response;
    }

    public static Object actionIdToJson(String actionId) {
        JSONObject object = new JSONObject();
        safePut(object, ServiceConstant.PARA_FEED_ID, actionId);
        return object;
    }

    public static Object boardListToJSON(List<Board> boardList) {

        if (boardList == null) {
            return null;
        }
        JSONArray retArray = new JSONArray();
        for (Board board : boardList) {
            JSONObject object = new JSONObject();
            // board
            safePut(object, ServiceConstant.PARA_INDEX, board.getIndex());
            safePut(object, ServiceConstant.PARA_VERSION, board.getVersion());
            safePut(object, ServiceConstant.PARA_TYPE, board.getType());
            safePut(object, ServiceConstant.PARA_STATUS, board.getStatus());
            safePut(object, ServiceConstant.PARA_BOARDID, board.getBoardId());

            // image board
            safePut(object, ServiceConstant.PARA_AD_IMAGE_URL, board
                    .getAdImageUrl());
            safePut(object, ServiceConstant.PARA_IMAGE_URL, board.getImageUrl());
            safePut(object, ServiceConstant.PARA_CN_AD_IMAGE_URL, board
                    .getCNAdImageUrl());
            safePut(object, ServiceConstant.PARA_CN_IMAGE_URL, board
                    .getCNImageUrl());

            safePut(object, ServiceConstant.PARA_IMAGE_CLICK_URL, board
                    .getClickUrl());
            safePut(object, ServiceConstant.PARA_AD_PLATFORM, board
                    .getAdPlatform());
            safePut(object, ServiceConstant.PARA_AD_PUBLISH_ID, board
                    .getAdPublishId());

            // web board
            safePut(object, ServiceConstant.PARA_LOCAL_URL, board.getLocalUrl());
            safePut(object, ServiceConstant.PARA_REMOTE_URL, board
                    .getRemoteUr());
            safePut(object, ServiceConstant.PARA_WEB_TYPE, board.getWebType());

            retArray.add(object);
        }
        return retArray;
    }

    public static Object feedTimesToJSON(UserAction feed) {
        // JSONArray times = new JSONArray();
        JSONObject object = new JSONObject();
        object.put(ServiceConstant.PARA_COMMENT_TIMES, feed.getCommentTimes());
        object.put(ServiceConstant.PARA_GUESS_TIMES, feed.getGuessTimes());
        object.put(ServiceConstant.PARA_CORRECT_TIMES, feed.getCorrectTimes());
        object.put(ServiceConstant.PARA_FLOWER_TIMES, feed.getFlowerTimes());
//		object.put(ServiceConstant.PARA_TOMATO_TIMES, feed.getTomatoTimes());
        object.put(ServiceConstant.PARA_SAVE_TIMES, feed.getSaveTimes());
        object.put(ServiceConstant.PARA_PLAY_TIMES, feed.getPlayTimes());
        return object;
    }

    public static Object contestListToJSON(List<Contest> list) {

        if (list != null && !list.isEmpty()) {
            JSONArray jArray = new JSONArray();
            for (Contest contest : list) {
                JSONObject object = new JSONObject();
                safePut(object, ServiceConstant.PARA_TYPE, contest.getType());
                safePut(object, ServiceConstant.PARA_TITLE, contest.getTitle());
                safePut(object, ServiceConstant.PARA_CONTESTID, contest
                        .getContestId());

                int startDate = DateUtil.dateToInt(contest.getStartDate());
                int endDate = DateUtil.dateToInt(contest.getEndDate());

                safePut(object, ServiceConstant.PARA_START_DATE, startDate);
                safePut(object, ServiceConstant.PARA_END_DATE, endDate);
                safePut(object, ServiceConstant.PARA_CONTEST_URL, contest
                        .getContestUrl());
                safePut(object, ServiceConstant.PARA_STATEMENT_URL, contest
                        .getStatementUrl());
                safePut(object, ServiceConstant.PARA_OPUS_COUNT, contest
                        .getOpusCount());

                safePut(object, ServiceConstant.PARA_CONTEST_IPAD_URL, contest
                        .getContestIPadUrl());
                safePut(object, ServiceConstant.PARA_STATEMENT_IPAD_URL,
                        contest.getStatementIPadUrl());

                safePut(object, ServiceConstant.PARA_PARTICIPANT_COUNT, contest
                        .getOpusCount());
                safePut(object, ServiceConstant.PARA_CAN_SUMMIT_COUNT, contest
                        .getSummitCount());

                ContestLimit limit = contest.getLimit();
                if (limit != null) {
                    JSONObject jObject = new JSONObject();
                    jObject.put(ServiceConstant.PARA_LEVEL, limit.getLevel());
                    jObject.put(ServiceConstant.PARA_OPUS_COUNT, limit
                            .getOpusCount());
                    safePut(object, ServiceConstant.PARA_LIMIT, jObject);
                }
                jArray.add(object);
            }
            return jArray;
        }
        return null;
    }

    public static byte[] contestListToPB(List<Contest> list, String userId) {

        try {
            DataQueryResponse.Builder builder = GameMessageProtos.DataQueryResponse
                    .newBuilder().setResultCode(ErrorCode.ERROR_SUCCESS);

            List<ObjectId> groupIds = new ArrayList<ObjectId>();
            for (Contest contest : list) {
                if (contest.getIsGroup() && ObjectId.isValid(contest.getGroupId())) {
                    groupIds.add(new ObjectId(contest.getGroupId()));
                }
            }

            Map<String, Group> groupMap = GroupManager.getGroupMapByIds(groupIds);

            for (Contest contest : list) {
                GroupProtos.PBContest.Builder contestBuilder = GroupProtos.PBContest.newBuilder();

                contestBuilder.setContestId(contest.getContestId());
                contestBuilder.setType(contest.getType());
                contestBuilder.setStartDate(contest.getIntStartDate());
                contestBuilder.setEndDate(contest.getIntEndDate());
                contestBuilder.setStatus(contest.getStatus());
                contestBuilder.setParticipantCount(contest.getParticipantCount());
                contestBuilder.setOpusCount(contest.getOpusCount());
                contestBuilder.setTitle(contest.getTitle());
                if (contest.getContestUrl() != null) {
                    contestBuilder.setContestUrl(contest.getContestUrl());
                }
                if (contest.getStatementUrl() != null) {
                    contestBuilder.setStatementUrl(contest.getStatementUrl());
                }
                contestBuilder.setCanSubmitCount(contest.getSummitCount());

                contestBuilder.setVoteStartDate(contest.getIntVoteStartDate());
                contestBuilder.setVoteEndDate(contest.getIntVoteEndDate());
                contestBuilder.setIsAnounymous(contest.getIsAnonymous());
                contestBuilder.setMaxFlowerPerContest(contest.getMaxFlowerPerContest());
                contestBuilder.setMaxFlowerPerOpus(contest.getMaxFlowerPerOpus());

                contestBuilder.setCanSubmit(contest.canSubmit());
                contestBuilder.setCanVote(contest.canVote());

                contestBuilder.setContestantsOnly(contest.consestantsOnly());
                contestBuilder.setJoinersType(contest.getJoinersType());

                if (contest.getDesc() != null) {
                    contestBuilder.setDesc(contest.getDesc());
                }

                List<Integer> awardRuleList = contest.getAwardRuleList();
                if (awardRuleList != null) {
                    contestBuilder.addAllAwardRules(awardRuleList);
                }

                Map<String, User> userMap = ContestManager.getContestUsers(contest);
//                logger.info("<contestToPB> userMap "+userMap.toString());

                // set judge list
                List<String> judges = contest.getJudgeList();
                for (String judge : judges) {
                    User user = userMap.get(judge);
                    if (user != null) {
                        contestBuilder.addJudges(userToPBGameUser(user));
                    } else {
                        logger.warn("<contestToPB> judge " + judge + " user not found");
                    }
                }

                List<String> contestants = contest.getContestantList();
                for (String contestant : contestants) {
                    User user = userMap.get(contestant);
                    if (user != null) {
                        contestBuilder.addContestants(userToPBGameUser(user));
                    } else {
                        logger.warn("<contestToPB> contestants " + contestants + " user not found");
                    }
                }

                List<String> reporters = contest.getReporterList();
                for (String reporter : reporters) {
                    User user = userMap.get(reporter);
                    if (user != null) {
                        contestBuilder.addReporters(userToPBGameUser(user));
                    } else {
                        logger.warn("<contestToPB> judge " + reporter + " user not found");
                    }

                }

                List<UserAward> winners = contest.getWinnerList();
                for (UserAward winner : winners) {
                    User user = userMap.get(winner.getUserId());
                    logger.info("<contestToPB> winner user " + winner.toString());
                    IntKeyValue rankType = contest.getRankTypeInfo(winner.getAwardType());
                    GameBasicProtos.PBUserAward pbUserAward = winner.toPBUserAward(user, rankType);
                    if (user != null) {
                        contestBuilder.addWinnerUsers(pbUserAward);
                    } else {
                        logger.warn("<contestToPB> winner user " + winner.getUserId() + " user not found");
                    }
                }

                List<UserAward> awardList = contest.getAwardResult();
                for (UserAward award : awardList) {
                    User user = userMap.get(award.getUserId());
                    IntKeyValue rankType = contest.getRankTypeInfo(award.getAwardType());
                    GameBasicProtos.PBUserAward pbUserAward = award.toPBUserAward(user, rankType);
                    if (user != null) {
                        contestBuilder.addAwardUsers(pbUserAward);
                    } else {
                        logger.warn("<contestToPB> award user " + award.getUserId() + " user not found");
                    }

                }

                Map<Integer, String> rankTypes = contest.getAllRankType();
                if (rankTypes != null) {
                    for (Integer rankType : rankTypes.keySet()) {
                        String rankValue = rankTypes.get(rankType);
                        if (rankType != null && rankValue != null) {
                            GameBasicProtos.PBIntKeyValue.Builder pbIntKeyValueBuilder = GameBasicProtos.PBIntKeyValue.newBuilder();
                            pbIntKeyValueBuilder.setKey(rankType);
                            pbIntKeyValueBuilder.setValue(rankValue);
                            GameBasicProtos.PBIntKeyValue pbIntKeyValue = pbIntKeyValueBuilder.build();
                            contestBuilder.addRankTypes(pbIntKeyValue);
                        }
                    }
                }

                boolean groupInfoOK = true;
                if (contest.getIsGroup()) {
                    if (contest.getGroupId() == null || !groupMap.containsKey(contest.getGroupId())){
                        // is group, but group not found!!! maybe deleted
                        groupInfoOK = false;
                    }
                    else{
                        Group group = groupMap.get(contest.getGroupId());
                        GroupProtos.PBGroup pbGroup = group.toProtoBufModel();
                        if (pbGroup != null) {
                            contestBuilder.setGroup(pbGroup);
                        }
                        else{
                            groupInfoOK = false;
                        }

                        boolean inContest = true;
                        if (contest.getJoinersType() == Contest.JOINER_TYPE_MEMBER) {
                            inContest = GroupUserManager.isUserGroupMember(DBService.getInstance().getMongoDBClient(), userId, group.getGroupId());
                        } else if (contest.getJoinersType() == Contest.JOINER_TYPE_MEMBER_GUEST) {
                            inContest = group.isUserInGuestList(userId);
                            if (!inContest) {
                                inContest = GroupUserManager.isUserGroupMember(DBService.getInstance().getMongoDBClient(), userId, group.getGroupId());
                            }
                        }

                        if (inContest) {
                            GameBasicProtos.PBGameUser.Builder userBuilder = GameBasicProtos.PBGameUser.newBuilder();
                            userBuilder.setUserId(userId);
                            userBuilder.setNickName("");
                            contestBuilder.addContestants(userBuilder.build());
                        }
                    }
                }

                if (groupInfoOK){
                    builder.addContestList(contestBuilder.build());
                }
            }

            DataQueryResponse response = builder.build();

            // TODO change to debug
//            logger.info("<contestListToPB> response="+response.toString());

            byte[] byteData = response.toByteArray();
            return byteData;

        } catch (Exception e) {
            logger.error("<contestListToPB> exception = " + e.toString(), e);
            return protocolBufferErrorNoData(ErrorCode.ERROR_PROTOCOL_BUFFER_PARSING);
        }
    }

    /**
     * @param list
     */
    public static JSONArray simpleFeedListToJSON(List<UserAction> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        JSONArray array = new JSONArray();
        for (UserAction action : list) {
            JSONObject jObject = new JSONObject();
            safePut(jObject, ServiceConstant.PARA_USERID, action
                    .getCreateUserId());
            safePut(jObject, ServiceConstant.PARA_IMAGE_URL, action
                    .getOpusImageUrl());
            safePut(jObject, ServiceConstant.PARA_WORD, action.getWord());
            safePut(jObject, ServiceConstant.PARA_FEED_ID, action.getActionId());
            array.add(jObject);
        }
        return array;
    }

    // bbs method below.

    public static byte[] bbsBoardListToProto(List<BBSBoard> boardList) {

        if (boardList == null || boardList.isEmpty()) {
            return protocolBufferNoData();
        }
        try {
            List<PBBBSBoard> list = new ArrayList<PBBBSBoard>();

            for (BBSBoard board : boardList) {
                PBBBSBoard pbBBSBoard = bbsBoardToProto(board);
                if (pbBBSBoard != null) {
                    list.add(pbBBSBoard);
                }
            }

            DataQueryResponse response = GameMessageProtos.DataQueryResponse
                    .newBuilder().setResultCode(ErrorCode.ERROR_SUCCESS)
                    .addAllBbsBoard(list).build();
            return response.toByteArray();
        } catch (Exception e) {
            logger.error("catch exception e=" + e.toString(), e);
            return null;
        }
    }

    private static PBBBSBoard bbsBoardToProto(BBSBoard board) {
        if (board != null) {
            PBBBSBoard.Builder builder = PBBBSBoard.newBuilder();
            // set required attributes
            builder.setBoardId(board.getBoardId());
            builder.setType(board.geType());
            builder.setName(board.getName());
            builder.setActionCount(board.getActionCount());
            builder.setPostCount(board.getPostCount());
            builder.setIndex(board.getIndex());

            // set optional attributes
            if (board.getIcon() != null) {
                builder.setIcon(board.getIcon());
            }
            if (board.getParentBoardId() != null) {
                builder.setParentBoardId(board.getParentBoardId());
            }
            if (board.getDesc() != null) {
                builder.setDesc(board.getDesc());
            }
            PBBBSPost lastPost = bbsPostToProto(board.getLastPost(), true, null, null);
            if (lastPost != null) {
                builder.setLastPost(lastPost);
            }
            Collection<BBSUser> adminList = board.getAdminUserList();
            if (adminList != null && !adminList.isEmpty()) {
                for (BBSUser user : adminList) {
                    PBBBSUser pbUser = bbsUserToProto(user);
                    builder.addAdminList(pbUser);
                }
            }
            return builder.build();
        }
        return null;
    }

    private static PBBBSPost bbsPostToProto(BBSPost post, boolean withoutData, String userId, String userGroupId) {
        if (post != null) {
            PBBBSPost.Builder builder = PBBBSPost.newBuilder();

            builder.setPostId(post.getPostId());
            builder.setBoardId(post.getBoardId());
            builder.setAppId(post.getAppId());
            builder.setDeviceType(post.getDeviceType());
            builder.setReplyCount(post.getReplyCount());
            builder.setSupportCount(post.getSupportCount());
            builder.setCreateDate(DateUtil.dateToInt(post.getCreateDate()));
            builder.setModifyDate(DateUtil.dateToInt(post.getModifyDate()));
            builder.setStatus(post.getStatus());
            builder.setMarked(post.isMarked());
            builder.setIsPrivate(post.isPrivate());

            PBBBSUser user = bbsUserToProto(post.getCreateUser());
            if (user == null) {
                logger.info("use is null, post id = " + post.getPostId());

            }
            builder.setCreateUser(user);

            if (post.isPrivate() && !post.getBoardId().equalsIgnoreCase(userGroupId)){
                logger.info("user group "+userGroupId+ " is not member of group "+post.getBoardId()+" and post is private, force set content to private");
                post.setContent(new BBSContent("私密帖子"));
            }

            PBBBSContent content = null;
            content = bbsContentToProto(post.getContent(), withoutData);
            builder.setContent(content);

            PBBBSReward reward = bbsRewardToProto(post.getReward());
            if (reward != null) {
                builder.setReward(reward);
            }

            return builder.build();
        }
        return null;
    }

    private static PBBBSUser bbsUserToProto(BBSUser user) {
        if (user != null && user.getUserId() != null) {
            PBBBSUser.Builder builder = PBBBSUser.newBuilder();
            builder.setUserId(user.getUserId());
            if (!StringUtil.isEmpty(user.getNickName())) {
                builder.setNickName(user.getNickName());
            }
            if (!StringUtil.isEmpty(user.getAvatar())) {
                builder.setAvatar(user.getAvatar());
            }
            if (!StringUtil.isEmpty(user.getGender())) {
                boolean gender = user.getGender().equalsIgnoreCase("m");
                builder.setGender(gender);
            }
            builder.setVip(user.getVip());
            return builder.build();
        }
        return null;
    }

    private static PBBBSReward bbsRewardToProto(BBSReward reward) {
        if (reward != null) {
            PBBBSReward.Builder builder = PBBBSReward.newBuilder();
            builder.setBonus(reward.getBonus());
            builder.setStatus(reward.getStatus());
            builder.setAwardDate(DateUtil.dateToInt(reward.getAwardDate()));
            if (reward.getActionId() != null) {
                builder.setActionId(reward.getActionId());
            }
            BBSUser user = reward.getWinner();
            if (user != null) {
                PBBBSUser pbUser = bbsUserToProto(user);
                builder.setWinner(pbUser);
            }
            return builder.build();
        }
        return null;
    }

    private static PBBBSContent bbsContentToProto(BBSContent content,
                                                  boolean withoutData) {
        if (content != null) {
            PBBBSContent.Builder builder = PBBBSContent.newBuilder();
            builder.setType(content.getType());
            if (!StringUtil.isEmpty(content.getText())) {
                builder.setText(content.getText());
            }
            switch (content.getType()) {
                case BBSContent.ContentTypeImage:
                    BBSImageContent imageContent = (BBSImageContent) content;
                    if (!StringUtil.isEmpty(imageContent.getLargeImageURL())) {
                        builder.setImageUrl(imageContent.getLargeImageURL());
                    }
                    if (!StringUtil.isEmpty(imageContent.getThumbImageURL())) {
                        builder.setThumbImageUrl(imageContent.getThumbImageURL());
                    }
                    break;
                case BBSContent.ContentTypeDraw:
                    BBSDrawContent drawContent = (BBSDrawContent) content;
                    if (!StringUtil.isEmpty(drawContent.getDrawLargeImageURL())) {
                        builder.setDrawImageUrl(drawContent.getDrawLargeImageURL());
                    }
                    if (!StringUtil.isEmpty(drawContent.getDrawThumbImageURL())) {
                        builder.setDrawThumbUrl(drawContent.getDrawThumbImageURL());
                    }
                    if (!withoutData) {
                        byte[] drawData = drawContent.getDrawData();
                        if (drawData != null && drawData.length != 0) {
                            try {
                                PBBBSDraw draw = PBBBSDraw.parseFrom(drawData);
                                builder.setDrawData(draw);
                            } catch (InvalidProtocolBufferException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    break;

                default:
                {
                    if (!StringUtil.isEmpty(content.getLargeImageURL())) {
                        builder.setImageUrl(content.getLargeImageURL());
                    }

                    if (!StringUtil.isEmpty(content.getThumbImageURL())) {
                        builder.setThumbImageUrl(content.getThumbImageURL());
                    }

                    if (!StringUtil.isEmpty(content.getOpusId())){
                        builder.setOpusId(content.getOpusId());
                        builder.setOpusCategory(content.getOpusCategory());
                    }
                }
                    break;
            }
            return builder.build();
        }
        return null;
    }

    public static byte[]    bbsPostListToProto(List<BBSPost> postList, String userId, String userGroupId) {
        int count = postList.size();
        if (count == 0) {
            return protocolBufferNoData();
        }
        List<PBBBSPost> pList = new ArrayList<PBBBSPost>(count);
        for (BBSPost post : postList) {
            PBBBSPost pbPost = bbsPostToProto(post, true, userId, userGroupId);
            pList.add(pbPost);
        }

        logger.debug("<bbsPostListToProto> post list="+pList.toString());

        DataQueryResponse response = GameMessageProtos.DataQueryResponse
                .newBuilder().setResultCode(ErrorCode.ERROR_SUCCESS)
                .addAllBbsPost(pList).build();
        return response.toByteArray();
    }

    public static byte[] bbsPrivilegeListToProto(List<BBSPrivilege> list) {
        int count = list.size();
        if (count == 0) {
            return protocolBufferNoData();
        }
        DataQueryResponse.Builder responseBuilder = GameMessageProtos.DataQueryResponse
                .newBuilder();

        for (BBSPrivilege privilege : list) {
            PBBBSPrivilege.Builder builder = PBBBSPrivilege.newBuilder();
            builder.setBoardId(privilege.getBoardId());
            builder.setPermission(privilege.getPermission());
            responseBuilder.addBbsPrivilegeList(builder.build());
        }
        return responseBuilder.setResultCode(ErrorCode.ERROR_SUCCESS).build()
                .toByteArray();
    }

    public static Object simplePostToJson(BBSPost post) {

        JSONObject object = new JSONObject();
        safePut(object, ServiceConstant.PARA_POSTID, post.getPostId());
        BBSContent content = post.getContent();
        if (content != null) {
            if (BBSContent.ContentTypeImage == content.getType()) {
                BBSImageContent imageContent = (BBSImageContent) content;
                safePut(object, ServiceConstant.PARA_IMAGE, imageContent.getLargeImageURL());
                safePut(object, ServiceConstant.PARA_THUMB_IMAGE, imageContent.getThumbImageURL());
            }
            else if (BBSContent.ContentTypeDraw == content.getType()) {
                BBSDrawContent drawContent = (BBSDrawContent) content;
                safePut(object, ServiceConstant.PARA_DRAW_IMAGE, drawContent
                        .getDrawLargeImageURL());
                safePut(object, ServiceConstant.PARA_DRAW_THUMB, drawContent
                        .getDrawThumbImageURL());
            }
            else{
                safePut(object, ServiceConstant.PARA_IMAGE, content.getLargeImageURL());
                safePut(object, ServiceConstant.PARA_THUMB_IMAGE, content.getThumbImageURL());
                safePut(object, ServiceConstant.PARA_CATEGORY, content.getOpusCategory());
                safePut(object, ServiceConstant.PARA_OPUS_ID, content.getOpusId());
            }

        }
        return object;
    }

    public static Object simpleActionToJson(BBSAction action) {
        JSONObject object = new JSONObject();
        safePut(object, ServiceConstant.PARA_ACTIONID, action.getActionId());
        BBSContent content = action.getContent();
        if (content != null) {
            if (BBSContent.ContentTypeImage == content.getType()) {
                BBSImageContent imageContent = (BBSImageContent) content;
                safePut(object, ServiceConstant.PARA_IMAGE, imageContent
                        .getLargeImageURL());
                safePut(object, ServiceConstant.PARA_THUMB_IMAGE, imageContent
                        .getThumbImageURL());
            }
            else if (BBSContent.ContentTypeDraw == content.getType()) {
                BBSDrawContent drawContent = (BBSDrawContent) content;
                safePut(object, ServiceConstant.PARA_DRAW_IMAGE, drawContent
                        .getDrawLargeImageURL());
                safePut(object, ServiceConstant.PARA_DRAW_THUMB, drawContent
                        .getDrawThumbImageURL());
            }
            else{
                safePut(object, ServiceConstant.PARA_IMAGE, content.getLargeImageURL());
                safePut(object, ServiceConstant.PARA_THUMB_IMAGE, content.getThumbImageURL());
                safePut(object, ServiceConstant.PARA_CATEGORY, content.getOpusCategory());
                safePut(object, ServiceConstant.PARA_OPUS_ID, content.getOpusId());
            }
        }
        return object;
    }

    public static byte[] bbsActionListToProto(List<BBSAction> actionList) {
        if (actionList == null || actionList.isEmpty()) {
            return protocolBufferNoData();
        }
        List<PBBBSAction> retList = new ArrayList<PBBBSAction>(actionList
                .size());
        for (BBSAction action : actionList) {
            PBBBSAction pbAction = bbsActionToPBBBSAction(action);
            if (pbAction != null) {
                retList.add(pbAction);
            }
        }
        DataQueryResponse response = GameMessageProtos.DataQueryResponse
                .newBuilder().setResultCode(ErrorCode.ERROR_SUCCESS)
                .addAllBbsAction(retList).build();
        return response.toByteArray();
    }

    private static PBBBSAction bbsActionToPBBBSAction(BBSAction action) {
        if (action != null) {
            PBBBSAction.Builder builder = PBBBSAction.newBuilder();
            builder.setActionId(action.getActionId());
            builder.setType(action.getType());
            builder.setDeviceType(action.getDeviceType());
            builder.setCreateDate(DateUtil.dateToInt(action.getCreateDate()));
            builder.setReplyCount(action.getReplyCount());

            // SET CREATE USER
            PBBBSUser createUser = bbsUserToProto(action.getCreateUser());
            builder.setCreateUser(createUser);
            // SET CONTENT
            PBBBSContent content = bbsContentToProto(action.getContent(), true);
            if (content != null) {
                builder.setContent(content);
            }
            // SET SOURCE
            PBBBSActionSource source = bbsActionSourceToPBBBSActionSource(action
                    .getActionSource());
            if (source != null) {
                builder.setSource(source);
            }
            return builder.build();
        }
        return null;

    }

    private static PBBBSActionSource bbsActionSourceToPBBBSActionSource(
            BBSActionSource actionSource) {
        if (actionSource != null) {
            PBBBSActionSource.Builder builder = PBBBSActionSource.newBuilder();
            builder.setPostId(actionSource.getPostId());
            builder.setPostUid(actionSource.getPostUid());
            if (actionSource.getActionId() != null) {
                builder.setActionId(actionSource.getActionId());
            }
            if (actionSource.getActionUid() != null) {
                builder.setActionUid(actionSource.getActionUid());
            }
            if (actionSource.getActionNickName() != null) {
                builder.setActionNick(actionSource.getActionNickName());
            }
            if (actionSource.getBriefText() != null) {
                builder.setBriefText(actionSource.getBriefText());
            }
            builder.setActionType(actionSource.getActionType());
            return builder.build();
        }
        return null;
    }

    public static byte[] bbsDrawDataToProto(byte[] byteData) {

        if (byteData == null) {
            return protocolBufferNoData();
        }
        PBBBSDraw draw;
        try {
            draw = PBBBSDraw.parseFrom(byteData);
            DataQueryResponse response = GameMessageProtos.DataQueryResponse
                    .newBuilder().setResultCode(ErrorCode.ERROR_SUCCESS)
                    .setBbsDrawData(draw).build();
            return response.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return protocolBufferNoData();
        }
    }


    public static Object bulletinListToJSON(List<Bulletin> bulletins) {

        if (bulletins != null && !bulletins.isEmpty()) {
            JSONArray jArray = new JSONArray();
            for (Bulletin bulletin : bulletins) {
                JSONObject object = new JSONObject();

                safePut(object, ServiceConstant.PARA_BULLETIN_ID, bulletin
                        .getBulletinId());
                safePut(object, ServiceConstant.PARA_TYPE, bulletin.getType());

                Date createDate = bulletin.getCreateDate();
                if (createDate != null) {
                    safePut(object, ServiceConstant.PARA_CREATE_DATE, DateUtil
                            .dateToString(createDate));
                }

                String url = bulletin.getUrl();
                if (url != null) {
                    safePut(object, ServiceConstant.PARA_URL, url);
                }
                safePut(object, ServiceConstant.PARA_GAME_ID, bulletin
                        .getGameId());
                safePut(object, ServiceConstant.PARA_FUNCTION, bulletin
                        .getFunction());
                safePut(object, ServiceConstant.PARA_BULLETIN_CONTENT, bulletin
                        .getMessagge());
                safePut(object, ServiceConstant.PARA_BULLETIN_FUNCTION_PARA, bulletin
                        .getFunctionParameter());

                jArray.add(object);
            }
            return jArray;
        }

        return null;

    }

    public static Object hotWordListToJSON(List<String> hotwords) {

        if (hotwords != null) {
            JSONArray jArray = new JSONArray();
            for (String word : hotwords) {
                JSONObject object = new JSONObject();
                safePut(object, ServiceConstant.PARA_HOTWORD_CONTENT, word);
                jArray.add(object);
            }
            return jArray;
        }

        return null;

    }

    public static byte[] userWallListToPB(List<UserWall> userWallList) {

        byte[] byteData = null;

        // set reponse
        DataQueryResponse.Builder builder = GameMessageProtos.DataQueryResponse
                .newBuilder().setResultCode(ErrorCode.ERROR_SUCCESS);

        // add walls
        for (UserWall wall : userWallList) {
            PBWall pbWall = wall.toPBWall();
            builder.addWallList(pbWall);
        }
        byteData = builder.build().toByteArray();

        return byteData;
    }

    public static byte[] objectIdListToPBData(List<ObjectId> list) {

        if (list == null || list.isEmpty()) {
            return protocolBufferNoData();
        }
        // set reponse
        try {
            DataQueryResponse.Builder builder = GameMessageProtos.DataQueryResponse
                    .newBuilder().setResultCode(ErrorCode.ERROR_SUCCESS);

            for (ObjectId oId : list) {
                if (oId != null && oId.toString() != null) {
                    builder.addIdList(oId.toString());
                }
            }
            byte[] byteData = builder.build().toByteArray();

            return byteData;

        } catch (Exception e) {
            logger.error("<objectIdListToPBData> exception = " + e.toString());
            return protocolBufferNoData();
        }
    }

    public static void setOpusForPBOpusBuilder(Opus opus, OpusProtos.PBOpus.Builder builder, AbstractXiaoji xiaoji) {

        builder.setOpusId(opus.getOpusId());
        builder.setType(OpusProtos.PBOpusType.valueOf(opus.getType()));
        builder.setName(opus.getName());
        if (opus.getDescription() != null)
            builder.setDesc(opus.getDescription());

        if (opus.getOpusImageUrl() != null)
            builder.setImage(xiaoji.getImageUploadManager().getRemoteURL(opus.getOpusImageUrl()));
        if (opus.getOpusThumbImageUrl() != null)
            builder.setThumbImage(xiaoji.getImageUploadManager().getRemoteURL(opus.getOpusThumbImageUrl()));
        if (opus.getDataUrl() != null)
            builder.setDataUrl(xiaoji.getDataUploadManager().getRemoteURL(opus.getDataUrl()));

        builder.setLanguage(OpusProtos.PBLanguage.valueOf(opus.getLanguage()));

        builder.setCategory(GameConstantsProtos.PBOpusCategoryType.valueOf(opus.getCategory()));

        builder.setCreateDate((int) (opus.getCreateDate().getTime() / 1000));
        builder.setStatus(opus.getOpusStatus());

        builder.setDeviceType(opus.getDeviceType());
        if (opus.getDeviceModel() != null)
            builder.setDeviceName(opus.getDeviceModel());
        if (opus.getAppId() != null)
            builder.setAppId(opus.getAppId());

        // author information
        if (opus.getCreateUserId() != null) {
            GameBasicProtos.PBGameUser.Builder userBuilder = GameBasicProtos.PBGameUser.newBuilder();
            userBuilder.setUserId(opus.getCreateUserId());
            if (opus.getNickName() != null) {
                userBuilder.setNickName(opus.getNickName());
            } else {
                userBuilder.setNickName("");
            }

            if (opus.getGender() != null) {
                userBuilder.setGender(User.boolGender(opus.getGender()));
            }

            if (opus.getSignature() != null) {
                userBuilder.setSignature(opus.getSignature());
            }

            if (opus.getAvatar() != null) {
                userBuilder.setAvatar(opus.getAvatar());
            }

            builder.setAuthor(userBuilder.build());
        }

        if (!StringUtil.isEmpty(opus.getContestId())) {
            builder.setContestId(opus.getContestId());
        }

        // target user
        if (opus.getTargetUserId() != null) {
            GameBasicProtos.PBGameUser.Builder userBuilder = GameBasicProtos.PBGameUser.newBuilder();
            userBuilder.setUserId(opus.getTargetUserId());
            if (opus.getTargetNickName() != null) {
                userBuilder.setNickName(opus.getTargetNickName());
            } else {
                userBuilder.setNickName("");
            }

            builder.setTargetUser(userBuilder.build());
        }

        // TODO action times

        // Xiaoji Specific OPUS
        xiaoji.opusToPB(opus, builder);

//        logger.info("<setOpusForPBOpusBuilder> builder data=" + builder.buildPartial().toString());
    }

    public static GameBasicProtos.PBGameUser userToPBGameUser(User user) {

        if (user == null) {
//            return protocolBufferErrorNoData(ErrorCode.ERROR_USERID_NOT_FOUND);
            return null;
        }

        try {
            GameBasicProtos.PBGameUser.Builder builder = GameBasicProtos.PBGameUser.newBuilder();
            builder.setUserId(user.getUserId());

            String nickName = user.getNickName();
            if (nickName != null)
                builder.setNickName(nickName);
            else {
                builder.setNickName("");
            }

            String avatar = user.getAvatar();
            if (avatar != null)
                builder.setAvatar(avatar);

            boolean gender = user.getBoolGender();
            builder.setGender(gender);

            String location = user.getLocation();
            if (location != null)
                builder.setLocation(location);

//            int level = user.getLevelByAppId(appId);
//            builder.setLevel(level);
//            builder.setUserLevel(level);
//
//            long exp = user.getExpByAppId(appId);
//            builder.setExperience(exp);
//
//            String email = user.getEmail();
//            if (email != null)
//                builder.setEmail(email);
//
//            String password = user.getPassword();
//            if (password != null)
//                builder.setPassword(password);
//
//            builder.setCoinBalance(user.getBalance());
//            builder.setIngotBalance(user.getIngotBalance());
//            builder.setFeatureOpus(user.getFeatureOpus());
//
//            // sns user - SINA
//            if (!StringUtil.isEmpty(user.getSinaID()) && !StringUtil.isEmpty(user.getSinaNickName())){
//                String snsId = user.getSinaID();
//                String snsNick = user.getSinaNickName();
//                String accessToken = user.getSinaAccessToken();
//                String refreshToken = user.getSinaRefreshToken();
//                int expireTime = user.getSinaExpireDate();
//
//                GameBasicProtos.PBSNSUser.Builder snsBuilder = GameBasicProtos.PBSNSUser.newBuilder();
//                snsBuilder.setType(ServiceConstant.REGISTER_TYPE_SINA);
//                snsBuilder.setUserId(snsId);
//                snsBuilder.setNickName(snsNick);
//
//                if (accessToken != null){
//                    snsBuilder.setAccessToken(accessToken);
//                }
//
//                if (refreshToken != null){
//                    snsBuilder.setRefreshToken(refreshToken);
//                }
//
//                snsBuilder.setExpireTime(expireTime);
//                builder.addSnsUsers(snsBuilder.build());
//            }
//
//            // sns user - QQ
//            if (!StringUtil.isEmpty(user.getQQID()) && !StringUtil.isEmpty(user.getQQNickName())){
//                String snsId = user.getQQID();
//                String snsNick = user.getQQNickName();
//                String accessToken = user.getQQAccessToken();
//                String refreshToken = user.getQQRefreshToken();
//                int expireTime = user.getQQExpireDate();
//                String qqOpenId = user.getQQOpenId();
//
//                GameBasicProtos.PBSNSUser.Builder snsBuilder = GameBasicProtos.PBSNSUser.newBuilder();
//                snsBuilder.setType(ServiceConstant.REGISTER_TYPE_QQ);
//                snsBuilder.setUserId(snsId);
//                snsBuilder.setNickName(snsNick);
//
//                if (accessToken != null){
//                    snsBuilder.setAccessToken(accessToken);
//                }
//
//                if (refreshToken != null){
//                    snsBuilder.setRefreshToken(refreshToken);
//                }
//
//                if (qqOpenId != null){
//                    snsBuilder.setQqOpenId(qqOpenId);
//                }
//
//                snsBuilder.setExpireTime(expireTime);
//                builder.addSnsUsers(snsBuilder.build());
//            }
//
//            if (!StringUtil.isEmpty(user.getFacebookId())){
//                String snsId = user.getFacebookId();
//                String snsNick = user.getFacebookNickName();
//                String accessToken = user.getFacebookAccessToken();
//                String refreshToken = user.getFacebookRefreshToken();
//                int expireTime = user.getFacebookExpireDate();
//
//                GameBasicProtos.PBSNSUser.Builder snsBuilder = GameBasicProtos.PBSNSUser.newBuilder();
//                snsBuilder.setType(ServiceConstant.REGISTER_TYPE_FACEBOOK);
//                snsBuilder.setUserId(snsId);
//                snsBuilder.setNickName(snsNick);
//
//                if (accessToken != null){
//                    snsBuilder.setAccessToken(accessToken);
//                }
//
//                if (refreshToken != null){
//                    snsBuilder.setRefreshToken(refreshToken);
//                }
//
//                snsBuilder.setExpireTime(expireTime);
//                builder.addSnsUsers(snsBuilder.build());
//            }
//
//            // user item
////            setItemIntoUserBuilder(builder, user);
//
//            String birthday = user.getBirthday();
//            if (birthday != null)
//                builder.setBirthday(birthday);
//
//            builder.setZodiac(user.getZodiac());
//            builder.setGuessWordLanguage(user.getGuessWordLanguage());
//
//            String deviceToken = user.getDeviceToken();
//            if (deviceToken != null)
//                builder.setDeviceToken(deviceToken);
//
//            String backgroundURL = user.getBackgroundRemoteURL();
//            if (backgroundURL != null)
//                builder.setBackgroundURL(backgroundURL);

            String signature = user.getSignature();
            if (signature != null)
                builder.setSignature(signature);

//            String blood = user.getBlood();
//            if (blood != null)
//                builder.setBloodGroup(blood);
//
//            int singRecordLimit = user.getSingRecordLimit();
//            if (singRecordLimit != 0){
//                builder.setSingRecordLimit(singRecordLimit);
//            }
//
//            GameBasicProtos.PBOpenInfoType openInfoType = user.getOpenInfoType();
//            builder.setOpenInfoType(openInfoType);
//
//            builder.setFanCount(user.getFanCount());
//            builder.setFollowCount(user.getFollowCount());

            GameBasicProtos.PBGameUser pbUser = builder.build();

            return pbUser;

        } catch (Exception e) {
            logger.error("<userToPB> catch exception" + e.toString(), e);
            return null;
        }
    }

    public static void setCanvasAndDescLabelInfo(UserAction opus, OpusProtos.PBOpus.Builder builder) {
        if (opus.getDescLabelInfo() != null) {
            DrawProtos.PBLabelInfo.Builder labelBuilder = DrawProtos.PBLabelInfo.newBuilder();
            labelBuilder.setStyle(opus.getDescStyle());

            DrawProtos.PBRect.Builder rectBuilder = DrawProtos.PBRect.newBuilder();

            int x = opus.getDescFrameX();
            int y = opus.getDescFrameY();
            int width = opus.getDescFrameWidth();
            int height = opus.getDescFrameHeight();

            if (x == 0 && y == 0 && width == 0 && height == 0) {
                int defaultWidth = ((int) opus.getCanvasWidth()) * 8 / 10;
                int defaultHeight = ((int) opus.getCanvasHeight()) * 8 / 10;
                rectBuilder.setX(((int) opus.getCanvasWidth() - defaultWidth) / 2);
                rectBuilder.setY(((int) opus.getCanvasHeight()) / 2);
                rectBuilder.setWidth(defaultWidth);
                rectBuilder.setHeight(defaultHeight);
            } else {
                rectBuilder.setX(opus.getDescFrameX());
                rectBuilder.setY(opus.getDescFrameY());
                rectBuilder.setWidth(opus.getDescFrameWidth());
                rectBuilder.setHeight(opus.getDescFrameHeight());
            }

//            rectBuilder.setX(opus.getDescFrameX());
//            rectBuilder.setY(opus.getDescFrameY());
//            rectBuilder.setWidth(opus.getDescFrameWidth());
//            rectBuilder.setHeight(opus.getDescFrameHeight());
            labelBuilder.setFrame(rectBuilder.build());

            labelBuilder.setTextColor(opus.getDescTextColor());
            labelBuilder.setTextStrokeColor(opus.getDescStrokeTextColor());

            builder.setDescLabelInfo(labelBuilder.build());
        }

        if (opus.getCanvasHeight() > 0.0f && opus.getCanvasWidth() > 0.0f) {
            GameBasicProtos.PBSize.Builder sizeBuilder = GameBasicProtos.PBSize.newBuilder();
            sizeBuilder.setHeight(opus.getCanvasHeight());
            sizeBuilder.setWidth(opus.getCanvasWidth());
            builder.setCanvasSize(sizeBuilder.build());
        }
    }

    public static void setCanvasAndDescLabelInfo(UserAction opus, PBFeed.Builder builder) {
        if (opus.getDescLabelInfo() != null) {
            DrawProtos.PBLabelInfo.Builder labelBuilder = DrawProtos.PBLabelInfo.newBuilder();
            labelBuilder.setStyle(opus.getDescStyle());

            DrawProtos.PBRect.Builder rectBuilder = DrawProtos.PBRect.newBuilder();

            int x = opus.getDescFrameX();
            int y = opus.getDescFrameY();
            int width = opus.getDescFrameWidth();
            int height = opus.getDescFrameHeight();

            if (x == 0 && y == 0 && width == 0 && height == 0) {
                int defaultWidth = ((int) opus.getCanvasWidth()) * 8 / 10;
                int defaultHeight = ((int) opus.getCanvasHeight()) * 8 / 10;
                rectBuilder.setX(((int) opus.getCanvasWidth() - defaultWidth) / 2);
                rectBuilder.setY(((int) opus.getCanvasHeight()) / 2);
                rectBuilder.setWidth(defaultWidth);
                rectBuilder.setHeight(defaultHeight);
            } else {
                rectBuilder.setX(opus.getDescFrameX());
                rectBuilder.setY(opus.getDescFrameY());
                rectBuilder.setWidth(opus.getDescFrameWidth());
                rectBuilder.setHeight(opus.getDescFrameHeight());
            }
            labelBuilder.setFrame(rectBuilder.build());

            labelBuilder.setTextColor(opus.getDescTextColor());
            labelBuilder.setTextStrokeColor(opus.getDescStrokeTextColor());
            labelBuilder.setTextFont(opus.getDescTextFontSize());

            builder.setDescLabelInfo(labelBuilder.build());
        }

        if (opus.getCanvasHeight() > 0.0f && opus.getCanvasWidth() > 0.0f) {
            GameBasicProtos.PBSize.Builder sizeBuilder = GameBasicProtos.PBSize.newBuilder();
            sizeBuilder.setHeight(opus.getCanvasHeight());
            sizeBuilder.setWidth(opus.getCanvasWidth());
            builder.setCanvasSize(sizeBuilder.build());
        }

    }

    public static byte[] opusToPB(Opus opus, AbstractXiaoji xiaoji) {
        OpusProtos.PBOpus.Builder builder = OpusProtos.PBOpus.newBuilder();

        builder.setOpusId(opus.getOpusId());
        builder.setType(OpusProtos.PBOpusType.valueOf(opus.getType()));
        builder.setName(opus.getName());
        if (opus.getDescription() != null)
            builder.setDesc(opus.getDescription());

        if (opus.getOpusImageUrl() != null)
            builder.setImage(xiaoji.getImageUploadManager().getRemoteURL(opus.getOpusImageUrl()));
        if (opus.getOpusThumbImageUrl() != null)
            builder.setThumbImage(xiaoji.getImageUploadManager().getRemoteURL(opus.getOpusThumbImageUrl()));
        if (opus.getDataUrl() != null)
            builder.setDataUrl(xiaoji.getDataUploadManager().getRemoteURL(opus.getDataUrl()));

        builder.setLanguage(OpusProtos.PBLanguage.valueOf(opus.getLanguage()));
        builder.setCategory(GameConstantsProtos.PBOpusCategoryType.valueOf(opus.getCategory()));

        builder.setCreateDate((int) (opus.getCreateDate().getTime() / 1000));
        builder.setStatus(opus.getOpusStatus());
        builder.setSpendTime(opus.getSpendTime());
        if (opus.getTags() != null) {
            builder.addAllTags(opus.getTags());
        }

        builder.setDeviceType(opus.getDeviceType());
        if (opus.getDeviceModel() != null)
            builder.setDeviceName(opus.getDeviceModel());
        if (opus.getAppId() != null)
            builder.setAppId(opus.getAppId());

        // author information
        if (opus.getCreateUserId() != null) {
            GameBasicProtos.PBGameUser.Builder userBuilder = GameBasicProtos.PBGameUser.newBuilder();
            userBuilder.setUserId(opus.getCreateUserId());
            if (opus.getNickName() != null) {
                userBuilder.setNickName(opus.getNickName());
            } else {
                userBuilder.setNickName("");
            }

            if (opus.getGender() != null) {
                userBuilder.setGender(User.boolGender(opus.getGender()));
            }

            if (opus.getSignature() != null) {
                userBuilder.setSignature(opus.getSignature());
            }

            if (opus.getAvatar() != null) {
                userBuilder.setAvatar(opus.getAvatar());
            }

            builder.setAuthor(userBuilder.build());
        }

        if (!StringUtil.isEmpty(opus.getContestId())) {
            builder.setContestId(opus.getContestId());
        }

        // target user
        if (opus.getTargetUserId() != null) {
            GameBasicProtos.PBGameUser.Builder userBuilder = GameBasicProtos.PBGameUser.newBuilder();
            userBuilder.setUserId(opus.getTargetUserId());
            if (opus.getTargetNickName() != null) {
                userBuilder.setNickName(opus.getTargetNickName());
            } else {
                userBuilder.setNickName("");
            }

            builder.setTargetUser(userBuilder.build());
        }

        if (opus.getDeviceModel() != null){
            builder.setDeviceName(opus.getDeviceModel());
        }

        setCanvasAndDescLabelInfo(opus, builder);

        // TODO action times

        // Xiaoji Specific OPUS
        xiaoji.opusToPB(opus, builder);

        // build pb opus
        OpusProtos.PBOpus pbOpus = builder.build();

        logger.debug("<opusToPB> data=" + pbOpus.toString());

        DataQueryResponse response = GameMessageProtos.DataQueryResponse
                .newBuilder().setResultCode(ErrorCode.ERROR_SUCCESS)
                .setOpus(pbOpus).build();
        return response.toByteArray();
    }

    public static byte[] songListToProtocolBuffer(List<Song> songList) {

        if (songList == null || songList.size() == 0) {
            return protocolBufferNoData();
        }

        DataQueryResponse.Builder responseBuilder = GameMessageProtos.DataQueryResponse
                .newBuilder();

        PBSongList.Builder pbSongListBuilder = PBSongList.newBuilder();
        for (Song song : songList) {
            PBSong.Builder pbSongBuilder = PBSong.newBuilder();

            pbSongBuilder.setSongId(song.getObjectId().toString());
            if (song.getSongName() != null) {
                pbSongBuilder.setName(song.getSongName());
            }

            if (song.getSingerName() != null) {
                pbSongBuilder.setAuthor(song.getSingerName());
            }

            pbSongBuilder.setLyric(song.getLyric());

//            if (song.getAllTags() != null){
//			    pbSongBuilder.addTag(song.getAllTags());
//            }

            pbSongListBuilder.addSongs(pbSongBuilder.build());
        }

        PBSongList songs = pbSongListBuilder.build();
//        logger.info("<songListToPB> songs=" + songs.toString());

        return responseBuilder.setResultCode(ErrorCode.ERROR_SUCCESS)
                .setSongs(songs)
                .build()
                .toByteArray();
    }

    public static byte[] userGuessOpusListToPB(List<Opus> opusList, AbstractXiaoji xiaoji) {

        if (opusList == null || opusList.size() == 0) {
            return protocolBufferNoData();
        }

        DataQueryResponse.Builder responseBuilder = GameMessageProtos.DataQueryResponse
                .newBuilder();

        List<OpusProtos.PBOpus> pbOpusList = new ArrayList<OpusProtos.PBOpus>();
        for (Opus opus : opusList) {
            OpusProtos.PBOpus.Builder builder = OpusProtos.PBOpus.newBuilder();

            setOpusForPBOpusBuilder(opus, builder, xiaoji);

            // TODO add user guess opus infomation here
            setOpusGuessInfo(opus, builder);

            OpusProtos.PBOpus pbOpus = builder.build();
            pbOpusList.add(pbOpus);
        }

        return responseBuilder.setResultCode(ErrorCode.ERROR_SUCCESS)
                .addAllOpusList(pbOpusList)
                .build()
                .toByteArray();

    }

    private static void setOpusGuessInfo(Opus opus, OpusProtos.PBOpus.Builder builder) {

        OpusProtos.PBOpusGuess.Builder guessBuilder = OpusProtos.PBOpusGuess.newBuilder();
        guessBuilder.setIsCorrect(opus.getGuessIsCorrect());
        builder.setGuessInfo(guessBuilder.build());

    }

    public static byte[] contestRankToPBUserGuessRank(TopUserGuessManager manager, UserGuess userGuess, String userId) {

        int ranking = manager.getUserRank(userId);
        int totalPlayUser = manager.getCurrentTopCount();
        int earn = AwardManager.getInstance().getAwardWithRank(ranking, totalPlayUser);
        int pass = userGuess.getPass();
        int spendTime = (int) (userGuess.getSpendTime() / 1000);

        OpusProtos.PBGuessRank.Builder rankBuilder = OpusProtos.PBGuessRank.newBuilder();
        rankBuilder.setRanking(ranking);
        rankBuilder.setTotalPlayer(totalPlayUser);
        rankBuilder.setEarn(earn);
        rankBuilder.setPass(pass);
        rankBuilder.setSpendTime(spendTime);

        OpusProtos.PBGuessRank pbGuessRank = rankBuilder.build();

        if (pbGuessRank == null) {
            return protocolBufferNoData();
        }

        DataQueryResponse.Builder responseBuilder = GameMessageProtos.DataQueryResponse
                .newBuilder();

        return responseBuilder.setResultCode(ErrorCode.ERROR_SUCCESS)
                .setGuessRank(pbGuessRank)
                .build()
                .toByteArray();
    }

    public static OpusProtos.PBGuessRank ahcievementToPBUserGuessRank(TopUserGuessManager manager, String userId) {

        UserGuessAchievement achievement = manager.getAchievement(userId);
        int ranking = manager.getUserRank(userId);
        int totalPlayUser = manager.getCurrentTopCount();
        User user = UserManager.findPublicUserInfoByUserId(DBService.getInstance().getMongoDBClient(), userId);
        String achievementKey = manager.getAchievementKey();

        if (achievement == null) {
            return null;
        }

        OpusProtos.PBGuessRank.Builder rankBuilder = OpusProtos.PBGuessRank.newBuilder();

        GameBasicProtos.PBGameUser pbGameUser = userToPBGameUser(user);
        if (pbGameUser != null) {
            rankBuilder.setUser(pbGameUser);
        }

        rankBuilder.setRanking(ranking);
        rankBuilder.setTotalPlayer(totalPlayUser);
        rankBuilder.setGuess(achievement.getTotalGuessTimes(achievementKey));
        rankBuilder.setPass(achievement.getPass(achievementKey));
        rankBuilder.setSpendTime(achievement.getSpendTime(achievementKey) / 1000);
        rankBuilder.setEarn(achievement.getEarn(achievementKey));

        return rankBuilder.build();
    }

    public static byte[] achievementListToPB(List<UserGuessAchievement> list, TopUserGuessManager manager) {

        String achievementKey = manager.getAchievementKey();

        DataQueryResponse.Builder responseBuilder = GameMessageProtos.DataQueryResponse
                .newBuilder();

        if (list == null) {
            return null;
        }

        for (UserGuessAchievement achievement : list) {

            // TODO low performance here
            String userId = achievement.getUserId(achievementKey).toString();
            OpusProtos.PBGuessRank pbGuessRank = ahcievementToPBUserGuessRank(manager, userId);

            responseBuilder.addGuessRankList(pbGuessRank);
        }

        int totalCount = manager.getCurrentTopCount();
        responseBuilder.setResultCode(ErrorCode.ERROR_SUCCESS);
        responseBuilder.setTotalCount(totalCount);

        DataQueryResponse response = responseBuilder.build();
//        logger.info("<userGuessListToPB> response = " + response.toString());

        return response.toByteArray();
    }

    public static byte[] guessTodayContestListToPB(AbstractXiaoji xiaoji) {

        DataQueryResponse.Builder responseBuilder = GameMessageProtos.DataQueryResponse
                .newBuilder();

        responseBuilder.addGuessContestList(guessContestToPBGuessContest(xiaoji, new Date()));

        responseBuilder.setResultCode(ErrorCode.ERROR_SUCCESS);

        DataQueryResponse response = responseBuilder.build();
//        logger.info("<userGuessListToPB> response = " + response.toString());
        return response.toByteArray();
    }

    private static OpusProtos.PBGuessContest guessContestToPBGuessContest(AbstractXiaoji xiaoji, Date date) {

        OpusProtos.PBGuessContest.Builder builder = OpusProtos.PBGuessContest.newBuilder();
        builder.setContestId(UserGuessUtil.getContestId(xiaoji.getCategoryName(), date));

        // set start time and end time
        int currentTime = (int) (DateUtil.getCurrentTime() / 1000);
        int startTime = UserGuessUtil.getContestStartTime();
        int endTime = UserGuessUtil.getContestEndTime();

        builder.setStartTime(startTime);
        builder.setEndTime(endTime);

        if (currentTime <= startTime) {
            builder.setState(OpusProtos.PBGuessContestState.GUESS_CONTEST_STATE_NOT_START_VALUE);
        } else if (currentTime > startTime && currentTime < endTime) {
            builder.setState(OpusProtos.PBGuessContestState.GUESS_CONTEST_STATE_ING_VALUE);
        } else {
            builder.setState(OpusProtos.PBGuessContestState.GUESS_CONTEST_STATE_END_VALUE);
        }

        return builder.build();
    }

    public static byte[] guessRecentContestListToPB(AbstractXiaoji xiaoji) {

        DataQueryResponse.Builder responseBuilder = GameMessageProtos.DataQueryResponse
                .newBuilder();

        responseBuilder.addGuessContestList(guessContestToPBGuessContest(xiaoji, new Date()));
        responseBuilder.addGuessContestList(guessContestToPBGuessContest(xiaoji, DateUtil.getDateBeforeToday(1)));
        responseBuilder.addGuessContestList(guessContestToPBGuessContest(xiaoji, DateUtil.getDateBeforeToday(2)));
        responseBuilder.addGuessContestList(guessContestToPBGuessContest(xiaoji, DateUtil.getDateBeforeToday(3)));
        responseBuilder.addGuessContestList(guessContestToPBGuessContest(xiaoji, DateUtil.getDateBeforeToday(4)));
        responseBuilder.addGuessContestList(guessContestToPBGuessContest(xiaoji, DateUtil.getDateBeforeToday(5)));
        responseBuilder.addGuessContestList(guessContestToPBGuessContest(xiaoji, DateUtil.getDateBeforeToday(6)));

        responseBuilder.setResultCode(ErrorCode.ERROR_SUCCESS);

        DataQueryResponse response = responseBuilder.build();
//        logger.info("<userGuessListToPB> response = " + response.toString());
        return response.toByteArray();
    }

    public static byte[] groupListToPB(List<Group> groups) {
        return toPBList(groups);
    }

    public static byte[] groupToPB(Group group) {
        DataQueryResponse.Builder responseBuilder = GameMessageProtos.DataQueryResponse
                .newBuilder();
        responseBuilder.setResultCode(ErrorCode.ERROR_SUCCESS);

        if (group != null) {
            GroupProtos.PBGroup pbGroup = group.toProtoBufModel();
            responseBuilder.setGroup(pbGroup);
        }
        DataQueryResponse response = responseBuilder.build();
        return response.toByteArray();
    }

    public static byte[] groupMemberListToPB(List<GroupUsersByTitle> members) {
        return toPBList(members);
    }

    public static byte[] userListToPB(List<User> userList) {
        return toPBList(userList);
    }

    public static byte[] toPBList(Collection<? extends ProtoBufCoding> list) {
        if (list == null || list.isEmpty()) {
            return protocolBufferNoData();
        }
        DataQueryResponse.Builder responseBuilder = GameMessageProtos.DataQueryResponse
                .newBuilder();
        responseBuilder.setResultCode(ErrorCode.ERROR_SUCCESS);
        for (ProtoBufCoding model : list) {
            model.addIntoResponse(responseBuilder);
        }
        DataQueryResponse response = responseBuilder.build();
        return response.toByteArray();
    }

    public static byte[] badgesToPB(Map<Integer, Integer> badges) {
        if (badges == null || badges.isEmpty()) {
            return protocolBufferNoData();
        }
        DataQueryResponse.Builder responseBuilder = GameMessageProtos.DataQueryResponse
                .newBuilder();
        responseBuilder.setResultCode(ErrorCode.ERROR_SUCCESS);
        for (Integer key : badges.keySet()) {
            Integer value = badges.get(key);
            GameBasicProtos.PBIntKeyIntValue.Builder buider = GameBasicProtos.PBIntKeyIntValue.newBuilder();
            buider.setKey(key);
            buider.setValue(value);
            responseBuilder.addBadges(buider);
        }
        DataQueryResponse response = responseBuilder.build();
        return response.toByteArray();

    }

    public static byte[] buildReponseWithURL(String imageURL) {
        DataQueryResponse.Builder responseBuilder = GameMessageProtos.DataQueryResponse
                .newBuilder();
        responseBuilder.setResultCode(ErrorCode.ERROR_SUCCESS);
        responseBuilder.setUrl(imageURL);
        DataQueryResponse response = responseBuilder.build();
        return response.toByteArray();

    }
}
