package com.orange.game.model.service.xiaojinumber;

import com.orange.common.redis.RedisCallable;
import com.orange.common.redis.RedisClient;
import com.orange.common.utils.StringUtil;
import com.orange.game.api.service.ElasticsearchService;
import com.orange.game.constants.ErrorCode;
import com.orange.game.model.dao.User;
import com.orange.game.model.manager.UserManager;
import com.orange.game.model.manager.timeline.*;
import com.orange.game.model.manager.xiaojinumber.AllocPoolManager;
import com.orange.game.model.manager.xiaojinumber.FreePoolManager;
import com.orange.game.model.manager.xiaojinumber.UsedPoolManager;
import com.orange.game.model.manager.xiaojinumber.UserNumberPoolManager;
import com.orange.game.model.service.DBService;
import com.orange.game.model.xiaoji.AbstractXiaoji;
import com.orange.game.model.xiaoji.XiaojiFactory;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 13-7-22
 * Time: 下午4:09
 * To change this template use File | Settings | File Templates.
 */
public class XiaojiNumberService {

    public static final Logger log = Logger.getLogger(XiaojiNumberService.class
            .getName());

    private static XiaojiNumberService ourInstance = new XiaojiNumberService();

    public static XiaojiNumberService getInstance() {
        return ourInstance;
    }

    private XiaojiNumberService() {
    }

    public String getOrCreateNewNumberForUser(final String userId,
                                              final AbstractXiaoji xiaoji,
                                              boolean removeOldNumber,
                                              boolean setUserNumber,
                                              int requestPoolType) {

        if (StringUtil.isEmpty(userId) || xiaoji == null){
            return null;
        }

        FreePoolManager freePoolManager = xiaoji.getNumberFreePoolManager(requestPoolType);
        UsedPoolManager usedPoolManager = UsedPoolManager.getInstance();
        UserNumberPoolManager userNumberPoolManager = UserNumberPoolManager.getInstance();

        String number = null;

        if (!removeOldNumber){
            number = userNumberPoolManager.getNumber(userId);
        }

        if (number == null){
            number = allocFreeNumber(userId, freePoolManager, userNumberPoolManager);
            log.info("<getOrCreateNewNumberForUser> alloc new number = "+number+" for user "+userId);
        }
        else{
            log.info("<getOrCreateNewNumberForUser> return current number = "+number+" for user "+userId);
        }

        if (setUserNumber && number != null){
            setUserNumber(userId, number, null);
        }

        // init empty records

//        DBService.getInstance().executeDBRequest(1, new Runnable() {
//            @Override
//            public void run() {
                OpusTimelineByCategoryManager opusTimelineManager = xiaoji.opusTimelineByCategoryManager();
                GuessOpusTimelineByCategoryManager guessOpusTimelineManager = xiaoji.guessOpusTimelineByCategoryManager();

                opusTimelineManager.insertEmptyId(userId);
                guessOpusTimelineManager.insertEmptyId(userId);

                OpusTimelineManager allOpusTimelineManager = xiaoji.opusTimelineManager();
                GuessOpusTimelineManager allGuessOpusTimelineManager = xiaoji.guessOpusTimelineManager();

                allOpusTimelineManager.insertEmptyId(userId);
                allGuessOpusTimelineManager.insertEmptyId(userId);
                TutorialTimelineManager.getInstance().insertEmptyId(userId);
//            }
//        });

        return number;
    }

    private String allocFreeNumber(final String userId,
                                   final FreePoolManager freePoolManager,
                                   final UserNumberPoolManager userNumberPoolManager) {

        List<String> retList = Collections.emptyList();

        retList = (List<String>)RedisClient.getInstance().execute(new RedisCallable<List<String>>() {
            @Override
            public List<String> call(Jedis jedis) {

                List<String> retList = new ArrayList<String>();

                String number = jedis.spop(freePoolManager.getRedisKey());
                if (number == null)
                    return Collections.emptyList();

                userNumberPoolManager.setUserNumber(jedis, userId, number);
                retList.add(number);
                return retList;
            }
        });

        if (retList != null && retList.size() > 0){
            return retList.get(0);
        }
        else{
            return null;
        }
    }

