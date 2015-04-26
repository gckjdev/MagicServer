package com.orange.game.model.manager.guessopus;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.orange.common.redis.RedisCallable;
import com.orange.common.redis.RedisClient;
import com.orange.common.utils.DateUtil;
import com.orange.common.utils.StringUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.common.CommonMongoIdComplexListManager;
import com.orange.game.model.common.CommonMongoIdListManager;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.dao.opus.Opus;
import com.orange.game.model.dao.opus.UserGuess;
import com.orange.game.model.manager.opus.OpusUtils;
import com.orange.game.model.manager.word.WordManager;
import com.orange.game.model.service.DBService;
import com.orange.network.game.protocol.model.OpusProtos;
import org.bson.types.ObjectId;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Transaction;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Linruin
 * Date: 13-7-16
 * Time: 上午9:51
 * To change this template use File | Settings | File Templates.
 */
public class UserGuessOpusPlayManager extends CommonMongoIdComplexListManager<Opus> {

    private static final String REDIS_OPUS_FOR_GUESS_PREFIX = "opus_for_guess_";            // sorted set
    private static final String REDIS_OPUS_GUESS_USER_PREFIX = "opus_guess_user_";          // map

    private static final int OPUS_GUESS_SCORE        = 2;
    private static final int OPUS_GUESS_MATCH_SCORE  = 1;


    private static final long MAX_SPEND_TIME = 3600l*24l*30l*1000l;        // 3600*24*30*1000;
    public  static final long SPEND_TIME_MULTIPLIER = 100000000;

    private final String opusForGuessTableName;
    private final String opusGuessUserTableName;
    private final int minDataLen;
    private final boolean checkWord;
    private final String contestId;
    private final String category;

    public static String getContestOpusForGuessTableName(String category){
        return getOpusForGuessTableName(category, UserGuessConstants.MODE_CONTEST);
    }

    private static String getOpusForGuessTableName(String category, String modeName){
        return REDIS_OPUS_FOR_GUESS_PREFIX + category + "_" + modeName;
    }

    private static String getOpusGuessUserTableName(String category, String modeName){
        return REDIS_OPUS_GUESS_USER_PREFIX + category + "_" + modeName;
    }

    public static String getContestTableName(String contestId){
        return  getTableName(null, UserGuessConstants.MODE_CONTEST, contestId);
    }

    private static String getTableName(String category, String modeName, String contestId){
        return  UserGuessUtil.getUserGuessInfoTableName(category, modeName, contestId);
    }

    public UserGuessOpusPlayManager(String category, String modeName, String contestId, int minDataLen, boolean checkWord) {

        super(getTableName(category, modeName, contestId),
                DBConstants.T_OPUS,
                DBConstants.F_OPUS_ID,
                Opus.class);

        this.isReverseRead = false;
        this.opusForGuessTableName = getOpusForGuessTableName(category, modeName);
        this.opusGuessUserTableName = getOpusGuessUserTableName(category, modeName);
        this.minDataLen = minDataLen;
        this.checkWord = checkWord;
        this.contestId = contestId;
        this.category = category;
    }

    private boolean isContest(){
        if (contestId == null || contestId.length() <= 0){
           return false;
        }
        return true;
    }

    @Override
    protected List<Opus> invokeOldGetList(String userId, int offset, int limit) {
        return null;
    }

    @Override
    protected List<Opus> invokeOldGetListForConstruct(String key) {
        return null;
    }


    public void createRedisIndex(final String opusId, final String userId, UserAction opus){

        // TODO clean expired records

        int dataLen = opus.getDataLength();
        log.info("<createOpusForGuessRedisIndex> Opus data len is " + dataLen);


        if (dataLen < minDataLen){
            log.info("<createOpusForGuessRedisIndex> Opus data len is " + dataLen + ", less than " + minDataLen + " skip");
            return;
        }

        if (checkWord && !isInWordDictionary(opus.getWord())){
            log.info("<createOpusForGuessRedisIndex> Opus word " + opus.getWord() + " is not in word dictionary" + " skip");
            return;
        }

        if (StringUtil.isEmpty(opus.getOpusImageUrl())){
            log.info("<createOpusForGuessRedisIndex> but opus "+opus.getOpusId()+" has no image url");
            return;
        }

        if (isContest()){
            GuessContestGeneratorManager.getInstance().insertContestPool(opusId, category);
        }
        else{

            int createTime = opus.getIntCreateDate();
            double initScore = buildOpusGuessScore(0, createTime);

            RedisClient.getInstance().zadd(opusForGuessTableName, initScore, opusId);

            Date date = new Date();
            RedisClient.getInstance().hset(opusGuessUserTableName, constructKey(userId, opusId), date.toString());
        }

    }

