package com.orange.game.model.dao;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.app.AbstractApp;
import com.orange.game.model.dao.app.AppFactory;

public class App extends CommonData {

	public App(DBObject dbObject) {
		super(dbObject);
	}

//	private enum AppRegistry {
//		Draw {
//			String appId()  { return DBConstants.APPID_DRAW;        }
//			String gameId() { return DBConstants.GAME_ID_DRAW;      }
//		},
//		DrawPro {
//			String appId()  { return DBConstants.APPID_DRAW_PRO;    }
//			String gameId() { return DBConstants.GAME_ID_DRAW;      }
//		},
//		LearnDraw {
//			String appId()  { return DBConstants.APPID_LEARN_DRAW;        }
//			String gameId() { return DBConstants.GAME_ID_LEARN_DRAW;      }
//		},
//		Dice {
//			String appId()  { return DBConstants.APPID_DICE;        }
//			String gameId() { return DBConstants.GAME_ID_DICE;      }
//		},
//		DiceNew {
//			String appId()  { return DBConstants.APPID_DICE_NEW;        }
//			String gameId() { return DBConstants.GAME_ID_DICE;      }
//		},
//		ZhaJinHua {
//			String appId()  { return DBConstants.APPID_ZHAJINHUA;   }
//			String gameId() { return DBConstants.GAME_ID_ZHAJINHUA; }
//		},
//		Candy {
//			String appId()  { return DBConstants.APPID_CANDY;       }
//			String gameId() { return DBConstants.GAME_ID_CANDY;     }
//		};
//		
//		abstract String appId();
//		abstract String gameId();
//	}
	
	
	public String getAppId() {
		String appId = (String) dbObject.get(DBConstants.F_APPID);
		return appId;
	}

	public String getVersion() {
		String version = (String) dbObject.get(DBConstants.F_VERSION);
		return version;
	}

	public String getAppUrl() {
		String appUrl = (String) dbObject.get(DBConstants.F_APPURL);
		return appUrl;
	}

	public List<String> getAppKeywordList(int type) {

		if (type == DBConstants.KEYWORD_TYPE_DEFAULT)
			return getAppKeywordList();

		BasicDBList list = (BasicDBList) dbObject.get(DBConstants.F_KEYWORD
				+ type);
		if (list == null)
			return null;

		List<String> retList = new LinkedList<String>();

		Iterator<Object> iter = list.iterator();
		while (iter.hasNext()) {
			BasicDBObject obj = (BasicDBObject) iter.next();
			String key = obj.getString(DBConstants.F_KEYWORD_NAME);
			if (key != null) {
				retList.add(key);
			}
		}

		return retList;

		// return getStringList(DBConstants.F_KEYWORD);
	}

	public List<String> getAppKeywordList() {

		BasicDBList list = (BasicDBList) dbObject.get(DBConstants.F_KEYWORD);
		if (list == null)
			return null;

		List<String> retList = new LinkedList<String>();

		Iterator<Object> iter = list.iterator();
		while (iter.hasNext()) {
			BasicDBObject obj = (BasicDBObject) iter.next();
			String key = obj.getString(DBConstants.F_KEYWORD_NAME);
			if (key != null) {
				retList.add(key);
			}
		}

		return retList;

		// return getStringList(DBConstants.F_KEYWORD);
	}

	private String getPushAppKey() {
		return (String) dbObject.get(DBConstants.F_PUSH_APP_KEY);
	}

	private String getPushAppSecret() {
		return (String) dbObject.get(DBConstants.F_PUSH_APP_SECRET);
	}

	private String getPushAppMasterSecret() {
		return (String) dbObject.get(DBConstants.F_PUSH_APP_MASTER_SECRET);
	}

	private String getDevCertificateFileName() {
		return (String) dbObject.get(DBConstants.F_PUSH_APP_DEV_CERTIFICATE);
	}

	private String getDevCertPassword() {
		return (String) dbObject
				.get(DBConstants.F_PUSH_APP_DEV_CERTIFICATE_PASSWORD);
	}

	private String getProductCertificateFileName() {
		return (String) dbObject
				.get(DBConstants.F_PUSH_APP_PRODUCT_CERTIFICATE);
	}

	private String getProductCertPassword() {
		return (String) dbObject
				.get(DBConstants.F_PUSH_APP_PRODUCT_CERTIFICATE_PASSWORD);
	}

