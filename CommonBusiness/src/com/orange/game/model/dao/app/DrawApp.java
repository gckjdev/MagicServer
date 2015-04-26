package com.orange.game.model.dao.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.mongodb.BasicDBObject;
import com.notnoop.apns.ApnsService;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.Price;
import com.orange.game.model.manager.MessageManager;
import com.orange.game.model.manager.NotificationService;
import com.orange.game.model.xiaoji.AbstractXiaoji;
import com.orange.game.model.xiaoji.XiaojiFactory;
import com.relayrides.pushy.apns.PushManager;
import com.relayrides.pushy.apns.util.SimpleApnsPushNotification;

public class DrawApp extends AbstractApp {

	public DrawApp(String appidDraw, String gameIdDraw) {
		this.appId = appidDraw;
		this.gameId = gameIdDraw;
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
		return 2000;
	}

	@Override
	public int registrationInitIngot() {
		return 0;
	}

	@Override
	public int registrationInitCoin() {
		return 2000;
	}

	@Override
	public List<BasicDBObject> defaultItems() {		
		List<BasicDBObject> list = new ArrayList<BasicDBObject>();		
		BasicDBObject tipsItem = createItem(DBConstants.C_ITEM_TYPE_TIPS,
				DBConstants.C_DEFAULT_TIPS_AMOUNT);
		list.add(tipsItem);

        // start from 7.0 version
        ///*
//        addItemIntoList(DBConstants.C_ITEM_TYPE_COPY_PAINT, list);
//        addItemIntoList(DBConstants.C_ITEM_TYPE_SHADOW, list);
//        addItemIntoList(DBConstants.C_ITEM_TYPE_GRADIENT, list);

        addItemIntoList(DBConstants.C_ITEM_TYPE_PAINT_PALETTE, list);
        addItemIntoList(DBConstants.C_ITEM_TYPE_PAINT_ALPHA, list);
        addItemIntoList(DBConstants.C_ITEM_TYPE_PAINT_PLAYER, list);
        addItemIntoList(DBConstants.C_ITEM_TYPE_PAINT_STRAW, list);
        addItemIntoList(DBConstants.C_ITEM_TYPE_PAINT_ERASER, list);
        addItemIntoList(DBConstants.C_ITEM_TYPE_PAINT_GRID, list);

        addItemIntoList(DBConstants.C_ITEM_TYPE_BACKGROUND_1, list);
        /*
        addItemIntoList(DBConstants.C_ITEM_TYPE_BACKGROUND_2, list);
        addItemIntoList(DBConstants.C_ITEM_TYPE_BACKGROUND_12, list);
        */

        addItemIntoList(DBConstants.C_ITEM_TYPE_PAINT_SHAPE, list);

        addItemIntoList(DBConstants.C_ITEM_TYPE_CANVAS_IPHONE_DEFAULT, list);
        addItemIntoList(DBConstants.C_ITEM_TYPE_CANVAS_IPAD_DEFAULT, list);

        addItemIntoList(DBConstants.C_ITEM_TYPE_CANVAS_IPAD_HORIZONTAL, list);
        addItemIntoList(DBConstants.C_ITEM_TYPE_CANVAS_IPAD_VERTICAL, list);

        addItemIntoList(DBConstants.C_ITEM_TYPE_CANVAS_IPHONE_HORIZONTAL, list);
        addItemIntoList(DBConstants.C_ITEM_TYPE_CANVAS_IPHONE_VERTICAL, list);

        addItemIntoList(DBConstants.C_ITEM_TYPE_CANVAS_IPHONE5_HORIZONTAL, list);
        addItemIntoList(DBConstants.C_ITEM_TYPE_CANVAS_IPHONE5_VERTICAL, list);

        //*/
		return list;
	}

	@Override
	public Price getPrice(int pricePackage) {
		return new Price(Price.COIN_AMOUNT_LIST[pricePackage], 
				Price.COIN_VALUE_LIST[pricePackage], 
				Price.COIN_PRODUCT_ID_LIST[pricePackage],
				Price.COIN_SAVE_LIST[pricePackage]);	
	}

	@Override
	public int getPriceListSize() {
		// TODO Auto-generated method stub
		return Price.COIN_PRICE_LIST_SIZE;
	}

	@Override
	public ApnsService apnsService() {
		return NotificationService.drawPushApsnServiceHolder.get();
	}

    @Override
    public PushManager<SimpleApnsPushNotification> pushManager() {
        return drawPushManager;
    }

    @Override
    public PushManager<SimpleApnsPushNotification> createPushManager() {
        return NotificationService.createPushService(
                NotificationService.DRAW_DEVELOPMENT_CERTIFICATION_PATH,
                NotificationService.DRAW_CERTIFICATION_PASSWORD,
                NotificationService.DRAW_PRODUCTION_CERTIFICATION_PATH,
                NotificationService.DRAW_CERTIFICATION_PASSWORD);
    }

    static PushManager<SimpleApnsPushNotification> drawPushManager =
            NotificationService.createPushService(
                    NotificationService.DRAW_DEVELOPMENT_CERTIFICATION_PATH,
                    NotificationService.DRAW_CERTIFICATION_PASSWORD,
                    NotificationService.DRAW_PRODUCTION_CERTIFICATION_PATH,
                    NotificationService.DRAW_CERTIFICATION_PASSWORD);

    @Override
	public boolean isSupportMessage() {
		return true;
	}

    @Override
    public String welcomeMessage() {
//        String message = "小吉画画欢迎你！\n喜欢绘画 —— 请【画画】，在这里可以挥舞天地\n休闲娱乐 —— 来【猜画】，挑战天才、奇才和鬼才，还可赚金币！\n结交好友 —— 逛【画榜】，看画留评论，和画友互动 \n还有更多功能，慢慢发掘！ \n欢迎加入玩家QQ群228119679\n关注新浪微博 @小吉画画\n";

        String message = "欢迎使用小吉画画！\n" +
                "\n" +
                "这里有四个板块，【画画】、【学画】、【论坛】与【精彩作品】：\n" +
                "【画画】—— 随身画板，走到哪画到哪！\n" +
                "【学画】—— 零基础学画，休闲闯关学画好开心！\n" +
                "【精彩作品】—— 观看作品绘画回放过程，还可以猜画留评论，结识更多小伙伴！\n" +
                "【论坛】—— 谈天说地，和更多热爱画画的朋友共同分享你的心得！\n" +
                "\n" +
                "还有更多功能（家族、比赛、你画我猜等等），等你来发掘，快到小吉碗里来！\n" +
                "\n" +
                "也欢迎关注我们的新浪微博与腾讯微博（@小吉画画）。感谢您的支持，祝您使用愉快！\n" +
                "\n" +
                "如有任何疑问,可以直接在此回复，我们尽快为您解答^_^";
        return message;
    }



	@Override
	public String welcomeCustomerServiceId() {		
//		String CUSTOMER_SERVICER_ID = "888888888888888888888888";
		return MessageManager.DRAW_CUSTOMER_SERVICER_ID;
	}
	
	@Override
	public AbstractXiaoji getXiaoji() {
		return XiaojiFactory.getInstance().getDraw();
	}
	

}
