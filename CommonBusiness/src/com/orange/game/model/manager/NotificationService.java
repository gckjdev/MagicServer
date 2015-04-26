package com.orange.game.model.manager;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.notnoop.apns.ApnsService;
import com.orange.common.apnsservice.PushMessageApnsService;
import com.orange.common.apnsservice.BasicService;
import com.orange.common.scheduler.ScheduleService;
import com.orange.common.utils.StringUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.constants.ErrorCode;
import com.orange.game.model.dao.App;
import com.orange.game.model.dao.User;
import com.orange.game.model.dao.app.AbstractApp;
import com.orange.game.model.dao.app.AppFactory;
import com.orange.game.model.dao.app.DrawApp;
import com.relayrides.pushy.apns.*;
import com.relayrides.pushy.apns.util.ApnsPayloadBuilder;
import com.relayrides.pushy.apns.util.SimpleApnsPushNotification;
import com.relayrides.pushy.apns.util.TokenUtil;
import io.netty.util.internal.ConcurrentSet;
import org.apache.log4j.Logger;

import javax.net.ssl.SSLHandshakeException;

public class NotificationService {

    public static String DRAW_DEVELOPMENT_CERTIFICATION_PATH = "./push_development.p12";
	public static String DRAW_PRODUCTION_CERTIFICATION_PATH = "./push_production.p12";

	private static String DRAW_PRO_DEVELOPMENT_CERTIFICATION_PATH = "./draw_pro_push_development.p12";
	private static String DRAW_PRO_PRODUCTION_CERTIFICATION_PATH = "./draw_pro_push_production.p12";

	public static String LITTLEGEE_DEVELOPMENT_CERTIFICATION_PATH = "./littlegee_push_development.p12";
    public static String LITTLEGEE_PRODUCTION_CERTIFICATION_PATH = "./littlegee_push_production.p12";

	private static String CALLTRACK_DEVELOPMENT_CERTIFICATION_PATH = "./calltrack_push_development.p12";
	private static String CALLTRACK_PRODUCTION_CERTIFICATION_PATH = "./calltrack_push_production.p12";

	private static String SECURESMS_DEVELOPMENT_CERTIFICATION_PATH = "./securesms_push_development.p12";
	private static String SECURESMS_PRODUCTION_CERTIFICATION_PATH = "./securesms_push_production.p12";	
	
	public static String SING_DEVELOPMENT_CERTIFICATION_PATH = "./sing_push_development.p12";
	public static String SING_PRODUCTION_CERTIFICATION_PATH = "./sing_push_production.p12";

	private static String ASKPS_DEVELOPMENT_CERTIFICATION_PATH = "./askps_push_development.p12";
	private static String ASKPS_PRODUCTION_CERTIFICATION_PATH = "./askps_push_production.p12";	

	private static String DICE_DEVELOPMENT_CERTIFICATION_PATH = "./dice_push_development.p12";
	private static String DICE_PRODUCTION_CERTIFICATION_PATH = "./dice_push_production.p12";
	private static String NEW_DICE_DEVELOPMENT_CERTIFICATION_PATH = "./new_dice_push_development.p12";
	private static String NEW_DICE_PRODUCTION_CERTIFICATION_PATH = "./new_dice_push_production.p12";
	private static String ZJH_DEVELOPMENT_CERTIFICATION_PATH = "./zhajinhua_push_development.p12";
	private static String ZJH_PRODUCTION_CERTIFICATION_PATH = "./zhajinhua_push_production.p12";
	public static String DRAW_CERTIFICATION_PASSWORD = "123456";
	private static String DICE_CERTIFICATION_PASSWORD = "123456";
	private static String ZJH_CERTIFICATION_PASSWORD = "123456";
	
	private static String DEFAULT_CERTIFICATION_PASSWORD = "123456";
	
	protected static final int MIN_DRAW_DATA_LEN = 1000;
	protected static final int EXECUTOR_POOL_NUM = 3;

	ExecutorService executor = Executors.newFixedThreadPool(EXECUTOR_POOL_NUM);

    public static final Logger log = Logger.getLogger(NotificationService.class.getName());
    public static final ConcurrentSet<PushManager> pushManagerSet = new ConcurrentSet<PushManager>();
    public static final ConcurrentHashMap<PushManager, ConcurrentSet<String>> expiredTokensMap = new ConcurrentHashMap<PushManager, ConcurrentSet<String>>();

