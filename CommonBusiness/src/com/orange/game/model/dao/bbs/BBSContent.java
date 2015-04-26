package com.orange.game.model.dao.bbs;

import com.mongodb.DBObject;
import com.orange.common.utils.StringUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.CommonData;
import com.orange.game.model.manager.bbs.BBSManager;

public class BBSContent extends CommonData {
//	String text;
//	int type;

	public static final int ContentTypeNo = 0;
	public static final int ContentTypeText = 1;
	public static final int ContentTypeImage = 2;
	public static final int ContentTypeDraw = 4;
    public static final int ContentTypeOpusDraw = 8;
    public static final int ContentTypeOpusSing = 16;
	

	public BBSContent(DBObject object) {
		super(object);
	}

	public BBSContent() {
		super();
		setType(ContentTypeText);
	}
	
	//default type is ContentTypeText
	public BBSContent(int type, String text) {
		super();
		setType(type);
		setText(text);
	}

	public BBSContent(String text) {
		super();
		setType(ContentTypeText);
		setText(text);
	}

    public BBSContent(String text, String thumbUrl, String imageUrl, String opusId, int opusCategory, int type) {
        super();
        setType(type);
        setText(text);
        setOpusId(opusId);
        setOpusCategory(opusCategory);
        setThumbImageURL(thumbUrl);
        setLargeImageURL(imageUrl);
    }

    private void setOpusCategory(int opusCategory) {
        put(DBConstants.F_CATEGORY, opusCategory);
    }

    private void setOpusId(String opusId) {
        put(DBConstants.F_OPUS_ID, opusId);
    }

    public String getOpusId() {
        return getString(DBConstants.F_OPUS_ID);
    }

    public int getOpusCategory() {
        return getInt(DBConstants.F_CATEGORY);
    }

    public String getText() {
		return getString(DBConstants.F_TEXT_CONTENT);
	}

	public int getType() {
		return getInt(DBConstants.F_TYPE);
	}

	public void setText(String text) {
		put(DBConstants.F_TEXT_CONTENT, text);
	}

	public void setType(int type) {
		put(DBConstants.F_TYPE, type);
	}

    public String getThumbImageURL() {
        String relativeURL = getString(DBConstants.F_THUMB_URL);
        if (StringUtil.isEmpty(relativeURL)) {
            return null;
        }
        String remoteURLString = BBSManager.getImageUploadRemoteDir();
        return remoteURLString + relativeURL;
    }

    public String getLargeImageURL() {
        String relativeURL = getString(DBConstants.F_IMAGE_URL);
        if (StringUtil.isEmpty(relativeURL)) {
            return null;
        }
        String remoteURLString = BBSManager.getImageUploadRemoteDir();
        return remoteURLString + relativeURL;
    }

    public void setThumbImageURL(String thumbImageURL) {
        put(DBConstants.F_THUMB_URL, thumbImageURL);
    }

    public void setLargeImageURL(String largeImageURL) {
        put(DBConstants.F_IMAGE_URL, largeImageURL);
    }

}
