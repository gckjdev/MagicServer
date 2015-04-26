package com.orange.game.model.manager;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.game.constants.DBConstants;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.Item;
import com.orange.game.model.dao.Price;
import com.orange.game.model.dao.User;



public class PriceManager {
	public static List<Price> getPriceListByType (int type, String appId) {
		List<Price> priceList = new ArrayList<Price>();
		int size = Price.getPriceListSizeByType(type, appId);
		for(int i = 0; i < size; ++ i)
		{
			Price price = Price.getPriceByTypeAndPackage(type, appId, i);
			if (price != null) {
				priceList.add(price);
			}
		}
		return priceList;
	}
	
	public static int getItemAmountByType(MongoDBClient clinet, String userId, int type)
	{
		ObjectId userObjectId = new ObjectId(userId);
		DBObject result = clinet.findOne(DBConstants.T_USER, DBConstants.F_USERID, userObjectId);
		if (result == null) {
			return 0;
		}
		User user = new User(result);
		return user.getItemAmountByType(type);
	}
	
	public static List<Item> getItemList(MongoDBClient clinet, String userId) {
		ObjectId userObjectId = new ObjectId(userId);
		DBObject result = clinet.findOne(DBConstants.T_USER, DBConstants.F_USERID, userObjectId);
		if (result == null) {
			return null;
		}
		User user = new User(result);
		return user.getItems();
	}
	
	public static int getAccountBalance(MongoDBClient clinet, String userId) {
		ObjectId userObjectId = new ObjectId(userId);
		
		DBObject query = new BasicDBObject();
		query.put(DBConstants.F_USERID, userObjectId);
		DBObject fields = new BasicDBObject();
		fields.put(DBConstants.F_ACCOUNT_BALANCE, 1);
		fields.put(DBConstants.F_ACCOUNT_INGOT_BALANCE, 1);
		
		DBObject result = clinet.findOne(DBConstants.T_USER, query, fields);
		if (result == null) {
			return 0;
		}
		User user = new User(result);
		return user.getBalance();
	}
}
