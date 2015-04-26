package com.orange.game.model.manager;

import com.mongodb.*;
import com.orange.common.log.ServerLog;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.common.utils.DateUtil;
import com.orange.common.utils.ListUtil;
import com.orange.common.utils.StringUtil;
import com.orange.game.api.service.ElasticsearchService;
import com.orange.game.constants.DBConstants;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.App;
import com.orange.game.model.dao.Board;
import com.orange.game.model.dao.Item;
import com.orange.game.model.dao.User;
import com.orange.game.model.dao.app.AbstractApp;
import com.orange.game.model.dao.app.AppFactory;
import com.orange.game.model.dao.group.Group;
import com.orange.game.model.dao.photo.UserPhoto;
import com.orange.game.model.manager.bbs.BBSPrivilegeManager;
import com.orange.game.model.manager.bbs.BoardManager;
import com.orange.game.model.manager.group.GroupManager;
import com.orange.game.model.manager.photo.UserPhotoManager;
import com.orange.game.model.manager.user.LevelUtils;
import com.orange.game.model.service.DBService;
import com.orange.game.model.service.VipService;
import com.orange.game.model.service.xiaojinumber.XiaojiNumberService;
import com.orange.game.model.xiaoji.AbstractXiaoji;
import com.orange.game.model.xiaoji.XiaojiFactory;
import com.orange.network.game.protocol.model.GameBasicProtos;
import com.orange.network.game.protocol.model.GameBasicProtos.PBGameCurrency;
import com.orange.network.game.protocol.model.GameBasicProtos.PBGameUser;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;

import java.util.*;

public class UserManager extends CommonManager {

    public static final Logger log = Logger.getLogger(UserManager.class
            .getName());
    public static int BALANCE_TYPE_COINS = PBGameCurrency.Coin_VALUE;
    public static int BALANCE_TYPE_INGOT = PBGameCurrency.Ingot_VALUE;
    public static String EXAMPLE_FAKE_TRANSACTION_RECEIPT1 = "eyJzaWduaW5nLXN0YXR1cyI9IjAiOyJwdXJjaGFzZS1pbmZvIj0iZXlKaWRuSnpJajBpTVM0d0lqc2ljSFZ5WTJoaGMyVXRaR0YwWlNJOUlqSXdNVE10TURFdE1qTWdNak02TWpVNk1EZ2dSWFJqTDBkTlZDSTdJblZ1YVhGMVpTMXBaR1Z1ZEdsbWFXVnlJajBpWldJMk5HSmpaVEV4WTJGaU5qTmtObU14WVdVMVlqZzRabVF4WmpRMFpUSmtZVFV6T1RBNVlpSTdJbTl5YVdkcGJtRnNMWFJ5WVc1ellXTjBhVzl1TFdsa0lqMGlORGMwTXpRNE5qWTNOekUyTkRRMk9EWXhNQ0k3SW5GMVlXNTBhWFI1SWowaU1TSTdJbkIxY21Ob1lYTmxMV1JoZEdVdGJYTWlQU0k0T1RJek1UWXdNelFpT3lKdmNtbG5hVzVoYkMxd2RYSmphR0Z6WlMxa1lYUmxJajBpTWpBeE15MHdNUzB5TXlBeU16b3lOVG93T0NCRmRHTXZSMDFVSWpzaWNISnZaSFZqZEMxcFpDSTlJbU52YlM1dmNtRnVaMlV1ZW1wb0xtTnZhVzV6TWpBd01EQWlPeUpwZEdWdExXbGtJajBpTkRjeU1EYzVOVGsxTlRjeE9UUXhNamc1TXlJN0luUnlZVzV6WVdOMGFXOXVMV2xrSWowaU5EYzBNelE0TmpZM056RTJORFEyT0RZeE1DSTdJbUpwWkNJOUltTnZiUzV2Y21GdVoyVXVlbWhoYW1sdWFIVmhJanQ5IjsicGlkIj0iMjM4ODAiOyJzaWduYXR1cmUiPSJNVE0xT0RrMU5EY3dPQzQzTkRRM01qRT0iO30=";
    public static String EXAMPLE_FAKE_TRANSACTION_RECEIPT2 = "eyJzaWduaW5nLXN0YXR1cyI9IjAiOyJwdXJjaGFzZS1pbmZvIj0iZXlKaWRuSnpJajBpTVM0d0lqc2ljSFZ5WTJoaGMyVXRaR0YwWlNJOUlqSXdNVE10TURFdE1qUWdNakU2TlRBNk16QWdSWFJqTDBkTlZDSTdJblZ1YVhGMVpTMXBaR1Z1ZEdsbWFXVnlJajBpTTJSaU16RTVZVGN6WldZNVpUYzNNRGhrWm1JNE5qUXpaRE5pWkdVek1UZG1ZVGswTkdFNE1TSTdJbTl5YVdkcGJtRnNMWFJ5WVc1ellXTjBhVzl1TFdsa0lqMGlORGMwTXpRNE56QXhOVGN6TnpNMU1EY3pOeUk3SW5GMVlXNTBhWFI1SWowaU1TSTdJbkIxY21Ob1lYTmxMV1JoZEdVdGJYTWlQU0l4TmpJM09ERTNOemNpT3lKdmNtbG5hVzVoYkMxd2RYSmphR0Z6WlMxa1lYUmxJajBpTWpBeE15MHdNUzB5TkNBeU1UbzFNRG96TUNCRmRHTXZSMDFVSWpzaWNISnZaSFZqZEMxcFpDSTlJbU52YlM1dmNtRnVaMlV1ZW1wb0xtTnZhVzV6TWpBd01EQWlPeUpwZEdWdExXbGtJajBpTkRjeU1EYzVOakk0TkRBek1qVXhNRGN4TXlJN0luUnlZVzV6WVdOMGFXOXVMV2xrSWowaU5EYzBNelE0TnpBeE5UY3pOek0xTURjek55STdJbUpwWkNJOUltTnZiUzV2Y21GdVoyVXVlbWhoYW1sdWFIVmhJanQ5IjsicGlkIj0iMjkzNSI7InNpZ25hdHVyZSI9Ik1UTTFPVEF6TlRRek1DNDRNVEF5TURBPSI7fQ==";
    public static String EXAMPLE_FAKE_TRANSACTION_RECEIPT3 = "eyJzaWduaW5nLXN0YXR1cyI9IjAiOyJwdXJjaGFzZS1pbmZvIj0iZXlKaWRuSnpJajBpTVM0d0lqc2ljSFZ5WTJoaGMyVXRaR0YwWlNJOUlqSXdNVE10TURFdE1qVWdNVE02TURrNk16SWdSWFJqTDBkTlZDSTdJblZ1YVhGMVpTMXBaR1Z1ZEdsbWFXVnlJajBpWldJMk5HSmpaVEV4WTJGaU5qTmtObU14WVdVMVlqZzRabVF4WmpRMFpUSmtZVFV6T1RBNVlpSTdJbTl5YVdkcGJtRnNMWFJ5WVc1ellXTjBhVzl1TFdsa0lqMGlORGMwTXpRNE56STBOekF4TmprMk16TTRNaUk3SW5GMVlXNTBhWFI1SWowaU1TSTdJbkIxY21Ob1lYTmxMV1JoZEdVdGJYTWlQU0l0TkRnMU9ETTVOVFl5SWpzaWIzSnBaMmx1WVd3dGNIVnlZMmhoYzJVdFpHRjBaU0k5SWpJd01UTXRNREV0TWpVZ01UTTZNRGs2TXpJZ1JYUmpMMGROVkNJN0luQnliMlIxWTNRdGFXUWlQU0pqYjIwdWIzSmhibWRsTG5wcWFDNWpiMmx1Y3pJd01EQXdJanNpYVhSbGJTMXBaQ0k5SWpRM01qQTNPVFkxTURnek1ETTJOVEF5TkRnaU95SjBjbUZ1YzJGamRHbHZiaTFwWkNJOUlqUTNORE0wT0RjeU5EY3dNVFk1TmpNek9ESWlPeUppYVdRaVBTSmpiMjB1YjNKaGJtZGxMbnBvWVdwcGJtaDFZU0k3ZlE9PSI7InBpZCI9IjM0NjM3Ijsic2lnbmF0dXJlIj0iTVRNMU9UQTVNRFUzTWk0eE5qWTRNakk9Ijt9";
    public static String EXAMPLE_FAKE_TRANSACTION_RECEIPT4 = "eyJzaWduaW5nLXN0YXR1cyI9IjAiOyJwdXJjaGFzZS1pbmZvIj0iZXlKaWRuSnpJajBpTVM0d0lqc2ljSFZ5WTJoaGMyVXRaR0YwWlNJOUlqSXdNVE10TURFdE1qVWdNVE02TURrNk1qVWdSWFJqTDBkTlZDSTdJblZ1YVhGMVpTMXBaR1Z1ZEdsbWFXVnlJajBpWldJMk5HSmpaVEV4WTJGaU5qTmtObU14WVdVMVlqZzRabVF4WmpRMFpUSmtZVFV6T1RBNVlpSTdJbTl5YVdkcGJtRnNMWFJ5WVc1ellXTjBhVzl1TFdsa0lqMGlORGMwTXpRNE56STBOams1TURBeU16STRNQ0k3SW5GMVlXNTBhWFI1SWowaU1TSTdJbkIxY21Ob1lYTmxMV1JoZEdVdGJYTWlQU0l0TlRFeU56YzVOalkwSWpzaWIzSnBaMmx1WVd3dGNIVnlZMmhoYzJVdFpHRjBaU0k5SWpJd01UTXRNREV0TWpVZ01UTTZNRGs2TWpVZ1JYUmpMMGROVkNJN0luQnliMlIxWTNRdGFXUWlQU0pqYjIwdWIzSmhibWRsTG5wcWFDNWpiMmx1Y3pJd01EQXdJanNpYVhSbGJTMXBaQ0k5SWpRM01qQTNPVFkxTURneU56YzFNalkxTVRJaU95SjBjbUZ1YzJGamRHbHZiaTFwWkNJOUlqUTNORE0wT0RjeU5EWTVPVEF3TWpNeU9EQWlPeUppYVdRaVBTSmpiMjB1YjNKaGJtZGxMbnBvWVdwcGJtaDFZU0k3ZlE9PSI7InBpZCI9IjM0NjM3Ijsic2lnbmF0dXJlIj0iTVRNMU9UQTVNRFUyTlM0M05ETTRNREU9Ijt9";
    public static int DEFAULT_FAKE_TRANSACTION_RECEIPT_LEN = EXAMPLE_FAKE_TRANSACTION_RECEIPT3.length();
    private static int FAKE_TRANSACTION_RECEIPT_LEN = getFakeTransactionReceiptLen();

    public static User findUserByDeviceId(MongoDBClient mongoClient,
                                          String deviceId) {
        if (mongoClient == null || deviceId == null || deviceId.length() <= 0)
            return null;

        if (!User.isValidDevice(deviceId)) {
            return null;
        }

        BasicDBList inValueList = new BasicDBList();
        inValueList.add(deviceId);
        BasicDBObject inValue = new BasicDBObject("$in", inValueList);

        BasicDBObject query = new BasicDBObject();
        query.put(DBConstants.F_DEVICEID_LIST, inValue);

        DBObject obj = mongoClient.findOne(DBConstants.T_USER, query);
        if (obj == null)
            return null;
        else
            return fillUserWithGroup(mongoClient, new User(obj));
    }

    public static User findUserByXiaojiNumber(String number) {

        if (number == null || number.length() <= 0)
            return null;

        BasicDBObject query = new BasicDBObject();
        query.put(DBConstants.F_XIAOJI_NUMBER, number);

        DBObject obj = DBService.getInstance().getMongoDBClient().findOne(DBConstants.T_USER, query);
        if (obj == null)
            return null;
        else
            return fillUserWithGroup(DBService.getInstance().getMongoDBClient(), new User(obj));

    }


    private static User fillUserWithGroup(MongoDBClient mongoDBClient, User user) {
        ObjectId gId = user.getFirstGroupId();
        log.info("fillUserWithGroup, user = " + user.getUserId());
        if (gId != null) {
            Group group = GroupManager.getSimpleGroup(mongoDBClient, gId.toString());
            user.setGroup(group);
            log.info("fillUserWithGroup, group = " + group.getGroupId());
        }
        return user;
    }

    public static User findUserByEmail(MongoDBClient mongoClient, String email) {
        if (mongoClient == null || email == null || email.length() <= 0)
            return null;

        DBObject obj = mongoClient.findOne(DBConstants.T_USER,
                DBConstants.F_EMAIL, email);
        if (obj == null) {
            return null;
        } else {
            return fillUserWithGroup(mongoClient, new User(obj));
        }
    }

    public static DBObject findUserByVerifyCode(MongoDBClient mongoClient,
                                                String vcd) {
        if (mongoClient == null || vcd == null || vcd.length() <= 0)
            return null;

        return mongoClient.findOne(DBConstants.T_USER,
                DBConstants.F_VERIFYCODE, vcd);

    }

    public static void updateStatusByVerifyCode(MongoDBClient mongoClient,
                                                String sta, String vcd) {
        if (mongoClient == null || sta == null || sta.length() <= 0
                || vcd == null || vcd.length() <= 0)
            return;

        Map<String, Object> equalCondition = new HashMap<String, Object>();
        Map<String, Object> updateMap = new HashMap<String, Object>();
        equalCondition.put(DBConstants.F_VERIFYCODE, vcd);
        updateMap.put(DBConstants.F_STATUS, sta);

        // TODO performance not good , need set fields
        mongoClient.findAndModifySet(DBConstants.T_USER, equalCondition,
                updateMap);
    }

    @Deprecated
    private static void updateEmail(MongoDBClient mongoClient, String email,
                                    String new_email) {
        if (mongoClient == null || email == null || email.length() <= 0
                || new_email == null || new_email.length() <= 0)
            return;

        mongoClient.findAndModify(DBConstants.T_USER, DBConstants.F_EMAIL,
                email, new_email);
    }

    @Deprecated
    private static void updatePassword(MongoDBClient mongoClient, String mail,
                                       String new_pwd) {
        if (mongoClient == null || mail == null || mail.length() <= 0
                || new_pwd == null || new_pwd.length() <= 0)
            return;

        Map<String, Object> equalCondition = new HashMap<String, Object>();
        Map<String, Object> updateMap = new HashMap<String, Object>();
        equalCondition.put(DBConstants.F_EMAIL, mail);
        updateMap.put(DBConstants.F_PASSWORD, new_pwd);

        mongoClient.findAndModifySet(DBConstants.T_USER, equalCondition,
                updateMap);

    }

    public static User createUserWithXiaoji(final MongoDBClient mongoClient,
                                        String appId,
                                        String deviceModel,
                                        String deviceId,
                                        String deviceOS,
                                        String deviceToken,
                                        String language,
                                        String countryCode,
                                        String nickName,
                                        String version,
                                        int poolType) {

        BasicDBObject user = new BasicDBObject();

        user.put(DBConstants.F_APPID, appId);
        user.put(DBConstants.F_NICKNAME, nickName);                // no name for default registration
        user.put(DBConstants.F_DEVICEMODEL, deviceModel);
        user.put(DBConstants.F_DEVICEID, deviceId);
        user.put(DBConstants.F_DEVICEOS, deviceOS);
        user.put(DBConstants.F_DEVICETOKEN, deviceToken);
        user.put(DBConstants.F_LANGUAGE, language);
        user.put(DBConstants.F_COUNTRYCODE, countryCode);
        user.put(DBConstants.F_CREATE_DATE, new Date());
        user.put(DBConstants.F_CREATE_SOURCE_ID, appId);
        user.put(DBConstants.F_STATUS, DBConstants.STATUS_NORMAL);
        user.put(DBConstants.F_SHAKE_XIAOJI, false);
        user.put(DBConstants.F_VERSION, version);
        user.put(DBConstants.F_CALCULATE_TAKE_COINS, true);

        initLevelInfo(user);
        initItems(user, appId);
        initUserAppIds(user, appId);
        setDefaultBalance(user, appId);

        log.info("<createUserWithXiaoji> user=" + user.toString());
        boolean result = mongoClient.insert(DBConstants.T_USER, user);
        if (result) {


            final User retUser = new User(user);

//            DBService.getInstance().executeDBRequest(0, new Runnable() {
//                @Override
//                public void run() {
//                    ElasticsearchService.addOrUpdateIndex(retUser, mongoClient);
//                }
//            });

            String userId = retUser.getUserId();
            String number = XiaojiNumberService.getInstance().getOrCreateNewNumberForUser(userId,
                    XiaojiFactory.getInstance().getDraw(), true, true, poolType);

            retUser.setXiaoji(number);
            return retUser;
        } else
            return null;

    }