	// thread-safe singleton implementation
	private static NotificationService manager = new NotificationService();
	private NotificationService() {

        ScheduleService.getInstance().scheduleEveryday(16, 0, 0, new Runnable(){
            @Override
            public void run() {
                // check push token expired
                try{
                    for (PushManager pushManager : pushManagerSet){
//                        pollExpireTokens(pushManager);
                    }
                }catch (Exception e){
                    log.error("push model clear exception exception="+e.toString(), e);
                }
            }
        });
    }
	public static NotificationService getInstance() {
		return manager;
	}

    public static void clearPushManager(PushManager pushManager){

        log.info("clear push model");
        if (expiredTokensMap.containsKey(pushManager)){
            expiredTokensMap.remove(pushManager);
        }

        pushManagerSet.remove(pushManager);
    }

    public static void pollExpireTokens(PushManager pushManager){
        try {

            // add into pushManagerSet
            pushManagerSet.add(pushManager);
            if (pushManager.isShutDown() || pushManager.isStarted() == false){
                log.info("<pollExpireTokens> but push model is shut down or is NOT started");
                return;
            }

            // create expire set
            ConcurrentSet<String> expireTokens = null;
            if (expiredTokensMap.containsKey(pushManager)){
                expireTokens = expiredTokensMap.get(pushManager);
            }
            else{
                expireTokens = new ConcurrentSet<String>();
                expiredTokensMap.put(pushManager, expireTokens);
            }

            // get expired tokens and clear
            List<ExpiredToken> list = pushManager.getExpiredTokens();
            expireTokens.clear();
            for (ExpiredToken expiredToken : list) {
                byte[] byteToken = expiredToken.getToken();
                if (byteToken != null){
                    String deviceToken = TokenUtil.tokenBytesToString(byteToken);
                    expireTokens.add(deviceToken);
                }
            }
            log.info("push model get expired tokens="+expireTokens.toString());

        } catch (Exception e) {
            log.error("push model get expired token exception="+e.toString(), e);
        }
    }

	// define thread local holder for DRAW PUSH APP
	public final static ThreadLocal<ApnsService> callTrackPushApsnServiceHolder = new ThreadLocal<ApnsService>(){
		public ApnsService initialValue(){
			if (BasicService.IS_TEST){
				return BasicService.createApnsService(CALLTRACK_DEVELOPMENT_CERTIFICATION_PATH, 
						DEFAULT_CERTIFICATION_PASSWORD);			
			}
			else{
				return BasicService.createApnsService(CALLTRACK_PRODUCTION_CERTIFICATION_PATH, 
						DEFAULT_CERTIFICATION_PASSWORD);				
			}
		}
	};
	
	// define thread local holder for DRAW PUSH APP
	public final static ThreadLocal<ApnsService> secureSmsPushApsnServiceHolder = new ThreadLocal<ApnsService>(){
		public ApnsService initialValue(){
			if (BasicService.IS_TEST){
				return BasicService.createApnsService(SECURESMS_DEVELOPMENT_CERTIFICATION_PATH, 
						DEFAULT_CERTIFICATION_PASSWORD);			
			}
			else{
				return BasicService.createApnsService(SECURESMS_PRODUCTION_CERTIFICATION_PATH, 
						DEFAULT_CERTIFICATION_PASSWORD);				
			}
		}
	};
	
	// define thread local holder for DRAW PUSH APP
	public final static ThreadLocal<ApnsService> singPushApsnServiceHolder = new ThreadLocal<ApnsService>(){
		public ApnsService initialValue(){
			if (BasicService.IS_TEST){
				return BasicService.createApnsService(SING_DEVELOPMENT_CERTIFICATION_PATH, 
						DEFAULT_CERTIFICATION_PASSWORD);			
			}
			else{
				return BasicService.createApnsService(SING_PRODUCTION_CERTIFICATION_PATH, 
						DEFAULT_CERTIFICATION_PASSWORD);				
			}
		}
	};
	
	// define thread local holder for DRAW PUSH APP
	public final static ThreadLocal<ApnsService> askpsPushApsnServiceHolder = new ThreadLocal<ApnsService>(){
		public ApnsService initialValue(){
			if (BasicService.IS_TEST){
				return BasicService.createApnsService(ASKPS_DEVELOPMENT_CERTIFICATION_PATH, 
						DEFAULT_CERTIFICATION_PASSWORD);			
			}
			else{
				return BasicService.createApnsService(ASKPS_PRODUCTION_CERTIFICATION_PATH, 
						DEFAULT_CERTIFICATION_PASSWORD);				
			}
		}
	};
	
