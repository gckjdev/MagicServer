package com.orange.game.model.dao;

import java.util.*;

import com.orange.common.utils.DBObjectUtil;
import com.orange.common.utils.DateUtil;
import com.orange.common.utils.StringUtil;
import com.orange.game.model.manager.ScoreManager;
import com.orange.game.model.xiaoji.AbstractXiaoji;
import com.orange.game.model.xiaoji.XiaojiFactory;
import com.orange.network.game.protocol.constants.GameConstantsProtos;
import com.orange.network.game.protocol.model.GameBasicProtos;
import com.orange.network.game.protocol.model.OpusProtos;
import com.orange.network.game.protocol.model.OpusProtos.PBOpusType;

import com.orange.network.game.protocol.model.SingProtos;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.orange.common.utils.FileUtils;
import com.orange.game.constants.DBConstants;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.manager.OpusManager;
import com.orange.game.model.service.DataService;

public class UserAction extends CommonData implements Comparable<UserAction> {

    public static final int REPOST_VALUE = 4;
    public static final int DRAW_TO_USER_VALUE = 5;
    public static final int FLOWER_VALUE = 6;
    public static final int TOMATO_VALUE = 7;
    public static final int ONLY_COMMENT_VALUE = 8;
    public static final int DRAW_CONTEST_VALUE = 9;
    public static final int SING_VALUE = 1000;
    public static final int SING_TO_USER_VALUE = 1001;
    public static final int SING_CONTEST_VALUE = 1002;
    public static final int ASK_PS_VALUE = 1500;
    public static final int ASK_PS_OPUS_VALUE = 1501;


    public static final int TYPE_UNKNOW = 0;
	public static final int TYPE_DRAW = PBOpusType.DRAW_VALUE;
	public static final int TYPE_GUESS = PBOpusType.GUESS_VALUE;
	public static final int TYPE_COMMENT = PBOpusType.COMMENT_VALUE;
	public static final int TYPE_REPOST = PBOpusType.REPOST_VALUE;
	public static final int TYPE_DRAW_TO_USER = PBOpusType.DRAW_TO_USER_VALUE;
	public static final int TYPE_FLOWER = PBOpusType.FLOWER_VALUE;
	public static final int TYPE_TOMATO = PBOpusType.TOMATO_VALUE;
	public static final int TYPE_DRAW_TO_CONTEST = PBOpusType.DRAW_CONTEST_VALUE;

    public static final int TYPE_LEARN_DRAW_PRACTICE = PBOpusType.DRAW_PRACTICE_VALUE;
    public static final int TYPE_LEARN_DRAW_CONQUER = PBOpusType.DRAW_CONQUER_VALUE;

    public static final int TYPE_SAVE = 100;
	public static final int TYPE_REMOVE_FAVORITE = 101;
	public static final int TYPE_RECOMMEND = 102;
	public static final int TYPE_UNRECOMMEND = 103;
    public static final int TYPE_REJECT_DRAW_TO_ME = 104;
    public static final int TYPE_CONTEST_COMMENT = 105;
    public static final int TYPE_PLAY = 106;
	public static final int TYPE_LAST_VALID_ACTION = 106;

	//public static final int TYPE_SPEAK = 1000;

    public static final int DRAW_ACTION_TYPE_PAINT = 0;
    public static final int DRAW_ACTION_TYPE_CLEAN = 1;
    public static final int DRAW_ACTION_TYPE_SHAPE = 2;
    public static final int DRAW_ACTION_TYPE_CHANGE_BACKGROUND = 3;
    public static final int DRAW_ACTION_TYPE_CHANGE_BACKGROUND_IMAGE = 4;
    public static final int DRAW_ACTION_TYPE_GRADIENT = 5;
    public static final int DRAW_ACTION_TYPE_CLIP = 6;

    public static final int COMMENT_TYPE_ALL = TYPE_UNKNOW;
	public static final int COMMENT_TYPE_COMMENT = TYPE_COMMENT;
	public static final int COMMENT_TYPE_FLOWER = TYPE_FLOWER;
	public static final int COMMENT_TYPE_TOMATO = TYPE_TOMATO;
    public static final int COMMENT_TYPE_SAVE = 8; // align with client
    public static final int COMMENT_TYPE_CONTEST_COMMENT = TYPE_CONTEST_COMMENT;

	public static final int COMMENT_TYPE_GUESS = TYPE_GUESS; // include the try
	// and guess
	public static final int COMMENT_TYPE_TRY = 20; // try but not correct
	public static final int COMMENT_TYPE_CORRECT = 21; // guess correct

	public static final int STATUS_NORMAL = 0;
	public static final int STATUS_DELETE = 1;

	public static final int LANGUAGE_UNKNOW = 0;

	public static final int TIMES_TYPE_MATCH = 1;
	public static final int TIMES_TYPE_GUESS = 2;
	public static final int TIMES_TYPE_CORRECT = 3;
	public static final int TIMES_TYPE_COMMENT = 4;
	public static final int TIMES_TYPE_FLOWER = 5;
	public static final int TIMES_TYPE_TOMATO = 6;
	public static final int TIMES_TYPE_SAVE = 7;
    public static final int TIMES_TYPE_CONTEST_COMMENT = 9;
    public static final int TIMES_TYPE_PLAY = 10;

