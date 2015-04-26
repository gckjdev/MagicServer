package com.orange.game.model.dao.app;

import java.util.List;

import com.mongodb.BasicDBObject;
import com.notnoop.apns.ApnsService;
import com.orange.game.model.dao.Price;
import com.orange.game.model.manager.NotificationService;
import com.relayrides.pushy.apns.PushManager;
import com.relayrides.pushy.apns.util.SimpleApnsPushNotification;

public class LittlegeeApp extends DrawApp {

	public LittlegeeApp(String appidDraw, String gameIdDraw) {
		super(appidDraw, gameIdDraw);
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
	public ApnsService apnsService() {
		return NotificationService.littlegeePushApsnServiceHolder.get();
	}

//	@Override
//	public String welcomeMessage() {
//		String message = "小吉画画欢迎你！加入画画玩家QQ群228119679和更多玩家交流，还可以关注新浪/腾讯微博 [小吉画画] "
//				+ "获取画画最新资讯和每日新鲜画榜吧！";
//		return message;
//	}

	@Override
	public String welcomeCustomerServiceId() {		
		String CUSTOMER_SERVICER_ID = "888888888888888888888888";
		return CUSTOMER_SERVICER_ID;
	}

    @Override
    public PushManager<SimpleApnsPushNotification> pushManager() {
        return littlegeePushManager;
    }

    static PushManager<SimpleApnsPushNotification> littlegeePushManager =
            NotificationService.createPushService(
                    NotificationService.LITTLEGEE_DEVELOPMENT_CERTIFICATION_PATH,
                    NotificationService.DRAW_CERTIFICATION_PASSWORD,
                    NotificationService.LITTLEGEE_PRODUCTION_CERTIFICATION_PATH,
                    NotificationService.DRAW_CERTIFICATION_PASSWORD);

    @Override
    public PushManager<SimpleApnsPushNotification> createPushManager() {
        return NotificationService.createPushService(
                NotificationService.LITTLEGEE_DEVELOPMENT_CERTIFICATION_PATH,
                NotificationService.DRAW_CERTIFICATION_PASSWORD,
                NotificationService.LITTLEGEE_PRODUCTION_CERTIFICATION_PATH,
                NotificationService.DRAW_CERTIFICATION_PASSWORD);
    }

}
