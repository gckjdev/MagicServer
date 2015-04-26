package com.orange.game.model.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.orange.game.constants.DBConstants;

public class Room extends CommonData {
	
    public static int ROOM_WAITTING = 1;
    public static int ROOM_PLAYING = 2;
    public static int ROOM_FULL = 3;

	
    public static int ROOM_TYPE_DRAW = 1; 
		
	
	public Room(DBObject dbObject) {
		super(dbObject);
		// TODO Auto-generated constructor stub
	}
	
    public String getRoomId() {
    	return this.getObjectId().toString();
    }

	public List<RoomUser> getPlayerList() {
		
		BasicDBList list = (BasicDBList)this.getDbObject().get(DBConstants.F_ROOM_USERS);
		if (list == null) {
			return Collections.emptyList();
		}
        Iterator<Object> iter = list.iterator();
        List<RoomUser> userList = new ArrayList<RoomUser>();
        while (iter.hasNext()) {
            BasicDBObject obj = (BasicDBObject) iter.next();
            if (obj != null) {
            	RoomUser user = new RoomUser(obj);
                userList.add(user);
            }
        }
		return userList;
	}

	public String getCreatorUserId() {
		return this.getString(DBConstants.F_CREATE_USERID);
	}
	
	public void setCreatorUserId(String userId) {
		this.put(DBConstants.F_CREATE_USERID, userId);
	}
	
	public String getPassword() {
		return this.getString(DBConstants.F_PASSWORD);
	}

	public void setPassword(String newPassword) {
		this.put(DBConstants.F_PASSWORD, newPassword);
	}

//	public String getGender() {
//		return this.getString(DBConstants.F_GENDER);
//	}
//
//	public void setGender(String gender) {
//		this.put(DBConstants.F_GENDER, gender);
//	}
	
    public void setCreateDate(Date createDate) {
        this.put(DBConstants.F_CREATE_DATE, createDate);
    }
    
	public Date getCreateDate() {
		return this.getDate(DBConstants.F_CREATE_DATE);
	}


	public int getStatus() {
		return this.getInt(DBConstants.F_STATUS);
	}

	public void setStatus(int status) {
		this.getDbObject().put(DBConstants.F_STATUS, status);
	}

//	public void setAvatar(String avatarURL) {
//		this.getDbObject().put(DBConstants.F_AVATAR, avatarURL);		
//	}
//	public String getAvatar() {
//		return this.getString(DBConstants.F_AVATAR);
//	}
//	
//	
    public void setCreatorNickName(String nickName) {
        this.put(DBConstants.F_NICKNAME, nickName);
    }

    public String getCreatorNickName() {
        return this.getString(DBConstants.F_NICKNAME);
    }
    

    public void setExpireDate(Date expireDate) {
        this.put(DBConstants.F_EXPIRE_DATE, expireDate);
    }

    public Date getExpireDate() {
        return this.getDate(DBConstants.F_EXPIRE_DATE);
    }
    
    public String getRoomName() {
        return this.getString(DBConstants.F_ROOM_NAME);
    }

    public void setRoomName(String roomName) {
        this.put(DBConstants.F_ROOM_NAME, roomName);
    }

    public String getServerAddress() {
        return this.getString(DBConstants.F_SERVER_ADDRESS);
    }

    public void setServerAddress(String serverAddress) {
        this.put(DBConstants.F_SERVER_ADDRESS, serverAddress);
    }
    
    public String getServerPort() {
        return this.getString(DBConstants.F_SERVER_PORT);
    }

    public void setServerPort(String serverPort) {
        this.put(DBConstants.F_SERVER_PORT, serverPort);
    }
    
    
    private RoomUser getRoomUserByUserId(String userId)
    {
		BasicDBList list = (BasicDBList)this.getDbObject().get(DBConstants.F_ROOM_USERS);
		if (list != null) {
			for (Object obj : list){
				RoomUser user = new RoomUser((BasicDBObject)obj);
				if (user.getUserId().equalsIgnoreCase(userId)){
					return user;
				}
			}
		}
		return null;
    }
    
    
    public List<RoomUser> getRoomUsers()
    {
		BasicDBList list = (BasicDBList)this.getDbObject().get(DBConstants.F_ROOM_USERS);
		if (list != null) {
			List<RoomUser> users = new ArrayList<RoomUser>();
			for (Object obj : list){
				RoomUser user = new RoomUser((BasicDBObject)obj);
				users.add(user);
			}
			return users;
		}

    	return null;
    }

}