	// device type
	public static final int DEVICE_TYPE_NO = 0;
	public static final int DEVICE_TYPE_IPHONE = 1;
	public static final int DEVICE_TYPE_IPAD = 2;
	
	
	public final  static int allOpusTypes[] = { UserAction.TYPE_DRAW,
		UserAction.TYPE_DRAW_TO_USER, UserAction.TYPE_DRAW_TO_CONTEST, 
		OpusProtos.PBOpusType.SING_VALUE,PBOpusType.SING_TO_USER_VALUE,
		PBOpusType.SING_CONTEST_VALUE,PBOpusType.ASK_PS_VALUE,
		PBOpusType.ASK_PS_OPUS_VALUE};

    public final  static int allDrawOpusTypes[] = { UserAction.TYPE_DRAW,
            UserAction.TYPE_DRAW_TO_USER, UserAction.TYPE_DRAW_TO_CONTEST};

    public final  static int allSingOpusTypes[] = { OpusProtos.PBOpusType.SING_VALUE,
            PBOpusType.SING_TO_USER_VALUE,
            PBOpusType.SING_CONTEST_VALUE };

    public final static int allContestTypes[] = { UserAction.TYPE_DRAW_TO_CONTEST, PBOpusType.SING_CONTEST_VALUE};

    public final static int allDrawContestTypes[] = { UserAction.TYPE_DRAW_TO_CONTEST };

	public final static int feedTypes[] = { UserAction.TYPE_DRAW,
		UserAction.TYPE_GUESS, UserAction.TYPE_DRAW_TO_USER };
	
	public final static int normalOpusTypes[] = { UserAction.TYPE_DRAW,
		UserAction.TYPE_DRAW_TO_USER, OpusProtos.PBOpusType.SING_VALUE,PBOpusType.SING_TO_USER_VALUE  };

    public final static int normalDrawOpusTypes[] = { UserAction.TYPE_DRAW,
            UserAction.TYPE_DRAW_TO_USER };

    public final static int normalSingOpusTypes[] = { PBOpusType.SING_TO_USER_VALUE,
            PBOpusType.SING_CONTEST_VALUE };

	Set<String> relatedUserIdList = new HashSet<String>();

    public static boolean isOpus(int actionType) {
        for (int i=0; i<allOpusTypes.length; i++){
            if (actionType == allOpusTypes[i]){
                return true;
            }
        }

        return false;
    }

    public static boolean isLearnDraw(int actionType) {
        return (actionType == TYPE_LEARN_DRAW_PRACTICE || actionType == TYPE_LEARN_DRAW_CONQUER);
    }

    public boolean isLearnDraw() {
        int type = getType();
        return (type == TYPE_LEARN_DRAW_PRACTICE || type == TYPE_LEARN_DRAW_CONQUER);
    }

    public static boolean isStroke(int drawActionType) {
        return (drawActionType != DRAW_ACTION_TYPE_CLEAN && drawActionType != DRAW_ACTION_TYPE_CLIP);
    }


    public static boolean isGuess(int actionType) {
        return (actionType == TYPE_GUESS);
    }

    public UserAction(DBObject dbObject) {
		super(dbObject);
	}

	public UserAction() {
		super();
		dbObject = new BasicDBObject();
	}

    public void setCategory(int category) {
        put(DBConstants.F_CATEGORY, category);
    }

    public int getCategory() {
        return getInt(DBConstants.F_CATEGORY);		// 0 is DRAW
    }


    public void setActionId(ObjectId objectId) {
		setObjectId(objectId);
	}

	public String getActionId() {
		ObjectId oid = getObjectId();
		if (oid == null) {
			return null;
		}
		return getObjectId().toString();
	}

	public void setType(int type) {
		this.put(DBConstants.F_TYPE, type);
	}

	public int getType() {
		return getInt(DBConstants.F_TYPE);
	}

	public String getAppId() {
		return this.getString(DBConstants.F_APPID);
	}

	public void setAppId(String appId) {
		this.put(DBConstants.F_APPID, appId);
	}

	public String getOpusId() {
		return this.getString(DBConstants.F_OPUS_ID);
	}

	public void setOpusId(String sourceId) {
		this.put(DBConstants.F_OPUS_ID, sourceId);
	}


    public void setSpendTime(int spendTime) {
        put(DBConstants.F_SPEND_TIME, spendTime);
    }

    public int getSpendTime() {
        return getInt(DBConstants.F_SPEND_TIME);
    }

    public void setTags(List<String> tagsList) {
        put(DBConstants.F_TAG, tagsList);
    }

    public List<String> getTags() {
        return getStringList(DBConstants.F_TAG);
    }

	public Date getCreateDate() {
		return this.getDate(DBConstants.F_CREATE_DATE);
	}

	public void setCreateDate(Date createDate) {
		this.getDbObject().put(DBConstants.F_CREATE_DATE, createDate);
	}

	private void setTextList(String field, Set<String> textList) {
		if (textList == null) {
			return;
		}
		BasicDBList list = new BasicDBList();
		for (String text : textList) {
			list.add(text);
		}
		this.put(field, list);
	}