    public boolean setUserNumber(final String userId, final String number, final AbstractXiaoji xiaoji) {

        if (StringUtil.isEmpty(userId) || StringUtil.isEmpty(number)){
            return false;
        }

        Boolean result = (Boolean)RedisClient.getInstance().execute(new RedisCallable<Boolean>() {
            @Override
            public Boolean call(Jedis jedis) {

            // update mongo db
            UserManager.updateUserXiaojiNumber(userId, number);

            Pipeline p = jedis.pipelined();
            p.multi();
            if (xiaoji != null){
                FreePoolManager freePoolManager = xiaoji.getNumberFreePoolManager(number);
                if (freePoolManager != null){
                    freePoolManager.removeNumber(p, number);
                }
            }

            UsedPoolManager.getInstance().addNumber(p, number);
            UserNumberPoolManager.getInstance().clearUserNumber(p, userId);
            p.exec();
            List result = p.syncAndReturnAll();

            if (result == null){
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
            }
        });

        return result.booleanValue();
    }


    int count[] = new int[10];
    int BIT = 6;
    int DIFF = 3;
    int value = 0;
    Set<String> numberSet = new HashSet<String>();

    public void cal(String numberPrefix, int bit)
    {
        if (bit == 0) {
            numberSet.add(numberPrefix + String.valueOf(value));
            return;
        }
        for (int i = 0; i < 10; i ++) {
            if (BIT == bit && i == 0) {
                continue;
            }
            if (count[i] < DIFF) {
                value = value * 10 + i;
                count[i]++;
                cal(numberPrefix, bit - 1);

                value /= 10;
                count[i]--;

            }
        }
    }

    public void generateNumberPoolForInviteCode(final FreePoolManager freePoolManager){


        RedisClient.getInstance().execute(new RedisCallable() {
            @Override
            public Object call(Jedis jedis) {

                int INVITE_CODE_LEN = 6;
                cal("", INVITE_CODE_LEN);
                freePoolManager.putNumbers(jedis, numberSet);
                numberSet.clear();
                return null;

            }

        });


    }


    public void generateNumberPool(final AbstractXiaoji xiaoji, final int requestPoolType, final String numberPrefix, final int codeLen){


        RedisClient.getInstance().execute(new RedisCallable() {
            @Override
            public Object call(Jedis jedis) {

                FreePoolManager freePoolManager = xiaoji.getNumberFreePoolManager(requestPoolType);
                cal(numberPrefix, codeLen);
                freePoolManager.putNumbers(jedis, numberSet);
                numberSet.clear();
                return null;

            }

        });


    }

    public int clearUserNumber(final String number, final FreePoolManager freePoolManager) {


        Boolean result = (Boolean)RedisClient.getInstance().execute(new RedisCallable<Boolean>() {
            @Override
            public Boolean call(Jedis jedis) {
                User user = UserManager.findUserByXiaojiNumber(number);
                String userId = "";
                if (user != null){
                    UserManager.removeUserXiaojiNumber(user, number);
                    userId = user.getUserId();
                }

                Pipeline p = jedis.pipelined();
                p.multi();
                UsedPoolManager.getInstance().removeNumber(p, number); // addNumber(transaction, number);
                UserNumberPoolManager.getInstance().clearUserNumber(p, userId);
                freePoolManager.putNumber(p, number);
                p.exec();
                List result = p.syncAndReturnAll();

                if (result == null){
                    return Boolean.FALSE;
                }

                return Boolean.TRUE;
            }
        });

        if (result.booleanValue()){
            return 0;
        }
        else{
            return ErrorCode.ERROR_SYSTEM;
        }

    }



}
