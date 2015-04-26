package com.orange.game.model.dao.bbs;

import com.mongodb.DBObject;
import com.orange.common.utils.StringUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.manager.bbs.BBSManager;

public class BBSDrawContent extends BBSContent {
	public BBSDrawContent(DBObject object) {
		super(object);
		setType(ContentTypeDraw);
	}

	public BBSDrawContent() {
		super();
		setType(ContentTypeDraw);
	}

	public BBSDrawContent(String text, String drawThumbImage, String drawImage,
			byte[] drawData) {
		super(BBSDrawContent.ContentTypeDraw, text);
		setDrawLargeImageURL(drawImage);
		setDrawThumbImageURL(drawThumbImage);
		setDrawData(drawData);
	}

	public String getDrawThumbImageURL() {
		String relativeURL = getString(DBConstants.F_DRAW_THUMB_URL);
		if (StringUtil.isEmpty(relativeURL)) {
			return null;
		}
		String remoteURLString = BBSManager.getDrawImageUploadRemoteDir();
		return remoteURLString + relativeURL;
	}

	public String getDrawLargeImageURL() {
		String relativeURL = getString(DBConstants.F_DRAW_IMAGE_URL);
		if (StringUtil.isEmpty(relativeURL)) {
			return null;
		}
		String remoteURLString = BBSManager.getDrawImageUploadRemoteDir();
		return remoteURLString + relativeURL;

	}

	public void setDrawThumbImageURL(String thumbImageURL) {
		put(DBConstants.F_DRAW_THUMB_URL, thumbImageURL);
	}

	public void setDrawLargeImageURL(String largeImageURL) {
		put(DBConstants.F_DRAW_IMAGE_URL, largeImageURL);
	}

	public void setDrawData(byte[] drawData) {
		put(DBConstants.F_DRAW_DATA, drawData);
	}

	public byte[] getDrawData() {
		return (byte[]) getObject(DBConstants.F_DRAW_DATA);
	}
}