	private List<String> getTextList(String field) {
		BasicDBList list = (BasicDBList) this.getObject(field);
		if (list == null || list.size() == 0) {
			return null;
		}
		List<String> retList = new ArrayList<String>();
		for (Object text : list) {
			retList.add((String) text);
		}
		return retList;
	}

	public void setGuessWordList(Set<String> guessWords) {
		setTextList(DBConstants.F_GUESS_WORD_LIST, guessWords);
	}

	public List<String> getGuessWordList() {
		return getTextList(DBConstants.F_GUESS_WORD_LIST);
	}

	public void setUserIdList(Set<String> userIdList) {
		setTextList(DBConstants.F_USERID_LIST, userIdList);
	}

	public List<String> getUserIdList() {
		return getTextList(DBConstants.F_USERID_LIST);
	}

	public void setGender(String gender) {
		this.put(DBConstants.F_GENDER, gender);
	}

	public String getGender() {
		return this.getString(DBConstants.F_GENDER);
	}

	public void setAvatar(String avatarURL) {
		this.getDbObject().put(DBConstants.F_AVATAR, avatarURL);
	}

	public String getAvatar() {
		// return this.getString(DBConstants.F_AVATAR);
		return User.getTranslatedAvatar(getString(DBConstants.F_AVATAR));
	}

	public void setNickName(String nickName) {
		this.put(DBConstants.F_NICKNAME, nickName);
	}

	public String getNickName() {
		return this.getString(DBConstants.F_NICKNAME);
	}

	public void setSignature(String value) {
		this.put(DBConstants.F_SIGNATURE, value);
	}

	public String getSignature() {
		return this.getString(DBConstants.F_SIGNATURE);
	}

	public void setDrawData(byte[] data) {
		this.dbObject.put(DBConstants.F_DRAW_DATA, data);
	}

	private byte[] getDrawData() {
		return (byte[]) this.dbObject.get(DBConstants.F_DRAW_DATA);
	}

	public void setCreateUserId(String uid) {
		put(DBConstants.F_CREATE_USERID, uid);
	}

	public String getCreateUserId() {
		return getString(DBConstants.F_CREATE_USERID);
	}

	public void setWord(String word) {
		put(DBConstants.F_WORD, word);
	}

	public String getWord() {
		return getString(DBConstants.F_WORD);
	}

	public void setLevel(int level) {
		put(DBConstants.F_LEVEL, level);
	}

	public int getLevel(int level) {
		return getInt(DBConstants.F_LEVEL);
	}

	public void setLanguage(int language) {
		put(DBConstants.F_LANGUAGE, language);
	}

	public int getLanguage() {
        Object obj = getObject(DBConstants.F_LANGUAGE);
        if (obj == null){
            return OpusProtos.PBLanguage.CHINESE_VALUE;
        }

		return getInt(DBConstants.F_LANGUAGE);
	}

	public void setMatchTimes(int matchTimes) {
		put(DBConstants.F_MATCH_TIMES, matchTimes);
	}

	public int getMatchTimes() {
		return getInt(DBConstants.F_MATCH_TIMES);
	}

	public void setCorrectTimes(int correctTimes) {
		put(DBConstants.F_CORRECT_TIMES, correctTimes);
	}

	public int getCorrectTimes() {
		return getInt(DBConstants.F_CORRECT_TIMES);
	}

	public void setScore(int score) {
		put(DBConstants.F_SCORE, score);
	}

	public int getScore() {
		return getInt(DBConstants.F_SCORE);
	}

	public void setCorrect(boolean correct) {
		put(DBConstants.F_CORRECT, correct);
	}

	public boolean isCorrect() {
		return getBoolean(DBConstants.F_CORRECT);
	}

	public void setComment(String comment) {
		put(DBConstants.F_COMMENT_CONTENT, comment);
	}

	public String getComment() {
		return getString(DBConstants.F_COMMENT_CONTENT);
	}

	public void setOpusCreatorUid(String opusCreatorUid) {
		put(DBConstants.F_OPUS_CREATOR_UID, opusCreatorUid);
	}

	public String getOpusCreatorUid() {
		return getString(DBConstants.F_OPUS_CREATOR_UID);
	}

	public int getGuessTimes() {
		return getInt(DBConstants.F_GUESS_TIMES);
	}

	public void setGuessTimes(int guessTimes) {
		put(DBConstants.F_GUESS_TIMES, guessTimes);
	}

	public int getSaveTimes() {
		return getInt(DBConstants.F_SAVE_TIMES);
	}

    public int getPlayTimes() {
        return getInt(DBConstants.F_PLAY_TIMES) + getGuessTimes(); // guess times also looked as play times
    }

	public void setSaveTimes(int guessTimes) {
		put(DBConstants.F_SAVE_TIMES, guessTimes);
	}

	public int getFlowerTimes() {
		return getInt(DBConstants.F_FLOWER_TIMES);
	}

	public void setFlowerTimes(int guessTimes) {
		put(DBConstants.F_FLOWER_TIMES, guessTimes);
	}

	public int getTomatoTimes() {
		return getInt(DBConstants.F_TOMATO_TIMES);
	}

	public void setTomatoTimes(int guessTimes) {
		put(DBConstants.F_TOMATO_TIMES, guessTimes);
	}

	public int getCommentTimes() {
		return getInt(DBConstants.F_COMMENT_TIMES);
	}

