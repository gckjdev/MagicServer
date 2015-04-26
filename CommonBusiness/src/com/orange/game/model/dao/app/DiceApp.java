package com.orange.game.model.dao.app;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.notnoop.apns.ApnsService;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.Price;
import com.orange.game.model.manager.NotificationService;
import com.orange.game.model.xiaoji.AbstractXiaoji;
import com.relayrides.pushy.apns.PushManager;
import com.relayrides.pushy.apns.util.SimpleApnsPushNotification;

public class DiceApp extends AbstractApp {

	public DiceApp(String appid, String gameid) {
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
		List<BasicDBObject> list = new ArrayList<BasicDBObject>();		
		// create default dice item
		BasicDBObject diceRerollItem = createItem(
				DBConstants.C_ITEM_TYPE_DICE_REROLL,
				DBConstants.C_DEFAULT_DICE_REROLL_AMOUNT);
		BasicDBObject diceDoubleCoinsItem = createItem(
				DBConstants.C_ITEM_TYPE_DICE_DOUBLE_COINS,
				DBConstants.C_DEFAULT_DICE_DOUBLE_COINS_AMOUNT);

		list.add(diceRerollItem);
		list.add(diceDoubleCoinsItem);
		return list;
	}

	@Override
	public Price getPrice(int pricePackage) {
		return new Price(Price.DICE_COIN_AMOUNT_LIST[pricePackage], 
				Price.DICE_COIN_VALUE_LIST[pricePackage], 
				Price.DICE_COIN_PRODUCT_ID_LIST[pricePackage],
				Price.COIN_SAVE_LIST[pricePackage]);	}

	@Override
	public int getPriceListSize() {
		return Price.DICE_COIN_PRICE_LIST_SIZE;
	}

	@Override
	public ApnsService apnsService() {
		return NotificationService.dicePushApsnServiceHolder.get();
	}

	@Override
	public boolean isSupportMessage() {
		return true;
	}

	@Override
	public String welcomeMessage() {
		String message = "大话骰欢迎你！ 欢乐棋牌，趣味无穷 ！ 快快招呼你的朋友一起玩吧～";
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
