package com.orange.game.traffic.statemachine;

import com.orange.common.statemachine.Condition;
import com.orange.game.traffic.model.dao.GameSession;

public class CommonGameCondition {

	public static class CheckUserCount implements Condition {

		@Override
		public int decide(Object context) {
			GameSession session = (GameSession)context;			
			return session.getUserList().getSessionUserCount();
		}
		
	}	
}