    public int getContestCommentTimes() {
        return getInt(DBConstants.F_CONTEST_COMMENT_TIMES);
    }


    public void setCommentTimes(int commentTimes) {
		put(DBConstants.F_COMMENT_TIMES, commentTimes);
	}

	public void setHot(double hot) {
		put(DBConstants.F_HOT, hot);

	}

	public double getHot() {
		return getDouble(DBConstants.F_HOT);

	}

	public void setHistoryScore(double score) {
		put(DBConstants.F_HISTORY_SCORE, score);

	}

	public double getHistoryScore() {
		return getDouble(DBConstants.F_HISTORY_SCORE);
	}

	public void setContestScore(double score) {
		put(DBConstants.F_CONTEST_SCORE, score);

	}

	public double getContestScore() {
		return getDouble(DBConstants.F_CONTEST_SCORE);
	}

	public void setContestId(String contestId) {
		put(DBConstants.F_CONTESTID, contestId);

	}

	public String getContestId() {
		return getString(DBConstants.F_CONTESTID);
	}

	public void setHasWords(boolean hasWords) {
		put(DBConstants.F_HAS_WORDS, hasWords);
	}

	public void setTargetUserId(String targetUserId) {
		put(DBConstants.F_TARGET_UID, targetUserId);
	}

	public String getTargetUserId() {
		return getString(DBConstants.F_TARGET_UID);
	}

	public void setTargetNickName(String targetNickName) {
		put(DBConstants.F_TARGET_NICK, targetNickName);
	}

	public String getTargetNickName() {
		return getString(DBConstants.F_TARGET_NICK);
	}

	// for learn draw
	public void setLearnDraw(LearnDraw learnDraw) {
		if (learnDraw != null && learnDraw.getDbObject() != null) {
			put(DBConstants.F_LEARN_DRAW, learnDraw.getDbObject());
		}
	}

	public LearnDraw getLearnDraw() {
		DBObject object = (DBObject) getObject(DBConstants.F_LEARN_DRAW);
		if (object != null) {
			return new LearnDraw(object);
		}
		return null;
	}

//	private String getFileUploadLocalDir() {
//		String dir = System.getProperty("upload.local.drawImage");
//		return (dir == null ? "" : dir);
//	}

	private String getFileUploadRemoteDir() {
        String dir = System.getProperty("upload.remote.drawImage");
        if (getCategory() != GameConstantsProtos.PBOpusCategoryType.DRAW_CATEGORY_VALUE){
            AbstractXiaoji xiaoji = XiaojiFactory.getInstance().getXiaoji(getCategory());
            if (xiaoji != null){
                dir = xiaoji.getImageUploadManager().getRemoteDir();
            }
        }

		return (dir == null ? "" : dir);
	}

	private static String OLD_IMAGE_KEY = "draw_image";
	private static int OLD_IMAGE_KEY_LEN = OLD_IMAGE_KEY.length();

	public void setOpusImageUrl(String drawImageUrl) {
		put(DBConstants.F_IMAGE_URL, drawImageUrl);
	}

	public String getOpusImageUrl() {
		return getString(DBConstants.F_IMAGE_URL);
	}

	public String createOpusImageUrl() {
		String relativeURL = getString(DBConstants.F_IMAGE_URL);
		if (relativeURL == null)
			return null;

		if (relativeURL.contains(OLD_IMAGE_KEY)) {
			int index = relativeURL.indexOf(OLD_IMAGE_KEY);
			if (index != -1) {
				int beginIndex = index + OLD_IMAGE_KEY_LEN + 1;
				String url = relativeURL.substring(beginIndex);

				String remoteURLString = getFileUploadRemoteDir();
				return remoteURLString + url;
			} else {
				return relativeURL;
			}
		} else {
			String remoteURLString = getFileUploadRemoteDir();
			return remoteURLString + relativeURL;
		}
	}

    public String createOpusLocalImageUrl() {
        String relativeURL = getString(DBConstants.F_IMAGE_URL);
        if (relativeURL == null)
            return null;

        if (relativeURL.contains(OLD_IMAGE_KEY)){
            int index = relativeURL.indexOf(OLD_IMAGE_KEY);
            if (index != -1) {
                int beginIndex = index + OLD_IMAGE_KEY_LEN + 1;
                String url = relativeURL.substring(beginIndex);
                log.info("<createOpusLocalImageUrl> fix relative URL from "+relativeURL+" to "+url);
                relativeURL = url;
            }
        }

        AbstractXiaoji xiaoji = XiaojiFactory.getInstance().getXiaoji(getCategory());
        if (xiaoji != null){
            return xiaoji.getImageUploadManager().getLocalURL(relativeURL);
        }
        else{
            return null;
        }
    }


    public void setOpusThumbImageUrl(String thumbImageUrl) {
		put(DBConstants.F_THUMB_URL, thumbImageUrl);
	}

	public String getOpusThumbImageUrl() {
		return getString(DBConstants.F_THUMB_URL);
	}

    public void setBgImageUrl(String imageUrl) {
        put(DBConstants.F_BGIMAGE_URL, imageUrl);
    }

    public void setBgImageName(String name) {
        put(DBConstants.F_BGIMAGE_NAME, name);
    }