    public static User createDeviceUser(MongoDBClient mongoClient,
                                        String appId, String deviceModel, String deviceId, String deviceOS,
                                        String deviceToken, String language, String countryCode, String nickName, String version) {

        BasicDBObject user = new BasicDBObject();

        user.put(DBConstants.F_APPID, appId);
        user.put(DBConstants.F_NICKNAME, nickName);                // no name for default registration
        user.put(DBConstants.F_DEVICEMODEL, deviceModel);
        user.put(DBConstants.F_DEVICEID, deviceId);
        user.put(DBConstants.F_DEVICEOS, deviceOS);
        user.put(DBConstants.F_DEVICETOKEN, deviceToken);
        user.put(DBConstants.F_LANGUAGE, language);
        user.put(DBConstants.F_COUNTRYCODE, countryCode);
        user.put(DBConstants.F_CREATE_DATE, new Date());
        user.put(DBConstants.F_CREATE_SOURCE_ID, appId);
        user.put(DBConstants.F_STATUS, DBConstants.STATUS_NORMAL);
        user.put(DBConstants.F_SHAKE_XIAOJI, false);
        user.put(DBConstants.F_VERSION, version);
        user.put(DBConstants.F_CALCULATE_TAKE_COINS, true);

        initLevelInfo(user);
        initItems(user, appId);
        initUserAppIds(user, appId);
        setDefaultBalance(user, appId);

        log.info("<createDeviceUser> user=" + user.toString());
        boolean result = mongoClient.insert(DBConstants.T_USER, user);
        if (result) {
            User retUser = new User(user);
            ElasticsearchService.addOrUpdateIndex(retUser, mongoClient);
            return retUser;
        } else
            return null;

    }

    public static User createUserByEmail(MongoDBClient mongoClient,
                                         String appId, String email, String password, String deviceToken,
                                         boolean isVerification) {

        BasicDBObject user = new BasicDBObject();

        // set default balance
        setDefaultBalance(user, appId);

        initItems(user, appId);
        initUserAppIds(user, appId);

        user.put(DBConstants.F_APPID, appId);
        user.put(DBConstants.F_CREATE_SOURCE_ID, appId);
        user.put(DBConstants.F_EMAIL, email);
        user.put(DBConstants.F_PASSWORD, password);
        user.put(DBConstants.F_VERIFYCODE, StringUtil.randomUUID());
        user.put(DBConstants.F_CREATE_DATE, new Date()); // DateUtil.currentDate());
        if (isVerification)
            user.put(DBConstants.F_STATUS, DBConstants.STATUS_TO_VERIFY);
        else
            user.put(DBConstants.F_STATUS, DBConstants.STATUS_NORMAL);
        user.put(DBConstants.F_DEVICETOKEN, deviceToken);

        boolean result = mongoClient.insert(DBConstants.T_USER, user);

        if (result)
            return fillUserWithGroup(mongoClient, new User(user));
        else
            return null;
    }

    private static void setDefaultBalance(BasicDBObject user, String appId) {

        AbstractApp app = AppFactory.getInstance().getApp(appId);

        // set default balance
        int balance = app.registrationInitCoin();
        user.put(DBConstants.F_ACCOUNT_BALANCE, balance);

        // set default ingot balance
        int ingotBalance = app.registrationInitIngot();
        user.put(DBConstants.F_ACCOUNT_INGOT_BALANCE, ingotBalance);

//		// set default balance
//		int balance = DBConstants.C_DEFAULT_BALANCE;
//		if (App.isDrawApp(appId) == false) {
//			balance = DBConstants.C_DEFAULT_DICE_BALANCE;
//		}
//		user.put(DBConstants.F_ACCOUNT_BALANCE, balance);
//
//		// set default ingot balance
//		int ingotBalance = DBConstants.C_DEFAULT_INGOT_BALANCE;
//		if (App.isDrawApp(appId)) {
//			ingotBalance = DBConstants.C_DEFAULT_INGOT_BALANCE;
//		}
//		else if (App.isDiceApp(appId)){
//			ingotBalance = DBConstants.C_DEFAULT_DICE_AWARD_INGOT;
//		}
//		else if (App.isZhajinhuaApp(appId)){
//			ingotBalance = DBConstants.C_DEFAULT_ZJH_AWARD_INGOT;
//		}
//		else if (App.isLearnDrawApp(appId)){
//			ingotBalance = DBConstants.C_DEFAULT_LEARN_DRAW_INGOT;
//		}
//
//		user.put(DBConstants.F_ACCOUNT_INGOT_BALANCE, ingotBalance);

    }

    private static void initUserAppIds(BasicDBObject user, String appId) {
        BasicDBList list = new BasicDBList();
        list.add(appId);

        user.put(DBConstants.F_APPID_LIST, list);
    }

    public static User createUserByEmail(MongoDBClient mongoClient,
                                         String appId, String email, String password, String deviceToken,
                                         String nation, String language, String deviceModel,
                                         String deviceOs, int deviceType, boolean isVerification, String nickName) {
        BasicDBObject user = new BasicDBObject();

        // set default balance
        setDefaultBalance(user, appId);

        initItems(user, appId);
        initUserAppIds(user, appId);

        if (nickName == null) {
            int index = email.indexOf("@");
            if (index >= 0) {
                nickName = email.substring(0, index);
            }
        }

        user.put(DBConstants.F_APPID, appId);
        user.put(DBConstants.F_CREATE_SOURCE_ID, appId);
        user.put(DBConstants.F_EMAIL, email);
        user.put(DBConstants.F_PASSWORD, password);
        user.put(DBConstants.F_VERIFYCODE, StringUtil.randomUUID());
        user.put(DBConstants.F_CREATE_DATE, new Date()); // DateUtil.currentDate());
        user.put(DBConstants.F_NICKNAME, nickName);

        user.put(DBConstants.F_COUNTRYCODE, nation);
        user.put(DBConstants.F_LANGUAGE, language);
        user.put(DBConstants.F_DEVICEMODEL, deviceModel);
        user.put(DBConstants.F_DEVICEOS, deviceOs);
        user.put(DBConstants.F_DEVICE_TYPE, deviceType);

        user.put(DBConstants.F_SHAKE_XIAOJI, true);
        user.put(DBConstants.F_SHAKE_NUMBER_TIMES, 10);

        initLevelInfo(user);

        if (isVerification)
            user.put(DBConstants.F_STATUS, DBConstants.STATUS_TO_VERIFY);
        else
            user.put(DBConstants.F_STATUS, DBConstants.STATUS_NORMAL);
        user.put(DBConstants.F_DEVICETOKEN, deviceToken);

        boolean result = mongoClient.insert(DBConstants.T_USER, user);
        if (result) {
            User retUser = new User(user);
            ElasticsearchService.addOrUpdateIndex(retUser, mongoClient);
            return retUser;
        } else
            return null;
    }

    private static BasicDBObject createItem(int type, int amount) {

        BasicDBObject tipsItem = new BasicDBObject();
        tipsItem.put(DBConstants.F_ITEM_TYPE, type);
        tipsItem.put(DBConstants.F_ITEM_AMOUNT, amount);
        return tipsItem;
    }

    private static void initItems(BasicDBObject user, String appId) {

//		// create default tips item
//		BasicDBObject tipsItem = createItem(DBConstants.C_ITEM_TYPE_TIPS,
//				DBConstants.C_DEFAULT_TIPS_AMOUNT);
//
//		// create default dice item
//		BasicDBObject diceRerollItem = createItem(
//				DBConstants.C_ITEM_TYPE_DICE_REROLL,
//				DBConstants.C_DEFAULT_DICE_REROLL_AMOUNT);
//		BasicDBObject diceDoubleCoinsItem = createItem(
//				DBConstants.C_ITEM_TYPE_DICE_DOUBLE_COINS,
//				DBConstants.C_DEFAULT_DICE_DOUBLE_COINS_AMOUNT);
//
//		// add push to item array
//		BasicDBList items = new BasicDBList();
//		items.add(tipsItem);
//		items.add(diceRerollItem);
//		items.add(diceDoubleCoinsItem);
//
//		if (App.isDrawProApp(appId)) {
//			BasicDBObject penItem1 = createItem(
//					DBConstants.C_ITEM_TYPE_PEN_WATER, 1);
//			BasicDBObject penItem2 = createItem(
//					DBConstants.C_ITEM_TYPE_PEN_PEN, 1);
//			BasicDBObject penItem3 = createItem(
//					DBConstants.C_ITEM_TYPE_PEN_ICE, 1);
//			BasicDBObject penItem4 = createItem(
//					DBConstants.C_ITEM_TYPE_PEN_QUILL, 1);
//
//			items.add(penItem1);
//			items.add(penItem2);
//			items.add(penItem3);
//			items.add(penItem4);
//		}
//		else if (App.isLearnDrawApp(appId)){
//			BasicDBObject playItem = createItem(DBConstants.C_ITEM_TYPE_PAINT_PLAYER, 1);
//			items.add(playItem);
//		}

        BasicDBList items = new BasicDBList();
        AbstractApp app = AppFactory.getInstance().getApp(appId);
        if (app == null)
            return;

        items.addAll(app.defaultItems());

        user.put(DBConstants.F_ITEMS, items);
        user.put(DBConstants.F_CALCULATE_TAKE_COINS, true);
    }

    public static void addSearchKeyword(MongoDBClient mongoClient,
                                        String deviceId, String keywords) {
        addSearchKeyword(mongoClient, deviceId, keywords, false, 0.0f, 0.0f);
    }

    public static void addSearchKeyword(MongoDBClient mongoClient,
                                        String deviceId, String keywords, double longitude, double latitude) {
        addSearchKeyword(mongoClient, deviceId, keywords, true, longitude,
                latitude);
    }

    public static void addSearchKeyword(MongoDBClient mongoClient,
                                        String deviceId, String keyword, boolean hasLocation,
                                        double longitude, double latitude) {

        if (deviceId == null || keyword == null || keyword.isEmpty())
            return;

        // user found, add keywords into user history
        BasicDBObject value = new BasicDBObject();
        value.put(DBConstants.F_SEARCH_HISTORY, keyword);

        DBObject addToSet = new BasicDBObject();
        addToSet.put("$addToSet", value);

        DBObject query = new BasicDBObject();
        query.put(DBConstants.F_DEVICEID, deviceId);

        log.info("<addSearchKeyword> query=" + query.toString() + ",value="
                + addToSet);
        mongoClient.updateAll(DBConstants.T_USER, query, addToSet);

    }

    public static void addSearchKeywordDetailRecord(MongoDBClient mongoClient,
                                                    String deviceId, String[] keywords, boolean hasLocation,
                                                    double longitude, double latitude) {

        if (deviceId == null || keywords == null || keywords.length == 0)
            return;

        // user found, add keywords into user history
        BasicDBObject searchRecord = new BasicDBObject();

        // set date
        searchRecord.put(DBConstants.F_DATE, new Date());

        // set keywords
        BasicDBList keywordList = new BasicDBList();
        Collections.addAll(keywordList, keywords);

//        for (int i = 0; i < keywords.length; i++) {
//            keywordList.add(keywords[i]);
//        }
        searchRecord.put(DBConstants.F_KEYWORD, keywordList);

        // if (hasLocation) {
        // Gps gps = new Gps(latitude, longitude);
        // searchRecord.put(DBConstants.F_GPS, gps.toDoubleList());
        // }

        DBObject value = new BasicDBObject();
        value.put(DBConstants.F_SEARCH_HISTORY, searchRecord);

        DBObject pushValue = new BasicDBObject();
        pushValue.put("$push", value);

        DBObject query = new BasicDBObject();
        query.put(DBConstants.F_DEVICEID, deviceId);

        System.out.println("<addSearchKeyword> query=" + query.toString()
                + ",value=" + pushValue);
        mongoClient.updateAll(DBConstants.T_USER, query, pushValue);
    }

    public static User findUserByUserId(MongoDBClient mongoClient, String userId) {
        if (mongoClient == null || userId == null || userId.length() <= 0)
            return null;

        DBObject obj = mongoClient.findOne(DBConstants.T_USER, DBConstants.F_USERID, new ObjectId(userId));
        if (obj == null) {
            return null;
        }
        return fillUserWithGroup(mongoClient, new User(obj));
    }

    public static User findUserByUserId(MongoDBClient mongoClient, String userId, BasicDBObject fields) {
        if (mongoClient == null || userId == null || userId.length() <= 0)
            return null;

        BasicDBObject query = new BasicDBObject("_id", new ObjectId(userId));

        DBObject obj = mongoClient.findOne(DBConstants.T_USER, query, fields);
        if (obj == null) {
            return null;
        }
        return fillUserWithGroup(mongoClient, new User(obj));
    }

    // just uid,nick,avatar,deviceToken,email
    public static User findSimpleUserInfoByUserId(MongoDBClient mongoClient,
                                                  String userId) {
        if (mongoClient == null || userId == null || userId.length() <= 0)
            return null;

        DBObject query = new BasicDBObject();
        query.put(DBConstants.F_USERID, new ObjectId(userId));
        DBObject fields = getUserPublicReturnFields();

//				new BasicDBObject();
//		fields.put(DBConstants.F_NICKNAME, 1);
//		fields.put(DBConstants.F_AVATAR, 1);
//		fields.put(DBConstants.F_DEVICETOKEN, 1);
//		fields.put(DBConstants.F_DEVICEID, 1);
//		fields.put(DBConstants.F_EMAIL, 1);
//		fields.put(DBConstants.F_APPID, 1);
//		fields.put(DBConstants.F_LEVEL_INFO, 1);

        DBObject obj = mongoClient.findOne(DBConstants.T_USER, query, fields);
        if (obj == null)
            return null;
        return fillUserWithGroup(mongoClient, new User(obj));
    }

    public static User findUserAccountInfoByUserId(MongoDBClient mongoClient,
                                                   String userId) {
        if (mongoClient == null || userId == null || userId.length() <= 0)
            return null;

        DBObject query = new BasicDBObject();
        query.put(DBConstants.F_USERID, new ObjectId(userId));
        DBObject fields = getUserPublicReturnFields();
        DBObject obj = mongoClient.findOne(DBConstants.T_USER, query, fields);
        if (obj == null)
            return null;
        return fillUserWithGroup(mongoClient, new User(obj));
    }

    public static List<User> findUserAccountInfoByUserIdList(
            MongoDBClient mongoClient, List<String> userIdList) {

        if (mongoClient == null || userIdList == null || userIdList.size() <= 0)
            return null;

        int size = userIdList.size();

        BasicDBList list = new BasicDBList();
        for (String userId : userIdList) {
            list.add(new ObjectId(userId));
        }

        BasicDBObject in = new BasicDBObject();
        in.put("$in", list);

        DBObject query = new BasicDBObject();
        query.put("_id", in);

        DBObject fields = getUserPublicReturnFields();

//		DBObject fields = new BasicDBObject();
//		fields.put(DBConstants.F_ACCOUNT_BALANCE, 1);
//		fields.put(DBConstants.F_ACCOUNT_INGOT_BALANCE, 1);
        log.info("<findUserAccountInfo> query=" + query.toString());
        DBCursor cursor = mongoClient.find(DBConstants.T_USER, query, fields,
                null, 0, size);
        if (cursor == null)
            return Collections.emptyList();

        List<User> retList = new ArrayList<User>();
        while (cursor.hasNext()) {
            DBObject obj = cursor.next();
            retList.add(new User(obj));
        }
        log.info("<findUserAccountInfo> result=" + retList.toString());
        cursor.close();
        return retList;
    }

    // private static int calculateRelation(MongoDBClient mongoClient,
    // String userId, String friendId) {
    // int relation = 0;
    // if (RelationManager.isUserFollowFriend(mongoClient, userId, friendId)) {
    // relation = User.RELATION_FOLLOW;
    // }
    // if (RelationManager.isUserFollowFriend(mongoClient, friendId, userId)) {
    // relation |= User.RELATION_FAN;
    // }
    // return relation;
    // }

    public static User findPublicUserInfoByUserId(MongoDBClient mongoClient,
                                                  String userId, String targetUserId, boolean isReturnFanFollowInfo) {

        DBObject query = new BasicDBObject();
        query.put(DBConstants.F_USERID, new ObjectId(targetUserId));
        DBObject fields = getUserPublicReturnFields();
        DBObject obj = mongoClient.findOne(DBConstants.T_USER, query, fields);
        if (obj == null)
            return null;

        User user = new User(obj);
        fillUserWithGroup(mongoClient, user);
        int relation = RelationManager.userRelationWithFriend(mongoClient,
                userId, targetUserId);

//        UserFriend userFriendInfo = RelationManager.userFriendInfoWithFriend(mongoClient,
//                userId, targetUserId);

        user.setRelation(relation);

        if (isReturnFanFollowInfo) {
            long fanCount = RelationManager.getFanCount(mongoClient, "", targetUserId);
            user.setFanCount(fanCount);

            long followCount = RelationManager.getFollowCount(mongoClient, "", targetUserId);
            user.setFollowCount(followCount);
        }

        return user;
    }

    public static User findPublicUserInfoByUserId(String userId) {
        MongoDBClient mongoClient = DBService.getInstance().getMongoDBClient();
        return findPublicUserInfoByUserId(mongoClient, userId);
    }

    public static User findPublicUserInfoByUserId(MongoDBClient mongoClient,
                                                  String userId) {

        if (StringUtil.isEmpty(userId)) {
            return null;
        }

        DBObject query = new BasicDBObject();
        query.put(DBConstants.F_USERID, new ObjectId(userId));
        DBObject fields = getUserPublicReturnFields();
        DBObject obj = mongoClient.findOne(DBConstants.T_USER, query, fields);
        if (obj == null)
            return null;

        User user = new User(obj);
        return user;
    }

