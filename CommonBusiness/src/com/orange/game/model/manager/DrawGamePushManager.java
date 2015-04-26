package com.orange.game.model.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.orange.game.model.manager.bbs.BBSManager;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;

import com.orange.common.mongodb.MongoDBClient;
import com.orange.common.utils.StringUtil;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.Message;
import com.orange.game.model.dao.Room;
import com.orange.game.model.dao.User;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.dao.bbs.BBSAction;
import com.orange.game.model.dao.bbs.BBSActionSource;
import com.orange.game.model.dao.bbs.BBSContent;
import com.orange.game.model.dao.bbs.BBSPost;
import com.orange.game.model.dao.bbs.BBSUser;
import com.orange.game.model.manager.friend.FriendManager;
import com.orange.game.model.service.DBService;
import com.orange.game.model.xiaoji.AbstractXiaoji;
import com.orange.game.model.xiaoji.XiaojiFactory;

public class DrawGamePushManager {
	private static final int CONTENT_MAX_LENGTH = 13;
	private static final int NICK_MAX_LENGTH = 9;

	private static MongoDBClient mongoClient = DBService.getInstance().getMongoDBClient(); //new MongoDBClient(DBConstants.D_GAME);

	private static Logger logger = Logger.getLogger("DrawGamePushManager");

	private static String DRAW_LOCALIZE_FORMAT_KEY = "kPushMessage";

	private static String PUSH_MESSAGE_KEY_ASK_LOCATION = "kPALM";
	private static String PUSH_MESSAGE_KEY_REPLY_LOCATION = "kPRLM";
	private static String PUSH_MESSAGE_KEY_REJECT_LOCATION = "kPRRLM";

	private static String DRAW_LOCALIZE_FORMAT_KEY_TEXT = "kPushTextMessage";
	private static String DRAW_LOCALIZE_FORMAT_KEY_DRAW = "kPushDrawMessage";
	private static String DRAW_LOCALIZE_FORMAT_KEY_FAN = "kFM";
	private static String DRAW_LOCALIZE_FORMAT_KEY_DRAW_TOUSER = "kPDUM";

	private static String DRAW_LOCALIZE_FORMAT_KEY_FLOWER = "kPSF";
	private static String DRAW_LOCALIZE_FORMAT_KEY_TOMATO = "kPTT";
	private static String DRAW_LOCALIZE_FORMAT_KEY_COMMENT = "kPCM";
	private static String DRAW_LOCALIZE_FORMAT_KEY_REPLY = "kPRCM";

	// bbs
	private static String DRAW_LOCALIZE_FORMAT_KEY_BBS_PAY = "kPBP";
	private static String DRAW_LOCALIZE_FORMAT_KEY_BBS_COMMENT = "kPBC";
	private static String DRAW_LOCALIZE_FORMAT_KEY_BBS_REPLY = "kPBR";
	private static String DRAW_LOCALIZE_FORMAT_KEY_BBS_SUPPORT = "kPBS";

	private static Integer PUSH_TYPE_ROOM = 1;
	private static Integer PUSH_TYPE_MESSAGE = 2;
	private static Integer PUSH_TYPE_FEED = 3;// guess
	private static Integer PUSH_TYPE_FAN = 4;
	private static Integer PUSH_TYPE_COMMENT = 5;
	private static Integer PUSH_TYPE_REPLY = 6;
	private static Integer PUSH_TYPE_DRAWTOME = 7;
	private static Integer PUSH_TYPE_FLOWER = 8;
	private static Integer PUSH_TYPE_TOMATO = 9;
	private static Integer PUSH_TYPE_BBSPAY = 10;
	private static Integer PUSH_TYPE_BBSCOMMENT = 11;
	private static Integer PUSH_TYPE_BBSREPLY = 12;
	private static Integer PUSH_TYPE_BBSSUPPORT = 13;

	private static String PUSH_TYPE_KEY = "PT";

	private static String FAN_BADGE = "FAB";
	private static String MESSAGE_BADGE = "MB";
	private static String FEED_BADGE = "FEB";
	private static String ROOM_BADGE = "RB";
	private static String COMMENT_BADGE = "CB";
	private static String REPLY_BADGE = "RPB";
	private static String DRAWTOME_BADGE = "DB";
	private static String FLOWER_BADGE = "FLB";
	private static String TOMATO_BADGE = "TMB";
	private static String TIME_LINE_OPUS_BADGE = "TLOB";
	private static String TIME_LINE_GUESS_BADGE = "TLGB";