    public String getBgImageUrl() {
        return getString(DBConstants.F_BGIMAGE_URL);
    }

    public String getBgImageName() {
        return getString(DBConstants.F_BGIMAGE_NAME);
    }

    public String createBgImageUrl(){
        String relativeURL = getString(DBConstants.F_BGIMAGE_URL);
        if (StringUtil.isEmpty(relativeURL))
            return null;

        return getXiaoji().getImageUploadManager().getRemoteURL(relativeURL);
    }

	public String createOpusThumbImageUrl() {

        String relativeURL = getString(DBConstants.F_THUMB_URL);
        if (relativeURL == null)
            return null;

        if (isXiaojiDraw()){
            // here is just to make compability for old draw code
            if (relativeURL.contains(OLD_IMAGE_KEY)) {
                // to make old data compatibility
                int index = relativeURL.indexOf(OLD_IMAGE_KEY);
                if (index != -1) {
                    int beginIndex = index + OLD_IMAGE_KEY_LEN + 1;
                    String url = relativeURL.substring(beginIndex);

                    String remoteURLString = getFileUploadRemoteDir();
                    return remoteURLString + url;
                } else {
                    return relativeURL;
                }
            } else {
                String remoteURLString = getFileUploadRemoteDir();
                return remoteURLString + relativeURL;
            }
        }
        else{
            return getXiaoji().getImageUploadManager().getRemoteURL(relativeURL);
        }
	}

    public boolean isXiaojiDraw(){
        return (getCategory() == GameConstantsProtos.PBOpusCategoryType.DRAW_CATEGORY_VALUE);
    }

    public AbstractXiaoji getXiaoji(){
        return XiaojiFactory.getInstance().getXiaoji(getCategory());
    }

	public void setDataLength(int dataLen) {
		put(DBConstants.F_DRAW_DATA_LEN, dataLen);
	}

	public int getDataLength() {
		return getInt(DBConstants.F_DRAW_DATA_LEN);
	}

	public void setOpusStatus(int opusStatus) {
		put(DBConstants.F_OPUS_STATUS, opusStatus);
	}

	public int getOpusStatus() {
		return getInt(DBConstants.F_OPUS_STATUS);
	}

	public void setOpusWord(String word) {
		put(DBConstants.F_OPUS_WORD, word);
	}

	public String getOpusWord() {
		return getString(DBConstants.F_OPUS_WORD);
	}

	public void setOpusCreatorNickName(String nickName) {
		put(DBConstants.F_OPUS_CREATOR_NICKNAME, nickName);
	}

	public String getOpusCreatorNickName() {
		return getString(DBConstants.F_OPUS_CREATOR_NICKNAME);
	}

	public void setOpusCreatorAvatar(String avatar) {
		put(DBConstants.F_OPUS_CREATOR_AVATAR, avatar);
	}

	public String getOpusCreatorAvatar() {
		return User.getTranslatedAvatar(getString(DBConstants.F_OPUS_CREATOR_AVATAR));
	}

	public void setOpusCreatorGender(String gender) {
		put(DBConstants.F_OPUS_CREATOR_GENDER, gender);
	}

	public String getOpusCreatorGender() {
		return getString(DBConstants.F_OPUS_CREATOR_GENDER);
	}

	// Comment info
	public CommentInfo getCommentInfo() {
		DBObject object = (DBObject) getObject(DBConstants.F_COMMENT_INFO);
		if (object == null) {
			return null;
		}
		return new CommentInfo(object);
	}

	public void setCommentInfo(CommentInfo info) {
		if (info != null) {
			put(DBConstants.F_COMMENT_INFO, info.getDbObject());
		}
	}

	public boolean isDrawType() {
		return (getType() == TYPE_DRAW || getType() == TYPE_DRAW_TO_USER);
	}

	public boolean isGuessOrCommentType() {
		return (getType() == TYPE_GUESS || getType() == TYPE_COMMENT);
	}

	public void addRelatedOpusId(String uid) {
		if (uid == null)
			return;

		relatedUserIdList.add(uid);
		BasicDBList list = new BasicDBList();
		list.addAll(relatedUserIdList);
		put(DBConstants.F_OPUS_RELATED_USER_ID, list);
	}

	public int compareTo(UserAction o) {

		if (getHot() < o.getHot()) {
			return -1;
		} else if (getHot() > o.getHot()) {

			return 1;
		}
		return 0;
	}

	public boolean isContestDraw() {
		return (!StringUtil.isEmpty(getContestId())); //getType() == TYPE_DRAW_TO_CONTEST;
	}

	public void setDeviceModel(String deviceModel) {
		if (deviceModel != null) {
			put(DBConstants.F_DEVICEMODEL, deviceModel);
		}
	}

	public void setDeviceType(int devicetype) {
		put(DBConstants.F_DEVICE_TYPE, devicetype);
	}

	public int getDeviceType() {
		return getInt(DBConstants.F_DEVICE_TYPE);
	}

	public void setDescription(String description) {
		put(DBConstants.F_DESCRIPTION, description);
	}

	public String getDescription() {
		return getString(DBConstants.F_DESCRIPTION);
	}

	public void setWordScore(int score) {
		put(DBConstants.F_DRAW_WORD_SCORE, score);
	}