    public static BasicDBObject getUserPublicReturnFields() {
        BasicDBObject fields = new BasicDBObject();

        // change by Benson, only disable some fields
        fields.put(DBConstants.F_LEVEL_INFO, 0);
        fields.put(DBConstants.F_FANS, 0);
        fields.put(DBConstants.F_FOLLOWS, 0);

        /*
        fields.put(DBConstants.F_NICKNAME, 1);
		fields.put(DBConstants.F_AVATAR, 1);
		fields.put(DBConstants.F_EMAIL, 1);
		fields.put(DBConstants.F_SINA_NICKNAME, 1);
		fields.put(DBConstants.F_QQ_NICKNAME, 1);
		fields.put(DBConstants.F_QQID, 1);
		fields.put(DBConstants.F_SINAID, 1);
		fields.put(DBConstants.F_FACEBOOKID, 1);
		fields.put(DBConstants.F_LEVEL_INFO, 1);

		AppFactory.getInstance().putAllLevelFields(fields);

		fields.put(DBConstants.F_GENDER, 1);
		fields.put(DBConstants.F_LOCATION, 1);
		fields.put(DBConstants.F_SIGNATURE, 1);
		fields.put(DBConstants.F_BLOOD, 1);
		fields.put(DBConstants.F_ZODIAC, 1);
		fields.put(DBConstants.F_BACKGROUND, 1);
		fields.put(DBConstants.F_BIRTHDAY, 1);
		fields.put(DBConstants.F_GUESS_WORD_LANGUAGE, 1);
		fields.put(DBConstants.F_ACCOUNT_BALANCE, 1);
		fields.put(DBConstants.F_ACCOUNT_INGOT_BALANCE, 1);
		fields.put(DBConstants.F_DEVICETOKEN, 1);
		fields.put(DBConstants.F_DEVICEID, 1);
		fields.put(DBConstants.F_APPID, 1);
		fields.put(DBConstants.F_ITEMS, 1);
		fields.put(DBConstants.F_OPEN_INFO_TYPE, 1);
        fields.put(DBConstants.F_SING_RECORD_LIMIT, 1);
        fields.put(DBConstants.F_XIAOJI_NUMBER, 1);
        */

        return fields;
    }

    public static List<User> findPublicUserInfo(List<String> targetUserIdList) {
        return findPublicUserInfoByUserIdList(ListUtil.stringListToObjectIdList(targetUserIdList));
    }

    public static List<User> findPublicUserInfoByUserIdList(List<ObjectId> targetUserIdList) {
        return findPublicUserInfoByUserIdList(DBService.getInstance().getMongoDBClient(), targetUserIdList);
    }

    public static List<User> findPublicUserInfoByUserIdList(MongoDBClient mongoClient, List<ObjectId> targetUserIdList) {

        if (targetUserIdList == null || targetUserIdList.size() == 0)
            return Collections.emptyList();

        DBObject query = new BasicDBObject(DBConstants.F_USERID,
                new BasicDBObject("$in", targetUserIdList));
        DBObject fields = getUserPublicReturnFields();

        DBCursor cursor = mongoClient.findAll(DBConstants.T_USER, query, fields); //find(DBConstants.T_USER, query, fields);
        if (cursor == null)
            return Collections.emptyList();

        Map<ObjectId, User> map = new HashMap<ObjectId, User>();
        if (cursor != null) {
            while (cursor.hasNext()) {
                DBObject dbObject = (DBObject) cursor.next();
                User user = new User(dbObject);
                map.put(user.getObjectId(), user);
            }
            cursor.close();
        }

        // sort user by user id sequence
        List<User> userList = new ArrayList<User>();
        for (ObjectId uid : targetUserIdList) {
            if (map.containsKey(uid)) {
                userList.add(map.get(uid));
            }
        }

		/*
        List<User> userList = new ArrayList<User>();
		while (cursor.hasNext()) {
			DBObject dbObject = (DBObject) cursor.next();
			User user = new User(dbObject);
			int relation = RelationManager.userRelationWithFriend(mongoClient,
					userId, user.getUserId());
			user.setRelation(relation);
			userList.add(user);
		}
		cursor.close();
		*/

        return userList;
    }

//	public static User findUserForRecommend(final MongoDBClient mongoClient) {
//
//		DBObject obj = mongoClient.findAndModifyUpsert(DBConstants.T_USER,
//				DBConstants.F_RECOMMEND_STATUS,
//				DBConstants.C_RECOMMEND_STATUS_NOT_RUNNING,
//				DBConstants.C_RECOMMEND_STATUS_RUNNING);
//		if (obj != null) {
//			return new User(obj);
//		}
//
//		return null;
//
//	}userList

    public static List<User> findPublicUserInfoByUserIdList(MongoDBClient mongoClient,
                                                            String userId, List<ObjectId> targetUserIdList, boolean isReturnRelation) {

        DBObject query = new BasicDBObject(DBConstants.F_USERID,
                new BasicDBObject("$in", targetUserIdList));
        DBObject fields = getUserPublicReturnFields();

        DBCursor cursor = mongoClient.findAll(DBConstants.T_USER, query, fields); //find(DBConstants.T_USER, query, fields);
        if (cursor == null)
            return Collections.emptyList();

        Map<ObjectId, User> map = new HashMap<ObjectId, User>();
        if (cursor != null) {
            while (cursor.hasNext()) {
                DBObject dbObject = (DBObject) cursor.next();
                User user = new User(dbObject);
                if (isReturnRelation) {
                    int relation = RelationManager.userRelationWithFriend(mongoClient, userId, user.getUserId());
                    user.setRelation(relation);
                }
                map.put(user.getObjectId(), user);
            }
            cursor.close();
        }

        // sort user by user id sequence
        List<User> userList = new ArrayList<User>();
        for (ObjectId uid : targetUserIdList) {
            if (map.containsKey(uid)) {
                userList.add(map.get(uid));
            }
        }

		/*
        List<User> userList = new ArrayList<User>();
		while (cursor.hasNext()) {
			DBObject dbObject = (DBObject) cursor.next();
			User user = new User(dbObject);
			int relation = RelationManager.userRelationWithFriend(mongoClient,
					userId, user.getUserId());
			user.setRelation(relation);
			userList.add(user);
		}
		cursor.close();
		*/

        return userList;
    }

    public static boolean save(MongoDBClient mongoClient, User user) {
        // System.out.println("user=" + user.toString());
        mongoClient.save(DBConstants.T_USER, user.getDbObject());
        return true;
    }

    private static BasicDBObject createItemForAdd(String itemId, String appId,
                                                  String categoryName, String subCategoryName, String keywords,
                                                  String city, double maxPrice, double minRebate, Date expireDate,
                                                  String latitude, String longitude, String radius) {

        if (StringUtil.isEmpty(itemId))
            return null;

        BasicDBObject item = new BasicDBObject();
        item.put(DBConstants.F_ITEM_ID, itemId);
        item.put(DBConstants.F_CREATE_DATE, new Date());

        if (!StringUtil.isEmpty(categoryName))
            item.put(DBConstants.F_CATEGORY_NAME, categoryName);
        if (!StringUtil.isEmpty(subCategoryName))
            item.put(DBConstants.F_SUB_CATEGORY_NAME, subCategoryName);
        if (!StringUtil.isEmpty(keywords))
            item.put(DBConstants.F_KEYWORD, keywords);
        if (!StringUtil.isEmpty(city))
            item.put(DBConstants.F_CITY, city);
        if (maxPrice >= 0.0f)
            item.put(DBConstants.F_MAX_PRICE, maxPrice);
        if (minRebate >= 0.0f)
            item.put(DBConstants.F_MIN_REBATE, minRebate);
        if (latitude != null && latitude.length() > 0 && longitude != null
                && longitude.length() > 0 && radius != null
                && radius.length() > 0) {
            item.put(DBConstants.F_LATITUDE, latitude);
            item.put(DBConstants.F_LONGITUDE, longitude);
            item.put(DBConstants.F_RADIUS, radius);
        }

        item.put(DBConstants.F_APPID, appId);
        item.put(DBConstants.F_EXPIRE_DATE, expireDate);

        return item;
    }

    private static BasicDBObject createItemForUpdate(String itemId,
                                                     String categoryName, String subCategoryName, String keywords,
                                                     String city, double maxPrice, double minRebate, Date expireDate,
                                                     String latitude, String longitude, String radius) {

        if (StringUtil.isEmpty(itemId))
            return null;

        // key should like "xxx.$.xxx" for array update
        String prefix = DBConstants.F_SHOPPING_LIST.concat(".$.");

        BasicDBObject item = new BasicDBObject();

        item.put(prefix.concat(DBConstants.F_CATEGORY_NAME), categoryName);
        item.put(prefix.concat(DBConstants.F_SUB_CATEGORY_NAME),
                subCategoryName);
        item.put(prefix.concat(DBConstants.F_KEYWORD), keywords);
        item.put(prefix.concat(DBConstants.F_CITY), city);
        item.put(prefix.concat(DBConstants.F_EXPIRE_DATE), expireDate);

        if (maxPrice >= 0.0f)
            item.put(prefix.concat(DBConstants.F_MAX_PRICE), maxPrice);
        else
            item.put(prefix.concat(DBConstants.F_MAX_PRICE), null);

        if (minRebate >= 0.0f)
            item.put(prefix.concat(DBConstants.F_MIN_REBATE), minRebate);
        else
            item.put(prefix.concat(DBConstants.F_MIN_REBATE), null);

        if (latitude != null && latitude.length() > 0 && longitude != null
                && longitude.length() > 0 && radius != null
                && radius.length() > 0) {
            item.put(prefix.concat(DBConstants.F_LATITUDE), latitude);
            item.put(prefix.concat(DBConstants.F_LONGITUDE), longitude);
            item.put(prefix.concat(DBConstants.F_RADIUS), radius);
        } else {
            item.put(prefix.concat(DBConstants.F_LATITUDE), null);
            item.put(prefix.concat(DBConstants.F_LONGITUDE), null);
            item.put(prefix.concat(DBConstants.F_RADIUS), null);
        }

        return item;
    }

    public static boolean addUserShoppingItem(MongoDBClient mongoClient,
                                              String userId, String itemId, String appId, String categoryName,
                                              String subCategoryName, String keywords, String city,
                                              double maxPrice, double minRebate, Date expireDate,
                                              String latitude, String longitude, String radius) {

        BasicDBObject item = createItemForAdd(itemId, appId, categoryName,
                subCategoryName, keywords, city, maxPrice, minRebate,
                expireDate, latitude, longitude, radius);
        if (item == null)
            return false;

        BasicDBObject query = new BasicDBObject();
        ObjectId id = new ObjectId(userId);
        query.put(DBConstants.F_USERID, id);

        BasicDBObject pushValue = new BasicDBObject();
        pushValue.put(DBConstants.F_SHOPPING_LIST, item);

        BasicDBObject update = new BasicDBObject();
        update.put("$push", pushValue);

        mongoClient.updateAll(DBConstants.T_USER, query, update);
        return true;
    }

    private static String getItemArrayKey() {
        return DBConstants.F_SHOPPING_LIST.concat(".").concat(
                DBConstants.F_ITEM_ID);
    }

    public static boolean updateUserShoppingItem(MongoDBClient mongoClient,
                                                 String userId, String itemId, String categoryName,
                                                 String subCategoryName, String keywords, String city,
                                                 double maxPrice, double minRebate, Date expireDate,
                                                 String latitude, String longitude, String radius) {

        BasicDBObject item = createItemForUpdate(itemId, categoryName,
                subCategoryName, keywords, city, maxPrice, minRebate,
                expireDate, latitude, longitude, radius);
        if (item == null)
            return false;

        BasicDBObject query = new BasicDBObject();
        ObjectId id = new ObjectId(userId);
        query.put(DBConstants.F_USERID, id);
        String queryKeyForItem = getItemArrayKey();
        query.put(queryKeyForItem, itemId);

        BasicDBObject update = new BasicDBObject();
        update.put("$set", item);

        mongoClient.updateAll(DBConstants.T_USER, query, update);

        return true;
    }

    public static void registerUserDeviceToken(String userId, String deviceToken) {

        // TODO fix push notification

        // RegisterService registerService =
        // RegisterService.createService(PushNotificationConstants.APPLICATION_KEY,
        // PushNotificationConstants.APPLICATION_SECRET,
        // PushNotificationConstants.APPLICATION_MASTER_SECRET,
        // userId, deviceToken);
        // registerService.handleServiceRequest();
    }

    public static void bindUserByEmail(MongoDBClient mongoClient, String appId,
                                       User user, String email, String password, boolean needVerification) {

        user.put(DBConstants.F_EMAIL, email);
        user.put(DBConstants.F_PASSWORD, password);
        user.put(DBConstants.F_VERIFYCODE, StringUtil.randomUUID());
        user.put(DBConstants.F_CREATE_DATE, new Date()); // DateUtil.currentDate());
        if (needVerification)
            user.put(DBConstants.F_STATUS, DBConstants.STATUS_TO_VERIFY);
        else
            user.put(DBConstants.F_STATUS, DBConstants.STATUS_NORMAL);

        UserManager.save(mongoClient, user);

    }

    public static User findUserBySinaId(MongoDBClient mongoClient, String snsId) {
        if (mongoClient == null || snsId == null || snsId.length() <= 0)
            return null;

        DBObject obj = mongoClient.findOne(DBConstants.T_USER,
                DBConstants.F_SINAID, snsId);
        if (obj == null)
            return null;

        return fillUserWithGroup(mongoClient, new User(obj));
    }

    public static User findUserByTencentId(MongoDBClient mongoClient,
                                           String snsId) {
        if (mongoClient == null || snsId == null || snsId.length() <= 0)
            return null;

        DBObject obj = mongoClient.findOne(DBConstants.T_USER,
                DBConstants.F_QQID, snsId);
        if (obj == null)
            return null;

        return fillUserWithGroup(mongoClient, new User(obj));
    }

    public static User findUserByFacebookId(MongoDBClient mongoClient,
                                            String snsId) {
        if (mongoClient == null || snsId == null || snsId.length() <= 0)
            return null;

        DBObject obj = mongoClient.findOne(DBConstants.T_USER,
                DBConstants.F_FACEBOOKID, snsId);
        if (obj == null)
            return null;

        return fillUserWithGroup(mongoClient, new User(obj));
    }

    public static void bindUserBySnsId(MongoDBClient mongoClient, User user,
                                       String snsId, String nickName, String avatar, String accessToken,
                                       String accessTokenSecret, String province, String city,
                                       String location, String gender, String birthday, String domain,
                                       int registerType) {

        user.put(DBConstants.F_CREATE_DATE, new Date());

        if (avatar != null && avatar.length() > 0)
            user.put(DBConstants.F_AVATAR, avatar);

        if (province != null && province.length() > 0)
            user.put(DBConstants.F_PROVINCE, province);

        if (city != null && city.length() > 0)
            user.put(DBConstants.F_CITY, city);

        if (location != null && location.length() > 0)
            user.put(DBConstants.F_LOCATION, location);

        if (gender != null && gender.length() > 0)
            user.put(DBConstants.F_GENDER, gender);

        if (birthday != null && birthday.length() > 0)
            user.put(DBConstants.F_BIRTHDAY, birthday);

        // if (!StringUtil.isEmpty(user.getNickName())){
        // user.setNickName(nickName);
        // }

        switch (registerType) {

            case ServiceConstant.REGISTER_TYPE_SINA: {
                user.put(DBConstants.F_SINAID, snsId);
                user.put(DBConstants.F_SINA_NICKNAME, nickName);
                user.put(DBConstants.F_SINA_DOMAIN, domain);
                user.put(DBConstants.F_SINA_ACCESS_TOKEN, accessToken);
                user.put(DBConstants.F_SINA_ACCESS_TOKEN_SECRET, accessTokenSecret);
            }
            break;

            case ServiceConstant.REGISTER_TYPE_QQ: {
                user.put(DBConstants.F_QQID, snsId);
                user.put(DBConstants.F_QQ_NICKNAME, nickName);
                user.put(DBConstants.F_QQ_DOMAIN, domain);
                user.put(DBConstants.F_QQ_ACCESS_TOKEN, accessToken);
                user.put(DBConstants.F_QQ_ACCESS_TOKEN_SECRET, accessTokenSecret);
            }
            break;

            default:
                break;
        }

        UserManager.save(mongoClient, user);

    }

