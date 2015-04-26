package com.orange.game.traffic.service;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.orange.common.log.ServerLog;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.manager.UserManager;
import com.orange.game.traffic.model.dao.GameSession;
import com.orange.game.traffic.model.dao.GameUser;
import com.orange.network.game.protocol.model.GameBasicProtos.PBUserResult;

public class UserGameResultService {

	// thread-safe singleton implementation
		private static UserGameResultService service = new UserGameResultService();
		private UserGameResultService() {
		}

		public static UserGameResultService getInstance() {
			return service;
		}	
	
		public PBUserResult makePBUserResult(String userId, boolean win, int gainCoins) {
			
			PBUserResult result = PBUserResult.newBuilder()
									.setUserId(userId)
									.setWin(win)
									.setGainCoins(gainCoins)
									.build();
			return result;
		}
		
		 
		public void writeAllUserGameResultIntoDB(final GameSession session, final String gameId) {
			
			final GameDBService dbService = GameDBService.getInstance();
			
			dbService.executeDBRequest(session.getSessionId()	, new Runnable() {
				
				@Override
				public void run() {

					int sessionId = session.getSessionId();
					MongoDBClient dbClient = dbService.getMongoDBClient(sessionId);
					
					Collection<PBUserResult> resultList = session.getUserResults();
					List<GameUser> gameUserList = session.getUserList().getAllUsers();
					
					Set<String> gameResultUserIdSet = new HashSet<String>();
					
					// update the winner and loser
					for ( PBUserResult result : resultList ) {
						String userId = result.getUserId();
						// record which two users get userResults
						gameResultUserIdSet.add(userId);
 						doWriteGameResult(sessionId, dbClient, userId, result,gameId);
					}
					
					// update other players
					for(GameUser gameUser : gameUserList) {
						String userId = gameUser.getUserId();
						if ( gameUser.isPlaying() == true  && !gameResultUserIdSet.contains(userId)) {
							doWriteGameResult(sessionId, dbClient, userId, null, gameId);
						}
					}
			}
		} ); // end of dbService.executeDBRequest
	}		
			
	public void writeUserGameResultIndoDb(final int sessionId, 
			final PBUserResult result, final String gameId) {
			
		final GameDBService dbService = GameDBService.getInstance();
			
		dbService.executeDBRequest(sessionId, new Runnable() {
				
			@Override
			public void run() {
					MongoDBClient dbClient = dbService.getMongoDBClient(sessionId);
					String userId = result.getUserId();
 					doWriteGameResult(sessionId, dbClient, userId, result, gameId);
			}
		}); 
	}
		
	private void doWriteGameResult(int sessionId, MongoDBClient dbClient, 
			String userIdString, PBUserResult result, String gameId) {
				
				// query by user_id and game_id
				DBObject query = new BasicDBObject();
				query.put(DBConstants.F_USERID, userIdString);
				query.put(DBConstants.F_GAMEID, gameId);

				// update
				DBObject update = new BasicDBObject();
				DBObject incUpdate = new BasicDBObject();
				DBObject dateUpdate = new BasicDBObject();
				
				incUpdate.put(DBConstants.F_PLAY_TIMES, 1);
				if ( result != null ) {
					if (result.getWin() == true) {
						incUpdate.put(DBConstants.F_WIN_TIMES, 1);
					} else {
						incUpdate.put(DBConstants.F_LOSE_TIMES, 1);
					}
				}
				dateUpdate.put(DBConstants.F_MODIFY_DATE, new Date());
				
				update.put("$inc", incUpdate);
				update.put("$set", dateUpdate);

				ServerLog.info(sessionId, "<writeUserGameResultIntoDB> query="+query.toString()+", update="+update.toString());
				dbClient.upsertAll(DBConstants.T_USER_GAME_RESULT, query, update);
			}

	
	
    /**
     * 
     * @param source: from which game or wherever possible
     */
	public void writeUserCoinsIntoDB(final int sessionId, final PBUserResult result, final int source) {
		
		final GameDBService dbService = GameDBService.getInstance();
		
		dbService.executeDBRequest(sessionId, new Runnable() {
			
			@Override
			public void run() {
				if ( result == null ) 
					return;
				
				MongoDBClient dbClient = dbService.getMongoDBClient(sessionId);
				boolean win = result.getWin();
				String userId = result.getUserId();
				int amount = result.getGainCoins();
				
				if (win){
					UserManager.chargeAccount(dbClient, userId, amount, source, null, null);
					ServerLog.info(sessionId, "<writeUserCoinsIntoDB> "+ userId+ " gets charged "+ amount + " coins.");
				}
				else{
					UserManager.deductAccount(dbClient, userId, -amount, source);
					ServerLog.info(sessionId, "<writeUserCoinsIntoDB> "+ userId+ " gets deducted "+ amount + " coins.");
				}
			}
		});
		
	}
	
	public void writeAllUserCoinsIntoDB(final GameSession session, final int source) {
			
			Collection<PBUserResult> resultList = session.getUserResults();
			for (PBUserResult result : resultList){
				writeUserCoinsIntoDB(session.getSessionId(), result, source);
			}
			
	}

		
		
	}
