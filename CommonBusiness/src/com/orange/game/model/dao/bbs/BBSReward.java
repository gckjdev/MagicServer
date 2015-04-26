package com.orange.game.model.dao.bbs;

import java.util.Date;

import org.bson.types.ObjectId;

import com.mongodb.DBObject;
import com.orange.common.utils.StringUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.CommonData;

public class BBSReward extends CommonData {
	
	public static final int RewardStatusNo = 0;
	public static final int RewardStatusOn = 1;
	public static final int RewardStatusOff = 2;
	
	public BBSReward()
	{
		super();
		setStatus(RewardStatusOn);
	}
	
	public BBSReward(DBObject dbObject) {
		super(dbObject);
		setStatus(RewardStatusOn);
	}
	
	public BBSReward(int bonus) {
		super();
		setBonus(bonus);
		setStatus(RewardStatusOn);
	}

	public int getBonus() {
		return getInt(DBConstants.F_BONUS);
	}

	public int getStatus() {
		return getInt(DBConstants.F_STATUS);
	}

	public BBSUser getWinner() {
		DBObject object = (DBObject) getObject(DBConstants.F_WINNER);
		if (object != null) {
			return new BBSUser(object);
		}
		return null;
	}

	public Date getAwardDate() {
		return getDate(DBConstants.F_AWARD_DATE);
	}

	public void setBonus(int bonus) {
		put(DBConstants.F_BONUS, bonus);
	}

	public void setStatus(int status) {
		put(DBConstants.F_STATUS, status);
	}

	public void setWinner(BBSUser winner) {
		if (winner != null) {
			put(DBConstants.F_WINNER, winner.getDbObject());
		}
	}

	public void setAwardDate(Date awardDate) {
		put(DBConstants.F_AWARD_DATE, awardDate);
	}
	
	public void setActionId(String actionId) {
		if (!StringUtil.isEmpty(actionId)) {
			put(DBConstants.F_ACTION_ID, new ObjectId(actionId));	
		}
	}
	
	public String getActionId()
	{
		ObjectId oId = (ObjectId) getObject(DBConstants.F_ACTION_ID);
		if (oId != null) {
			return oId.toString();
		}
		return null;
	}
}
