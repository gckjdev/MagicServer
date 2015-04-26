package com.orange.game.model.manager;

import com.orange.common.mongodb.MongoDBClient;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.User;
import com.orange.network.game.protocol.model.GameBasicProtos.PBGameCurrency;

public class UserItemManager extends CommonManager {

	public static User buyItem(MongoDBClient mongoClient, String userId, String targetUserId, int itemId,
			int itemCount, int itemTotalPrice, int itemCurrency) {

		if (userId == null || targetUserId == null){
			log.warn("<buyItem> but userId or targetUserId null");
			return null;
		}
		
		User user = UserManager.findUserAccountInfoByUserId(mongoClient, userId);
		if (user == null){
			log.warn("<buyItem> but userId "+userId+" not found");
			return null;
		}
		
		// check balance enough or not
		boolean hasEnoughBalance = false;
		if (itemCurrency == PBGameCurrency.Ingot_VALUE){
			hasEnoughBalance = user.hasEnoughIngotBalance(itemTotalPrice);
		}
		else{
			hasEnoughBalance = user.hasEnoughBalance(itemTotalPrice);
		}
		if (!hasEnoughBalance){
			log.warn("<buyItem> but userId "+userId+" balance not enough, user ingot balance="+user.getIngotBalance()+", balance="+user.getBalance());
			return null;
		}
		
		// deduct money from user
		user = UserManager.chargeAccount(mongoClient, userId, -itemTotalPrice, DBConstants.C_CHARGE_SOURCE_PURCHASE_ITEM, itemCurrency);

		// buy item for target user
		User targetUser = null; //UserManager.incItemAmount(mongoClient, targetUserId, itemId, itemCount);

        if (itemId == DBConstants.C_ITEM_TYPE_PURSE_1000){
            log.info("<buyItem> buy 1000 coins for user "+targetUserId);
            targetUser = UserManager.chargeAccount(mongoClient, targetUserId, 1000*itemCount, DBConstants.C_CHARGE_SOURCE_USE_PURSE, null, null);
        }
        else{
            targetUser = UserManager.incItemAmount(mongoClient, targetUserId, itemId, itemCount);
        }

		if (userId.equals(targetUserId)){
			// same user, return the latest user after use item
			return targetUser;
		}
		else{
			// not the same user, only return user with account changed
			return user;
		}
	}
	
	public static User useItem(MongoDBClient mongoClient, String userId, int itemId,
			int itemCount, boolean forceBuy, int itemTotalPrice, int itemCurrency) {

		if (userId == null){
			log.warn("<useItem> but userId null");
			return null;
		}
		
		User user = UserManager.findPublicUserInfoByUserId(mongoClient, userId);
		if (user == null){
			log.warn("<useItem> but userId "+userId+" not found");
			return null;
		}
		
		// check balance enough or not
		boolean hasEnoughItem = false;
		int currentItemCount = user.getItemAmountByType(itemId);
		hasEnoughItem = ( currentItemCount >= itemCount );
		
		if (!hasEnoughItem){
			
			if (forceBuy){

				// try to buy item
				
				// check balance enough or not
				boolean hasEnoughBalance = false;
				if (itemCurrency == PBGameCurrency.Ingot_VALUE){
					hasEnoughBalance = user.hasEnoughIngotBalance(itemTotalPrice);
				}
				else{
					hasEnoughBalance = user.hasEnoughBalance(itemTotalPrice);
				}
				if (!hasEnoughBalance){
					log.warn("<useItem> try to buy item, but userId "+userId+" balance not enough, user ingot balance="+user.getIngotBalance()+", balance="+user.getBalance());
					return null;
				}
				
				// deduct money from user
				user = UserManager.chargeAccount(mongoClient, userId, -itemTotalPrice, DBConstants.C_CHARGE_SOURCE_PURCHASE_ITEM, itemCurrency);
				return user;
			}
			else{
				log.warn("<useItem> but userId "+userId+" item amount not enough, count="+currentItemCount);
				return null;
			}
		}
		
		// buy item for target user		
		user = UserManager.incItemAmount(mongoClient, userId, itemId, -itemCount);		
		return user;
	}
	

}
