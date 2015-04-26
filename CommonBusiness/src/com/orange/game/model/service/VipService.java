package com.orange.game.model.service;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.orange.common.redis.RedisClient;
import com.orange.common.scheduler.ScheduleService;
import com.orange.common.utils.DateUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.constants.ErrorCode;
import com.orange.game.model.dao.User;
import com.orange.game.model.manager.CommonManager;
import com.orange.game.model.manager.MessageManager;
import com.orange.game.model.manager.UserManager;
import com.orange.game.model.manager.user.VipTopUserManager;
import com.orange.game.model.utiils.DataUtils;
import org.apache.commons.lang.time.DateUtils;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 14-1-23
 * Time: 下午4:56
 * To change this template use File | Settings | File Templates.
 */
public class VipService extends CommonManager {

    public static final int TYPE_MONTH = 1;
    public static final int TYPE_YEAR = 2;

    public static final int VIP_FLAG = 1;
    private static final String STAT_DATA_KEY = "stat_data";
    private static final String MAP_KEY_BUY_VIP_USER_COUNT = "buy_vip_user_count";

    private static final String REDIS_KEY_VIP_CONFIG = "vip_config";
    private static final String REDIS_KEY_VIP_LEFT = "vip_left";
    private static final String OPEN_WEEK_DAY = "open_week_day";
    private static final Object MAP_KEY_MONTH = "vip_month_";
    private static final Object MAP_KEY_YEAR = "vip_year_";
    private static final int INIT_LEFT_COUNT = 100;
    private static final int VIP_MONTH_INIT_LEFT_COUNT = 8;
    private static final int VIP_YEAR_INIT_LEFT_COUNT = 100;

    private static VipService ourInstance = new VipService();

    public static VipService getInstance() {
        return ourInstance;
    }

    private VipService() {

//        int TRY_TIMES = 3;
//        for (int i=0; i<TRY_TIMES; i++){
//            ScheduleService.getInstance().scheduleEveryMonth(i, new Runnable() {
//                @Override
//                public void run() {
//                    chargeCoinsForVipUser();
//                }
//            });
//        }
    }

    private void chargeCoinsForVipUser() {

        BasicDBObject query = new BasicDBObject(DBConstants.F_VIP, DBConstants.C_VIP_FLAG);

        BasicDBObject returnFields = new BasicDBObject();
        returnFields.put(DBConstants.F_VIP, 1);
        returnFields.put(DBConstants.F_VIP_EXPIRE_DATE, 1);
        returnFields.put(DBConstants.F_VIP_MONTHLY_CHARGE, 1);

        log.info("<chargeCoinsForVipUser> for VIP users starting...");
        DBCursor cursor = mongoClient.find(DBConstants.T_USER, query, null, returnFields, 0, Integer.MAX_VALUE);
        if (cursor == null)
            return;

        int count = 0;
        while (cursor.hasNext()){
            BasicDBObject obj = (BasicDBObject)cursor.next();
            User user = new User(obj);
            if (user.isVip() && !user.hasChargeVipCoin()){
                // charge coins
                // update flag
                UserManager.chargeAccount(mongoClient, user.getUserId(), DBConstants.C_VIP_MONTHLY_CHARGE, DBConstants.C_CHARGE_SOURCE_VIP_MONTHLY, UserManager.BALANCE_TYPE_COINS);
                count ++;
            }
        }
        log.info("<chargeCoinsForVipUser> for VIP users end... total "+count+" user charged");
        cursor.close();
    }


    public User purchaseVipService(String userId, int type, int monthCount) {

        User user = UserManager.findPublicUserInfoByUserId(userId);
        if (user == null){
            return null;
        }

        Date startDate = new Date();
        Date expireDate = null;
        Date now = new Date();
        if (type == TYPE_YEAR){
            monthCount = 12; // one year is 12 months
        }

        BasicDBObject updateObj = new BasicDBObject();
        if (!user.isVip()){

            expireDate = DateUtils.addMonths(startDate, monthCount);

            updateObj.put(DBConstants.F_VIP, DBConstants.C_VIP_FLAG);
            updateObj.put(DBConstants.F_VIP_EXPIRE_DATE, expireDate);
            updateObj.put(DBConstants.F_VIP_LAST_PAY_DATE, now);
        }
        else{

            // already is VIP
            startDate = user.getVipExpireDate();
            expireDate = DateUtils.addMonths(startDate, monthCount);
            updateObj.put(DBConstants.F_VIP_EXPIRE_DATE, expireDate);
            updateObj.put(DBConstants.F_VIP_LAST_PAY_DATE, now);
        }

        // charge user account
        int amount = DBConstants.C_VIP_MONTHLY_CHARGE * monthCount;
        UserManager.chargeAccount(mongoClient, user.getUserId(), amount, DBConstants.C_CHARGE_SOURCE_VIP_MONTHLY, UserManager.BALANCE_TYPE_COINS);

        // update user info
        user = UserManager.updateUserByDBObject(mongoClient, userId, updateObj);

        // write history
        writeVipPaymentHistory(user, updateObj, type, monthCount);

        // inc buy count
        RedisClient.getInstance().hinc(STAT_DATA_KEY, MAP_KEY_BUY_VIP_USER_COUNT, 1);

        VipTopUserManager.getInstance().updateVipUser(userId, user.getIntVipLastPayDate(), user.getIntVipExpireDate());

        if (type == TYPE_MONTH){
            decMonthVipLeft();
        }
        else{
            decYearVipLeft();
        }

        return user;
    }

