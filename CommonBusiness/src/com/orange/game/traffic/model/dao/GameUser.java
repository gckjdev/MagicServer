package com.orange.game.traffic.model.dao;

import org.eclipse.jetty.util.log.Log;
import org.jboss.netty.channel.Channel;

import com.orange.game.model.dao.User;
import com.orange.game.traffic.robot.client.AbstractRobotManager;
import com.orange.network.game.protocol.model.GameBasicProtos.PBGameUser;

public class GameUser {

	final PBGameUser pbUser;
	final Channel channel;
	volatile int currentSessionId = -1;
	volatile int seatId = -1;
	volatile boolean isPlaying = false;
	volatile boolean loseGame = false;
	volatile boolean isTakenOver = false;
	volatile boolean isRemoving = false;
	volatile int balance = Integer.MAX_VALUE;
	volatile int zombieTimeOutTimes = 0;			// used to detect whether user is a zoombie
	
	private static final int MAX_ZOMBIE_TIME_OUT_TIMES = 5;
	
	public GameUser(PBGameUser user, Channel channel, int sessionId){
		this.pbUser = user;
		this.channel = channel;
		this.currentSessionId = sessionId;		
		
		if (user == null)
			return;
		
		if (user.hasSeatId()){
			this.seatId = user.getSeatId();
		}
		
		if (user.hasIsPlaying()){
			this.isPlaying = user.getIsPlaying();
		}
		
		if (user.hasIsTakenOver()){
			this.isTakenOver = user.getIsTakenOver();
		}
	}			
	
	public static GameUser createBlankUser(){
		return new GameUser(null, null, -1);
	}
	
	@Override
	public String toString() {
		return "[nickName=" + getNickName() + ", userId=" + getUserId() + "]";
	}

	public String getUserId() {
		return pbUser.getUserId();
	}

	public String getNickName() {
		return pbUser.getNickName();
	}
	
	public boolean isPlaying(){
		return this.isPlaying;
	}
	
	public void setPlaying(boolean value){
		this.isPlaying = value;
	}
	
	public int getCurrentSessionId() {
		return currentSessionId;
	}

	public void setCurrentSessionId(int currentSessionId) {
		this.currentSessionId = currentSessionId;
	}

	public Channel getChannel() {
		return channel;
	}

	public PBGameUser getPBUser() {
		String avatarURL = getAvatar();
		if (avatarURL != null){
			return PBGameUser.newBuilder(pbUser).setAvatar(avatarURL).setSeatId(seatId).setIsPlaying(isPlaying).setIsTakenOver(isTakenOver).build();			
		}
		else{
			return PBGameUser.newBuilder(pbUser).setSeatId(seatId).setIsPlaying(isPlaying).setIsTakenOver(isTakenOver).build();
		}
	}

	public boolean isTakenOver() {
		return isTakenOver;
	}

	public void setTakenOver(boolean isTakenOver) {
		this.isTakenOver = isTakenOver;
	}

	public String getAvatar() {
		return User.getTranslatedAvatar(pbUser.getAvatar());
	}

	public boolean getGender() {
		return pbUser.getGender();
	}

	public String getLocation() {
		return pbUser.getLocation();
	}

	public void setSeatId(int id){
		seatId = id;
	}
	
	public int getSeatId() {
		return seatId;
	}

	public boolean isRobotUser() {
		return AbstractRobotManager.isRobotUser(getUserId());
	}	

	public boolean isRemoving(){
		return isRemoving;
	}

	public void setRemoving(){
		isRemoving = true;
	}

	public void setLoseGame(boolean b) {
		loseGame = b;
	}
	
	public boolean hasLosedGame() {
		return loseGame;
	}

	public int getBalance() {
		return balance;
	}

	public void setBalance(int balance) {
		this.balance = balance;
	}
	
	public void resetZombieTimeOut(){
		this.zombieTimeOutTimes = 0;
	}
	
	public void incZombieTimeOut(){
		this.zombieTimeOutTimes ++;
	}
	
	public boolean isZombie(){
		return (this.zombieTimeOutTimes >= MAX_ZOMBIE_TIME_OUT_TIMES);
	}
	
	volatile int interfaceVersion = 0;

	public int getInterfaceVersion() {
		return interfaceVersion;
	}

	public void setInterfaceVersion(int interfaceVersion) {
		this.interfaceVersion = interfaceVersion;
	}
	
}
