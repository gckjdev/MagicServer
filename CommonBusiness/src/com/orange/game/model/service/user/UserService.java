package com.orange.game.model.service.user;

import com.orange.common.mongodb.MongoDBClient;
import com.orange.common.scheduler.ScheduleService;
import com.orange.common.utils.PropertyUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.User;
import com.orange.game.model.manager.MessageManager;
import com.orange.game.model.manager.UserManager;
import com.orange.game.model.service.DBService;
import com.orange.game.model.xiaoji.AbstractXiaoji;
import com.orange.game.model.xiaoji.XiaojiFactory;
import com.orange.protocol.message.MessageProtos;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 14-3-19
 * Time: 上午11:22
 * To change this template use File | Settings | File Templates.
 */
public class UserService {

    public static final Logger log = Logger.getLogger(UserService.class.getName());

    private static final int AWARD_USER_DRAW_COUNT = 1000;
//    private static final int AWARD_USER_SING_COUNT = 100;

    private static UserService ourInstance = new UserService();

    public static UserService getInstance() {
        return ourInstance;
    }

    private UserService() {
    }


    public void scheduleAwardPopUserService(){

        log.info("<scheduleAwardPopUserService>");
        boolean isActive = (PropertyUtil.getIntProperty("config.enable_daily_active_award", 0) == 1);
        if (isActive){
            ScheduleService.getInstance().scheduleEveryday(21,0,0, new Runnable(){

                @Override
                public void run() {
                    AbstractXiaoji draw = XiaojiFactory.getInstance().getDraw();
                    awardPopUser(draw, AWARD_USER_DRAW_COUNT);
                }
            });
        }
    }

    private void awardPopUser(AbstractXiaoji xiaoji, int maxAwardUserCount) {
        try{
            List<User> userList = xiaoji.popUserManager().getTopList(0, maxAwardUserCount);
            int index = 0;
            for (User user : userList){
                index ++;
                final int topIndex = index;
                final int amount = getAwardByIndex(index, maxAwardUserCount);
                final String userId = user.getUserId();
                log.info("<awardPopUser> award user "+user.getNickName()+" "+amount+" at "+topIndex);
                DBService.getInstance().executeDBRequest(4, new Runnable() {
                    @Override
                    public void run() {
                        UserManager.chargeAccount(DBService.getInstance().getMongoDBClient(),
                                userId, amount,
                                DBConstants.C_CHARGE_SOURCE_POP_USER, null, null);

                        String message = "今天你活跃指数在小吉排名第【"+topIndex+"】位，获得"+amount+"金币奖励，望加油继续努力 (^v^) ";
                        MessageManager.sendSystemMessage(DBService.getInstance().getMongoDBClient(),
                                userId,
                                message,
                                DBConstants.APPID_DRAW,
                                false);
                    }
                });

                Thread.sleep(100);
            }

        }
        catch(Exception e){
            log.error("<awardPopUser> but catch exception "+e.toString(), e);
        }
    }

    private int getAwardByIndex(int index, int max) {
        if (index >= 0 && index <= 10){
            return 500;
        }
        else if (index > 10 && index <= 100){
            return 300;
        }
        else if (index > 100 && index <= 200){
            return 200;
        }
        else{
            return 100;
        }
    }

    public int searchUserByKey(String searchKey, MessageProtos.PBSearchUserResponse.Builder builder) {
        // TODO
        return 0;
    }
}
