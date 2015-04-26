package com.orange.game.model.dao.app;

import java.util.HashSet;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.notnoop.apns.ApnsService;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.App;
import com.orange.game.model.dao.Price;
import com.orange.game.model.xiaoji.AbstractXiaoji;
import com.relayrides.pushy.apns.PushManager;
import com.relayrides.pushy.apns.util.SimpleApnsPushNotification;

public abstract class AbstractApp {

	HashSet<String> appIdList = new HashSet<String>();
	String gameId;
	String appId;
		
	public abstract boolean isFree();
	
	public String appId(){
		return appId;
	}
	
	public String gameId(){
		return gameId;
	}
	
	public abstract int registrationRewardIngot();
	public abstract int registrationRewardCoin();
	
	public abstract int registrationInitIngot();
	public abstract int registrationInitCoin();
	
	// default items
	public abstract List<BasicDBObject> defaultItems();
	
	// for old coin price
	public abstract Price getPrice(int pricePackage);
	public abstract int getPriceListSize();
	
	public abstract ApnsService apnsService();

    public abstract PushManager<SimpleApnsPushNotification> pushManager();
    public abstract PushManager<SimpleApnsPushNotification> createPushManager();

    static Object pushManagerLock = new Object();
    public PushManager<SimpleApnsPushNotification> smartPushManager(){
        synchronized (pushManagerLock){
            PushManager<SimpleApnsPushNotification> manager = pushManager();
            if (manager == null){
                return null;
            }

            if (manager.isShutDown()){
                manager = createPushManager();
            }

            return manager;
        }
    }
	
	public String gameUserRankFieldName(){
		return gameUserRankFieldName(gameId());
	}
	
	public static String gameUserRankFieldName(String gameId){
		String fieldString = gameId + "_rank_score";
		return fieldString.toLowerCase();
	}
	
	public static String userLevelField(String gameId){
		if (gameId == null)
			return "";
		
		String fieldString = "level_" + gameId.toLowerCase();
		return fieldString;
	}	
	
	public static String userLevelFieldByAppId(String appId){
		String gameId = App.getGameIdByAppId(appId);
		return userLevelField(gameId);
	}
	
	public abstract boolean isSupportMessage();
	public abstract String welcomeMessage();
	public abstract String welcomeCustomerServiceId();
	public abstract AbstractXiaoji getXiaoji();	
	
	protected static BasicDBObject createItem(int type, int amount) {

		BasicDBObject tipsItem = new BasicDBObject();
		tipsItem.put(DBConstants.F_ITEM_TYPE, type);
		tipsItem.put(DBConstants.F_ITEM_AMOUNT, amount);
		return tipsItem;
	}

    protected static void addItemIntoList(int type, List<BasicDBObject> list) {
        BasicDBObject item = createItem(type, 1);
        list.add(item);
    }

	public boolean isAppMe(String appId) {
		return appId().equalsIgnoreCase(appId);
	}
	
	public boolean isGameMe(String gameId) {
		return gameId().equalsIgnoreCase(gameId);
	}
	
	public String toString(){
		return appId + ","+ gameId;
	}

	
}