	private static boolean isDrawProApp(String appId) {
		if (appId == null)
			return false;

		if (appId.equals(DBConstants.APPID_DRAW_PRO))
			return true;

		return false;
	}

	public static String getGameIdByAppId(String appId) {
		
		AbstractApp app = AppFactory.getInstance().getApp(appId);
		if (app == null)
			return appId;
		
		return app.gameId();
		
//		if (appId == null || appId.isEmpty()) {
//			return DBConstants.GAME_ID_DRAW;
//		}
//		if (appId.equals(DBConstants.APPID_GAME)) {		// for history
//			return DBConstants.GAME_ID_DRAW;
//		}		
//		if (appId.equals(DBConstants.GAME_ID_DICE)
//				|| appId.equals(DBConstants.APPID_DICE)
//				|| appId.equals(DBConstants.APPID_DICE_NEW)) {
//			return DBConstants.GAME_ID_DICE;
//		} 
//		if (appId.equals(DBConstants.GAME_ID_ZHAJINHUA)
//				|| appId.equals(DBConstants.APPID_ZHAJINHUA)) {
//			return DBConstants.GAME_ID_ZHAJINHUA;
//		} 		
//		else if (appId.equals(DBConstants.APPID_DRAW)
//				|| appId.equals(DBConstants.APPID_DRAW_PRO)
//				|| appId.equals(DBConstants.GAME_ID_DRAW)) {
//			return DBConstants.GAME_ID_DRAW;
//		} else {
//			return appId;
//		}
	}
	
	private static boolean isLearnDrawApp(String appId) {
		return (appId != null && appId.equals(DBConstants.APPID_LEARN_DRAW));
	}

	private static boolean isDiceApp(String appId) {
		return (appId != null && (appId.equals(DBConstants.APPID_DICE) || appId.equals(DBConstants.APPID_DICE_NEW)));
	}

	private static boolean isNewDiceApp(String appId) {
		return (appId != null && appId.equals(DBConstants.APPID_DICE_NEW));
	}

	private static boolean isDrawApp(String appId) {
		return (appId != null && (appId.equals(DBConstants.APPID_DRAW) || appId
				.equals(DBConstants.APPID_DRAW_PRO)));
	}
	
	private static boolean isZhajinhuaApp(String appId) {
		return (appId != null && appId.equals(DBConstants.APPID_ZHAJINHUA));
	}

	private static String getDrawGameId() {
		return DBConstants.GAME_ID_DRAW;
	}

	private static String getDiceGameId() {
		return DBConstants.GAME_ID_DICE;
	}

	private static String getCandyGameId() {
		return DBConstants.GAME_ID_CANDY;
	}
	
	private static String getZhajinhuaGameId() {
		return DBConstants.GAME_ID_ZHAJINHUA;
	}

	
	public static boolean isValidGameId(String gameId) {
		
//		for ( AppRegistry app: AppRegistry.values()) {
//			if ( gameId.equals(app.gameId()))
//				return true;
//		}
//		return false;

		return AppFactory.getInstance().isValidGameId(gameId);
	
	}

	public static boolean isValidAppId(String appId) {
		
		return AppFactory.getInstance().isValidAppId(appId);
		
//		for ( AppRegistry app: AppRegistry.values()) {
//			if ( appId.equals(app.appId()))
//				return true;
//		}
//		return false;
	}

	public static int getAwardIngot(String appId) {

		AbstractApp app = AppFactory.getInstance().getApp(appId);
		if (app == null)
			return 0;
		
		return app.registrationRewardIngot();
		
//		if (isDiceApp(appId)){
//			return DBConstants.C_DEFAULT_DICE_AWARD_INGOT;
//		}
//		
//		if (isZhajinhuaApp(appId)){
//			return DBConstants.C_DEFAULT_ZJH_AWARD_INGOT;
//		}
//		
//		return 0;
	}

	public static int getAwardCoin(String appId) {

		AbstractApp app = AppFactory.getInstance().getApp(appId);
		if (app == null)
			return 0;
		
		return app.registrationRewardCoin();
		
//		if (isDiceApp(appId)){
//			return DBConstants.C_DEFAULT_DICE_AWARD_COINS;
//		}
//		
//		if (isZhajinhuaApp(appId)){
//			return DBConstants.C_DEFAULT_ZJH_AWARD_COINS;
//		}
//		
//		if (isDrawApp(appId)){
//			return DBConstants.C_DEFAULT_DRAW_AWARD_COINS;
//		}

		
		
//		return 0;
	}	
	
	



}
