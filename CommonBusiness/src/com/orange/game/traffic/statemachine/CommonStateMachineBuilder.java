package com.orange.game.traffic.statemachine;

import com.orange.common.statemachine.Action;
import com.orange.common.statemachine.Condition;
import com.orange.common.statemachine.StateMachineBuilder;

public abstract class CommonStateMachineBuilder extends StateMachineBuilder {

	final protected Action initGame = new CommonGameAction.InitGame();
	final protected Action startGame = new CommonGameAction.StartGame();
	final protected Action finishGame = new CommonGameAction.FinishGame();
	final protected Action selectPlayUser = new CommonGameAction.SelectPlayUser();		
	final protected Action kickPlayUser = new CommonGameAction.KickPlayUser();
	final protected Action prepareRobot = new CommonGameAction.PrepareRobot();
	final protected Action clearRobotTimer = new CommonGameAction.ClearRobotTimer();
	final protected Action clearTimer = new CommonGameAction.ClearTimer();

	final protected Action setAllUserPlaying = new CommonGameAction.SetAllUserPlaying();
	final protected Action clearAllUserPlaying = new CommonGameAction.ClearAllUserPlaying();

	final protected Action setOneUserWaitTimer = new CommonGameAction.SetOneUserWaitTimer(); 
	
	final protected Condition checkUserCount = new CommonGameCondition.CheckUserCount();
	
	final protected Action kickZombieUser = new CommonGameAction.KickZombieUser();
	final protected Action incUserZombieTimeOut = new CommonGameAction.IncUserZombieTimeOut();
}
