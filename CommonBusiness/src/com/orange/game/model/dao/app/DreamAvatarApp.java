package com.orange.game.model.dao.app;

import com.relayrides.pushy.apns.PushManager;
import com.relayrides.pushy.apns.util.SimpleApnsPushNotification;

public class DreamAvatarApp extends LearnDrawApp {

	public DreamAvatarApp(String appid, String gameid) {
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

    @Override
    public PushManager<SimpleApnsPushNotification> createPushManager() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