	public void setDrawDataUrl(String drawDataUrl) {
		put(DBConstants.F_DRAW_DATA_URL, drawDataUrl);
	}

    public String getDrawDataUrl() {
        return getString(DBConstants.F_DRAW_DATA_URL);
    }

	public String getRemoteDrawDataUrl(boolean isCompressed) {
		String localUrl = getString(DBConstants.F_DRAW_DATA_URL);
		if (localUrl == null) {
			return null;
		}

        if (getCategory() == GameConstantsProtos.PBOpusCategoryType.DRAW_CATEGORY_VALUE){
		    return DataService.getInstance().generateRemoteDrawDataUrl(localUrl, isCompressed);
        }
        else{
            AbstractXiaoji xiaoji = XiaojiFactory.getInstance().getXiaoji(getCategory());
            if (xiaoji == null)
                return null;

            return xiaoji.getDataUploadManager().getRemoteURL(localUrl);
        }
	}

	public byte[] readOldDrawData() {
		return getDrawData();
	}

	public byte[] readDrawData(boolean isCompressed) {

		// read draw data locally and unzip data to byte, then return
		String localUrl = getString(DBConstants.F_DRAW_DATA_URL);
		if (localUrl == null || isLocalDataFileExist(isCompressed) == false) {
			// for old data
			return getDrawData();
		}

		byte[] data = DataService.getInstance().readDrawByteData(localUrl,
				isCompressed);
		if (data == null) {
			// for old data
			data = getDrawData();
		}

		return data;
	}

	public void setFileGen(int i) {
		put(DBConstants.F_FILE_GEN, 1);
		put(DBConstants.F_FILE_GEN_RESULT, 0);
	}

	public boolean isLocalDataFileExist(boolean isCompressed) {
		String localUrl = getString(DBConstants.F_DRAW_DATA_URL);
		if (localUrl == null) {
			return false;
		}

		String path = DataService.getInstance().generateLocalDrawDataUrl(
				localUrl, isCompressed);
		if (FileUtils.isFileExists(path)) {
			return true;
		}

		return false;
	}

	public  boolean isContest() {
		int actionType = getType();
		for (int type:allContestTypes) {
			if (type == actionType) {
				return true;
			}
		}
		return false;
	}
	
	
	
//	public int getCategoryType() {
//		return getInt(DBConstants.F_CATEGORY);
//	}

    public BasicDBObject getOpusRank() {
        return (BasicDBObject)getObject(DBConstants.F_RANK);
    }

    public double getSpecialRankScore(int rankType) {

        BasicDBObject allRanks = getOpusRank();
        if (allRanks == null)
            return 0;

        BasicDBObject rankInfo = (BasicDBObject)allRanks.get(ScoreManager.getRankTypeField(rankType));
        if (rankInfo == null)
            return 0;

        return rankInfo.getDouble(DBConstants.F_SCORE);


    }

    public int getIntCreateDate() {
        return getIntDate(DBConstants.F_CREATE_DATE);
    }

    public int getQualityScore() {
        return getInt(DBConstants.F_QUALITY_SCORE);
    }

    public void setQualityScore(int quality) {
        put(DBConstants.F_QUALITY_SCORE, quality);
    }


    private BasicDBObject getOrCreateSingInfo(){
        DBObject obj = (DBObject)getObject(DBConstants.F_SING_INFO);
        if (obj == null){
            obj = new BasicDBObject();
            put(DBConstants.F_SING_INFO, obj);
        }

        return (BasicDBObject)obj;
    }

    public void setSingVoiceType(int number) {
        BasicDBObject obj = getOrCreateSingInfo();
        obj.put(DBConstants.F_VOICE_TYPE, number);
        put(DBConstants.F_SING_INFO, obj);
    }

    public void setSingDuration(float value) {
        BasicDBObject obj = getOrCreateSingInfo();
        obj.put(DBConstants.F_DURATION, value);
        put(DBConstants.F_SING_INFO, obj);
    }

    public void setSingPitch(float value) {
        BasicDBObject obj = getOrCreateSingInfo();
        obj.put(DBConstants.F_PITCH, value);
        put(DBConstants.F_SING_INFO, obj);
    }

    public void setSingFormant(float value) {
        BasicDBObject obj = getOrCreateSingInfo();
        obj.put(DBConstants.F_FORMANT, value);
        put(DBConstants.F_SING_INFO, obj);
    }

    public void setSongName(String value) {
        BasicDBObject obj = getOrCreateSingInfo();
        obj.put(DBConstants.F_SONG_NAME, value);
        put(DBConstants.F_SING_INFO, obj);
    }

    public void setSongAuthor(String value) {
        BasicDBObject obj = getOrCreateSingInfo();
        obj.put(DBConstants.F_SONG_AUTHOR, value);
        put(DBConstants.F_SING_INFO, obj);
    }

    public void setSongId(String value) {
        BasicDBObject obj = getOrCreateSingInfo();
        obj.put(DBConstants.F_SONG_ID, value);
        put(DBConstants.F_SING_INFO, obj);
    }

    public void setSongLyricURL(String value) {
        BasicDBObject obj = getOrCreateSingInfo();
        obj.put(DBConstants.F_SONG_LYRIC_URL, value);
        put(DBConstants.F_SING_INFO, obj);
    }

