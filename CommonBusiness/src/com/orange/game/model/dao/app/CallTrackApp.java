package com.orange.game.model.dao.app;

import java.util.Collections;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.notnoop.apns.ApnsService;
import com.orange.game.model.dao.Price;
import com.orange.game.model.manager.NotificationService;
import com.orange.game.model.xiaoji.AbstractXiaoji;
import com.relayrides.pushy.apns.PushManager;
import com.relayrides.pushy.apns.util.SimpleApnsPushNotification;

public class CallTrackApp extends AbstractApp {

	public CallTrackApp(String appidCalltrack, String gameIdCalltrack) {
		this.appId = appidCalltrack;
		this.gameId = gameIdCalltrack;
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
	public List<BasicDBObject> defaultItems() {
		return Collections.emptyList();
	}

	@Override
	public Price getPrice(int pricePackage) {
		return null;
	}

	@Override
	public int getPriceListSize() {
		return 0;
	}

	@Override
	public ApnsService apnsService() {
		return NotificationService.callTrackPushApsnServiceHolder.get();
	}

	@Override
	public boolean isSupportMessage() {
		return true;
	}

	@Override
	public String welcomeMessage() {
		String message = "Welcome! 欢迎使用本应用！";		
		return message;
	}

	@Override
	public String welcomeCustomerServiceId() {
		String CUSTOMER_SERVICER_ID = "888888888888888888888888";
		return CUSTOMER_SERVICER_ID;
	}
	
	@Override
	public AbstractXiaoji getXiaoji() {
		return null;
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
