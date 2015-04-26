package com.orange.game.traffic.statemachine;

import com.orange.common.statemachine.Event;
import com.orange.common.statemachine.State;


public class CommonGameState extends State {

	public CommonGameState(Object stateId) {
		super(stateId);
	}

	public int validateEvent(Event event, Object context){
		return 0;
	}

	public void exitAction(Event event, Object context) {
	}
	
	public void enterAction(Event event, Object context) {
	}

}
