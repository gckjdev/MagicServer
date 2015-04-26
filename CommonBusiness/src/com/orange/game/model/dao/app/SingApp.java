package com.orange.game.model.dao.app;

import java.util.Collections;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.notnoop.apns.ApnsService;
import com.orange.game.model.dao.Price;
import com.orange.game.model.manager.MessageManager;
import com.orange.game.model.manager.NotificationService;
import com.orange.game.model.xiaoji.AbstractXiaoji;
import com.orange.game.model.xiaoji.XiaojiFactory;
import com.relayrides.pushy.apns.PushManager;
import com.relayrides.pushy.apns.util.SimpleApnsPushNotification;

public class SingApp extends AbstractApp {

	public SingApp(String appid, String gameId) {
		this.appId = appid;
		this.gameId = gameId;
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
		return 500;
	}

	@Override
	public int registrationInitIngot() {
		return 0;
	}

	@Override
	public int registrationInitCoin() {
		return 500;
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
		return NotificationService.singPushApsnServiceHolder.get();
	}

	@Override
	public boolean isSupportMessage() {
		return true;
	}

	@Override
	public String welcomeMessage() {
		String message = "小吉大舌头欢迎你！加入玩家QQ群228119679和更多玩家交流，还可以关注新浪/腾讯微博[小吉大舌头]"
				+ "获取有趣动态";
		return message;
	}

	@Override
	public String welcomeCustomerServiceId() {		
		String CUSTOMER_SERVICER_ID = MessageManager.DRAW_CUSTOMER_SERVICER_ID; // "888888888888888888888888";
		return CUSTOMER_SERVICER_ID;
	}
	
	@Override
	public AbstractXiaoji getXiaoji() {
		return XiaojiFactory.getInstance().getSing();
	}


    @Override
    public PushManager<SimpleApnsPushNotification> pushManager() {
        return singPushManager;
    }

    static PushManager<SimpleApnsPushNotification> singPushManager =
            NotificationService.createPushService(
                    NotificationService.SING_DEVELOPMENT_CERTIFICATION_PATH,
                    NotificationService.DRAW_CERTIFICATION_PASSWORD,
                    NotificationService.SING_PRODUCTION_CERTIFICATION_PATH,
                    NotificationService.DRAW_CERTIFICATION_PASSWORD);

    @Override
    public PushManager<SimpleApnsPushNotification> createPushManager() {
        return NotificationService.createPushService(
                NotificationService.SING_DEVELOPMENT_CERTIFICATION_PATH,
                NotificationService.DRAW_CERTIFICATION_PASSWORD,
                NotificationService.SING_PRODUCTION_CERTIFICATION_PATH,
                NotificationService.DRAW_CERTIFICATION_PASSWORD);
    }

}
