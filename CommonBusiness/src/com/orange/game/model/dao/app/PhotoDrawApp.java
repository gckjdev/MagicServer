package com.orange.game.model.dao.app;

import java.util.List;

import com.mongodb.BasicDBObject;
import com.notnoop.apns.ApnsService;
import com.orange.game.model.dao.Price;
import com.relayrides.pushy.apns.PushManager;
import com.relayrides.pushy.apns.util.SimpleApnsPushNotification;

public class PhotoDrawApp extends LearnDrawApp {


	public PhotoDrawApp(String appid, String gameid) {
		super(appid, gameid);
	}

	@Override
	public boolean isFree() {
		return false;
	}
	
	@Override
	public int registrationRewardIngot() {
		return 0;
	}

	@Override
	public int registrationRewardCoin() {
		return 0;
	}

	@Override
	public int registrationInitIngot() {
		return 0;
	}

	@Override
	public int registrationInitCoin() {
		return 0;
	}

    @Override
    public PushManager<SimpleApnsPushNotification> pushManager() {
        return null;
    }


}
