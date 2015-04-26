package com.orange.game.model.dao.app;

import java.util.HashMap;
import org.apache.log4j.Logger;

import com.mongodb.BasicDBObject;
import com.orange.game.constants.DBConstants;

public class AppFactory {

	public static final Logger log = Logger.getLogger(AppFactory.class
			.getName());

	private AbstractApp drawApp = new DrawApp(DBConstants.APPID_DRAW, DBConstants.GAME_ID_DRAW);	
	private AbstractApp oldDiceApp = new DiceApp(DBConstants.APPID_DICE, DBConstants.GAME_ID_DICE);
	private AbstractApp newDiceApp = new DiceApp(DBConstants.APPID_DICE_NEW, DBConstants.GAME_ID_DICE);	
	private AbstractApp zjhApp = new ZjhApp(DBConstants.APPID_ZHAJINHUA, DBConstants.GAME_ID_ZHAJINHUA);	
	private AbstractApp learnDrawApp = new LearnDrawApp(DBConstants.APPID_LEARN_DRAW, DBConstants.GAME_ID_LEARN_DRAW);	
	private AbstractApp pureDrawFreeApp = new PureDrawFreeApp(DBConstants.APPID_PURE_DRAW_FREE, DBConstants.GAME_ID_PURE_DRAW_FREE);
	private AbstractApp pureDrawApp = new PureDrawApp(DBConstants.APPID_PURE_DRAW, DBConstants.GAME_ID_PURE_DRAW);
	private AbstractApp photoDrawFreeApp = new PhotoDrawFreeApp(DBConstants.APPID_PHOTO_DRAW_FREE, DBConstants.GAME_ID_PHOTO_DRAW_FREE);
	private AbstractApp photoDrawApp = new PhotoDrawApp(DBConstants.APPID_PHOTO_DRAW, DBConstants.GAME_ID_PHOTO_DRAW);
	private AbstractApp littlegeeApp = new LittlegeeApp(DBConstants.APPID_LITTLEGEE, DBConstants.GAME_ID_DRAW);
	private AbstractApp calltrackApp = new CallTrackApp(DBConstants.APPID_CALLTRACK, DBConstants.GAME_ID_CALLTRACK);
	private AbstractApp securesmsApp = new SecureSmsApp(DBConstants.APPID_SECURESMS, DBConstants.GAME_ID_SECURESMS);
	
	private AbstractApp dreamAvatarFreeApp = new DreamAvatarFreeApp(DBConstants.APPID_DREAM_AVATAR_FREE, DBConstants.GAME_ID_DREAM_AVATAR_FREE);
	private AbstractApp dreamAvatarApp = new DreamAvatarApp(DBConstants.APPID_DREAM_AVATAR, DBConstants.GAME_ID_DREAM_AVATAR);
	private AbstractApp dreamLockScreenFreeApp = new DreamLockScreenFreeApp(DBConstants.APPID_DREAM_LOCK_SCREEN, DBConstants.GAME_ID_DREAM_LOCK_SCREEN);
	private AbstractApp dreamLockScreenApp = new DreamLockScreenApp(DBConstants.APPID_DREAM_LOCK_SCREEN_FREE, DBConstants.GAME_ID_DREAM_LOCK_SCREEN_FREE);

	private AbstractApp singApp = new SingApp(DBConstants.APPID_SING, DBConstants.GAME_ID_SING);
	private AbstractApp askpsApp = new AskPsApp(DBConstants.APPID_ASK_PS, DBConstants.GAME_ID_ASK_PS);

	private HashMap<String, AbstractApp> appMap = new HashMap<String, AbstractApp>();
	
	private static AppFactory factory = new AppFactory();
	
	private AppFactory(){
		appMap.put(drawApp.appId(), drawApp);
		appMap.put(oldDiceApp.appId(), oldDiceApp);
		appMap.put(newDiceApp.appId(), newDiceApp);
		appMap.put(zjhApp.appId(), zjhApp);
		appMap.put(learnDrawApp.appId(), learnDrawApp);
		appMap.put(pureDrawFreeApp.appId(), pureDrawFreeApp);
		appMap.put(pureDrawApp.appId(), pureDrawApp);
		appMap.put(photoDrawFreeApp.appId(), photoDrawFreeApp);
		appMap.put(photoDrawApp.appId(), photoDrawApp);
		
		appMap.put(dreamAvatarApp.appId(), dreamAvatarApp);
		appMap.put(dreamAvatarFreeApp.appId(), dreamAvatarFreeApp);

		appMap.put(dreamLockScreenApp.appId(), dreamLockScreenApp);
		appMap.put(dreamLockScreenFreeApp.appId(), dreamLockScreenFreeApp);

		appMap.put(littlegeeApp.appId(), littlegeeApp);
		appMap.put(calltrackApp.appId(), calltrackApp);
		appMap.put(securesmsApp.appId(), securesmsApp);
		
		appMap.put(singApp.appId(), singApp);
		appMap.put(askpsApp.appId(), askpsApp);
	}
	
	public static AppFactory getInstance(){
		return factory;
	}
	
	public AbstractApp getApp(String appId){
		if (appId == null){
			log.warn("<getApp> but appId is null");
			return null;			
		}
		else if (appMap.containsKey(appId))
			return appMap.get(appId);
		else {
			log.warn("<getApp> but appId "+appId+" not found!");
			return null;
		}
	}

	public boolean isValidAppId(String appId) {
		if (appId == null){
			return false;
		}
		
		return appMap.containsKey(appId);
	}

	public boolean isValidGameId(String gameId) {
		if (gameId == null)
			return false;
		
		for (AbstractApp app : appMap.values()){
			if (app.gameId.equalsIgnoreCase(gameId)){
				return true;
			}
		}
		return false;
	}

    public void putAllLevelFields(BasicDBObject fields) {
		for (AbstractApp app : appMap.values()){
			fields.put(AbstractApp.userLevelField(app.gameId()), 1);
		}
	}

	// TODO init level info for each game, can be empty and update when needed
	public void initLevelInfo(BasicDBObject user) {
	}

    public boolean isDiceZJHApp(String appId){
        if (appId == null){
            return false;
        }

        if (appId.equalsIgnoreCase(DBConstants.APPID_DICE) ||
                appId.equalsIgnoreCase(DBConstants.APPID_DICE_NEW) ||
                appId.equalsIgnoreCase(DBConstants.APPID_DICE_PRO) ||
                appId.equalsIgnoreCase(DBConstants.APPID_ZHAJINHUA))
            return true;

        return false;
    }
}