	private static String BBS_COMMENT_BADGE = "BCB";

	// private static String BBS_PAY_BADGE = "BPB";

	private static String getShortText(String longText, int maxLen) {
		if (longText == null) {
			return "";
		}
		if (longText.length() > maxLen + 1) {
			return longText.substring(0, maxLen);
		}
		return longText;
	}

	private static int getBadgeAndSetUserInfo(String userId,
			HashMap<String, Object> userInfo,String appId) {

		int sum = 0;

		int feedBadge = 0;
		int messageBadge = 0; // (int) MessageManager.getNewMessageCount(mongoClient, userId);   // rem for performance
		int fanBadge = 0; // FriendManager.getFriendfansmanager().getCountData(userId).getNewCount(); // rem for performance
		int roomBadge = 0;
		AbstractXiaoji xiaoji = XiaojiFactory.getInstance().getXiaoji(appId);
		int commentBadge = 0;
		int drawToMeBadge = 0;
		int timelineGuessBadge = 0;
		int timelineOpusBadge= 0;

        /*
		if(xiaoji != null){
            commentBadge = xiaoji.commentTimelineManager().getCountData(userId).getNewCount();
            drawToMeBadge = xiaoji.drawToUserOpusManager().getCountData(userId).getNewCount();

            // rem for performance
//            timelineGuessBadge = xiaoji.guessOpusTimelineByCategoryManager().getCountData(userId).getNewCount();
//            timelineOpusBadge = xiaoji.opusTimelineByCategoryManager().getCountData(userId).getNewCount();

		}
		*/

		int bbsCommentBadge = 0; // BBSManager.getNewBBSActionCount(mongoClient, userId);

		userInfo.put(FAN_BADGE, fanBadge);
		userInfo.put(FEED_BADGE, feedBadge);
		userInfo.put(MESSAGE_BADGE, messageBadge);
		userInfo.put(ROOM_BADGE, roomBadge);
		userInfo.put(COMMENT_BADGE, commentBadge);
		userInfo.put(REPLY_BADGE, commentBadge);
		userInfo.put(DRAWTOME_BADGE, drawToMeBadge);
		userInfo.put(BBS_COMMENT_BADGE, bbsCommentBadge);
		userInfo.put(TIME_LINE_GUESS_BADGE, timelineGuessBadge);
		userInfo.put(TIME_LINE_OPUS_BADGE, timelineOpusBadge);
		sum = feedBadge + messageBadge + fanBadge + roomBadge + commentBadge + drawToMeBadge;

        logger.info("<getBadge> userId="+userId+", badge info="+userInfo.toString());

        if (sum <= 0){
            sum = 1; // at least bigger than one for push badge
        }

		return sum;
	}

	public static void sendMessage(Room room, String userId, String appId) {
		User user = UserManager.findSimpleUserInfoByUserId(mongoClient, userId);
		if (user != null) {

			String sound = "default";
			String deviceToken = user.getDeviceToken();
            if (deviceToken == null){
                logger.info("send PUSH message, but user "+user.getUserId()+" device token null");
                return;
            }

            HashMap<String, Object> userInfo = new HashMap<String, Object>();
			userInfo.put(PUSH_TYPE_KEY, PUSH_TYPE_ROOM);
			userInfo.put(ServiceConstant.PARA_ROOM_ID, room.getRoomId());
			int badge = getBadgeAndSetUserInfo(userId, userInfo,appId);

			List<String> values = new ArrayList<String>(3);
			values.add(getShortText(room.getCreatorNickName(), NICK_MAX_LENGTH));
			values.add(getShortText(room.getRoomName(), NICK_MAX_LENGTH));

			NotificationService.getInstance().sendMessage(appId, deviceToken,
					badge, DRAW_LOCALIZE_FORMAT_KEY, values, sound, userInfo,
					PUSH_TYPE_ROOM, user, true);
		} else {
			logger.info("send PUSH message, but user null");
		}
	}

