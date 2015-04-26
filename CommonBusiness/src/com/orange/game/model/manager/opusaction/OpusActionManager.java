package com.orange.game.model.manager.opusaction;

import java.util.*;

import com.mongodb.BasicDBObject;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.common.CommonMongoIdListManager;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.manager.OpusManager;
import com.orange.game.model.manager.opus.OpusUtils;
import com.orange.game.model.service.DBService;
import org.bson.types.ObjectId;

public class OpusActionManager extends CommonMongoIdListManager<UserAction> {

	final static String TABLE_OPUS_ACTION_PREFIX = "opus_action_";
	final static String TABLE_OPUS_ACTION_DATA = DBConstants.T_OPUS_ACTION;

	final int actionType;
	
	public OpusActionManager(String actionTableSuffix, int actionType) {
		super(TABLE_OPUS_ACTION_PREFIX+actionTableSuffix.toLowerCase(), TABLE_OPUS_ACTION_DATA, UserAction.class);
		this.actionType = actionType;
        this.autoFixTotalSize = true;
	}
	
	@Override
    protected BasicDBObject returnMongoDBFields(){
		if (actionType == UserAction.TYPE_GUESS) {
			return OpusUtils.createGuessReturnFields();
		}else{
			return OpusUtils.createReturnFields();
		}
    }

    @Override
    protected String deleteStatusFieldName(){
        return  DBConstants.F_OPUS_STATUS;
    }

    @Override
    protected int deleteStatusValue(){
        return UserAction.STATUS_DELETE;
    }

    @Override
    protected String indexBeforeDate() {
        return DBConstants.C_OPUS_INDEX_BEFORE_DATE;
    }

    @Override
	protected List<UserAction> invokeOldGetList(String opusId,int offset,int limit){

		return OpusManager.getCommentList(DBService.getInstance().getMongoDBClient(), opusId,
				null, actionType, offset, limit);
		
	};
	
	@Override
	protected List<UserAction> invokeOldGetListForConstruct(String key){

        if (UserAction.isCreateAfterAugust(key)){
            return Collections.emptyList();
        }

        List<UserAction> actions = new ArrayList<UserAction>();
		int count = 10000;
		int offset = 0;
		int limit = 1000;
		List<UserAction> tempList = Collections.emptyList();
		while (count != 0) {
			// pass null appId here, it looks useless
			tempList = OpusManager.getCommentList(DBService.getInstance().getMongoDBClient(), key, null,actionType, offset, limit);
            if (tempList == null){
                return Collections.emptyList();
            }
			actions.addAll(tempList);
			if(tempList.size()<1000){
				break;
			}
			offset = limit;
			limit+=1000;
			count-=1000;
		}				
		Collections.sort(actions, new Comparator<UserAction>() {

			@Override
			public int compare(UserAction o1, UserAction o2) {
				// TODO Auto-generated method stub
				return o1.getCreateDate().compareTo(o2.getCreateDate());
			}
		});
		return actions;
	};

	public void insertIndex(String key,String actionId ) {
        log.info("insert opus action, key="+key+", actionId="+actionId);
		this.insertAndConstructIndex(key, actionId, false);
	}
	
	
	public List<UserAction> getList(String key,int offset,int limit){
		return getListAndConstructIndex(key, offset, limit);
	}
	
	
	public void removeIndex(String key,String actionId) {
		this.removeId(key, actionId, false);
	}


    public UserAction insertGuessAction(String appId,
                             String opusCreatorUid,
                             String userId,
                             String opusId,
                             String avatar,
                             String gender,
                             String nickName,
                             Set<String> guessWords,
                             boolean correct
                             ){
        UserAction userAction = new UserAction(new BasicDBObject());
        userAction.setActionId(new ObjectId());
        userAction.setAppId(appId);
        userAction.setOpusCreatorUid(opusCreatorUid);
        userAction.addRelatedOpusId(opusCreatorUid);
        userAction.addRelatedOpusId(userId);
        userAction.setOpusId(opusId);
        userAction.setCreateUserId(userId);
        userAction.setNickName(nickName);
        userAction.setAvatar(avatar);
        userAction.setGender(gender);
        userAction.setCreateDate(new Date());
        userAction.setType(UserAction.TYPE_GUESS);
        userAction.setGuessWordList(guessWords);
        userAction.setCorrect(correct);
        userAction.setHasWords((guessWords != null && guessWords.size() > 0));
        userAction.setOpusStatus(UserAction.STATUS_NORMAL);
        DBService.getInstance().getMongoDBClient().insert(DBConstants.T_OPUS_ACTION, userAction.getDbObject());

        return userAction;
    }

}