    public static User createUserBySnsId(MongoDBClient mongoClient,
                                         String appId, String snsId, String nickName, String avatar,
                                         String accessToken, String accessTokenSecret, String province,
                                         String city, String location, String gender, String birthday,
                                         String domain, String deviceToken, int registerType) {
        BasicDBObject user = new BasicDBObject();
        user.put(DBConstants.F_APPID, appId);
        user.put(DBConstants.F_CREATE_SOURCE_ID, appId);
        user.put(DBConstants.F_CREATE_DATE, new Date()); // DateUtil.currentDate());

        user.put(DBConstants.F_AVATAR, avatar);
        user.put(DBConstants.F_PROVINCE, province);
        user.put(DBConstants.F_CITY, city);
        user.put(DBConstants.F_LOCATION, location);
        user.put(DBConstants.F_GENDER, gender);
        user.put(DBConstants.F_BIRTHDAY, birthday);

        user.put(DBConstants.F_DEVICETOKEN, deviceToken);

        switch (registerType) {
            case ServiceConstant.REGISTER_TYPE_SINA:
                user.put(DBConstants.F_SINAID, snsId);
                user.put(DBConstants.F_SINA_NICKNAME, nickName);
                user.put(DBConstants.F_SINA_DOMAIN, domain);
                user.put(DBConstants.F_SINA_ACCESS_TOKEN, accessToken);
                user.put(DBConstants.F_SINA_ACCESS_TOKEN_SECRET, accessTokenSecret);
                break;
            case ServiceConstant.REGISTER_TYPE_QQ:
                user.put(DBConstants.F_QQID, snsId);
                user.put(DBConstants.F_QQ_NICKNAME, nickName);
                user.put(DBConstants.F_QQ_DOMAIN, domain);
                user.put(DBConstants.F_QQ_ACCESS_TOKEN, accessToken);
                user.put(DBConstants.F_QQ_ACCESS_TOKEN_SECRET, accessTokenSecret);
                break;
            case ServiceConstant.REGISTER_TYPE_FACEBOOK:
                user.put(DBConstants.F_FACEBOOKID, snsId);
                break;
            case ServiceConstant.REGISTER_TYPE_TWITTER:
                user.put(DBConstants.F_TWITTERID, snsId);
                break;
            case ServiceConstant.REGISTER_TYPE_RENREN:
                user.put(DBConstants.F_RENRENID, snsId);
                break;
            default:
                break;
        }

        // // create default tips item
        // BasicDBObject tipsItem = new BasicDBObject();
        // tipsItem.put(DBConstants.F_ITEM_TYPE, DBConstants.C_ITEM_TYPE_TIPS);
        // tipsItem.put(DBConstants.F_ITEM_AMOUNT,
        // DBConstants.C_DEFAULT_TIPS_AMOUNT);
        //
        // // add push to item array
        // BasicDBList items = new BasicDBList();
        // items.add(tipsItem);
        // user.put(DBConstants.F_ITEMS, items);

        initItems(user, appId);
        initLevelInfo(user);
        initUserAppIds(user, appId);

        boolean result = mongoClient.insert(DBConstants.T_USER, user);
        if (result)
            return fillUserWithGroup(mongoClient, new User(user));
        else
            return null;

    }

    private static void initLevelInfo(BasicDBObject user) {
        /*
		// init user level info object
		BasicDBObject drawLevelInfo = new BasicDBObject();
		drawLevelInfo.put(DBConstants.F_CREATE_SOURCE_ID,
				DBConstants.GAME_ID_DRAW);
		drawLevelInfo.put(DBConstants.F_LEVEL, 1);
		drawLevelInfo.put(DBConstants.F_EXP, 0L);

		// init dice level info object
		BasicDBObject diceLevelInfo = new BasicDBObject();
		diceLevelInfo.put(DBConstants.F_CREATE_SOURCE_ID,
				DBConstants.GAME_ID_DICE);
		diceLevelInfo.put(DBConstants.F_LEVEL, 1);
		diceLevelInfo.put(DBConstants.F_EXP, 0L);

		// init zjh level info object
		BasicDBObject zjhLevelInfo = new BasicDBObject();
		zjhLevelInfo.put(DBConstants.F_CREATE_SOURCE_ID,
				DBConstants.GAME_ID_ZHAJINHUA);
		zjhLevelInfo.put(DBConstants.F_LEVEL, 1);
		zjhLevelInfo.put(DBConstants.F_EXP, 0L);

		// init level info
		BasicDBList levelInfo = new BasicDBList();
		levelInfo.add(drawLevelInfo);
		levelInfo.add(diceLevelInfo);
		levelInfo.add(zjhLevelInfo);
		user.put(DBConstants.F_LEVEL_INFO, levelInfo);
		*/

        AppFactory.getInstance().initLevelInfo(user);


    }

    public static User createUserBySnsId(MongoDBClient mongoClient,
                                         String appId, String snsId, String nickName, String avatar,
                                         String accessToken, String accessTokenSecret, String province,
                                         String city, String location, String gender, String birthday,
                                         String domain, String deviceToken, int registerType, String nation,
                                         String language, String deviceModel, String deviceOs,
                                         int deviceType, String refreshToken, String qqOpenId,
                                         Date expireDate) {
        BasicDBObject user = new BasicDBObject();
        user.put(DBConstants.F_APPID, appId);
        user.put(DBConstants.F_CREATE_SOURCE_ID, appId);
        user.put(DBConstants.F_CREATE_DATE, new Date()); // DateUtil.currentDate());

        user.put(DBConstants.F_AVATAR, avatar);
        user.put(DBConstants.F_PROVINCE, province);
        user.put(DBConstants.F_CITY, city);
        user.put(DBConstants.F_LOCATION, location);
        user.put(DBConstants.F_NICKNAME, nickName);
        user.put(DBConstants.F_GENDER, gender);
        user.put(DBConstants.F_BIRTHDAY, birthday);

        user.put(DBConstants.F_DEVICETOKEN, deviceToken);
        user.put(DBConstants.F_COUNTRYCODE, nation);
        user.put(DBConstants.F_LANGUAGE, language);
        user.put(DBConstants.F_DEVICEMODEL, deviceModel);
        user.put(DBConstants.F_DEVICEOS, deviceOs);
        user.put(DBConstants.F_DEVICE_TYPE, deviceType);

        user.put(DBConstants.F_SHAKE_XIAOJI, true);
        user.put(DBConstants.F_SHAKE_NUMBER_TIMES, 10);

        switch (registerType) {
            case ServiceConstant.REGISTER_TYPE_SINA:
                user.put(DBConstants.F_SINAID, snsId);
                user.put(DBConstants.F_SINA_NICKNAME, nickName);
                user.put(DBConstants.F_SINA_DOMAIN, domain);
                user.put(DBConstants.F_SINA_ACCESS_TOKEN, accessToken);
                user.put(DBConstants.F_SINA_ACCESS_TOKEN_SECRET, accessTokenSecret);

                user.put(DBConstants.F_SINA_REFRESH_TOKEN, refreshToken);
                user.put(DBConstants.F_SINA_EXPIRE_DATE, expireDate);

                break;
            case ServiceConstant.REGISTER_TYPE_QQ:
                user.put(DBConstants.F_QQID, snsId);
                user.put(DBConstants.F_QQ_NICKNAME, nickName);
                user.put(DBConstants.F_QQ_DOMAIN, domain);
                user.put(DBConstants.F_QQ_ACCESS_TOKEN, accessToken);
                user.put(DBConstants.F_QQ_ACCESS_TOKEN_SECRET, accessTokenSecret);

                user.put(DBConstants.F_QQ_REFRESH_TOKEN, refreshToken);
                user.put(DBConstants.F_QQ_EXPIRE_DATE, expireDate);
                user.put(DBConstants.F_QQ_OPEN_ID, qqOpenId);
                break;

            case ServiceConstant.REGISTER_TYPE_FACEBOOK:
                user.put(DBConstants.F_FACEBOOKID, snsId);
                user.put(DBConstants.F_FACEBOOK_ACCESS_TOKEN, accessToken);
                user.put(DBConstants.F_FACEBOOK_EXPIRE_DATE, expireDate);
                break;

            case ServiceConstant.REGISTER_TYPE_TWITTER:
                user.put(DBConstants.F_TWITTERID, snsId);
                break;
            case ServiceConstant.REGISTER_TYPE_RENREN:
                user.put(DBConstants.F_RENRENID, snsId);
                break;
            default:
                break;
        }

        // // create default tips item
        // BasicDBObject tipsItem = new BasicDBObject();
        // tipsItem.put(DBConstants.F_ITEM_TYPE, DBConstants.C_ITEM_TYPE_TIPS);
        // tipsItem.put(DBConstants.F_ITEM_AMOUNT,
        // DBConstants.C_DEFAULT_TIPS_AMOUNT);
        //
        // // add push to item array
        // BasicDBList items = new BasicDBList();
        // items.add(tipsItem);
        // user.put(DBConstants.F_ITEMS, items);

        setDefaultBalance(user, appId);
        initItems(user, appId);
        initLevelInfo(user);
        initUserAppIds(user, appId);

        boolean result = mongoClient.insert(DBConstants.T_USER, user);
        if (result) {
            User retUser = new User(user);
            ElasticsearchService.addOrUpdateIndex(retUser, mongoClient);
            return retUser;
        } else
            return null;
    }

    public static void updateUserDeviceInfo(MongoDBClient mongoClient, String userId, DBObject deviceObj) {
        BasicDBObject query = new BasicDBObject();
        query.put("_id", new ObjectId(userId));
        query.put(DBConstants.F_DEVICES + "." + DBConstants.F_DEVICEID, deviceObj.get(DBConstants.F_DEVICEID));

        BasicDBObject updateValue = new BasicDBObject();
        if (deviceObj.get(DBConstants.F_DEVICE_TYPE) != null) {
            updateValue.put(DBConstants.F_DEVICES + ".$." + DBConstants.F_DEVICE_TYPE, deviceObj.get(DBConstants.F_DEVICE_TYPE));
        }
        if (deviceObj.get(DBConstants.F_DEVICEMODEL) != null) {
            updateValue.put(DBConstants.F_DEVICES + ".$." + DBConstants.F_DEVICEMODEL, deviceObj.get(DBConstants.F_DEVICEMODEL));
        }
        if (deviceObj.get(DBConstants.F_DEVICEID) != null) {
            updateValue.put(DBConstants.F_DEVICES + ".$." + DBConstants.F_DEVICEID, deviceObj.get(DBConstants.F_DEVICEID));
        }
        if (deviceObj.get(DBConstants.F_DEVICEOS) != null) {
            updateValue.put(DBConstants.F_DEVICES + ".$." + DBConstants.F_DEVICEOS, deviceObj.get(DBConstants.F_DEVICEOS));
        }
        if (deviceObj.get(DBConstants.F_DEVICETOKEN) != null) {
            updateValue.put(DBConstants.F_DEVICES + ".$." + DBConstants.F_DEVICETOKEN, deviceObj.get(DBConstants.F_DEVICETOKEN));
        }

        BasicDBObject update = new BasicDBObject();
        update.put("$set", updateValue);

        BasicDBObject addToSetValue = new BasicDBObject();
        if (deviceObj.get(DBConstants.F_DEVICETOKEN) != null) {
            addToSetValue.put(DBConstants.F_DEVICETOKEN_LIST, deviceObj.get(DBConstants.F_DEVICETOKEN));
            updateValue.put(DBConstants.F_DEVICETOKEN, deviceObj.get(DBConstants.F_DEVICETOKEN));
        }
        if (deviceObj.get(DBConstants.F_DEVICEID) != null) {
            addToSetValue.put(DBConstants.F_DEVICEID_LIST, deviceObj.get(DBConstants.F_DEVICEID));
        }

        if (addToSetValue.size() > 0) {
            update.put("$addToSet", addToSetValue);
        }

//        if (deviceObj.get(DBConstants.F_DEVICEID) != null){
//            BasicDBObject addToSet = new BasicDBObject(DBConstants.F_DEVICEID_LIST, deviceObj.get(DBConstants.F_DEVICEID));
//            update.put("$addToSet", addToSet);
//        }
//        if (deviceObj.get(DBConstants.F_DEVICETOKEN) != null){
//            BasicDBObject addToSet = new BasicDBObject(DBConstants.F_DEVICETOKEN_LIST, deviceObj.get(DBConstants.F_DEVICETOKEN));
//            update.put("$addToSet", addToSet);
//        }

        mongoClient.updateOne(DBConstants.T_USER, query, update);
    }

    public static void addUserDeviceInfo(MongoDBClient mongoClient, String userId, DBObject deviceObj) {
        BasicDBObject query = new BasicDBObject();
        query.put("_id", new ObjectId(userId));

        String deviceId = (String) deviceObj.get(DBConstants.F_DEVICEID);

        BasicDBObject update = new BasicDBObject();
        update.put("$push", new BasicDBObject(DBConstants.F_DEVICES, deviceObj));

        BasicDBObject addToSetValue = new BasicDBObject();
        if (deviceObj.get(DBConstants.F_DEVICETOKEN) != null) {
            addToSetValue.put(DBConstants.F_DEVICETOKEN_LIST, deviceObj.get(DBConstants.F_DEVICETOKEN));
        }
        if (deviceObj.get(DBConstants.F_DEVICEID) != null) {
            addToSetValue.put(DBConstants.F_DEVICEID_LIST, deviceObj.get(DBConstants.F_DEVICEID));
        }

        if (addToSetValue.size() > 0) {
            update.put("$addToSet", addToSetValue);
        }

        mongoClient.updateOne(DBConstants.T_USER, query, update);
    }

    ;

    public static void clearUserDeviceInfo(String userId, String deviceId, String deviceToken) {

        if (StringUtil.isEmpty(userId) || StringUtil.isEmpty(deviceId))
            return;

        BasicDBObject query = new BasicDBObject();
        query.put("_id", new ObjectId(userId));

        BasicDBObject update = new BasicDBObject();

        update.put("$pull", new BasicDBObject(DBConstants.F_DEVICES + "." + DBConstants.F_DEVICEID, deviceId));
        update.put("$pull", new BasicDBObject(DBConstants.F_DEVICEID_LIST, deviceId));
        if (!StringUtil.isEmpty(deviceToken)) {
            update.put("$pull", new BasicDBObject(DBConstants.F_DEVICETOKEN_LIST, deviceToken));
        }

        DBService.getInstance().getMongoDBClient().updateAll(DBConstants.T_USER, query, update);
    }

    public static void updateUserXiaojiNumber(String userId, String number) {

        BasicDBObject update = new BasicDBObject();
        update.put(DBConstants.F_XIAOJI_NUMBER, number);

        UserManager.updateUserByDBObject(DBService.getInstance().getMongoDBClient(), userId, update);
    }

    public static int removeUserDevice(String userId, String deviceId, String deviceToken) {

        if (userId == null) {
            return ErrorCode.ERROR_PARAMETER_USERID_EMPTY;
        }

        if (deviceId == null && deviceToken == null) {
            return ErrorCode.ERROR_PARAMETER_DEVICEID_EMPTY;
        }

        BasicDBObject query = new BasicDBObject("_id", new ObjectId(userId));

        BasicDBObject pullValues = new BasicDBObject();
        BasicDBObject unsetValues = new BasicDBObject();

        if (deviceId != null) {
            pullValues.put(DBConstants.F_DEVICEID_LIST, deviceId);
            unsetValues.put(DBConstants.F_DEVICEID, 1);
        }

        if (deviceToken != null) {
            pullValues.put(DBConstants.F_DEVICETOKEN_LIST, deviceToken);
            unsetValues.put(DBConstants.F_DEVICETOKEN, 1);
        }


        BasicDBObject pull = new BasicDBObject("$pull", pullValues);
        pull.put("$unset", unsetValues);

        DBService.getInstance().getMongoDBClient().updateOne(DBConstants.T_USER, query, pull);
        return 0;
    }

    public static User addDefaultItemIfNeeded(User user, String appId) {

        if (user == null)
            return null;

        AbstractApp app = AppFactory.getInstance().getApp(appId);
        if (app == null)
            return user;

        List<BasicDBObject> itemList = app.defaultItems();
        if (itemList == null || itemList.size() == 0) {
            return user;
        }

        int returnCoins = 0;
        boolean hasNewItems = false;
        BasicDBList newUserItemList = new BasicDBList();
        BasicDBList userItemList = user.getItemList();
        if (userItemList == null) {

            hasNewItems = true;
            newUserItemList.addAll(itemList);
        } else {

            // add all exists items
            newUserItemList.addAll(userItemList);

            // add all non-exist items
            for (BasicDBObject item : itemList) {
                Iterator<Object> iter = userItemList.iterator();
                boolean found = false;
                while (iter.hasNext()) {
                    BasicDBObject obj = (BasicDBObject) iter.next();
                    if (obj != null) {
                        int type = obj.getInt(DBConstants.F_ITEM_TYPE);
                        if (type == item.getInt(DBConstants.F_ITEM_TYPE)) {
                            found = true;
                            break;
                        }
                    }
                }

                if (!found) {
                    hasNewItems = true;
                    newUserItemList.add(item);
                } else {
                    if (user.hasSetTakeCoinsForItem() == false) {
                        int itemCoins = getItemPrice(item.getInt(DBConstants.F_ITEM_TYPE));
                        returnCoins += itemCoins;
                    }
                }
            }
        }

        if (hasNewItems) {
            log.info("<addFreeDrawItemIfNeeded> new items added, final list=" + newUserItemList.toString());

            BasicDBObject obj = new BasicDBObject();
            obj.put(DBConstants.F_ITEMS, newUserItemList);
            if (user.hasSetTakeCoinsForItem() == false) {
                obj.put(DBConstants.F_TAKE_COINS, returnCoins);
                obj.put(DBConstants.F_CALCULATE_TAKE_COINS, true);
            }
            user = UserManager.updateUserByDBObject(DBService.getInstance().getMongoDBClient(), user.getUserId(), obj);
        } else {
            log.info("<addFreeDrawItemIfNeeded> no new items for user " + user.getUserId() + ", " + user.getNickName());
        }

        return user;

    }

