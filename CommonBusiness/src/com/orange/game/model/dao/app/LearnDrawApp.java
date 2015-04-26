package com.orange.game.model.dao.app;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.notnoop.apns.ApnsService;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.Price;
import com.orange.game.model.xiaoji.AbstractXiaoji;
import com.orange.game.model.xiaoji.XiaojiFactory;
import com.relayrides.pushy.apns.PushManager;
import com.relayrides.pushy.apns.util.SimpleApnsPushNotification;

public class LearnDrawApp extends AbstractApp {

	public LearnDrawApp(String appid, String gameid) {
		this.appId = appid;
		this.gameId = gameid;
	}

	@Override
	public boolean isFree() {
		return true;
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
		List<BasicDBObject> list = new ArrayList<BasicDBObject>();		
		BasicDBObject playItem = createItem(DBConstants.C_ITEM_TYPE_PAINT_PLAYER, 1);	
		list.add(playItem);
		return list;
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
		return null;
	}

	@Override
	public boolean isSupportMessage() {
		return false;
	}

	@Override
	public String welcomeMessage() {
		return null;
	}

	@Override
	public String welcomeCustomerServiceId() {
		return null;
	}

	@Override
	public AbstractXiaoji getXiaoji() {
		return XiaojiFactory.getInstance().getDraw();
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
