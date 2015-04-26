package com.orange.game.model.dao.bbs;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.orange.common.elasticsearch.ESORMable;
import com.orange.common.utils.StringUtil;
import com.orange.game.model.manager.ScoreManager;
import com.orange.game.model.manager.bbs.BBSManager;
import org.bson.types.ObjectId;

import com.mongodb.DBObject;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.CommonData;

public class BBSPost extends CommonData implements ESORMable {

	public static final int StatusNormal = 0;
	public static final int StatusDelete = 1;
	public static final int StatusMark = 0x1 << 1;
	public static final int StatusTop = 0x1 << 2;
	
	
	public BBSPost(DBObject dbObject) {
		super(dbObject);
		// TODO Auto-generated constructor stub
	}

	public BBSPost() {
		super();
	}

	public BBSPost(String boardId, String appId, int deviceType, BBSUser user,
			BBSContent content, BBSReward reward) {
		super();
		setPostId(new ObjectId());
		setBoardId(boardId);
		setAppId(appId);
		setDeviceType(deviceType);
		setCreateUser(user);
		setContent(content);
		setReward(reward);
		Date date = new Date();
		setCreateDate(date);
		setModifyDate(date);
		setReplyCount(0);
		setSupportCount(0);
		setStatus(StatusNormal);
	}


	public void setStatus(int status) {
		put(DBConstants.F_STATUS, status);
	}



	public int getStatus() {
		return getInt(DBConstants.F_STATUS);
	}

	public void setModifyDate(Date date) {
		put(DBConstants.F_MODIFY_DATE, date);
	}

	public Date getModifyDate() {
		return getDate(DBConstants.F_MODIFY_DATE);
	}

	public String getPostId() {
		ObjectId oId = getObjectId();
		if (oId != null) {
			return oId.toString();
		}
		return null;
	}

	public int getDeviceType() {
		return getInt(DBConstants.F_DEVICE_TYPE);
	}

	public Date getCreateDate() {
		return getDate(DBConstants.F_CREATE_DATE);
	}

	public BBSUser getCreateUser() {
		DBObject object = (DBObject) getObject(DBConstants.F_CREATE_USER);
		if (object != null) {
			return new BBSUser(object);
		}else{
            log.info("<getCreateUser> user is null, post id = " + getPostId());
        }
		return null;
	}

	public BBSContent getContent() {
		DBObject object = (DBObject) getObject(DBConstants.F_CONTENT);
		if (object != null) {
			BBSContent content = new BBSContent(object);
			int type = content.getType();
			if (type == BBSContent.ContentTypeImage) {
				content = new BBSImageContent(object);
			} else if (type == BBSContent.ContentTypeDraw) {
				content = new BBSDrawContent(object);
			}

            return content;
		}
		return null;
	}

	public String getBoardId() {
		ObjectId oId = (ObjectId) getObject(DBConstants.F_BOARD_ID);
		if (oId != null) {
			return oId.toString();
		}
		return null;
	}

	public String getAppId() {
		String appId = getString(DBConstants.F_APPID);
		if (appId == null) {
			return "";
		}
		return appId;
	}

	public int getReplyCount() {
		return getInt(DBConstants.F_REPLY_COUNT);
	}

	public BBSReward getReward() {
		DBObject object = (DBObject) getObject(DBConstants.F_REWARD);
		if (object != null) {
			return new BBSReward(object);
		}
		return null;
	}

	// setter
	public void setPostId(ObjectId postId) {
		put("_id", postId);
	}

	public void setDeviceType(int deviceType) {
		put(DBConstants.F_DEVICE_TYPE, deviceType);
	}

	public void setCreateDate(Date createDate) {
		put(DBConstants.F_CREATE_DATE, createDate);
	}

	public void setCreateUser(BBSUser createUser) {
		if (createUser != null) {
			put(DBConstants.F_CREATE_USER, createUser.getDbObject());
		}
	}

	public void setContent(BBSContent content) {
		if (content != null) {
			put(DBConstants.F_CONTENT, content.getDbObject());
		}
	}

