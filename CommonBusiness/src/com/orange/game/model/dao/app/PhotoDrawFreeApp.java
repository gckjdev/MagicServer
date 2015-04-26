package com.orange.game.model.dao.app;

import java.util.List;

import com.mongodb.BasicDBObject;
import com.notnoop.apns.ApnsService;
import com.orange.game.model.dao.Price;
import com.relayrides.pushy.apns.PushManager;
import com.relayrides.pushy.apns.util.SimpleApnsPushNotification;

public class PhotoDrawFreeApp extends PhotoDrawApp {

	public PhotoDrawFreeApp(String appidPhotoDraw, String gameIdPhotoDraw) {
		super(appidPhotoDraw, gameIdPhotoDraw);
	}

	@Override
	public boolean isFree() {
		return true;
	}

	@Override
	public int registrationInitIngot() {
		return 0;
	}
	
	@Override
	public int registrationRewardIngot() {
		return 0;
	}

    @Override
    public PushManager<SimpleApnsPushNotification> pushManager() {
        return null;
    }

}