	// define thread local holder for DRAW PUSH APP
	public final static ThreadLocal<ApnsService> drawPushApsnServiceHolder = new ThreadLocal<ApnsService>(){
		public ApnsService initialValue(){
			if (BasicService.IS_TEST){
				return BasicService.createApnsService(DRAW_DEVELOPMENT_CERTIFICATION_PATH, 
						DRAW_CERTIFICATION_PASSWORD);			
			}
			else{
				return BasicService.createApnsService(DRAW_PRODUCTION_CERTIFICATION_PATH, 
						DRAW_CERTIFICATION_PASSWORD);				
			}
		}
	};
	
	// define thread local holder for DRAW PUSH APP
	public final static ThreadLocal<ApnsService> littlegeePushApsnServiceHolder = new ThreadLocal<ApnsService>(){
		public ApnsService initialValue(){
			if (BasicService.IS_TEST){
				return BasicService.createApnsService(LITTLEGEE_DEVELOPMENT_CERTIFICATION_PATH, 
						DRAW_CERTIFICATION_PASSWORD);			
			}
			else{
				return BasicService.createApnsService(LITTLEGEE_PRODUCTION_CERTIFICATION_PATH, 
						DRAW_CERTIFICATION_PASSWORD);				
			}
		}
	};
	
	// define thread local holder for DRAW PRO PUSH APP
	public final static ThreadLocal<ApnsService> drawProPushApsnServiceHolder = new ThreadLocal<ApnsService>(){
		public ApnsService initialValue(){
			if (BasicService.IS_TEST){
				return BasicService.createApnsService(DRAW_PRO_DEVELOPMENT_CERTIFICATION_PATH, 
						DRAW_CERTIFICATION_PASSWORD);			
			}
			else{
				return BasicService.createApnsService(DRAW_PRO_PRODUCTION_CERTIFICATION_PATH, 
						DRAW_CERTIFICATION_PASSWORD);				
			}
		}
	};	
	
	// define thread local holder for DICE PUSH APP
	public final static ThreadLocal<ApnsService> dicePushApsnServiceHolder = new ThreadLocal<ApnsService>(){
		public ApnsService initialValue(){
			if (BasicService.IS_TEST){
				return BasicService.createApnsService(DICE_DEVELOPMENT_CERTIFICATION_PATH, 
						DICE_CERTIFICATION_PASSWORD);			
			}
			else{
				return BasicService.createApnsService(DICE_PRODUCTION_CERTIFICATION_PATH, 
						DICE_CERTIFICATION_PASSWORD);				
			}
		}
	};	
	
	// define thread local holder for DICE PUSH APP
	public final static ThreadLocal<ApnsService> newDicePushApsnServiceHolder = new ThreadLocal<ApnsService>(){
		public ApnsService initialValue(){
			if (BasicService.IS_TEST){
				return BasicService.createApnsService(NEW_DICE_DEVELOPMENT_CERTIFICATION_PATH, 
						DICE_CERTIFICATION_PASSWORD);			
			}
			else{
				return BasicService.createApnsService(NEW_DICE_PRODUCTION_CERTIFICATION_PATH, 
						DICE_CERTIFICATION_PASSWORD);				
			}
		}
	};		
	
	// define thread local holder for DICE PUSH APP
	public final static ThreadLocal<ApnsService> zjhPushApsnServiceHolder = new ThreadLocal<ApnsService>(){
		public ApnsService initialValue(){
			if (BasicService.IS_TEST){
				return BasicService.createApnsService(ZJH_DEVELOPMENT_CERTIFICATION_PATH, 
						ZJH_CERTIFICATION_PASSWORD);			
			}
			else{
				return BasicService.createApnsService(ZJH_PRODUCTION_CERTIFICATION_PATH, 
						ZJH_CERTIFICATION_PASSWORD);				
			}
		}
	};

    public static ThreadLocal<ApnsService> createApnsServiceHolder(final String devCertPath, final String devCertPassword,
                                                                   final String productionCertPath, final String productionCertPassword){
        return new ThreadLocal<ApnsService>(){
            public ApnsService initialValue(){
                if (BasicService.IS_TEST){
                    return BasicService.createApnsService(devCertPath,
                            devCertPassword);
                }
                else{
                    return BasicService.createApnsService(productionCertPath,
                            productionCertPassword);
                }
            }
        };
    }


    private static class MyRejectedNotificationListener implements RejectedNotificationListener<SimpleApnsPushNotification> {

        public void handleRejectedNotification(
                final PushManager<? extends SimpleApnsPushNotification> pushManager,
                final SimpleApnsPushNotification notification,
                final RejectedNotificationReason reason) {

            log.info(String.format("%s was rejected with rejection reason %s", notification, reason));
        }
    }