    public BasicDBObject getSingInfo(){
        return (BasicDBObject)getObject(DBConstants.F_SING_INFO);
    }

    public String getDataUrl() {
        return getString(DBConstants.F_DATA_URL);
    }

    public String getDeviceModel() {
        return getString(DBConstants.F_DEVICEMODEL);
    }

    public String getSongId() {
        BasicDBObject singInfo = getSingInfo();
        if (singInfo == null)
            return null;

        return singInfo.getString(DBConstants.F_SONG_ID);
    }

    public String getSongName() {
        BasicDBObject singInfo = getSingInfo();
        if (singInfo == null)
            return null;

        return singInfo.getString(DBConstants.F_SONG_NAME);
    }

    public String getSongLyricURL() {
        BasicDBObject singInfo = getSingInfo();
        if (singInfo == null)
            return null;

        return singInfo.getString(DBConstants.F_SONG_LYRIC_URL);
    }

    public String getSongAuthor() {
        BasicDBObject singInfo = getSingInfo();
        if (singInfo == null)
            return null;

        return singInfo.getString(DBConstants.F_SONG_AUTHOR);
    }

    public SingProtos.PBVoiceType getSingVoiceType() {
        BasicDBObject singInfo = getSingInfo();
        if (singInfo == null)
            return null;

        return SingProtos.PBVoiceType.valueOf(singInfo.getInt(DBConstants.F_VOICE_TYPE));
    }

    public float getSingDuration() {
        BasicDBObject singInfo = getSingInfo();
        if (singInfo == null)
            return 1.0f;

        return (float)singInfo.getDouble(DBConstants.F_DURATION);
    }

    public float getSingPitch() {
        BasicDBObject singInfo = getSingInfo();
        if (singInfo == null)
            return 1.0f;

        return (float)singInfo.getDouble(DBConstants.F_PITCH);
    }

    public float getSingFormant() {
        BasicDBObject singInfo = getSingInfo();
        if (singInfo == null)
            return 1.0f;

        return (float)singInfo.getDouble(DBConstants.F_FORMANT);
    }



    public boolean getGuessIsCorrect() {
        return getBoolean(DBConstants.F_CORRECT);
    }

    public BasicDBObject getDescLabelInfo() {
        return (BasicDBObject )getObject(DBConstants.F_DESC_LABEL_INFO);
    }

    public int getDescStyle() {
        BasicDBObject labelInfo = getDescLabelInfo();
        if (labelInfo == null){
            return 0;
        }

        return DBObjectUtil.getInt(labelInfo, DBConstants.F_STYLE);


    }

//    public float getDescXRatio() {
//        BasicDBObject labelInfo = getDescLabelInfo();
//        if (labelInfo == null){
//            return 0;
//        }
//
//        return (float)DBObjectUtil.getDouble(labelInfo, DBConstants.F_X_RATIO);
//
//    }
//
//    public float getDescYRatio() {
//        BasicDBObject labelInfo = getDescLabelInfo();
//        if (labelInfo == null){
//            return 0;
//        }
//
//        return (float)DBObjectUtil.getDouble(labelInfo, DBConstants.F_Y_RATIO);
//
//    }

    public int getDescStrokeTextColor() {
        BasicDBObject labelInfo = getDescLabelInfo();
        if (labelInfo == null){
            return 0;
        }

        return DBObjectUtil.getInt(labelInfo, DBConstants.F_STROKE_TEXT_COLOR);

    }

    public int getDescTextColor() {
        BasicDBObject labelInfo = getDescLabelInfo();
        if (labelInfo == null){
            return 0;
        }

        return DBObjectUtil.getInt(labelInfo, DBConstants.F_TEXT_COLOR);
    }

    public int getDescFrameX() {
        BasicDBObject labelInfo = getDescLabelInfo();
        if (labelInfo == null){
            return 0;
        }

        return DBObjectUtil.getInt(labelInfo, DBConstants.F_FRAME_X);
    }

    public int getDescFrameY() {
        BasicDBObject labelInfo = getDescLabelInfo();
        if (labelInfo == null){
            return 0;
        }

        return DBObjectUtil.getInt(labelInfo, DBConstants.F_FRAME_Y);

    }

    public int getDescFrameWidth() {
        BasicDBObject labelInfo = getDescLabelInfo();
        if (labelInfo == null){
            return 0; //(int)getCanvasWidth();
        }

        int width = DBObjectUtil.getInt(labelInfo, DBConstants.F_FRAME_WIDTH);
//        if (width == 0){
//            return (int)getCanvasWidth();
//        }

        return width;
    }


    public int getDescFrameHeight() {
        BasicDBObject labelInfo = getDescLabelInfo();
        if (labelInfo == null){
            return 0; //(int)getCanvasHeight();
        }

        int height = DBObjectUtil.getInt(labelInfo, DBConstants.F_FRAME_HEIGHT);
//        if (height == 0){
//            return (int)getCanvasHeight();
//        }

        return height;

    }

    public float getDescTextFontSize() {
        BasicDBObject labelInfo = getDescLabelInfo();
        if (labelInfo == null){
            return 0;
        }

        return (float)DBObjectUtil.getDouble(labelInfo, DBConstants.F_TEXT_FONT_SIZE);

    }