	public static void sendMessage(UserAction opus, UserAction guessAction,
			String appId) {

		String userId = opus.getCreateUserId();
		String guessNick = guessAction.getNickName();
		String word = opus.getWord();
		if (StringUtil.isEmpty(userId) || StringUtil.isEmpty(guessNick)
				|| StringUtil.isEmpty(word)) {
			return;
		}
		User user = UserManager.findSimpleUserInfoByUserId(mongoClient, userId);
		if (user != null) {
			String sound = "default";
			String deviceToken = user.getDeviceToken();
            if (deviceToken == null){
                logger.info("send PUSH message, but user "+user.getUserId()+" device token null");
                return;
            }


            List<String> values = new ArrayList<String>();
			values.add(getShortText(guessNick, NICK_MAX_LENGTH));
			values.add(word);

			String formatKey = guessAction.isCorrect() ? "kNGRD" : "kNTGD";
			HashMap<String, Object> userInfo = new HashMap<String, Object>();
			userInfo.put(PUSH_TYPE_KEY, PUSH_TYPE_FEED);
			userInfo.put(ServiceConstant.PARA_FEED_ID, guessAction
					.getActionId());
			int badge = getBadgeAndSetUserInfo(userId, userInfo,appId);

			NotificationService.getInstance().sendMessage(appId, deviceToken,
					badge, formatKey, values, sound, userInfo, PUSH_TYPE_FEED, user, true);
		} else {
			logger.info("send PUSH message, but user or user device token empty");
		}
	}

	public static void sendMessage(Message message, String appId) {
		List<ObjectId> userIdList = new ArrayList<ObjectId>();
		userIdList.add(new ObjectId(message.getFrom()));
		userIdList.add(new ObjectId(message.getTo()));

		HashMap<String, User> userMap = UserManager.getUserMapByUserIdList(
				mongoClient, userIdList);

		User friendUser = userMap.get(message.getTo());
		if (friendUser != null) {

			String sound = "default";
			String deviceToken = friendUser.getDeviceToken();
            if (deviceToken == null){
                logger.info("send PUSH message, but user "+friendUser.getUserId()+" device token null");
                return;
            }

            HashMap<String, Object> userInfo = new HashMap<String, Object>();
			userInfo.put(PUSH_TYPE_KEY, PUSH_TYPE_MESSAGE);
			userInfo.put(ServiceConstant.PARA_MESSAGE_ID, message
					.getMessageId());

			String userId = friendUser.getUserId();
			int badge = getBadgeAndSetUserInfo(userId, userInfo,appId);
			// userInfo.put(ServiceConstant.PARA_MESSAGETEXT,
			// message.getText());

			List<String> values = new ArrayList<String>(3);
			User fromUser = userMap.get(message.getFrom());
			values.add(getShortText(fromUser.getNickName(), NICK_MAX_LENGTH));
			values.add(getShortText(message.getText(), CONTENT_MAX_LENGTH));

			// String messageBody = (message.getText() == null
			// || message.getText().isEmpty() || message.getText()
			// .length() == 0) ? DRAW_LOCALIZE_FORMAT_KEY_DRAW
			// : DRAW_LOCALIZE_FORMAT_KEY_TEXT;

			String messageBody = generatePushMessageBody(message);

			NotificationService.getInstance().sendMessage(appId, deviceToken,
					badge, messageBody, values, sound, userInfo,
					PUSH_TYPE_MESSAGE, friendUser, true);

			logger.info("push message (" + message.getText() + ") to "
					+ message.getTo() + " succeffully, deviceToken = "
					+ deviceToken);
		} else {
			logger
					.info("send PUSH message, but user or user device token empty");
		}
	}

	private static String generatePushMessageBody(Message message) {

		switch (message.getType()) {
		case Message.MessageTypeLocationRequest:
			return PUSH_MESSAGE_KEY_ASK_LOCATION;

		case Message.MessageTypeLocationResponse:
			if (message.getReplyResult() == 0) {
				return PUSH_MESSAGE_KEY_REPLY_LOCATION;
			} else {
				return PUSH_MESSAGE_KEY_REJECT_LOCATION;
			}

		case Message.MessageTypeDraw:
			return DRAW_LOCALIZE_FORMAT_KEY_DRAW;

		case Message.MessageTypeText:
		default:
			return DRAW_LOCALIZE_FORMAT_KEY_TEXT;
		}
	}

	public static void newFanFollow(String userId, String appId) {

		User user = UserManager.findSimpleUserInfoByUserId(mongoClient, userId);
		if (user != null) {

			String sound = "default";
			String deviceToken = user.getDeviceToken();
            if (deviceToken == null){
                logger.info("send PUSH message, but user "+user.getUserId()+" device token null");
                return;
            }


            HashMap<String, Object> userInfo = new HashMap<String, Object>();
			userInfo.put(PUSH_TYPE_KEY, PUSH_TYPE_FAN);
			// userInfo.put(ServiceConstant.PARA_ROOM_ID, room.getRoomId());
			int badge = getBadgeAndSetUserInfo(userId, userInfo,appId);

			List<String> values = new ArrayList<String>(2);
			// values.add(room.getCreatorNickName());
			// values.add(room.getRoomName());

			NotificationService.getInstance().sendMessage(appId, deviceToken,
					badge, DRAW_LOCALIZE_FORMAT_KEY_FAN, values, sound,
					userInfo, PUSH_TYPE_FAN, user, true);
		} else {
			logger
					.info("send PUSH message, but user or user device token empty");
		}
	}