    public void clearOpusForGuess(){
        RedisClient.getInstance().del(opusForGuessTableName);
    }

    private boolean isInWordDictionary(String word) {
        return WordManager.getInstance().isValidWord(word);
    }

    public void updateRedisIndex(final String opusId, final String userId){

        updateOpusGuessScore(opusId, OPUS_GUESS_SCORE);

        Date date = new Date();
        RedisClient.getInstance().hset(opusGuessUserTableName, constructKey(userId, opusId), date.toString());
    }

    private void updateOpusGuessScore(String opusId, int score){
        double incScore = buildOpusGuessScore(score, 0);
        RedisClient.getInstance().zinc(opusForGuessTableName, incScore, opusId);
    }

    private double buildOpusGuessScore(int score, int time){
        int reverseTime = 1999999999 - time;
        return ((long)score << 32) + ((long)reverseTime);
    }

    private void batchIncOpusGuessScore(final Set<String> opusSet) {

        RedisClient.getInstance().execute(new RedisCallable() {
            @Override
            public Object call(Jedis jedis) {

                if (opusSet == null || opusSet.size() == 0)
                    return null;

                double incScore = buildOpusGuessScore(OPUS_GUESS_MATCH_SCORE, 0);

                Pipeline p = jedis.pipelined();
//                p.multi();
                for (String opusId : opusSet){
                    if (opusId != null){
                        p.zincrby(opusForGuessTableName, incScore, opusId);
                    }
                }
//                p.exec();
                p.syncAndReturnAll();
                log.info("<batchIncOpusGuessScore> total "+opusSet.size()+" opus score increased into "+opusForGuessTableName);
                return null;
            }
        });
    }



    private String constructKey(final String userId, final String opusId){
        return userId + ":" + opusId;
    }

    private String getOpusIdFromCompoundKey(String compundKey){
        if (compundKey == null){
            return null;
        }

        String[] chs = compundKey.split(":");
        if (chs != null &&  chs.length == 2){
            return chs[1];

        }

        return null;
    }

    public List<String> createList(final String userId, final int size){

        if (size <= 0 || StringUtil.isEmpty(userId)){
            log.warn(opusForGuessTableName + " <createList> but size = "+size + " userId="+userId + " incorrect!");
            return Collections.emptyList();
        }

        final List<String> existOpusIdList = getIdList(userId, 0, 10000); //DBConstants.F_OPUS_STATUS, UserAction.STATUS_DELETE);

        List<String> resultList = (List<String>)RedisClient.getInstance().execute(new RedisCallable<List<String>>() {
            @Override
            public List<String> call(Jedis jedis) {


                if (isContest()){

                    List<String> retOpusIdList = GuessContestGeneratorManager.getInstance().getRandomOpusList(contestId, size, category);
                    return retOpusIdList;
                }

                List<String> retOpusIdList = new ArrayList<String>();

                int start = 0;
                int end = 0;


                for (int tryCount=0;tryCount<100;tryCount++){
                    end = start + size * 2 - 1;

                    Set<String> opusSet = jedis.zrange(opusForGuessTableName, start, end);

                    batchIncOpusGuessScore(opusSet);

                    if (opusSet.size() <= 0){
                        // no more record to add, break
                        break;
                    }

                    List<String> compoundKeyList = new ArrayList<String>();
                    for (String opusId : opusSet){
                        String compoundKey = constructKey(userId, opusId);
                        compoundKeyList.add(compoundKey);
                    }

                    String[] idStringList = new String[compoundKeyList.size()];
                    compoundKeyList.toArray(idStringList);
                    if (idStringList != null && idStringList.length > 0){
                        List<String> opusUserGuessStatusList = jedis.hmget(opusGuessUserTableName, idStringList);

                        for (int i=0; i<compoundKeyList.size() && i<opusUserGuessStatusList.size(); i++){
                            String opusId = getOpusIdFromCompoundKey(compoundKeyList.get(i));
                            String isExist = opusUserGuessStatusList.get(i);

                            int index = existOpusIdList.indexOf(opusId);
                            // 当这幅作品没有被猜过而且这幅作品也不在表中时，才可以插入这个表(避免重复的作品)。
                            if (isExist == null && index == -1){

                                retOpusIdList.add(opusId);
                            }
                        }
                    }

                    if (retOpusIdList.size() >= size){
                        // enough opus, break and return
                        break;
                    }

                    start = end + 1;
                }

                return retOpusIdList;
            }
        });

        return resultList;
    }


