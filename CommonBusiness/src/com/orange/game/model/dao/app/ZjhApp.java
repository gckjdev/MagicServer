package com.orange.game.model.dao.app;

import java.util.Collections;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.notnoop.apns.ApnsService;
import com.orange.game.model.dao.Price;
import com.orange.game.model.manager.NotificationService;
import com.orange.game.model.xiaoji.AbstractXiaoji;
import com.orange.game.model.xiaoji.XiaojiFactory;
import com.relayrides.pushy.apns.PushManager;
import com.relayrides.pushy.apns.util.SimpleApnsPushNotification;

public class ZjhApp extends AbstractApp {

	public ZjhApp(String appid, String gameid) {
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
		return 1000;
	}

	@Override
	public List<BasicDBObject> defaultItems() {
		return Collections.emptyList();
	}

	@Override
	public Price getPrice(int pricePackage) {
		return new Price(Price.ZJH_COIN_AMOUNT_LIST[pricePackage], 
				Price.ZJH_COIN_VALUE_LIST[pricePackage], 
				Price.ZJH_COIN_PRODUCT_ID_LIST[pricePackage],
				Price.COIN_SAVE_LIST[pricePackage]);				
	}

	@Override
	public int getPriceListSize() {
		return Price.ZJH_COIN_PRICE_LIST_SIZE;
	}

	@Override
	public ApnsService apnsService() {
		return NotificationService.zjhPushApsnServiceHolder.get();
	}

	@Override
	public boolean isSupportMessage() {
		return true;
	}

	@Override
	public String welcomeMessage() {
		String message = "诈金花欢迎你！ 刺激好玩的诈金花扑克牌，趣味无穷 ！ 快快招呼你的朋友一起玩吧～";
		return message;
	}

	@Override
	public String welcomeCustomerServiceId() {
		String CUSTOMER_SERVICER_ID = "888888888888888888888889";
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