    private static int getItemPrice(int itemType) {

        switch (itemType) {
            case DBConstants.C_ITEM_TYPE_COPY_PAINT:
                return 500;
            case DBConstants.C_ITEM_TYPE_SHADOW:
                return 2000;
            case DBConstants.C_ITEM_TYPE_GRADIENT:
                return 2000;
            case DBConstants.C_ITEM_TYPE_PAINT_PALETTE:
                return 4000;
            case DBConstants.C_ITEM_TYPE_PAINT_ALPHA:
                return 10000;
            case DBConstants.C_ITEM_TYPE_PAINT_PLAYER:
                return 2000;
            case DBConstants.C_ITEM_TYPE_PAINT_STRAW:
                return 1000;
            case DBConstants.C_ITEM_TYPE_PAINT_GRID:
                return 1000;

            case DBConstants.C_ITEM_TYPE_BACKGROUND_1:
            case DBConstants.C_ITEM_TYPE_BACKGROUND_2:
            case DBConstants.C_ITEM_TYPE_BACKGROUND_12:
                return 2000;
            case DBConstants.C_ITEM_TYPE_PAINT_SHAPE:
                return 2500;
            case DBConstants.C_ITEM_TYPE_CANVAS_IPAD_HORIZONTAL:
            case DBConstants.C_ITEM_TYPE_CANVAS_IPAD_VERTICAL:
            case DBConstants.C_ITEM_TYPE_CANVAS_IPHONE_HORIZONTAL:
            case DBConstants.C_ITEM_TYPE_CANVAS_IPHONE_VERTICAL:
            case DBConstants.C_ITEM_TYPE_CANVAS_IPHONE5_HORIZONTAL:
            case DBConstants.C_ITEM_TYPE_CANVAS_IPHONE5_VERTICAL:
                return 2000;

        }

        return 0;

//        addItemIntoList(DBConstants.C_ITEM_TYPE_COPY_PAINT, list);
//        addItemIntoList(DBConstants.C_ITEM_TYPE_SHADOW, list);
//        addItemIntoList(DBConstants.C_ITEM_TYPE_GRADIENT, list);
//        addItemIntoList(DBConstants.C_ITEM_TYPE_PAINT_PALETTE, list);
//        addItemIntoList(DBConstants.C_ITEM_TYPE_PAINT_ALPHA, list);
//        addItemIntoList(DBConstants.C_ITEM_TYPE_PAINT_PLAYER, list);
//        addItemIntoList(DBConstants.C_ITEM_TYPE_PAINT_STRAW, list);
//        addItemIntoList(DBConstants.C_ITEM_TYPE_PAINT_ERASER, list);
//        addItemIntoList(DBConstants.C_ITEM_TYPE_PAINT_GRID, list);
//
//        addItemIntoList(DBConstants.C_ITEM_TYPE_BACKGROUND_1, list);
//        addItemIntoList(DBConstants.C_ITEM_TYPE_BACKGROUND_2, list);
//        addItemIntoList(DBConstants.C_ITEM_TYPE_BACKGROUND_12, list);
//
//        addItemIntoList(DBConstants.C_ITEM_TYPE_PAINT_SHAPE, list);
//
//        addItemIntoList(DBConstants.C_ITEM_TYPE_CANVAS_IPHONE_DEFAULT, list);
//        addItemIntoList(DBConstants.C_ITEM_TYPE_CANVAS_IPAD_DEFAULT, list);
//        addItemIntoList(DBConstants.C_ITEM_TYPE_CANVAS_IPAD_HORIZONTAL, list);
//        addItemIntoList(DBConstants.C_ITEM_TYPE_CANVAS_IPAD_VERTICAL, list);
//        addItemIntoList(DBConstants.C_ITEM_TYPE_CANVAS_IPHONE_HORIZONTAL, list);
//        addItemIntoList(DBConstants.C_ITEM_TYPE_CANVAS_IPHONE_VERTICAL, list);
//        addItemIntoList(DBConstants.C_ITEM_TYPE_CANVAS_IPHONE5_HORIZONTAL, list);
//        addItemIntoList(DBConstants.C_ITEM_TYPE_CANVAS_IPHONE5_VERTICAL, list);

    }

    public static void removeUserXiaojiNumber(User user, String number) {
        BasicDBObject query = new BasicDBObject("_id", user.getObjectId());
        BasicDBObject update = new BasicDBObject();
        update.put("$unset", new BasicDBObject(DBConstants.F_XIAOJI_NUMBER, 1));
        DBService.getInstance().getMongoDBClient().updateAll(DBConstants.T_USER, query, update);
    }

    public static void updateUserPopScore(String userId, double score) {
        BasicDBObject query = new BasicDBObject();
        query.put(DBConstants.F_USERID, new ObjectId(userId));
        BasicDBObject update = new BasicDBObject();
        BasicDBObject updateValue = new BasicDBObject();
        updateValue.put(DBConstants.F_POP_SCORE, score);
        update.put("$set", updateValue);

        MongoDBClient mongoDBClient = DBService.getInstance().getMongoDBClient();

        mongoDBClient.updateOne(DBConstants.T_USER, query, update);
    }

    public static void writeTransactionReceiptCheck(MongoDBClient mongoClient,
                                                    String userId, int amount, int source, String transactionId,
                                                    String transactionReceipt, TransactionType type) {

        if (StringUtil.isEmpty(transactionId)
                || StringUtil.isEmpty(transactionReceipt))
            return;

        BasicDBObject query = new BasicDBObject();
        query.put(DBConstants.F_TRANSACTION_ID, transactionId);

        BasicDBObject update = new BasicDBObject();
        update.put(DBConstants.F_TRANSACTION_ID, transactionId);

        mongoClient.updateOrInsert(DBConstants.T_TRANSACTION_ID_CHECK, query,
                update);
        log.info("<writeTransactionReceiptCheck> query=" + query.toString()
                + ", update=" + update.toString());

        BasicDBObject query2 = new BasicDBObject();
        query2.put(DBConstants.F_TRANSACTION_RECEIPT, transactionReceipt);

        BasicDBObject update2 = new BasicDBObject();
        update2.put(DBConstants.F_TRANSACTION_RECEIPT, transactionReceipt);

        mongoClient.updateOrInsert(DBConstants.T_TRANSACTION_RECEIPT_CHECK,
                query2, update2);
        log.info("<writeTransactionReceiptCheck> query=" + query2.toString()
                + ", update=" + update2.toString());

    }

    private static void writeBalanceTransaction(MongoDBClient mongoClient,
                                                String userId, int amount, int source, String transactionId,
                                                String transactionReceipt, TransactionType type, int balanceType) {

        if (source != DBConstants.C_CHARGE_SOURCE_PURCHASE_COIN) {
            return;
        }

        BasicDBObject transaction = new BasicDBObject();
        transaction.put(DBConstants.F_TRANSACTION_USERID, userId);
        transaction.put(DBConstants.F_AMOUNT, amount);
        transaction.put(DBConstants.F_TRANSACTION_ID, transactionId);
        transaction.put(DBConstants.F_TRANSACTION_RECEIPT, transactionReceipt);
        transaction.put(DBConstants.F_CREATE_DATE, new Date());
        transaction.put(DBConstants.F_CREATE_SOURCE_ID, source);
        transaction.put(DBConstants.F_TYPE, type.toString());
        transaction.put(DBConstants.F_BALANCE_TYPE, balanceType);

        mongoClient.insert(DBConstants.T_USER_BUY_COINS_HISTORY, transaction);
        log.info("<writeTransaction> transaction=" + transaction.toString());
    }

    public static void writeTransaction(MongoDBClient mongoClient,
                                        String userId, int amount, int source, String transactionId,
                                        String transactionReceipt, TransactionType type) {

        writeBalanceTransaction(mongoClient, userId, amount, source, transactionId,
                transactionReceipt, type, BALANCE_TYPE_COINS);

    }

    public static void writeIngotTransaction(MongoDBClient mongoClient,
                                             String userId, int amount, int source, String transactionId,
                                             String transactionReceipt, TransactionType type) {

        writeBalanceTransaction(mongoClient, userId, amount, source, transactionId,
                transactionReceipt, type, BALANCE_TYPE_INGOT);

    }

    public static void writeItemTransaction(MongoDBClient mongoClient,
                                            String userId, int itemType, int itemAmount) {

        BasicDBObject transaction = new BasicDBObject();
        transaction.put(DBConstants.F_TRANSACTION_USERID, userId);
        transaction.put(DBConstants.F_ITEM_TYPE, itemType);
        transaction.put(DBConstants.F_ITEM_AMOUNT, itemAmount);
        transaction.put(DBConstants.F_CREATE_DATE, new Date());

        mongoClient.insert(DBConstants.T_USER_BUY_ITEM_HISTORY, transaction);
        log
                .info("<writeItemTransaction> transaction="
                        + transaction.toString());
    }

    public static User chargeIngotAccount(MongoDBClient mongoClient, String userId,
                                          int amount, int source, String transactionId,
                                          String transactionReceipt) {

        if (userId == null)
            return null;

        if (!ObjectId.isValid(userId)) {
            log.warn("<chargeIngotAccount> but userId invalid, userId=" + userId);
            return null;
        }

        // step 1: update amount
        BasicDBObject query = new BasicDBObject();
        query.put(DBConstants.F_USERID, new ObjectId(userId));

        BasicDBObject incValue = new BasicDBObject();
        incValue.put(DBConstants.F_ACCOUNT_INGOT_BALANCE, amount);

        DBObject fields = getUserPublicReturnFields();
//		BasicDBObject fields = new BasicDBObject();
//		fields.put(DBConstants.F_ACCOUNT_INGOT_BALANCE, 1);
//		fields.put(DBConstants.F_ACCOUNT_BALANCE, 1);
//		fields.put(DBConstants.F_ITEMS, 1);
//		fields.put(DBConstants.F_LEVEL_INFO, 1);

        BasicDBObject update = new BasicDBObject();
        update.put("$inc", incValue);

        log.info("<F_ACCOUNT_INGOT_BALANCE> query=" + query + ", update=" + update + ", fields=" + fields.toString());
        DBObject obj = mongoClient.findAndModify(DBConstants.T_USER, query,
                update, fields);
        if (obj == null)
            return null;

        // step 2: write transaction log
        writeIngotTransaction(mongoClient, userId, amount, source, transactionId,
                transactionReceipt, TransactionType.CHARGE);

        // step 3: insert transactionReceipt for duplicate check
        writeTransactionReceiptCheck(mongoClient, userId, amount, source,
                transactionId, transactionReceipt, TransactionType.CHARGE);

        // step 4: for wall coins
        writeWallAwardLog(mongoClient, userId, amount, source);

        User user = new User(obj);
        return user;
    }

    public static User chargeAccount(MongoDBClient mongoClient, String userId,
                                     int amount, int source, String transactionId,
                                     String transactionReceipt) {

        if (userId == null)
            return null;

        if (!ObjectId.isValid(userId)) {
            log.warn("<chargeAccount> but userId invalid, userId=" + userId);
            return null;
        }

        // step 1: update amount
        BasicDBObject query = new BasicDBObject();
        query.put(DBConstants.F_USERID, new ObjectId(userId));

        BasicDBObject incValue = new BasicDBObject();
        incValue.put(DBConstants.F_ACCOUNT_BALANCE, amount);

        DBObject fields = getUserPublicReturnFields();

        // for award handling
        addAwardBalance(incValue, source, amount);

        BasicDBObject update = new BasicDBObject();
        update.put("$inc", incValue);

        if (source == DBConstants.C_CHARGE_SOURCE_AWARD_TAKE_COINS) {
            BasicDBObject updateValue = new BasicDBObject();
            updateValue.put(DBConstants.F_TAKE_COINS, 0);
            update.put("$set", updateValue);
        } else if (source == DBConstants.C_CHARGE_SOURCE_VIP_MONTHLY) {
            BasicDBObject updateValue = new BasicDBObject();
            updateValue.put(DBConstants.F_VIP_MONTHLY_CHARGE, new Date());
            update.put("$set", updateValue);
        }

        log.info("<chargeAccount> query=" + query + ", update=" + update + ", fields=" + fields.toString());
        DBObject obj = mongoClient.findAndModify(DBConstants.T_USER, query,
                update, fields);
        if (obj == null)
            return null;

        // step 2: write transaction log
        writeTransaction(mongoClient, userId, amount, source, transactionId,
                transactionReceipt, TransactionType.CHARGE);

        // step 3: insert transactionReceipt for duplicate check
        writeTransactionReceiptCheck(mongoClient, userId, amount, source,
                transactionId, transactionReceipt, TransactionType.CHARGE);

        // step 4: for wall coins
        writeWallAwardLog(mongoClient, userId, amount, source);

        User user = new User(obj);
        return user;
    }

    private static void writeWallAwardLog(MongoDBClient mongoClient,
                                          String userId, int amount, int source) {

        if (source != DBConstants.C_CHARGE_SOURCE_YOUMI_APP_REWARD &&
                source != DBConstants.C_CHARGE_SOURCE_APP_REWARD) {
            return;
        }

        BasicDBObject transaction = new BasicDBObject();
        transaction.put(DBConstants.F_TRANSACTION_USERID, userId);
        transaction.put(DBConstants.F_AMOUNT, amount);
        transaction.put(DBConstants.F_CREATE_DATE, new Date());
        transaction.put(DBConstants.F_CREATE_SOURCE_ID, source);

        mongoClient.insert(DBConstants.T_USER_WALL_AWARD_HISTORY, transaction);
        log.info("<writeWallAwardLog> data=" + transaction.toString());
    }

    private static void addAwardBalance(BasicDBObject incValue, int source,
                                        int amount) {

        if (source == Item.ADD_GUESS_COIN_TYPE) {
            log
                    .info("<addAwardBalance> deduct guess balancem, amount"
                            + amount);

            // decrease the guess balance.
            incValue.put(DBConstants.F_GUESS_BALANCE, -amount);
        }
    }

    public static User deductAccount(MongoDBClient mongoClient, String userId,
                                     int amount, int source) {
        if (userId == null)
            return null;

        if (!ObjectId.isValid(userId)) {
            log.warn("<deductAccount> but userId invalid, userId=" + userId);
            return null;
        }

        // step 1: update amount
        BasicDBObject query = new BasicDBObject();
        query.put(DBConstants.F_USERID, new ObjectId(userId));

        BasicDBObject incValue = new BasicDBObject();
        incValue.put(DBConstants.F_ACCOUNT_BALANCE, -amount);

        DBObject fields = getUserPublicReturnFields();
//		BasicDBObject fields = new BasicDBObject();
//		fields.put(DBConstants.F_ACCOUNT_BALANCE, 1);
//		fields.put(DBConstants.F_ACCOUNT_INGOT_BALANCE, 1);
//		fields.put(DBConstants.F_ITEMS, 1);
//		fields.put(DBConstants.F_LEVEL_INFO, 1);


        // for award handling
        addAwardBalance(incValue, source, amount);

        BasicDBObject update = new BasicDBObject();
        update.put("$inc", incValue);

        log.info("<deductAccount> query=" + query + ", update=" + update);
        DBObject obj = mongoClient.findAndModify(DBConstants.T_USER, query, update, fields);
        if (obj == null)
            return null;

        // step 2: write transaction log
        writeTransaction(mongoClient, userId, amount, source, null, null,
                TransactionType.DEDUCT);

        User user = new User(obj);
        return user;

    }

    public static User updateItemAmount(MongoDBClient mongoClient,
                                        String userId, int itemType, int itemAmount) {

//		BasicDBObject fields = new BasicDBObject(DBConstants.F_ITEMS, 1);
        BasicDBObject fields = getUserPublicReturnFields();

        User user = UserManager.findUserByUserId(mongoClient, userId, fields);
        if (user == null)
            return null;

        user.createOrUpdateItem(itemType, itemAmount);
//		mongoClient.save(DBConstants.T_USER, user.getDbObject());

        BasicDBObject query = new BasicDBObject("_id", user.getObjectId());

        BasicDBObject update = new BasicDBObject();
        BasicDBObject updateValue = new BasicDBObject();
        updateValue.put(DBConstants.F_ITEMS, user.getItemList());
        update.put("$set", updateValue);

        mongoClient.updateOne(DBConstants.T_USER, query, update);
        log.info("<updateItemAmount> query=" + query.toString() + ", update=" + update.toString());

        writeItemTransaction(mongoClient, userId, itemType, itemAmount);
        return user;
    }

