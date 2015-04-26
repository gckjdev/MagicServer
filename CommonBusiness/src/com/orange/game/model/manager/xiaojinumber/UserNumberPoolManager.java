package com.orange.game.model.manager.xiaojinumber;

import com.orange.common.processor.CommonProcessor;
import com.orange.common.redis.RedisClient;
import com.orange.common.utils.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Transaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 13-7-22
 * Time: 下午4:45
 * To change this template use File | Settings | File Templates.
 */
public class UserNumberPoolManager {

    public static final Logger log = Logger.getLogger(UserNumberPoolManager.class
            .getName());

    private static final char NUMBER_SEPERATOR = ',';
    private static final String STRING_SEPERATOR = ",";
    private static final String REDIS_KEY = "number_for_user";
    private static UserNumberPoolManager ourInstance = new UserNumberPoolManager();

    public static UserNumberPoolManager getInstance() {
        return ourInstance;
    }

    private UserNumberPoolManager() {
    }



    public void setUserNumbers(Transaction transaction, String userId, List<String> numberList) {

        String numberString = StringUtils.join(numberList, NUMBER_SEPERATOR);
        transaction.hset(REDIS_KEY, userId, numberString);
        log.info("<setUserNumbers> userId="+userId+", numbers="+numberString);
    }

    public List<String> getNumbers(String userId) {

        String numberString = RedisClient.getInstance().hget(REDIS_KEY, userId);
        if (numberString == null){
            return Collections.emptyList();
        }

        String[] numberList = numberString.split(STRING_SEPERATOR);
        if (numberList == null || numberList.length == 0){
            return Collections.emptyList();
        }

        List<String> retList = new ArrayList<String>();
        Collections.addAll(retList, numberList);
//        for (int i=0; i<numberList.length; i++)
//            retList.add(numberList[i]);

        return retList;
    }

    public void setUserNumber(Jedis jedis, String userId, String number) {
        jedis.hset(REDIS_KEY, userId, number);
        log.info("<setUserNumber> userId="+userId+", numbers="+number);
    }

    public String getNumber(String userId) {
        return RedisClient.getInstance().hget(REDIS_KEY, userId);
    }

    public void clearUserNumber(Pipeline p, String userId) {
        log.info("clear userId="+userId+" from user number pool "+REDIS_KEY);
        p.hdel(REDIS_KEY, userId);
        return;
    }
}
