package com.orange.game.model.dao;

import java.util.Date;

import com.orange.game.constants.DBConstants;

public class UserGameStatus extends CommonData  {

	public String getUserId() {
		return this.getObjectId().toString();
	}
	
	public String getServerAddress(){
		return this.getString(DBConstants.F_SERVER_ADDRESS);
	}

	public int getServerPort(){
		return this.getInt(DBConstants.F_SERVER_PORT);
	}

	public int getSessionId(){
		return this.getInt(DBConstants.F_SESSION_ID);
	}

	public String getGameId(){
		return this.getString(DBConstants.F_GAME_ID);
	}
	
	public String getServerId(){
		return this.getString(DBConstants.F_SERVER_ID);
	}

	public Date getModifyDate(){
		return this.getDate(DBConstants.F_MODIFY_DATE);
	}
	
	public int getStatus(){
		return this.getInt(DBConstants.F_STATUS);
	}

	
}
