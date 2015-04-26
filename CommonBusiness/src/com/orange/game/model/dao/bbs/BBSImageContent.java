package com.orange.game.model.dao.bbs;

import com.mongodb.DBObject;
import com.orange.common.utils.StringUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.manager.bbs.BBSManager;

public class BBSImageContent extends BBSContent {

	public BBSImageContent(DBObject object) {
		super(object);
	}

	public BBSImageContent() {
		super();
		setType(ContentTypeImage);
	}

	public BBSImageContent(String text, String thumbImage, String image) {
		super(ContentTypeImage, text);
		setLargeImageURL(image);
		setThumbImageURL(thumbImage);
	}



}