    public static User incItemAmount(MongoDBClient mongoClient,
                                     String userId, int itemType, int itemAmount) {

        BasicDBObject fields = getUserPublicReturnFields();
        User user = UserManager.findUserByUserId(mongoClient, userId, fields);
        if (user == null)
            return null;

        user.addItem(itemType, itemAmount);

        BasicDBObject query = new BasicDBObject("_id", user.getObjectId());

        BasicDBObject update = new BasicDBObject();
        BasicDBObject updateValue = new BasicDBObject();
        updateValue.put(DBConstants.F_ITEMS, user.getItemList());
        update.put("$set", updateValue);

        DBObject newObj = mongoClient.findAndModify(DBConstants.T_USER, query, update);
        log.info("<incItemAmount> query=" + query.toString() + ", update=" + update.toString());

        writeItemTransaction(mongoClient, userId, itemType, itemAmount);

        if (newObj != null) {
            return fillUserWithGroup(mongoClient, new User(newObj));
        } else {
            return user;
        }
    }

    public static int updateUserBalance(MongoDBClient mongoClient,
                                        String userId, int balance) {

        // step 1: update amount
        BasicDBObject query = new BasicDBObject();
        query.put(DBConstants.F_USERID, new ObjectId(userId));

        BasicDBObject update = new BasicDBObject();
        BasicDBObject updateValue = new BasicDBObject();
        updateValue.put(DBConstants.F_ACCOUNT_BALANCE, balance);
        update.put("$set", updateValue);

        DBObject fields = getUserPublicReturnFields();

        log.info("<updateUserBalance> query=" + query + ", update=" + update);
        DBObject obj = mongoClient.findAndModify(DBConstants.T_USER, query, update, fields);
        if (obj == null)
            return ErrorCode.ERROR_USERID_NOT_FOUND;

        // step 2: write transaction log
        writeTransaction(mongoClient, userId, balance, 0, null, null,
                TransactionType.UPDATE);

        return 0;
    }

    public static int updateUserIngotBalance(MongoDBClient mongoClient,
                                             String userId, int balance) {

        // step 1: update amount
        BasicDBObject query = new BasicDBObject();
        query.put(DBConstants.F_USERID, new ObjectId(userId));

        BasicDBObject update = new BasicDBObject();
        BasicDBObject updateValue = new BasicDBObject();
        updateValue.put(DBConstants.F_ACCOUNT_INGOT_BALANCE, balance);
        update.put("$set", updateValue);

//		BasicDBObject fields = new BasicDBObject();
//		fields.put(DBConstants.F_ACCOUNT_INGOT_BALANCE, 1);

        DBObject fields = getUserPublicReturnFields();

        log.info("<updateUserIngotBalance> query=" + query + ", update=" + update);
        DBObject obj = mongoClient.findAndModify(DBConstants.T_USER, query, update, fields);
        if (obj == null)
            return ErrorCode.ERROR_USERID_NOT_FOUND;

        // step 2: write transaction log
        writeIngotTransaction(mongoClient, userId, balance, 0, null, null,
                TransactionType.UPDATE);

        return 0;
    }

    public static void resetCount(MongoDBClient mongoClient, String userId,
                                  String field) {
        if (userId == null)
            return;
        DBObject query = new BasicDBObject(DBConstants.F_USERID, new ObjectId(
                userId));
        DBObject update = new BasicDBObject("$set", new BasicDBObject(field, 0));
        mongoClient.updateAll(DBConstants.T_USER, query, update);
    }

    private static void increaseCount(MongoDBClient mongoClient, String userId,
                                      String field) {
        if (userId == null)
            return;
        DBObject query = new BasicDBObject(DBConstants.F_USERID, new ObjectId(
                userId));
        DBObject update = new BasicDBObject("$inc", new BasicDBObject(field, 1));
        mongoClient.updateAll(DBConstants.T_USER, query, update);
    }

    private static void increaseCount(MongoDBClient mongoClient,
                                      List<Object> userIdList, String field) {
        if (userIdList == null || userIdList.isEmpty())
            return;
        DBObject query = new BasicDBObject(DBConstants.F_USERID,
                new BasicDBObject("$in", userIdList));
        DBObject update = new BasicDBObject("$inc", new BasicDBObject(field, 1));

        mongoClient.updateAll(DBConstants.T_USER, query, update);
    }

    public static List<User> searchUser(MongoDBClient mongoClient,
                                        String userId, String key, int offset, int limit) {
        List<User> userList = new ArrayList<User>();

		/* rem by Benson 2013-01-30 to avoid performance issue
		Pattern pattern = Pattern.compile(key, Pattern.CASE_INSENSITIVE);

		BasicDBList list = new BasicDBList();
		list.add(new BasicDBObject(DBConstants.F_NICKNAME, pattern));
		list.add(new BasicDBObject(DBConstants.F_QQ_NICKNAME, pattern));
		list.add(new BasicDBObject(DBConstants.F_SINA_NICKNAME, pattern));
		list.add(new BasicDBObject(DBConstants.F_EMAIL, pattern));
		list.add(new BasicDBObject(DBConstants.F_FACEBOOKID, pattern));
		list.add(new BasicDBObject(DBConstants.F_QQID, pattern));

		BasicDBObject query = new BasicDBObject();
		query.put("$or", list);
		*/

//		Pattern pattern = Pattern.compile(key, Pattern.CASE_INSENSITIVE);
//		BasicDBObject query = new BasicDBObject();
//		query.put(DBConstants.F_NICKNAME, pattern);

        BasicDBObject query = new BasicDBObject();
        query.put(DBConstants.F_NICKNAME, key);

//		DBObject order = new BasicDBObject();
//		order.put("_id", 1);

//		DBObject returnFields = new BasicDBObject();
//		returnFields.put(DBConstants.F_USERID, 1);
//		returnFields.put(DBConstants.F_EMAIL, 1);
//		returnFields.put(DBConstants.F_NICKNAME, 1);
//		returnFields.put(DBConstants.F_AVATAR, 1);
//		returnFields.put(DBConstants.F_QQID, 1);
//		returnFields.put(DBConstants.F_SINA_NICKNAME, 1);
//		returnFields.put(DBConstants.F_QQ_NICKNAME, 1);
//		returnFields.put(DBConstants.F_FACEBOOKID, 1);
//		returnFields.put(DBConstants.F_GENDER, 1);
//		returnFields.put(DBConstants.F_CREATE_DATE, 1);
//		returnFields.put(DBConstants.F_LOCATION, 1);
//		returnFields.put(DBConstants.F_LEVEL_INFO, 1);

        DBObject fields = getUserPublicReturnFields();

        DBCursor cursor = mongoClient.find(DBConstants.T_USER, query,
                fields, null, offset, limit);
        if (cursor != null) {
            while (cursor.hasNext()) {
                DBObject dbObject = (DBObject) cursor.next();
                User user = new User(dbObject);
                userList.add(user);
            }
            cursor.close();
        }
        return userList;
    }

    public static DBObject updateLevelAndExp(MongoDBClient mongoClient,
                                             String userId, String gameId, long exp, int level,
                                             boolean shouldCoverOldData, int type, long awardExp) {

        User user = UserManager.findUserByUserId(mongoClient, userId);
        DBObject resultDBObject = new BasicDBObject();

        if (user != null) {

            long currentExp = user.getExpByGameId(gameId);
            int currentLevel = user.getLevelByGameId(gameId);


            resultDBObject.put(DBConstants.F_CREATE_SOURCE_ID, gameId);
            resultDBObject.put(DBConstants.F_EXP, Math.max(currentExp, exp));
            resultDBObject.put(DBConstants.F_LEVEL, Math.max(currentLevel, level));

            BasicDBObject query = new BasicDBObject();
            query.put(DBConstants.F_USERID, new ObjectId(userId));
            query.put(DBConstants.F_LEVEL_INFO.concat(".").concat(
                    DBConstants.F_CREATE_SOURCE_ID), gameId);

            BasicDBObject update = new BasicDBObject();
            BasicDBObject updateValue = new BasicDBObject();
            updateValue.put(DBConstants.F_LEVEL_INFO.concat(".$.").concat(
                    DBConstants.F_EXP), exp);
            updateValue.put(DBConstants.F_LEVEL_INFO.concat(".$.").concat(
                    DBConstants.F_LEVEL), level);

            log.info("<updateLevelAndExp> gameId = " + gameId);
            updateValue.put(AbstractApp.gameUserRankFieldName(gameId), exp);

            update.put("$set", updateValue);
            log.info("<updateLevelAndExp> query=" + query.toString()
                    + ", update=" + update.toString());
            mongoClient.updateOne(DBConstants.T_USER, query, update);

            BasicDBObject userLevelObject = User.createUserLevelObject(gameId, level, exp);
            UserManager.updateUserByDBObject(mongoClient, userId, userLevelObject);

            if (type == ServiceConstant.CONST_SYNC_TYPE_AWARD) {
                // update award

                // decrease the guess balance.
                update = new BasicDBObject();
                DBObject incValue = new BasicDBObject();
                incValue.put(DBConstants.F_AWARD_EXP, -awardExp);
                update.put("$inc", incValue);

                DBObject queryForUpdate = new BasicDBObject();
                queryForUpdate.put("_id", new ObjectId(userId));
                log.info("<updateLevelAndExp> update award, query="
                        + query.toString() + ", update=" + update.toString());
                mongoClient.updateOne(DBConstants.T_USER, queryForUpdate,
                        update);
            }
        }

        return resultDBObject;

    }

    public static Set<User> getUserSet(MongoDBClient mongoClient, Collection<ObjectId> userIdSet) {
        BasicDBObject query = new BasicDBObject(DBConstants.F_USERID,
                new BasicDBObject("$in", userIdSet));
        DBCursor cursor = mongoClient.find(DBConstants.T_USER, query, getUserPublicReturnFields(), null, 0, 0);
        log.info("<getUserSet> query = " + query);
        if (cursor != null) {

            Set<User> userSet = new HashSet<User>();

            while (cursor.hasNext()) {
                DBObject dbObject = (DBObject) cursor.next();
                User user = new User(dbObject);
                userSet.add(user);
            }
            cursor.close();
            return userSet;
        }
        return Collections.emptySet();
    }

    public static List<User> getUserList(MongoDBClient mongoClient,
                                         List<ObjectId> userIdList) {
        BasicDBObject query = new BasicDBObject(DBConstants.F_USERID,
                new BasicDBObject("$in", userIdList));
        DBCursor cursor = mongoClient.find(DBConstants.T_USER, query, null, 0, 0);
        log.info("<getUserList> query = " + query);

        Map<ObjectId, User> map = new HashMap<ObjectId, User>();
        if (cursor != null) {
            while (cursor.hasNext()) {
                DBObject dbObject = (DBObject) cursor.next();
                User user = new User(dbObject);
                map.put(user.getObjectId(), user);
            }
            cursor.close();
        }

        // sort user by user id sequence
        List<User> userList = new ArrayList<User>();
        for (ObjectId uid : userIdList) {
            if (map.containsKey(uid)) {
                userList.add(map.get(uid));
            }
        }

        return userList;
    }

    public static String getNickNameByIdInList(List<User> friendUserList,
                                               String friendId) {
        if (friendUserList != null && !friendUserList.isEmpty()) {
            for (User user : friendUserList) {
                if (user.getUserId().equals(friendId)) {
                    if (user.getNickName() != null) {
                        return user.getNickName();
                    } else if (user.getSinaNickName() != null) {
                        return user.getSinaNickName();
                    } else if (user.getQQNickName() != null) {
                        return user.getQQNickName();
                    }
                }
            }
        }
        return null;
    }

    public static String getAvatarByIdInList(List<User> friendUserList,
                                             String friendId) {
        if (friendUserList != null && !friendUserList.isEmpty()) {
            for (User user : friendUserList) {
                if (user.getUserId().equals(friendId)) {
                    return user.getAvatar();
                }
            }
        }
        return null;
    }

    public static boolean getGenderByIdInList(List<User> friendUserList,
                                              String friendId) {
        if (friendUserList != null && !friendUserList.isEmpty()) {
            for (User user : friendUserList) {
                if (user.getUserId().equals(friendId)) {
                    return user.isMale();
                }
            }
        }
        return false;
    }

    public static HashMap<String, User> getUserMapByUserIdList(
            MongoDBClient mongoClient, List<ObjectId> userIdList) {
        BasicDBObject query = new BasicDBObject(DBConstants.F_USERID,
                new BasicDBObject("$in", userIdList));
        DBCursor cursor = mongoClient.find(DBConstants.T_USER, query, null, 0,
                0);
        log.info("<getUserList> query = " + query);
        HashMap<String, User> userMap = new HashMap<String, User>();

        if (cursor != null) {
            while (cursor.hasNext()) {
                DBObject dbObject = (DBObject) cursor.next();
                User user = new User(dbObject);
                userMap.put(user.getUserId(), user);
            }
            cursor.close();
        }

        return userMap;
    }

    public static HashMap<ObjectId, User> getUserMapByUserIdList(
            MongoDBClient mongoClient, Collection<?> uidCollection,
            DBObject returnFields) {
        BasicDBObject query = new BasicDBObject(DBConstants.F_USERID,
                new BasicDBObject("$in", uidCollection));
        DBCursor cursor = mongoClient.findByFieldInValues(DBConstants.T_USER,
                "_id", uidCollection, returnFields);
        log.info("<getUserList> query = " + query);
        HashMap<ObjectId, User> userMap = new HashMap<ObjectId, User>();

        if (cursor != null) {
            while (cursor.hasNext()) {
                DBObject dbObject = (DBObject) cursor.next();
                User user = new User(dbObject);
                userMap.put(user.getObjectId(), user);
            }
            cursor.close();
        }
        return userMap;
    }

    public static boolean completenessCheck(User user) {

        if (user.getNickName() == null || user.getNickName().isEmpty()) {
            return false;
        }
        return true;
    }

    /*
	public static long countUserAction(MongoDBClient mongoClient, String appId,
			int type) {
		DBObject query = new BasicDBObject();
		query.put(DBConstants.F_TYPE, type);
		return mongoClient.count(DBConstants.T_ACTION, query);
	}
	*/

    public static void updateMongoDBUserByDBObject(MongoDBClient mongoClient,
                                            String userId, DBObject obj) {

        if (StringUtil.isEmpty(userId)) {
            return;
        }

        if (obj.containsField(DBConstants.F_DEVICEID)) {
            String deviceId = (String) obj.get(DBConstants.F_DEVICEID);
            if (!User.isValidDevice(deviceId)) {
                obj.removeField(DBConstants.F_DEVICEID);
            }
        }

        DBObject query = new BasicDBObject(DBConstants.F_USERID, new ObjectId(userId));
        DBObject update = new BasicDBObject("$set", obj);

        log.info("<updateUserByDBObject> query = " + query + "update = " + update);
        mongoClient.updateOne(DBConstants.T_USER, query, update);
    }

    public static User updateUserByDBObject(final MongoDBClient mongoClient,
                                            final String userId,
                                            final DBObject obj) {

        if (StringUtil.isEmpty(userId)) {
            return null;
        }

        if (obj.containsField(DBConstants.F_DEVICEID)) {
            String deviceId = (String) obj.get(DBConstants.F_DEVICEID);
            if (!User.isValidDevice(deviceId)) {
                obj.removeField(DBConstants.F_DEVICEID);
            }
        }

        DBObject query = new BasicDBObject(DBConstants.F_USERID, new ObjectId(userId));
        DBObject update = new BasicDBObject("$set", obj);

        log.info("<updateUserByDBObject> query = " + query + "update = " + update);
        DBObject object = mongoClient.findAndModify(DBConstants.T_USER, query, update);

        if (object != null) {
            final User retUser = new User(object);
            if (retUser.hasFieldForSearch(obj)) {
//                DBService.getInstance().executeDBRequest(0, new Runnable() {
//                    @Override
//                    public void run() {
                        ElasticsearchService.addOrUpdateIndex(retUser, mongoClient);
//                    }
//                });

            }
            return retUser;
        } else
            return null;

    }

    public static Date getFeedTimeStamp(MongoDBClient mongoClient, String userId) {
        if (StringUtil.isEmpty(userId)) {
            return null;
        }
        DBObject query = new BasicDBObject();
        query.put(DBConstants.F_USERID, new ObjectId(userId));
        DBObject fields = new BasicDBObject();
        fields.put(DBConstants.F_FEED_TIMESTAMP, 1);
        DBObject result = mongoClient
                .findOne(DBConstants.T_USER, query, fields);
        if (result != null) {
            return (Date) result.get(DBConstants.F_FEED_TIMESTAMP);
        }
        return null;
    }

    public static void updateFeedTimestamp(MongoDBClient mongoClient,
                                           String userId, Date timestamp) {
        if (StringUtil.isEmpty(userId)) {
            return;
        }
        DBObject query = new BasicDBObject();
        query.put(DBConstants.F_USERID, new ObjectId(userId));
        DBObject set = new BasicDBObject();
        set.put(DBConstants.F_FEED_TIMESTAMP, timestamp);
        DBObject update = new BasicDBObject();
        update.put("$set", set);
        mongoClient.updateAll(DBConstants.T_USER, query, update);
    }

