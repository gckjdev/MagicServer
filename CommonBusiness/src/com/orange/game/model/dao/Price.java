package com.orange.game.model.dao;

import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.app.AbstractApp;
import com.orange.game.model.dao.app.AppFactory;
//import com.orange.game.model.dao.App;
public class Price {
	int amount;
	double vale;
	int savePercent;
	String appleProductId;

//	public static int COIN_AMOUNT1 = 400;
//	public static int COIN_AMOUNT2 = 1200;
//	public static int COIN_AMOUNT3 = 3600;
//	
//	public static int ITEM_AMOUNT1 = 10;
//	public static int ITEM_AMOUNT2 = 20;
//	public static int ITEM_AMOUNT3 = 50;
//	
//	public static double COIN_VALUE1 = 1.99;
//	public static double COIN_VALUE2 = 4.99;
//	public static double COIN_VALUE3 = 9.99;
//	
//	public static double ITEM_VALUE1 = 400;
//	public static double ITEM_VALUE2 = 720;
//	public static double ITEM_VALUE3 = 1600;
	
	public final static int COIN_SAVE_LIST[] = {0, 15, 33, 50};
	public final static int COIN_AMOUNT_LIST[] = {8000, 14400, 60000, 200000};
	public final static int DICE_COIN_AMOUNT_LIST[] = {10000, 18000, 66000, 180000};
	public final static int ZJH_COIN_AMOUNT_LIST[] = {10000, 18000, 66000, 180000};
	
	public final static int ITEM_AMOUNT_LIST[] = {10, 20, 50};
	public final static int ITEM_SAVE_LIST[] = {0, 10, 20};
	public final static double COIN_VALUE_LIST[] = {1.99, 2.99, 9.99, 24.99};
	public final static double DICE_COIN_VALUE_LIST[] = {1.99, 2.99, 9.99, 24.99};
	public final static double ZJH_COIN_VALUE_LIST[] = {1.99, 2.99, 9.99, 24.99};
	public final static double ITEM_VALUE_LIST[] = {400, 720, 1600};
	
	public final static String COIN_PRODUCT_ID_LIST[] = {"com.orange.draw.coins400", "com.orange.draw.coins2400", "com.orange.draw.coins6000", "com.orange.draw.coins20000"};
	public final static String PRO_COIN_PRODUCT_ID_LIST[] = {"com.orange.drawpro.coins400", "com.orange.drawpro.coins2400", "com.orange.drawpro.coins6000", "com.orange.drawpro.coins20000"};
	public final static String DICE_COIN_PRODUCT_ID_LIST[] = {"com.orange.dice.coins1200", "com.orange.dice.coins2400", "com.orange.dice.coins6000", "com.orange.dice.coins20000"};
	public final static String ZJH_COIN_PRODUCT_ID_LIST[] = {"com.orange.zjh.coins1200", "com.orange.zjh.coins2400", "com.orange.zjh.coins6000", "com.orange.zjh.coins20000"};

	public static int DICE_COIN_PRICE_LIST_SIZE = DICE_COIN_PRODUCT_ID_LIST.length;
	public static int ZJH_COIN_PRICE_LIST_SIZE = ZJH_COIN_PRODUCT_ID_LIST.length;
	public static int PRO_COIN_PRICE_LIST_SIZE = PRO_COIN_PRODUCT_ID_LIST.length;
	public static int COIN_PRICE_LIST_SIZE = COIN_AMOUNT_LIST.length;
	public static int ITEM_PRICE_LIST_SIZE = ITEM_AMOUNT_LIST.length;
	
	public Price(int amount, double vale, int save) {
		super();
		this.amount = amount;
		this.vale = vale;
		this.savePercent = save;
	}
	
	public Price(int amount, double vale, String productId, int save) {
		super();
		this.amount = amount;
		this.vale = vale;
		this.appleProductId = productId;
		this.savePercent = save;
	}

	public int getAmount() {
		return amount;
	}
	public void setAmount(int amount) {
		this.amount = amount;
	}
	public double getVale() {
		return vale;
	}
	public void setVale(double vale) {
		this.vale = vale;
	}
	
	public static int getPriceListSizeByType(int type, String appId){
		int listSize = 0;
		if (type == ServiceConstant.CONST_PRICE_TYPE_COIN){
			
			AbstractApp app = AppFactory.getInstance().getApp(appId);
			if (app == null)
				return 0;

			return app.getPriceListSize();
			
//			if (App.isDrawProApp(appId)){
//				listSize = PRO_COIN_PRICE_LIST_SIZE;
//			}
//			else if (App.isDiceApp(appId)){
//				listSize = DICE_COIN_PRICE_LIST_SIZE;
//			}
//			else if (App.isZhajinhuaApp(appId)){
//				listSize = ZJH_COIN_PRICE_LIST_SIZE;
//			}			
//			else{
//				listSize = COIN_PRICE_LIST_SIZE;
//			}
		}
		else{
			listSize = ITEM_PRICE_LIST_SIZE;
		}
		return listSize;
	}
	
	public static Price getPriceByTypeAndPackage(int type, String appId, int pricePackage) {
		
		int listSize = getPriceListSizeByType(type, appId);		
		if(pricePackage < 0 || pricePackage >= listSize)
			return null;
		
		if (type == ServiceConstant.CONST_PRICE_TYPE_COIN) {
			
			AbstractApp app = AppFactory.getInstance().getApp(appId);
			if (app == null)
				return null;
			
			return app.getPrice(pricePackage);
			
			/*
			if (App.isDrawProApp(appId)){
				return new Price(COIN_AMOUNT_LIST[pricePackage], 
						COIN_VALUE_LIST[pricePackage], 
						PRO_COIN_PRODUCT_ID_LIST[pricePackage],
						COIN_SAVE_LIST[pricePackage]);
			}
			else if (App.isDiceApp(appId)){
				return new Price(DICE_COIN_AMOUNT_LIST[pricePackage], 
						DICE_COIN_VALUE_LIST[pricePackage], 
						DICE_COIN_PRODUCT_ID_LIST[pricePackage],
						COIN_SAVE_LIST[pricePackage]);				
			}
			else if (App.isZhajinhuaApp(appId)){
				return new Price(ZJH_COIN_AMOUNT_LIST[pricePackage], 
						ZJH_COIN_VALUE_LIST[pricePackage], 
						ZJH_COIN_PRODUCT_ID_LIST[pricePackage],
						COIN_SAVE_LIST[pricePackage]);				
			}			
			else{
				return new Price(COIN_AMOUNT_LIST[pricePackage], 
						COIN_VALUE_LIST[pricePackage], 
						COIN_PRODUCT_ID_LIST[pricePackage],
						COIN_SAVE_LIST[pricePackage]);
			}
			*/
		}else if (type == ServiceConstant.CONST_PRICE_TYPE_ITEM) {
			return new Price(ITEM_AMOUNT_LIST[pricePackage], 
					ITEM_VALUE_LIST[pricePackage],
					ITEM_SAVE_LIST[pricePackage]);
		}
		return null;
	}

	public String getAppleProductId() {
		return this.appleProductId;
	}
	
	public int getSavePercent() {
		return savePercent;
	}
}