    public void clearList(String userId){

        Map<String, Object> clearObjectMap = new HashMap<String, Object>();
        clearObjectMap.put(DBConstants.F_AWARD_COIN, 0);

        super.clearList(userId, clearObjectMap);
    }

    @Override
    protected String indexBeforeDate() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }


    public List<Opus> getList(String userId, int offset, int limit){

        // even opus is deleted, it must be returned here for guess
        List<Opus> opusList = getList(userId, offset, limit, OpusUtils.createReturnFields(), null, 0); //DBConstants.F_OPUS_STATUS, UserAction.STATUS_DELETE);

        if (opusList.size() < limit){
            log.info("<getOpusForGuess> offset = " + offset + " limit = " + limit
                    + ", but list size is " + opusList.size());
            generateAndInsertUserOpusIntoDb(userId, limit);
            // TODO maybe need to update redis

            opusList = getList(userId, offset, limit, OpusUtils.createReturnFields(), null, 0);
        }

        if (opusList.size() < limit){
            // 如果数据不足的话，游戏无法正常玩(比如返回9副，那么用户猜了9副，没有第10副，拿不到钱)。
            log.warn("<getOpusForGuess> list size is " + opusList.size() + ", less than " + limit + ", return null");
            return Collections.emptyList();
        }

        return opusList;
    }

    public void generateAndInsertUserOpusIntoDb(String userId, int size){

        // need to generate data for user guess
        List<String> newOpusIdList = createList(userId, size);

        log.info("<generateAndInsertUserOpusIntoDb> list size = "+ newOpusIdList.size());

        // 若不够size条记录，则不要插进记录了。
        if (newOpusIdList.size() < size){
            return;
        }

        // insert user guess opus data into DB
        // TODO check performance of mongo db
        for (String opusId : newOpusIdList){

            BasicDBObject guessData = new BasicDBObject();
            guessData.put(DBConstants.F_OPUS_ID, new ObjectId(opusId));
            guessData.put(DBConstants.F_CREATE_DATE, new Date());
            guessData.put(DBConstants.F_MODIFY_DATE, null);
            guessData.put(DBConstants.F_CORRECT, false);
            guessData.put(DBConstants.F_GUESS_TIMES, 0);
            guessData.put(DBConstants.F_SPEND_TIME, MAX_SPEND_TIME);
            guessData.put(DBConstants.F_START_DATE, null);
            guessData.put(DBConstants.F_END_DATE, null);
            guessData.put(DBConstants.F_WORD_LIST, null);

            insertObject(userId, guessData, false, false, false);
        }
    }


    public void updateOpusGuessInfo(String userId, String opusId, Set<String> guessWords, boolean correct, Date startDate, Date endDate) {

        int guessTimes = (guessWords == null) ? 0 : guessWords.size();

        BasicDBObject guessData = new BasicDBObject();
        guessData.put(DBConstants.F_CORRECT, correct);
        guessData.put(DBConstants.F_SPEND_TIME, (endDate.getTime() - startDate.getTime()));
        guessData.put(DBConstants.F_MODIFY_DATE, new Date());
        guessData.put(DBConstants.F_START_DATE, startDate);
        guessData.put(DBConstants.F_END_DATE, endDate);
        guessData.put(DBConstants.F_WORD_LIST, guessWords);
        guessData.put(DBConstants.F_GUESS_TIMES, guessTimes);

        // update mongo db data
        updateIndexObject(userId, opusId, guessData);

        if (!isContest()) {
            // contest情况下不需要考虑平均分布的情况
            updateRedisIndex(opusId, userId);
        }
    }

    public UserGuess getUserGuessInfoById(String userId){
        BasicDBObject query = new BasicDBObject(DBConstants.F_OWNER, new ObjectId(userId));
        DBObject obj = DBService.getInstance().getMongoDBClient().findOne(getIdListTableName(), query);
        if (obj == null)
            return null;

        return new UserGuess(obj);
    }

    public void updateUserGuessAward(String userId, int addAward){

        if (StringUtil.isEmpty(userId) || addAward == 0){
            return;
        }

        BasicDBObject query = new BasicDBObject(DBConstants.F_OWNER, new ObjectId(userId));
        BasicDBObject update = new BasicDBObject();
        BasicDBObject incValue = new BasicDBObject(DBConstants.F_AWARD_COIN, addAward);
        update.put("$inc", incValue);

        log.info("<updateUserGuessAward> userId="+userId+", add award="+addAward);
        DBService.getInstance().getMongoDBClient().updateAll(getIdListTableName(), query, update);
    }

}