    private static class MyFailedConnectionListener implements FailedConnectionListener<SimpleApnsPushNotification> {

        public void handleFailedConnection(
                final PushManager<? extends SimpleApnsPushNotification> pushManager,
                final Throwable cause) {

            log.error("push connection failure, cause="+cause.toString());
            if (cause instanceof SSLHandshakeException) {
                // This is probably a permanent failure, and we should shut down
                // the PushManager.
                log.error("push connection failure, cause i SSLHandshake");
                try {
                    pushManager.unregisterRejectedNotificationListener(rejectedNotificationListener);
                    pushManager.unregisterFailedConnectionListener(failedConnectionListener);
                    pushManager.shutdown();
                    clearPushManager(pushManager);
                } catch (InterruptedException e) {
                    log.error("shutdown push model but catch exception="+e.toString(), e);
                }
            }
        }
    }

    static MyFailedConnectionListener failedConnectionListener = new MyFailedConnectionListener();
    static MyRejectedNotificationListener rejectedNotificationListener = new MyRejectedNotificationListener();
    static Object pushServiceLock = new Object();
    public static PushManager<SimpleApnsPushNotification> createPushService(final String devCertPath, final String devCertPassword,
                                                                   final String productionCertPath, final String productionCertPassword){

        synchronized (pushServiceLock){
            if (BasicService.IS_TEST){
                PushManagerFactory<SimpleApnsPushNotification> pushManagerFactory = null;
                PushManager<SimpleApnsPushNotification> pushManager = null;
                try {
                    log.info("<createPushService> development="+devCertPath+", pass="+devCertPassword);
                    pushManagerFactory = new PushManagerFactory<SimpleApnsPushNotification>(
                            ApnsEnvironment.getSandboxEnvironment(),
                            PushManagerFactory.createDefaultSSLContext(
                                    devCertPath, devCertPassword));

                    pushManager = pushManagerFactory.buildPushManager();
                    pushManager.start();
                    pushManager.registerRejectedNotificationListener(rejectedNotificationListener);
                    pushManager.registerFailedConnectionListener(failedConnectionListener);

//                    pollExpireTokens(pushManager);
                    return pushManager;

                } catch (Exception e) {
                    log.error("catch exception while create push model, e="+e.toString(), e);
                    return null;
                }
            }
            else{
                PushManagerFactory<SimpleApnsPushNotification> pushManagerFactory = null;
                PushManager<SimpleApnsPushNotification> pushManager = null;
                try {
                    log.info("<createPushService> production="+productionCertPath+", pass="+productionCertPassword);
                    pushManagerFactory = new PushManagerFactory<SimpleApnsPushNotification>(
                            ApnsEnvironment.getProductionEnvironment(),
                            PushManagerFactory.createDefaultSSLContext(
                                    productionCertPath, productionCertPassword));

                    pushManager = pushManagerFactory.buildPushManager();
                    pushManager.start();
                    pushManager.registerRejectedNotificationListener(rejectedNotificationListener);
                    pushManager.registerFailedConnectionListener(failedConnectionListener);

//                    pollExpireTokens(pushManager);

                    return pushManager;

                } catch (Exception e) {
                    log.error("catch exception while create push model, e="+e.toString(), e);
                    return null;
                }
            }
        }
    }



    /*
    public int directSendMessage(final String appId, final String deviceToken, final int badge,
			final String alertMessage, final String sound) {

				ApnsService apnsService = null;				
				
				AbstractApp app = AppFactory.getInstance().getApp(appId);
				if (app == null)
					return 0;

				apnsService = app.apnsService();
				if (apnsService == null)
					return 0;

				PushMessageApnsService service = new PushMessageApnsService(apnsService, 
						deviceToken, 
						badge, 
						sound, 
						alertMessage);
				
				return service.handleServiceRequest();

	}
	*/