    private void writeVipPaymentHistory(User user, BasicDBObject updateObj, int type, int monthCount) {

        BasicDBObject obj = new BasicDBObject();
        obj.put(DBConstants.F_UID, user.getUserId());
        obj.put(DBConstants.F_NICKNAME, user.getNickName());
        obj.put(DBConstants.F_XIAOJI_NUMBER, user.getXiaojiNumber());
        obj.put(DBConstants.F_TYPE, type);
        obj.put(DBConstants.F_COUNT, monthCount);
        obj.put(DBConstants.F_DATE, new Date());
        obj.putAll(updateObj.toMap());

        mongoClient.insert(DBConstants.T_VIP_PAY_HISTORY, obj);
    }

    public int getBuyVipUserCount() {
        String value = RedisClient.getInstance().hget(STAT_DATA_KEY, MAP_KEY_BUY_VIP_USER_COUNT);
        return Integer.parseInt(value);
    }

    public void sendPurcahseVipMessage(User user, String appId) {
        if (user == null)
            return;

        String date = DateUtil.dateToChineseStringByFormat(user.getVipExpireDate(), "yyyy-MM-dd");
        String message = "恭喜你已经成功购买了VIP会员服务，会员有效期直至"+date+"，非常感谢对小吉的热心支持，我们会更加努力的，也欢迎提出宝贵意见！（本消息为系统消息，请勿回复）";
        MessageManager.sendSystemMessage(mongoClient, user.getUserId(), message, appId, true);
    }

    public int getVipOpenWeekDay() {

        String openWeekDay = RedisClient.getInstance().hget(REDIS_KEY_VIP_CONFIG, OPEN_WEEK_DAY);
        if (openWeekDay == null){
            return -1;
        }
        else{
            return Integer.parseInt(openWeekDay);
        }
    }

    public Date getVipNextOpenDate() {

        Date now = new Date();
        int nowWeekDay = DateUtil.getWeekday(now);

        int vipOpenWeekDay = getVipOpenWeekDay();
        if (vipOpenWeekDay == -1){
            return now;
        }

        if (nowWeekDay > vipOpenWeekDay){
            int addDays = 7 - (nowWeekDay - vipOpenWeekDay);
            Date today = DateUtil.getDateOfToday();
            return DateUtils.addDays(today, addDays);
        }
        else if (nowWeekDay == vipOpenWeekDay){
            // same date
            return DateUtil.getDateOfToday();
        }
        else{
            int addDays = vipOpenWeekDay - nowWeekDay;
            Date today = DateUtil.getDateOfToday();
            return DateUtils.addDays(today, addDays);
        }
    }

    public int getVipLeft(String field, int defaultLeft){
        String left = RedisClient.getInstance().hget(REDIS_KEY_VIP_LEFT, field);
        if (left == null){
            return RedisClient.getInstance().hinc(REDIS_KEY_VIP_LEFT, field, defaultLeft);
        }

        return Integer.parseInt(left);
    }

    public int decVipLeft(String field){
        int value = RedisClient.getInstance().hinc(REDIS_KEY_VIP_LEFT, field, -1);
        return value;
    }

    public int decMonthVipLeft(){
        Date now = new Date();
        String field = MAP_KEY_MONTH + DateUtil.dateToChineseStringByFormat(now, "yyyyMMdd");
        return decVipLeft(field);
    }

    public int decYearVipLeft(){
        Date now = new Date();
        String field = MAP_KEY_YEAR + DateUtil.dateToChineseStringByFormat(now, "yyyyMMdd");
        return decVipLeft(field);
    }

    public int getMonthVipLeft() {
        Date now = new Date();
        String field = MAP_KEY_MONTH + DateUtil.dateToChineseStringByFormat(now, "yyyyMMdd");
        return getVipLeft(field, VIP_MONTH_INIT_LEFT_COUNT);
    }

    public int getYearVipLeft() {
        Date now = new Date();
        String field = MAP_KEY_YEAR + DateUtil.dateToChineseStringByFormat(now, "yyyyMMdd");
        return getVipLeft(field, VIP_YEAR_INIT_LEFT_COUNT);
    }

}