    // public static long getNewFanCount(MongoDBClient mongoClient, String
    // userId) {
    // User user = UserManager.findSimpleUserInfoByUserId(mongoClient, userId);
    // if (user != null) {
    // return user.getInt(DBConstants.F_NEW_FAN_COUNT);
    // }
    // return 0;
    // }
    public static long getNewFanCount(MongoDBClient mongoClient, String userId) {
        if (mongoClient == null || userId == null || userId.length() <= 0)
            return 0;
        DBObject query = new BasicDBObject();
        query.put(DBConstants.F_USERID, new ObjectId(userId));
        DBObject fields = new BasicDBObject();
        fields.put(DBConstants.F_NEW_FAN_COUNT, 1);
        DBObject obj = mongoClient.findOne(DBConstants.T_USER, query, fields);
        if (obj == null)
            return 0;
        User user = new User(obj);
        return user.getNewFanCount();
    }

    public static long countUser(MongoDBClient mongoClient, String appId) {
        return mongoClient.count(DBConstants.T_USER, new BasicDBObject());
    }

    // use incBalanceAndExpForAward to replace addGuessCoin
    @Deprecated
    public static void addGuessCoin(MongoDBClient mongoClient,
                                    String opusCreatorUid, int guessCorrectCoin) {

        log.info("<addGuessCoin>: uid = " + opusCreatorUid + " , coin = "
                + guessCorrectCoin);
        DBObject query = new BasicDBObject();
        query.put(DBConstants.F_USERID, new ObjectId(opusCreatorUid));

        DBObject update = new BasicDBObject();
        DBObject inc = new BasicDBObject();
        inc.put(DBConstants.F_GUESS_BALANCE, guessCorrectCoin);
        update.put("$inc", inc);
        mongoClient.updateOne(DBConstants.T_USER, query, update);
    }

    public static void incBalanceAndExpForAward(final MongoDBClient mongoClient,
                                                final String userId, final String appId, final int balance, final int exp) {

//		DBObject query = new BasicDBObject();
//		query.put(DBConstants.F_USERID, new ObjectId(userId));
//
//		DBObject update = new BasicDBObject();
//
//		DBObject inc1 = new BasicDBObject();
//		inc1.put(DBConstants.F_ACCOUNT_BALANCE, balance);
//		inc1.put(DBConstants.F_AWARD_EXP, exp);
//		update.put("$inc", inc1);
//
//		log.info("<incBalanceAndExpForAward> query=" + query.toString()
//				+ ", update=" + update.toString());
//		mongoClient.updateOne(DBConstants.T_USER, query, update);

        DBService.getInstance().executeDBRequest(0, new Runnable() {

            @Override
            public void run() {
                if (exp != 0) {
                    UserManager.increaseExperience(mongoClient, userId, App.getGameIdByAppId(appId), exp);
                }

                if (balance != 0) {
                    UserManager.chargeAccount(mongoClient, userId, balance, DBConstants.C_CHARGE_SOURCE_FLOWER, UserManager.BALANCE_TYPE_COINS);
                }
            }
        });

    }

    // leak check
    public static List<User> getTopPlayerList(MongoDBClient mongoClient,
                                              String gameId, String appId, int offset, int limit) {

//		DBObject returnFields = new BasicDBObject();
//		returnFields.put(DBConstants.F_NICKNAME, 1);
//		returnFields.put(DBConstants.F_AVATAR, 1);
//		returnFields.put(DBConstants.F_GENDER, 1);
//		returnFields.put(DBConstants.F_LEVEL_INFO, 1);

        DBObject returnFields = getUserPublicReturnFields();

        DBObject orderBy = new BasicDBObject();

        String rankField = AbstractApp.userLevelField(gameId) + "." + DBConstants.F_LEVEL;
        orderBy.put(rankField, -1);

//        orderBy.put(DBConstants.F_DRAW_RANK_SCORE, -1);
        DBCursor cursor = mongoClient.find(DBConstants.T_USER,
                new BasicDBObject(), returnFields, orderBy, offset, limit);
        if (cursor != null) {
            List<User> users = new ArrayList<User>();
            while (cursor.hasNext()) {
                DBObject object = cursor.next();
                User user = new User(object);
                users.add(user);
            }
            cursor.close();
            return users;
        }
        return null;
    }

    public static void updateUserDeviceInfo(MongoDBClient mongoClient,
                                            String userId, String deviceId, String version) {

        // step 1: update amount
        BasicDBObject query = new BasicDBObject();
        query.put(DBConstants.F_USERID, new ObjectId(userId));

        BasicDBObject update = new BasicDBObject();
        BasicDBObject updateValue = new BasicDBObject();
        updateValue.put(DBConstants.F_DEVICEID, deviceId);
        updateValue.put(DBConstants.F_VERSION, version);
        update.put("$set", updateValue);

        log
                .info("<updateUserDeviceInfo> query=" + query + ", update="
                        + update);
        mongoClient.updateOne(DBConstants.T_USER, query, update);
    }

    public static boolean verifyDuplicateAward(MongoDBClient mongoClient,
                                               String userId, int source, int amount) {

        if (source == DBConstants.C_CHARGE_SOURCE_APP_REWARD) {
            if (userId == null)
                return true;

            if (amount > 0) {
                BasicDBObject query = new BasicDBObject();
                query.put("user_id", userId);

                BasicDBObject update = new BasicDBObject();
                BasicDBObject incValue = new BasicDBObject();
                incValue.put(DBConstants.F_COUNT, 1);
                update.put("$inc", incValue);
                update.put("$set", new BasicDBObject("user_id", userId));

                DBObject userAwardCounter = mongoClient.findAndModifyUpsert(
                        DBConstants.T_USER_AWARD_COUNTER, query, update);
                if (userAwardCounter != null) {
                    int value = ((BasicDBObject) userAwardCounter)
                            .getInt(DBConstants.F_COUNT);
                    int MAX_DUPLIATE_VALUE = 15;
                    if (value > MAX_DUPLIATE_VALUE) {
                        log.info("<verifyDuplicateAward> exceed max value");
                        return true;
                    } else {
                        log.info("<verifyDuplicateAward> award counter="
                                + userAwardCounter.toString());
                    }
                }
            }
        }

        return false;
    }

    public static void incDrawToMeCount(MongoDBClient mongoClient, String targetUserId, AbstractXiaoji xiaoji) {

        if (xiaoji == null) {
            return;
        }

        increaseCount(mongoClient, targetUserId, xiaoji.getDrawToMeField()); // DBConstants.F_DRAWTOME_COUNT);
    }

    public static void incNewFanCount(MongoDBClient mongoClient, String userId) {
        increaseCount(mongoClient, userId, DBConstants.F_NEW_FAN_COUNT);
    }

    public static void incNewFanCount(MongoDBClient mongoClient,
                                      List<Object> userIdList) {
        increaseCount(mongoClient, userIdList, DBConstants.F_NEW_FAN_COUNT);
    }

    public static void inNewBBSActionCount(MongoDBClient mongoClient,
                                           List<Object> userIdList) {
        increaseCount(mongoClient, userIdList,
                DBConstants.F_NEW_BBSACTION_COUNT);
    }

    public static void inNewGroupActionCount(MongoDBClient mongoClient,
                                             List<Object> userIdList) {
        increaseCount(mongoClient, userIdList,
                DBConstants.F_NEW_GROUPACTION_COUNT);
    }

    public static void awardForClickBoard(MongoDBClient mongoClient,
                                          String boardId, String userId, String appId, String gameId,
                                          String source, int deviceType) {

        Board board = BoardManager.findBoard(mongoClient, boardId);
        if (board == null)
            return;

        int rewardAmount = board.getReward();
        if (rewardAmount <= 0)
            return;

        log.info("<awardForClickBoard> userId=" + userId + ", boardId="
                + boardId + ", award=" + rewardAmount);
        UserManager.chargeAccount(mongoClient, userId, rewardAmount,
                DBConstants.C_CHARGE_SOURCE_CLICK_BOARD, null, null);
    }

    public static void resetPushCounter(MongoDBClient mongoClient) {
        BasicDBObject query = new BasicDBObject();
        BasicDBObject update = new BasicDBObject();

        BasicDBObject updateValue = new BasicDBObject();
        updateValue.put(DBConstants.F_PUSH_COUNT, 0);
        updateValue.put(DBConstants.F_PUSH_DATE, null);
        update.put("$set", updateValue);

        log.info("<resetPushCounter> query = " + query.toString() + ", value="
                + update.toString());
        mongoClient.updateAll(DBConstants.T_USER_PUSH_INFO, query, update);
    }

    public static List<DBObject> findUsersByRange(MongoDBClient mongoClient,
                                                  int offset, int size) {

        DBCollection collection = mongoClient.getDb().getCollection(
                DBConstants.T_USER);
        DBCursor cursor = collection.find();

        if (cursor == null) {
            return Collections.emptyList();
        }
        List<DBObject> result = cursor.skip(offset).limit(size).toArray();
        cursor.close();

        return result;
    }

    public static int getTotalUsersCount(MongoDBClient mongoClient) {

        int count = 0;
        DBCollection collection = mongoClient.getDb().getCollection(
                DBConstants.T_USER);
        DBCursor cursor = collection.find();

        count = cursor.count();
        cursor.close();

        return count;
    }

    public static boolean isFakeTransaction(MongoDBClient mongoClient,
                                            String userId, int source, int amount, String transactionId,
                                            String transactionReceipt) {

        if (StringUtil.isEmpty(transactionId)
                || StringUtil.isEmpty(transactionReceipt))
            return false;

        BasicDBObject query = new BasicDBObject();
        query.put(DBConstants.F_TRANSACTION_RECEIPT, transactionReceipt);

        DBObject obj = mongoClient.findOne(
                DBConstants.T_TRANSACTION_RECEIPT_CHECK, query);
        if (obj != null) {
            log
                    .info("<isFakeTransaction> duplicate transaction recepition found for userId="
                            + userId);
            return true;
        }

        return false;
    }

    private static int getFakeTransactionReceiptLen() {
        String lenStr = System.getProperty("config.fake_receipt_len");
        int len = 0;
        if (StringUtil.isEmpty(lenStr)) {
            len = DEFAULT_FAKE_TRANSACTION_RECEIPT_LEN;
        } else {
            len = Integer.parseInt(lenStr);
        }

        return len;
    }

    public static boolean isFakeTransactionReceipt(String transactionReceipt) {
        if (StringUtil.isEmpty(transactionReceipt)) {
            return false;
        }

        if (transactionReceipt.length() <= FAKE_TRANSACTION_RECEIPT_LEN)
            return true;

        return false;
    }

    private static final long EXPIRE_TIME_FOR_REPORT = 20 * 60 * 1000;    // 20 minutes

    public static void blackUser(MongoDBClient mongoClient, String targetUserId, int days) {
        BasicDBObject obj = new BasicDBObject("_id", new ObjectId(targetUserId));
        if (days == 0) {
            obj.put(DBConstants.F_EXPIRE_DATE, DateUtil.dateFromString("20201231000000"));
        } else {
            obj.put(DBConstants.F_EXPIRE_DATE, new Date(System.currentTimeMillis() + (long)days * 24 * 60 * 60 * 1000));
        }
        log.info("<blackUser> "+obj.toString());
        mongoClient.insert(DBConstants.T_BLACK_USER, obj);
    }

    public static void blackDevice(MongoDBClient mongoClient, String deviceId, int days) {
        BasicDBObject obj = new BasicDBObject("_id", deviceId);
        if (days == 0) {
            obj.put(DBConstants.F_EXPIRE_DATE, DateUtil.dateFromString("20201231000000"));
        } else {
            obj.put(DBConstants.F_EXPIRE_DATE, new Date(System.currentTimeMillis() + (long)days * 24 * 60 * 60 * 1000));
        }
        log.info("<blackDevice> "+obj.toString());
        mongoClient.insert(DBConstants.T_BLACK_DEVICE, obj);
    }

    public static void unblackUser(MongoDBClient mongoClient, String targetUserId) {
        BasicDBObject query = new BasicDBObject("_id", new ObjectId(targetUserId));
        mongoClient.remove(DBConstants.T_BLACK_USER, query);
    }

    public static void unblackDevice(MongoDBClient mongoClient, String deviceId) {
        BasicDBObject query = new BasicDBObject("_id", deviceId);
        mongoClient.remove(DBConstants.T_BLACK_DEVICE, query);
    }

    public static boolean detectChargeBySuperAdmin(MongoDBClient mongoClient,
                                                   String userId, String adminUserId, int source) {

        if (source != DBConstants.C_CHARGE_SOURCE_BY_ADMIN) {
            return true;
        }

        if (!BBSPrivilegeManager.isSuperAdmin(mongoClient, adminUserId))
            return false;

        return true;
    }

    public static User chargeAccount(MongoDBClient mongoClient, String userId,
                                     int amount, int source, int balanceType) {

        if (balanceType == BALANCE_TYPE_COINS) {
            return chargeAccount(mongoClient, userId, amount, source, null, null);
        } else {
            return chargeIngotAccount(mongoClient, userId, amount, source, null, null);
        }
    }

    public static int updateUserAvatar(MongoDBClient mongoClient, String userId, String url) {
        BasicDBObject obj = new BasicDBObject(DBConstants.F_AVATAR, url);
        User user = updateUserByDBObject(mongoClient, userId, obj);
        if (user == null)
            return ErrorCode.ERROR_USERID_NOT_FOUND;

        // add user background into user photo list
        UserPhotoManager.getInstance().addUserPhoto(mongoClient, userId, url, UserPhoto.TYPE_AVATAR, UserPhoto.RELATIVE_PATH);

        return ErrorCode.ERROR_SUCCESS;
    }

    public static int updateUserBackground(MongoDBClient mongoClient, String userId, String localPath) {
        BasicDBObject obj = new BasicDBObject(DBConstants.F_BACKGROUND, localPath);
        User user = updateUserByDBObject(mongoClient, userId, obj);
        if (user == null)
            return ErrorCode.ERROR_USERID_NOT_FOUND;

        // add user background into user photo list
        UserPhotoManager.getInstance().addUserPhoto(mongoClient, userId, localPath, UserPhoto.TYPE_BACKGROUND, UserPhoto.RELATIVE_PATH);

        return ErrorCode.ERROR_SUCCESS;
    }

    public static User updateUserByPB(MongoDBClient mongoClient, String userId,
                                      PBGameUser pbUser) {
        log.info("<updateUserByPB> userId=" + userId + ", pbUser=" + pbUser.toString());
        if (userId == null || pbUser == null)
            return null;

        BasicDBObject obj = new BasicDBObject();

        if (pbUser.hasPassword()) {
            obj.put(DBConstants.F_PASSWORD, pbUser.getPassword());
        }

        if (pbUser.hasNickName()) {
            obj.put(DBConstants.F_NICKNAME, pbUser.getNickName());
            log.info("nickName = " + pbUser.getNickName());
        }

        if (pbUser.hasGender()) {
            obj.put(DBConstants.F_GENDER, User.genderFromBool(pbUser.getGender()));
            log.info("gender = " + pbUser.getGender());
        }

        if (pbUser.hasDeviceId()) {
            obj.put(DBConstants.F_DEVICEID, pbUser.getDeviceId());
        }

        if (pbUser.hasDeviceOS()) {
            obj.put(DBConstants.F_DEVICEOS, pbUser.getDeviceOS());
        }

        if (pbUser.hasDeviceModel()) {
            obj.put(DBConstants.F_DEVICEMODEL, pbUser.getDeviceModel());
        }

        if (pbUser.hasDeviceToken()) {
            obj.put(DBConstants.F_DEVICETOKEN, pbUser.getDeviceToken());
        }


        if (pbUser.hasLocation()) {
            obj.put(DBConstants.F_LOCATION, pbUser.getLocation());
        }

        if (pbUser.hasEmail()) {
            obj.put(DBConstants.F_EMAIL, pbUser.getEmail());
        }

        if (pbUser.hasIsJailBroken()) {
            obj.put(DBConstants.F_IS_JAILBROKEN, pbUser.getIsJailBroken());
        }

        if (pbUser.hasLongitude() && pbUser.hasLatitude()) {
            obj.put(DBConstants.F_LONGITUDE, pbUser.getLongitude());
            obj.put(DBConstants.F_LATITUDE, pbUser.getLatitude());
        }

//		sinaId = request.getParameter(ServiceConstant.PARA_SINA_ID);
//		sinaNickName = request.getParameter(ServiceConstant.PARA_SINA_NICKNAME);
//		sinaToken = request.getParameter(ServiceConstant.PARA_SINA_ACCESS_TOKEN);
//		sinaTokenSecret = request.getParameter(ServiceConstant.PARA_SINA_ACCESS_TOKEN_SECRET);
//		qqId = request.getParameter(ServiceConstant.PARA_QQ_ID);
//		qqNickName = request.getParameter(ServiceConstant.PARA_QQ_NICKNAME);
//		qqToken = request.getParameter(ServiceConstant.PARA_QQ_ACCESS_TOKEN);
//		qqTokenSecret = request.getParameter(ServiceConstant.PARA_QQ_ACCESS_TOKEN_SECRET);
//		facebookId = request.getParameter(ServiceConstant.PARA_FACEBOOKID);
//

//		source = request.getParameter(ServiceConstant.PARA_SOURCE);
//		newAppId = request.getParameter(ServiceConstant.PARA_NEW_APPID);

        if (pbUser.hasCountryCode()) {
            obj.put(DBConstants.F_COUNTRYCODE, pbUser.getCountryCode());
        }

        if (pbUser.hasLanguage()) {
            obj.put(DBConstants.F_LANGUAGE, pbUser.getLanguage());
        }

        if (pbUser.hasDeviceType()) {
            obj.put(DBConstants.F_DEVICE_TYPE, Integer.valueOf(pbUser.getDeviceType()).intValue());
        }

        if (pbUser.hasSignature()) {
            obj.put(DBConstants.F_SIGNATURE, pbUser.getSignature());
            log.info("signature = " + pbUser.getSignature());
        }

        if (pbUser.hasBirthday()) {
            obj.put(DBConstants.F_BIRTHDAY, pbUser.getBirthday());
        }

        if (pbUser.hasZodiac()) {
            obj.put(DBConstants.F_ZODIAC, pbUser.getZodiac());
        }

        if (pbUser.hasGuessWordLanguage()) {
            obj.put(DBConstants.F_GUESS_WORD_LANGUAGE, pbUser.getGuessWordLanguage());
        }

        if (pbUser.hasBloodGroup()) {
            obj.put(DBConstants.F_BLOOD, pbUser.getBloodGroup());
        }

        if (pbUser.hasOpenInfoType()) {
            obj.put(DBConstants.F_OPEN_INFO_TYPE, pbUser.getOpenInfoType().getNumber());
        }

        if (pbUser.hasSingRecordLimit()) {
            obj.put(DBConstants.F_SING_RECORD_LIMIT, pbUser.getSingRecordLimit());
        }

        if (pbUser.getSnsCredentialsCount() > 0) {
            for (int i = 0; i < pbUser.getSnsCredentialsCount(); i++) {
                GameBasicProtos.PBSNSUserCredential userCredential = pbUser.getSnsCredentials(i);
                if (userCredential != null) {
                    String key = User.getSNSCredentialKey(userCredential.getType());
                    if (key != null) {
                        obj.put(key, userCredential.getCredential());
                    }
                }
            }
        }

        return updateUserByDBObject(mongoClient, userId, obj);
    }