	public static void newComment(UserAction comment, String appId,
			String userId, boolean isReply) {

		User user = UserManager.findSimpleUserInfoByUserId(mongoClient, userId);
		if (user != null) {

			String sound = "default";
			String deviceToken = user.getDeviceToken();
            if (deviceToken == null){
                logger.info("send PUSH message, but user "+user.getUserId()+" device token null");
                return;
            }


            HashMap<String, Object> userInfo = new HashMap<String, Object>();

			int badge = getBadgeAndSetUserInfo(userId, userInfo,appId);

			List<String> values = new ArrayList<String>(3);
			String content = comment.getComment();
			if (content == null) {
				content = "";
			} else {
				content = getShortText(content, CONTENT_MAX_LENGTH);
			}
			String shotNick = getShortText(comment.getNickName(),
					NICK_MAX_LENGTH);
			if (isReply) {
				values.add(shotNick);
				values.add(content);
				userInfo.put(PUSH_TYPE_KEY, PUSH_TYPE_REPLY);
				NotificationService.getInstance().sendMessage(appId,
						deviceToken, badge, DRAW_LOCALIZE_FORMAT_KEY_REPLY,
						values, sound, userInfo, PUSH_TYPE_REPLY, user, true);
			} else {
				values.add(shotNick);
				String word = OpusManager.getWordWithActionId(mongoClient,
						comment.getOpusId());
				if (word != null) {
					values.add(word);
				} else {
					values.add("");
				}
				values.add(content);
				userInfo.put(PUSH_TYPE_KEY, PUSH_TYPE_COMMENT);
				NotificationService.getInstance().sendMessage(appId,
						deviceToken, badge, DRAW_LOCALIZE_FORMAT_KEY_COMMENT,
						values, sound, userInfo, PUSH_TYPE_COMMENT, user, true);
			}

		} else {
			logger
					.info("send PUSH message, but user or user device token empty");
		}
	}

	public static void newItemToOpus(UserAction action, String appId,
			String userId) {

		User user = UserManager.findSimpleUserInfoByUserId(mongoClient, userId);
		if (user != null) {

			String sound = "default";
			String deviceToken = user.getDeviceToken();
            if (deviceToken == null){
                logger.info("send PUSH message, but user "+user.getUserId()+" device token null");
                return;
            }


            HashMap<String, Object> userInfo = new HashMap<String, Object>();

			// userInfo.put(ServiceConstant.PARA_ROOM_ID, room.getRoomId());
			int badge = getBadgeAndSetUserInfo(userId, userInfo,appId);

			List<String> values = new ArrayList<String>(3);
			values.add(getShortText(action.getNickName(), NICK_MAX_LENGTH));
			String word = OpusManager.getWordWithActionId(mongoClient, action
					.getOpusId());
			if (word != null) {
				values.add(word);
			}

			if (action.getType() == UserAction.TYPE_FLOWER) {
				userInfo.put(PUSH_TYPE_KEY, PUSH_TYPE_FLOWER);
				NotificationService.getInstance().sendMessage(appId,
						deviceToken, badge, DRAW_LOCALIZE_FORMAT_KEY_FLOWER,
						values, sound, userInfo, PUSH_TYPE_FLOWER, user, true);
			} else if (action.getType() == UserAction.TYPE_TOMATO) {
				userInfo.put(PUSH_TYPE_KEY, PUSH_TYPE_TOMATO);
				NotificationService.getInstance().sendMessage(appId,
						deviceToken, badge, DRAW_LOCALIZE_FORMAT_KEY_TOMATO,
						values, sound, userInfo, PUSH_TYPE_TOMATO, user, true);
			}

		} else {
			logger
					.info("send PUSH message, but user or user device token empty");
		}
	}

