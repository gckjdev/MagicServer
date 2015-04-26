package com.orange.game.model.manager.xiaojinumber;

import com.orange.common.utils.StringUtil;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 13-7-22
 * Time: 下午4:44
 * To change this template use File | Settings | File Templates.
 */
public class FreePoolManager {

    public static final Logger log = Logger.getLogger(FreePoolManager.class
            .getName());

    private static final String REDIS_KEY_PREFIX = "number_free_for_";
    private final String redisKey;

    public FreePoolManager(String categoryName, int requestPoolType) {

        if (TYPE_NUMBER_MAP.size() == 0){
            TYPE_NUMBER_MAP.putIfAbsent("139", 1);
            TYPE_NUMBER_MAP.putIfAbsent("123", 2);
        }

        redisKey = REDIS_KEY_PREFIX + categoryName + "_" + String.valueOf(requestPoolType);
    }

    public void allocNumbers(Transaction transaction, List<String> retList, int newFetchCount) {
        for (int i = 0; i < newFetchCount; i++) {
            Response<String> number = transaction.spop(getRedisKey());
            retList.add(number.get());
        }
    }

    public String getRedisKey() {
        return redisKey;
    }

    public void putNumber(Pipeline p, String number) {
        log.info("<putNumber> number = "+number + " into "+redisKey);
        p.sadd(getRedisKey(), number);
    }

    public void putNumbers(Jedis jedis, Set<String> numberSet) {
        String[] numbers = new String[numberSet.size()];
        numberSet.toArray(numbers);
        log.info("<putNumbers> putting.... key="+getRedisKey()+", number count = "+numbers.length);

        int start = 0;
        int end = numbers.length ;

        while (start < end){
            int next = start + 1000;
            if (next >= end){
                next = end;
            }

            String[] batchNumbers = new String[next-start];
            for (int i=start, j=0; i<next; i++, j++){
                batchNumbers[j] = numbers[i];
            }

            jedis.sadd(getRedisKey(), batchNumbers);
            log.info("<putNumbers> ongoing, key="+getRedisKey()+", number count = "+batchNumbers.length);

            batchNumbers = null;
            start = next;
        }

        log.info("<putNumbers> completed, key="+getRedisKey()+", number count = "+numbers.length);
        numbers = null;
    }

    static final ConcurrentMap<String, Integer> TYPE_NUMBER_MAP = new ConcurrentHashMap<String, Integer>();
    static final int NUMBER_PREFIX_LEN = 3;

    public static int getTypeByNumber(String number) {

        if (TYPE_NUMBER_MAP.size() == 0){
            TYPE_NUMBER_MAP.putIfAbsent("139", 1);
            TYPE_NUMBER_MAP.putIfAbsent("123", 2);
        }

        if (StringUtil.isEmpty(number) || number.length() < NUMBER_PREFIX_LEN){
            return -1;
        }

        Integer type = TYPE_NUMBER_MAP.get(number.substring(0, NUMBER_PREFIX_LEN));
        if (type == null){
            return -1;
        }

        return type.intValue();
    }

    public void removeNumber(Pipeline p, String number) {

        log.info("<removeNumber> number = "+number + " from "+redisKey);
        p.srem(getRedisKey(), number);
    }
}