    public static User increaseExperience(MongoDBClient mongoClient,
                                          String userId, String gameId, int addExp) {

        if (userId == null || gameId == null || addExp == 0)
            return null;


        DBObject userQuery = new BasicDBObject();
        userQuery.put(DBConstants.F_USERID, new ObjectId(userId));
        DBObject fields = getUserPublicReturnFields();
        DBObject obj = mongoClient.findOne(DBConstants.T_USER, userQuery, fields);
        if (obj == null)
            return null;

        User user = new User(obj);

        int currentLevel = user.getLevelByGameId(gameId);
        long currentExp = user.getExpByGameId(gameId);

        long newExp = currentExp + addExp;
        int newLevel = LevelUtils.getLevelByExp(newExp);
        int addLevel = newLevel - currentLevel;
        if (addLevel < 0) {
            addLevel = 0;
        }

        log.info("<increaseExperience> user " + user.getNickName() + " userId " + userId + " exp=" + newExp + " level=" + newLevel);

        // update new level and experience
		/*
		BasicDBObject query = new BasicDBObject();
		query.put(DBConstants.F_USERID, new ObjectId(userId));
		query.put(DBConstants.F_LEVEL_INFO.concat(".").concat(DBConstants.F_CREATE_SOURCE_ID), gameId);

		BasicDBObject update = new BasicDBObject();
		BasicDBObject updateValue = new BasicDBObject();
		updateValue.put(DBConstants.F_LEVEL_INFO.concat(".$.").concat(DBConstants.F_EXP), newExp);
		updateValue.put(DBConstants.F_LEVEL_INFO.concat(".$.").concat(DBConstants.F_LEVEL), newLevel);
		*/

        BasicDBObject query = new BasicDBObject();
        query.put(DBConstants.F_USERID, new ObjectId(userId));

        BasicDBObject updateValue = new BasicDBObject();
        updateValue.put(AbstractApp.gameUserRankFieldName(gameId), newExp);

        BasicDBObject incValue = new BasicDBObject();
        String levelField = AbstractApp.userLevelField(gameId);
        incValue.put(levelField + "." + DBConstants.F_LEVEL, addLevel);
        incValue.put(levelField + "." + DBConstants.F_EXP_NEW, addExp);

        BasicDBObject update = new BasicDBObject();
        update.put("$set", updateValue);
        update.put("$inc", incValue);

        log.info("<increaseExperience> query=" + query.toString() + ", update=" + update.toString());
        DBObject newObj = mongoClient.findAndModify(DBConstants.T_USER, query, update, fields);
        return fillUserWithGroup(mongoClient, new User(newObj));
    }

    // 在ElasticSearch中搜索用户，然后用搜索结果取得的userId在mongodb中查询结果并返回。
    public static List<User> searchUserFromES(MongoDBClient mongoClient,
                                              String userId, String keyString, int start, int offset) {

        List<User> result = new ArrayList<User>();

        // 在ES中进行多字段查找,目前查找的字段包括:
        // nick_name, email ,sina_nick,qq_nick, sina_id, qq_id, facebook_id, signature;
        List<String> candidateFields = new ArrayList<String>();
        candidateFields.add(DBConstants.ES_NICK_NAME);
        candidateFields.add(DBConstants.ES_EMAIL);
        candidateFields.add(DBConstants.ES_SINA_NICK);
        candidateFields.add(DBConstants.ES_QQ_NICK);
        candidateFields.add(DBConstants.ES_SINA_ID);
        candidateFields.add(DBConstants.ES_QQ_ID);
        candidateFields.add(DBConstants.ES_FACEBOOK_ID);
        candidateFields.add(DBConstants.ES_SIGNATURE);
        candidateFields.add(DBConstants.ES_XIAOJI_NUMBER);
        candidateFields.add(DBConstants.ES_USER_ID);

        List<ObjectId> uidList = ElasticsearchService.search(keyString, candidateFields, DBConstants.ES_USER_ID, start, offset, DBConstants.ES_INDEX_TYPE_USER);
        return findPublicUserInfoByUserIdList(mongoClient, userId, uidList, false);
    }

    public static void main(String[] args) {

        MongoDBClient mongoClient = new MongoDBClient(DBConstants.D_GAME);
        String keyStringToSearch = "皮皮";
        int start = 0;
        int offset = 30;
        List<User> result = searchUserFromES(mongoClient, null, keyStringToSearch, start, offset);
        ServerLog.info(0, "" + result.size());
        for (User user : result) {
            ServerLog.info(0, user.getUserId() + ", nick_name: " + user.getNickName() + ", sina_nick: "
                    + user.getSinaNickName() + ", qq_nick: " + user.getQQNickName());
        }
    }

    public static void writeAlipayTransaction(String userId, String appId,
                                              String gameId, String payAmount,
                                              String payDesc, String outTradeNo,
                                              int chargeType, int chargeValue,
                                              String productId) {

        BasicDBObject trans = new BasicDBObject();
        trans.put("_id", outTradeNo);
        trans.put(DBConstants.F_UID, userId);
        trans.put(DBConstants.F_APPID, appId);
        trans.put(DBConstants.F_GAME_ID, gameId);
        trans.put(DBConstants.F_AMOUNT, payAmount);
        trans.put(DBConstants.F_DESC, payDesc);
        trans.put(DBConstants.F_STATUS, DBConstants.PAYMENT_SENT);
        trans.put(DBConstants.F_STATUS_INFO, null);
        trans.put(DBConstants.F_TYPE, chargeType);
        trans.put(DBConstants.F_VALUE, chargeValue);
        trans.put(DBConstants.F_PRODUCTID, productId);

        trans.put(DBConstants.F_CREATE_DATE, new Date());

        log.info("<writeAlipayTransaction> " + trans.toString());
        DBService.getInstance().getMongoDBClient().insert(DBConstants.T_ALIPAY_TRANSACTION, trans);
    }

    public static void updateAlipayTransactionResult(String outTradeNo, String result) {

        BasicDBObject query = new BasicDBObject();
        query.put("_id", outTradeNo);

        BasicDBObject updateValue = new BasicDBObject();
        updateValue.put(DBConstants.F_STATUS, (result != null && result.equalsIgnoreCase("success")) ? DBConstants.PAYMENT_SUCCESS : DBConstants.PAYMENT_FAIL);
        updateValue.put(DBConstants.F_STATUS_INFO, result);
        updateValue.put(DBConstants.F_MODIFY_DATE, new Date());

        BasicDBObject update = new BasicDBObject();
        update.put("$set", updateValue);

        DBService.getInstance().getMongoDBClient().updateAll(DBConstants.T_ALIPAY_TRANSACTION, query, update);
    }

    public static void commitAlipayTransaction(String outTradeNo, String status, String tradeNo, String payAmount, String buyerEmail, String notify_data) {

        BasicDBObject query = new BasicDBObject();
        query.put("_id", outTradeNo);

        BasicDBObject obj = (BasicDBObject) DBService.getInstance().getMongoDBClient().findOne(DBConstants.T_ALIPAY_TRANSACTION, query);
        if (obj == null) {
            log.warn("<commitAlipayTransaction> but outTradeNo " + outTradeNo + " not found in DB");
            return;
        }

        boolean result = (status != null && status.equalsIgnoreCase("TRADE_FINISHED"));
        String productId = obj.getString(DBConstants.F_PRODUCTID);
        String appId = obj.getString(DBConstants.F_APPID);

        // charge user
        if (result) {
            String userId = obj.getString(DBConstants.F_UID);
            int chargeType = obj.getInt(DBConstants.F_TYPE);
            int chargeValue = obj.getInt(DBConstants.F_VALUE);

            if (productId != null && productId.equalsIgnoreCase(ServiceConstant.PRODUCT_BUY_VIP)) {
                User user = VipService.getInstance().purchaseVipService(userId, chargeType, chargeValue);
                VipService.getInstance().sendPurcahseVipMessage(user, appId);
            }
            else{
                int balanceType = 0;
                if (chargeType == GameBasicProtos.PBIAPProductType.IAPCoin_VALUE) {
                    balanceType = BALANCE_TYPE_COINS;
                } else {
                    balanceType = BALANCE_TYPE_INGOT;
                }

                UserManager.chargeAccount(DBService.getInstance().getMongoDBClient(), userId, chargeValue, DBConstants.C_CHARGE_SOURCE_ALIPAY_WEB, balanceType);
            }
        }

        // update transaction status
        BasicDBObject updateValue = new BasicDBObject();
        updateValue.put(DBConstants.F_STATUS, result ? DBConstants.PAYMENT_SUCCESS : DBConstants.PAYMENT_FAIL);
        updateValue.put(DBConstants.F_STATUS_INFO, status);
        updateValue.put(DBConstants.F_MODIFY_DATE, new Date());
        updateValue.put(DBConstants.F_TRANSACTION_RECEIPT, notify_data);
        updateValue.put(DBConstants.F_BUYER_EMAIL, buyerEmail);
        updateValue.put(DBConstants.F_TRANSACTION_ID, tradeNo);

        BasicDBObject update = new BasicDBObject();
        update.put("$set", updateValue);
        DBService.getInstance().getMongoDBClient().updateAll(DBConstants.T_ALIPAY_TRANSACTION, query, update);
    }

    public static User addAppForAward(String userId, String awardAppId, int amount) {

        BasicDBObject update = new BasicDBObject();
        BasicDBObject pushValue = new BasicDBObject(DBConstants.F_AWARD_APP_LIST, awardAppId);
        update.put("$addToSet", pushValue);
        update.put("$inc", new BasicDBObject(DBConstants.F_ACCOUNT_BALANCE, amount));

        BasicDBObject query = new BasicDBObject("_id", new ObjectId(userId));
        log.info("<addAppForAward> query=" + query.toString() + ", update=" + update.toString());
        DBObject obj = DBService.getInstance().getMongoDBClient().findAndModify(DBConstants.T_USER, query, update);
        if (obj == null)
            return null;

        User user = new User(obj);
        writeAwardAppLog(userId, awardAppId, amount);
        return user;
    }

    private static void writeAwardAppLog(String userId, String awardAppId, int amount) {
        BasicDBObject log = new BasicDBObject();
        log.put(DBConstants.F_UID, userId);
        log.put(DBConstants.F_APPID, awardAppId);
        log.put(DBConstants.F_AMOUNT, amount);
        log.put(DBConstants.F_CREATE_DATE, new Date());
        DBService.getInstance().getMongoDBClient().insert(DBConstants.T_HISTORY_AWARD_APP, log);
    }

    public static void autoFixUserData(final MongoDBClient mongoClient, final User user) {

        if (user == null)
            return;

        DBService.getInstance().executeDBRequest(2, new Runnable() {

            @Override
            public void run() {
                List<BasicDBObject> devices = user.getNoDuplicateDeviceInfo();
                List<String> apps = user.getAppIdList();
                List<String> awardApps = user.getAwardAppIdList();
                List<String> tokens = user.getDeviceTokens();

                Set<String> finalApps = new HashSet<String>();
                Set<String> finalTokens = new HashSet<String>();
                finalApps.addAll(apps);
                finalApps.addAll(awardApps);

                finalTokens.addAll(tokens);
                for (BasicDBObject device : devices) {
                    String token = device.getString(DBConstants.F_DEVICETOKEN);
                    if (!StringUtil.isEmpty(token)) {
                        finalTokens.add(token);
                    }
                }

                BasicDBObject update = new BasicDBObject();
                update.put(DBConstants.F_DEVICES, devices);
                update.put(DBConstants.F_AWARD_APP_LIST, finalApps);
                update.put(DBConstants.F_DEVICETOKEN_LIST, finalTokens);

                UserManager.updateUserByDBObject(mongoClient, user.getUserId(), update);
            }
        });

    }

    public static User setUserGroupMessageNotice(String userId, String groupId, boolean groupMessageNoticeOn) {

        if (StringUtil.isEmpty(userId) || StringUtil.isEmpty(groupId)) {
            return null;
        }

        BasicDBObject query = new BasicDBObject("_id", new ObjectId(userId));
        BasicDBObject update = new BasicDBObject();

        if (groupMessageNoticeOn) {
            update.put("$pull", new BasicDBObject(DBConstants.F_OFF_GROUPS, groupId));
        } else {
            update.put("$addToSet", new BasicDBObject(DBConstants.F_OFF_GROUPS, groupId));
        }

        log.info("<setUserGroupMessageNotice> query=" + query.toString() + ", update=" + update.toString());
        DBObject obj = DBService.getInstance().getMongoDBClient().findAndModify(DBConstants.T_USER, query, update);
        if (obj == null)
            return null;
        else
            return new User(obj);

    }

    public static boolean canBlackUser(String userId) {

        if (BBSPrivilegeManager.isSuperAdmin(mongoClient, userId)){
            return true;
        }

        User user = findPublicUserInfoByUserId(userId);
        if (user == null){
            return false;
        }

        return user.canBlackUser();
    }

    public static void removeUserDeviceToken(String deviceToken) {
        BasicDBObject obj = new BasicDBObject("_id", deviceToken);
        mongoClient.insert(DBConstants.T_EXPIRE_TOKENS, obj);
    }

    public static void addMainDeviceToken(String userId, String deviceToken) {

        if (StringUtil.isEmpty(userId) || StringUtil.isEmpty(deviceToken)){
            return;
        }

        BasicDBObject obj = new BasicDBObject(DBConstants.F_DEVICETOKEN, deviceToken);
        BasicDBObject update = new BasicDBObject("$set", obj);

        BasicDBObject addToSet = new BasicDBObject(DBConstants.F_DEVICETOKEN_LIST, deviceToken);
        update.put("$addToSet", addToSet);

        BasicDBObject query = new BasicDBObject("_id", new ObjectId(userId));
        log.info("<addMainDeviceToken> query="+query.toString()+", update="+update.toString());
        mongoClient.updateAll(DBConstants.T_USER, query, update);

    }

    public static void replaceMainDeviceToken(String userId, String deviceToken, String oldDeviceToken) {

        if (StringUtil.isEmpty(userId) || StringUtil.isEmpty(deviceToken)){
            return;
        }

        BasicDBList list = new BasicDBList();
        list.add(deviceToken);
        if (!StringUtil.isEmpty(oldDeviceToken)){
            list.add(oldDeviceToken);
        }

        BasicDBObject obj = new BasicDBObject(DBConstants.F_DEVICETOKEN, deviceToken);
        BasicDBObject update = new BasicDBObject("$set", obj);

        BasicDBObject each = new BasicDBObject("$each", list);
        BasicDBObject addToSet = new BasicDBObject(DBConstants.F_DEVICETOKEN_LIST, each);
        update.put("$addToSet", addToSet);

        BasicDBObject query = new BasicDBObject("_id", new ObjectId(userId));
        log.info("<replaceMainDeviceToken> query="+query.toString()+", update="+update.toString());
        mongoClient.updateAll(DBConstants.T_USER, query, update);

    }


    enum TransactionType {
        CHARGE, DEDUCT, UPDATE
    }
}
