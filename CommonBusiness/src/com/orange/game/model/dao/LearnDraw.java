package com.orange.game.model.dao;

import org.bson.types.ObjectId;

import com.mongodb.DBObject;
import com.orange.game.constants.DBConstants;
import com.orange.network.game.protocol.model.DrawProtos.PBLearnDraw;

public class LearnDraw extends CommonData {

//	public static int LearnDrawTypeNo = 0;
	public static int LearnDrawTypeAll = 0;
	public static int LearnDrawTypeCartoon = 1; // 漫画
	public static int LearnDrawTypeCharacter = 2; // 人物
	public static int LearnDrawTypeScenery = 3; // 风景
	public static int LearnDrawTypeOther = 10000; // 其他


	public LearnDraw() {
		super();
	}

	public LearnDraw(DBObject dbObject) {
		super(dbObject);
	}

	public String getDrawId() {
		ObjectId oid = getObjectId(DBConstants.F_OPUS_ID);
		if (oid != null) {
			return oid.toString();
		}
		return null;
	}

	public void setDrawId(String drawId) {
		ObjectId oId = new ObjectId(drawId);
		put(DBConstants.F_OPUS_ID, oId);
	}

	public int getBoughtCount() {
		return getInt(DBConstants.F_BOUGHT_TIMES);
	}

	public void setBoughtCount(int count) {
		put(DBConstants.F_BOUGHT_TIMES, count);
	}

	public int getPrice() {
		return getInt(DBConstants.F_PRICE);
	}

	public void setPrice(int price) {
		put(DBConstants.F_PRICE, price);
	}

	public void setType(int type) {
		put(DBConstants.F_TYPE, type);
	}

	public int getType() {
		return getInt(DBConstants.F_TYPE);
	}

	public PBLearnDraw toPBLearnDraw() {
		if (getDrawId() == null) {
			return null;
		}
		PBLearnDraw.Builder builder = PBLearnDraw.newBuilder();
		builder.setOpusId(getDrawId());
		builder.setBoughtCount(getBoughtCount());
		builder.setPrice(getPrice());
		builder.setType(getType());
		return builder.build();
	}

	public void setSellContentType(int sellContentType) {
		put(DBConstants.F_SELL_CONTENT_TYPE, sellContentType);
	}

}