	public int getSupportCount() {
		return getInt(DBConstants.F_SUPPROT_COUNT);
	}

	public void setBoardId(String boardId) {
		if (boardId != null) {
			put(DBConstants.F_BOARD_ID, new ObjectId(boardId));
		}
	}

	public void setAppId(String appId) {
		put(DBConstants.F_APPID, appId);
	}

	public void setReplyCount(int replyCount) {
		put(DBConstants.F_REPLY_COUNT, replyCount);
	}

	public void setSupportCount(int supportCount) {
		put(DBConstants.F_SUPPROT_COUNT, supportCount);
	}

	public void setReward(BBSReward reward) {
		if (reward != null) {
			put(DBConstants.F_REWARD, reward.getDbObject());
		}
	}
    public boolean isMarked()
    {
        return getBoolean(DBConstants.F_ISMARKED);
    }



    @Override
    public Map<String, Object> getESORM() {
        if (getDbObject() == null || getPostId() == null){
            return null;
        }
        Map<String, Object> ormMap = new HashMap<String, Object>(2);
        String nick = StringUtil.getEmptyStringWhenNull(getCreatorNickName());
        String content = StringUtil.getEmptyStringWhenNull(getContentText());
        String postId = StringUtil.getEmptyStringWhenNull(getPostId());
        ormMap.put(DBConstants.ES_NICK_NAME, nick);
        ormMap.put(DBConstants.ES_CONTENT, content);
        ormMap.put(DBConstants.ES_POST_ID, postId);

        if (mode == BBSManager.MODE_GROUP){
            String boardId = StringUtil.getEmptyStringWhenNull(getBoardId());
            ormMap.put(DBConstants.ES_GROUP_ID, boardId);
        }

        return ormMap;
    }
    private int mode = BBSManager.MODE_BBS;

    public void setMode(int mode){
        this.mode = mode;
    }

    @Override
    public String getESIndexType() {
        if (mode == BBSManager.MODE_GROUP)
            return DBConstants.ES_INDEX_TYPE_GROUP_TOPIC;
        return DBConstants.ES_INDEX_TYPE_POST;
    }


    @Override
    public String getID() {
        return getStringObjectId();
    }

    @Override
    public List<String> fieldsForIndex() {
        return null;
    }


    @Override
    public String getESIndexName() {
        return DBConstants.ES_INDEX_NAME;
    }

    @Override
    public boolean hasFieldForSearch() {
        if (getDbObject() == null){
            return false;
        }
        return (getContentText() != null || getCreatorNickName() != null);
    }


    @Override
    public boolean canBeIndexed()
    {
    	return getStatus() != BBSPost.StatusDelete;    	
    }


    private String getContentText() {
        BBSContent content = getContent();
        if (content != null){
            return content.getText();
        }
        return null;
    }

    private String getCreatorNickName() {
        BBSUser creator = getCreateUser();
        if (creator != null){
            return creator.getNickName();
        }
        return null;
    }

    public void setPrivate(boolean isPrivate) {
        put(DBConstants.F_IS_PRIVATE, isPrivate);
    }

    public boolean isPrivate(){
        return getBoolean(DBConstants.F_IS_PRIVATE);
    }
    final private static double POST_SCORE_ACTION_COEFFICIENT = 3.0;

    public double hotScore() {
        int supportCount = getSupportCount();
        int commentCount = getReplyCount();
        double heavy = (supportCount + commentCount) * POST_SCORE_ACTION_COEFFICIENT;
        Date date = getCreateDate();
        if (date == null){
            date = new Date();
        }
        return ScoreManager.calculateScore(heavy, date);
    }

    public void setForTutorial(boolean forTutorial){
        put(DBConstants.F_FOR_TUTORIAL,forTutorial);
    }

    public boolean getForTutorial(boolean forTutorial){
        return getBoolean(DBConstants.F_FOR_TUTORIAL);
    }

    public boolean isDelete() {
        return (getStatus() == StatusDelete);
    }

}
