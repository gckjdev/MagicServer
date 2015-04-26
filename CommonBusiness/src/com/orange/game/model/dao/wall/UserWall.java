package com.orange.game.model.dao.wall;

import java.util.Date;
import java.util.List;


import com.google.protobuf.InvalidProtocolBufferException;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.CommonData;
import com.orange.network.game.protocol.model.DrawProtos.PBLayout;
import com.orange.network.game.protocol.model.DrawProtos.PBWall;
import com.orange.network.game.protocol.model.DrawProtos.PBWallOpus;

public class UserWall extends CommonData {
	public UserWall(DBObject dbObject) {
		super(dbObject);
	}

	public static UserWall fromPBWall(PBWall pbWall, boolean isNew) {
		UserWall userWall = new UserWall(new BasicDBObject());
		
		// set basic info
		userWall.setUserId(pbWall.getUserId());
		userWall.setWallType(pbWall.getType().getNumber());
		userWall.setWallName(pbWall.getName());
		
		if (isNew){
			userWall.setCreateDate(new Date());
		}
		else{
			userWall.setModifyDate(new Date());
		}
		
		// set layout info
		if (pbWall.getContent() != null){
			userWall.setLayoutId(pbWall.getContent().getLayoutId());
			userWall.setDisplayMode(pbWall.getContent().getDisplayMode());
			userWall.setBackground(pbWall.getContent().getImageUrl());
		}
		
		// set opus info
		BasicDBList opusIdList = new BasicDBList();
		if (pbWall.getContent().getWallOpusesList() != null){
			for (PBWallOpus opus : pbWall.getContent().getWallOpusesList()){
				if (opus.getOpus() != null){
					opusIdList.add(opus.getOpus().getFeedId());
				}
			}
		}
		userWall.setOpusIdList(opusIdList);		
		
		// set pb wall binary data
		userWall.setWallData(pbWall);
		
		return userWall;
	}

	private void setCreateDate(Date date) {
		this.getDbObject().put(DBConstants.F_CREATE_DATE, date);
	}

	private void setModifyDate(Date date) {
		this.getDbObject().put(DBConstants.F_MODIFY_DATE, date);
	}	
	
	public void setWallData(PBWall pbWall) {
		this.getDbObject().put(DBConstants.F_WALL_DATA, pbWall.toByteArray());	
	}

	public void setUserId(String value) {
		this.getDbObject().put(DBConstants.F_FOREIGN_USER_ID, value);		
	}

	public void setWallType(int value) {
		this.getDbObject().put(DBConstants.F_TYPE, value);		
	}

	public void setWallName(String value) {
		this.getDbObject().put(DBConstants.F_NAME, value);		
	}

	public void setLayoutId(int value) {
		this.getDbObject().put(DBConstants.F_LAYOUT_ID, value);		
	}

	public void setDisplayMode(int value) {
		this.getDbObject().put(DBConstants.F_LAYOUT_DISPLAY_MODE, value);		
	}

	public void setBackground(String value) {
		this.getDbObject().put(DBConstants.F_BACKGROUND, value);		
	}

	public void setOpusIdList(BasicDBList opusIdList) {
		this.getDbObject().put(DBConstants.F_LAYOUT_OPUS_LIST, opusIdList);		
	}	
	
	public String getWallId() {
		return getStringObjectId();
	}
	
	public PBWall toPBWall(){
		byte[] data = (byte[])this.getDbObject().get(DBConstants.F_WALL_DATA);
		if (data == null){
			return null;
		}
		
		try {
			PBWall.Builder builder = PBWall.newBuilder(PBWall.parseFrom(data));
			
			// rebuild layout for background image
			PBLayout.Builder layoutBuilder = PBLayout.newBuilder(builder.getContent());
			String backgroundImage = getBackgroundRemoteURL();
			layoutBuilder.setImageUrl(backgroundImage);
			builder.setContent(layoutBuilder.build());
						
			builder.setWallId(getWallId());						
			return builder.build();
		} catch (Exception e) {
			log.error("<toPBWall> but catch exception="+e.toString(), e);
			return null;
		}		
	}

	public static final String localFileUploadDir = System.getProperty("upload.local.wallImage");
	public static final String remoteFileUploadDir = System.getProperty("upload.remote.wallImage");
	
	public static String getFileUploadLocalDir() {
		String dir = localFileUploadDir;
		return (dir == null ? "" : dir);
	}

	public static String getFileUploadRemoteDir() {
		String dir = remoteFileUploadDir;
		return (dir == null ? "" : dir);
	}	
	
	public String getBackgroundRemoteURL() {
		String relativeURL = getString(DBConstants.F_BACKGROUND);
		String remoteURLString = getFileUploadRemoteDir();
		return remoteURLString + relativeURL;
	}
	
}