    public int sendPushMessage(final String appId, final String deviceToken, final int badge,
                                 final String alertMessage, final String sound) {


        AbstractApp app = AppFactory.getInstance().getApp(appId);
        if (app == null){
            return ErrorCode.ERROR_APP_NOT_FOUND;
        }

        PushManager<SimpleApnsPushNotification> pushManager = app.smartPushManager();
        if (pushManager == null){
            return ErrorCode.ERROR_PUSH_MANAGER_NULL;
        }

        if (isExpiredToken(pushManager, deviceToken)){
            return ErrorCode.ERROR_DEVICE_TOKEN_EXPIRED;
        }

        final byte[] token = TokenUtil.tokenStringToByteArray(deviceToken);
        final ApnsPayloadBuilder payloadBuilder = new ApnsPayloadBuilder();

        payloadBuilder.setAlertBody(alertMessage);
        payloadBuilder.setSoundFileName(sound);
        payloadBuilder.setBadgeNumber(0);

        final String payload = payloadBuilder.buildWithDefaultMaximumLength();
        try {
            log.info("<sendPush> token="+deviceToken.toString()+", payload="+payload);
            pushManager.getQueue().put(new SimpleApnsPushNotification(token, payload));
            return 0;
        } catch (Exception e) {
            return ErrorCode.ERROR_PUSH_EXCEPTION;
        }
    }

	
	public void sendMessage(final String appId,
                            final String deviceToken,
                            final int badge,
                            final String localizeKey,
                            final List<String> localizeValues,
			                final String sound,
                            final HashMap<String, Object> userInfo,
                            final int type,
                            final User user,
                            final boolean autoBadge) {

		executor.execute(new Runnable() {

			@Override
			public void run() {

                String appIdForPush = appId;
                if (user != null && appId != null){
                    if (user != null){
                        List<String> appList = user.getAppIdList(); // user to be push message
                        if (appList != null && appList.size() > 0){
                            if (!appList.contains(appId)){
                                if (appList.contains(DBConstants.APPID_DRAW)){
                                    appIdForPush = DBConstants.APPID_DRAW;
                                }
                                else if (appList.contains(DBConstants.APPID_LITTLEGEE)){
                                    appIdForPush = DBConstants.APPID_LITTLEGEE;
                                }
                                else if (appList.contains(DBConstants.APPID_SING)){
                                    appIdForPush = DBConstants.APPID_SING;
                                }
                                else{
                                    appIdForPush = appList.get(0);
                                }
                                log.info("<sendPushMessage> set appId for push to "+appIdForPush);
                            }
                            else{
                                // use the same appId for push
                                log.info("<sendPushMessage> both user are using the same appId="+appIdForPush);
                            }
                        }
                    }
                }

                AbstractApp app = AppFactory.getInstance().getApp(appIdForPush);
                if (app == null){
                    app = AppFactory.getInstance().getApp(DBConstants.APPID_DRAW);
                }

                PushManager<SimpleApnsPushNotification> pushManager = app.smartPushManager();
                if (pushManager == null){
                    return;
                }

                if (isExpiredToken(pushManager, deviceToken)){
                    return;
                }

                final byte[] token = TokenUtil.tokenStringToByteArray(deviceToken);
                final ApnsPayloadBuilder payloadBuilder = new ApnsPayloadBuilder();

                if (userInfo != null){
                    Set<String> keys = userInfo.keySet();
                    for (String key : keys){
                        payloadBuilder.addCustomProperty(key, userInfo.get(key));
                    }
                }

                String[] alertArguments = null;
                if (localizeValues != null){
                    alertArguments = new String[localizeValues.size()];
                }
                if (localizeKey != null){
                    payloadBuilder.setLocalizedAlertMessage(localizeKey, localizeValues.toArray(alertArguments));
                }

                payloadBuilder.setBadgeNumber(badge);
                payloadBuilder.setSoundFileName(sound);

                final String payload = payloadBuilder.buildWithDefaultMaximumLength();
                try {
                    log.info("<sendPush> token="+deviceToken.toString()+", payload="+payload);
                    pushManager.getQueue().put(new SimpleApnsPushNotification(token, payload));
                    return;
                } catch (Exception e) {
                    return;
                }

                /*
				ApnsService apnsService = null;			
				AbstractApp app = AppFactory.getInstance().getApp(appIdForPush);
				if (app == null){
					apnsService = drawPushApsnServiceHolder.get();
				}
				else{
					apnsService = app.apnsService();
				}

				PushMessageApnsService service = new PushMessageApnsService(apnsService, 
						deviceToken, 
						badge, 
						sound, 
						userInfo, 
						localizeKey,
						localizeValues);
				
				service.handleServiceRequest();
				*/

			}

		});
	}

    public boolean isExpiredToken(PushManager<SimpleApnsPushNotification> pushManager, String deviceToken) {

        return false;

        /*
        if (pushManager == null || StringUtil.isEmpty(deviceToken)){
            return true;
        }

        if (!expiredTokensMap.containsKey(pushManager)){
            log.warn("<isExpiredToken> but push model map not in expire token map!!!");
            return false;
        }

        ConcurrentSet<String> set = expiredTokensMap.get(pushManager);
        boolean result = set.contains(deviceToken);
        log.info("<isExpiredToken> "+result);
        return result;
        */
    }

}