    public float getCanvasHeight() {
        return (float)getDouble(DBConstants.F_CANVAS_HEIGHT);
    }

    public float getCanvasWidth() {
        return (float)getDouble(DBConstants.F_CANVAS_WIDTH);
    }

    public void setCanvasWidth(float value) {
        put(DBConstants.F_CANVAS_WIDTH, value);
    }

    public void setCanvasHeight(float value) {
        put(DBConstants.F_CANVAS_HEIGHT, value);
    }

    public static boolean isCreateAfterAugust(String opusId) {
        return DateUtil.idAfterDate(opusId, "20130818000000");
    }

    public boolean isOpusToUser() {
        return (!StringUtil.isEmpty(getTargetUserId()));
    }

    public boolean isDeleted() {

        return (getOpusStatus() == STATUS_DELETE);
    }

    public void setUserVip(int vip) {
        put(DBConstants.F_VIP, vip);
    }

    public int getUserVip(){
        return getInt(DBConstants.F_VIP);
    }

    public boolean isContestDrawType() {
        return (getType() == TYPE_DRAW_TO_CONTEST);
    }

    public int getOpusToUserType(int category) {

        switch (category){
            case GameConstantsProtos.PBOpusCategoryType.SING_CATEGORY_VALUE:
                return PBOpusType.SING_TO_USER_VALUE;

            case GameConstantsProtos.PBOpusCategoryType.DRAW_CATEGORY_VALUE:
            default:
                return PBOpusType.DRAW_TO_USER_VALUE;

        }
    }

    public void setDraftCreateDate(Date draftCreateDate) {
        put(DBConstants.F_DRAFT_CREATE_DATE, draftCreateDate);
    }

    public void setDraftCompleteDate(Date date) {
        put(DBConstants.F_DRAFT_COMPLETE_DATE, date);
    }

    public void setStrokes(long strokes) {
        put(DBConstants.F_STROKES, strokes);
    }

    public int getDraftCreateDate() {
        return getIntDate(DBConstants.F_DRAFT_CREATE_DATE);
    }

    public int getDraftCompleteDate() {
        return getIntDate(DBConstants.F_DRAFT_COMPLETE_DATE);
    }

    public long getStrokes(){
        return getLong(DBConstants.F_STROKES);
    }

    public List<String> getClassList() {
        return getStringList(DBConstants.F_CLASS);
    }

    public List<GameBasicProtos.PBClass> getPBClassList() {

        List<String> classList = getClassList();
        if (classList.size() == 0){
            return Collections.emptyList();
        }

        List<GameBasicProtos.PBClass> list = new ArrayList<GameBasicProtos.PBClass>();
        for (String className : classList){
            GameBasicProtos.PBClass.Builder builder = GameBasicProtos.PBClass.newBuilder();
            builder.setClassId(className);
            list.add(builder.build());
        }

        return list;
    }

    public void setOpusClassList(List<String> classList) {
        put(DBConstants.F_CLASS, classList);
    }

    public void setTutorialId(String tutorialId) {
        put(DBConstants.F_TUTORIAL_ID, tutorialId);
    }

    public void setStageId(String value) {
        put(DBConstants.F_STAGE_ID, value);
    }

    public void setChapterOpusId(String value) {
        put(DBConstants.F_CHAPTER_OPUS_ID, value);
    }

    public void setRemoteUserTutorialId(String value) {
        put(DBConstants.F_REMOTE_USER_TUTORIAL_ID, value);
    }

    public void setLocalUserTutorialId(String value) {
        put(DBConstants.F_LOCAL_USER_TUTORIAL_ID, value);
    }

    public void setStageIndex(int value) {
        put(DBConstants.F_STAGE_INDEX, value);
    }

    public void setStageScore(int value) {
        put(DBConstants.F_STAGE_SCORE, value);
    }

    public void setChapterIndex(int value) {
        put(DBConstants.F_CHAPTER_INDEX, value);
    }

    public int getTotalCount() {
        return getInt(DBConstants.F_TOTAL_COUNT);
    }

    public int getTotalDefeat() {
        return getInt(DBConstants.F_TOTAL_DEFEAT);
    }

    public String getDeviceOs() {
        return getString(DBConstants.F_DEVICEOS);
    }

    public float getBgImageHeight() {
        return (float)getDouble(DBConstants.F_BGIMAGE_HEIGHT);
    }

    public float getBgImageWidth() {
        return (float)getDouble(DBConstants.F_BGIMAGE_WIDTH);
    }

    public void setBgImageHeight(float value) {
        put(DBConstants.F_BGIMAGE_HEIGHT, value);
    }

    public void setBgImageWidth(float value) {
        put(DBConstants.F_BGIMAGE_WIDTH, value);
    }

    public boolean isFeature() {
        return getBoolean(DBConstants.F_FEATURE_OPUS);
    }

    public void setStageRank(int rank) {
        put(DBConstants.F_STAGE_RANK, rank);
    }

    public int getStageRank() {
        return getInt(DBConstants.F_STAGE_RANK);
    }

    public int getStageScore() {
        return getInt(DBConstants.F_STAGE_SCORE);
    }

    public String getTutorialId() {
        return getString(DBConstants.F_TUTORIAL_ID);
    }

    public String getStageId() {
        return getString(DBConstants.F_STAGE_ID);
    }

}