	public static void sendMessage(UserAction opus, String targetUid,
			String appId) {
		User user = UserManager.findSimpleUserInfoByUserId(mongoClient,
				targetUid);
		if (user != null) {

			String sound = "default";
			String deviceToken = user.getDeviceToken();
            if (deviceToken == null){
                logger.info("send PUSH message, but user "+user.getUserId()+" device token null");
                return;
            }


            HashMap<String, Object> userInfo = new HashMap<String, Object>();
			userInfo.put(PUSH_TYPE_KEY, PUSH_TYPE_DRAWTOME);
			// userInfo.put(ServiceConstant.PARA_FEED_ID, opus.getActionId());
			int badge = getBadgeAndSetUserInfo(targetUid, userInfo,appId);

			List<String> values = new ArrayList<String>(2);
			values.add(getShortText(opus.getNickName(), NICK_MAX_LENGTH));

			NotificationService.getInstance().sendMessage(appId, deviceToken,
					badge, DRAW_LOCALIZE_FORMAT_KEY_DRAW_TOUSER, values, sound,
					userInfo, PUSH_TYPE_DRAWTOME, user, true);
		} else {
			logger.info("send PUSH message, but user or user device token empty");
		}

	}

	public static void payBBSReward(BBSPost post, BBSUser winner, String appId) {

		String userId = winner.getUserId();

		User user = UserManager.findSimpleUserInfoByUserId(mongoClient, userId);

		if (user != null) {

			String sound = "default";
			String deviceToken = user.getDeviceToken();
            if (deviceToken == null){
                logger.info("send PUSH message, but user "+user.getUserId()+" device token null");
                return;
            }

            HashMap<String, Object> userInfo = new HashMap<String, Object>();
			userInfo.put(PUSH_TYPE_KEY, PUSH_TYPE_BBSPAY);
			// userInfo.put(ServiceConstant.PARA_ROOM_ID, room.getRoomId());
			int badge = getBadgeAndSetUserInfo(userId, userInfo,appId);

			List<String> values = new ArrayList<String>(2);
			values.add(getShortText(post.getCreateUser().getNickName(),
					NICK_MAX_LENGTH));
			values.add(post.getReward().getBonus() + "");
			values.add(getShortText(post.getContent().getText(),
					CONTENT_MAX_LENGTH));

			NotificationService.getInstance().sendMessage(appId, deviceToken,
					badge, DRAW_LOCALIZE_FORMAT_KEY_BBS_PAY, values, sound,
					userInfo, PUSH_TYPE_BBSPAY, user, true);
		} else {
			logger
					.info("send PUSH message, but user or user device token empty");
		}
	}

	public static void newBBSAction(String userId, String appId,
			BBSUser createUser, BBSActionSource source, BBSContent content,
			int actionType, boolean isReply) {

		int pushType = 0;
		if (actionType == BBSAction.ActionTypeSupport) {
			pushType = PUSH_TYPE_BBSSUPPORT;
		} else if (isReply) {
			pushType = PUSH_TYPE_BBSREPLY;
		} else {
			pushType = PUSH_TYPE_COMMENT;
		}

		User user = UserManager.findSimpleUserInfoByUserId(mongoClient, userId);

		if (user != null) {

			String sound = "default";
			String deviceToken = user.getDeviceToken();
            if (deviceToken == null){
                logger.info("send PUSH message, but user "+user.getUserId()+" device token null");
                return;
            }

			HashMap<String, Object> userInfo = new HashMap<String, Object>();
			userInfo.put(PUSH_TYPE_KEY, pushType);
			int badge = getBadgeAndSetUserInfo(userId, userInfo,appId);

			String ll = null;

			List<String> values = new ArrayList<String>(2);
			values.add(getShortText(createUser.getNickName(), NICK_MAX_LENGTH));
			if (pushType == PUSH_TYPE_BBSREPLY) {
				values.add(getShortText(content.getText(), CONTENT_MAX_LENGTH));
				ll = DRAW_LOCALIZE_FORMAT_KEY_BBS_REPLY;
			} else if (pushType == PUSH_TYPE_BBSSUPPORT) {
				values.add(getShortText(source.getBriefText(),
						CONTENT_MAX_LENGTH));
				ll = DRAW_LOCALIZE_FORMAT_KEY_BBS_SUPPORT;
			} else {
				values.add(getShortText(content.getText(), CONTENT_MAX_LENGTH));
				ll = DRAW_LOCALIZE_FORMAT_KEY_BBS_COMMENT;
			}
			NotificationService.getInstance().sendMessage(appId, deviceToken,
					badge, ll, values, sound, userInfo, pushType, user, true);
		} else {
			logger.info("send PUSH message, but user or user device token empty");
		}

	}
}
