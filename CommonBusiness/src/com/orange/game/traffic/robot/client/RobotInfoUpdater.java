package com.orange.game.traffic.robot.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.orange.common.log.ServerLog;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.User;
import com.orange.game.model.manager.UserManager;

/**
 *  Update robot nickname and avatar periodically	
 *
 *  Google iamge API doc :https://developers.google.com/image-search/v1/jsondevguide 
 */
public class RobotInfoUpdater {

	protected RobotInfoUpdater() {
	}
	private static RobotInfoUpdater robotInfoUpdater = new RobotInfoUpdater();
	public static RobotInfoUpdater getInstance() {
		return robotInfoUpdater;
	}

	private static final String IMG_SEARCH_API_BASE_URL
	 			= "https://ajax.googleapis.com/ajax/services/search/images?v=1.0";
	
	
	private enum Gender {
		BEAUTIFUL_GIRL("美女") { 
			String encodedString() {  return "%E6%B8%85%E7%BA%AF%E7%BE%8E%E5%A5%B3"; } // “清纯美女”经encodeURI编码后的字符串 
			Boolean isMale() { return false; } 
		},
		LOLITA("萝莉") {
			String encodedString() { return "%E8%90%9D%E8%8E%89"; } // “萝莉”经encodeURI编码后的字符串
			Boolean isMale() { return false; }
		},
		HANDSOME_BOY("帅哥") {
			String encodedString() { return "%E5%B8%85%E5%93%A5"; }// “帅哥”经encodeURI编码后的字符串
			Boolean isMale() { return true; }
		},
		SHOTA("正太") {
			String encodedString() { return "%E6%AD%A3%E5%A4%AA"; }// “正太”经encodeURI编码后的字符串
			Boolean isMale() { return true; }
		};
		private final String description;
		private Gender(String desc) { this.description = desc; }
		@Override
		public String toString() { return description; }
		
		abstract String encodedString();
		abstract Boolean isMale();
	}
	
	
	private static final String REFERER_URL="http://www.my-ajax-site.com"; 
	private static final int RESULTS_PER_PAGE = 8; //an integer from 1–8 indicating the number of results to return per page.
	private static String[] USER_IP = { "58.215.164.153",
		 	"58.215.172.169", "58.215.184.18", "58.215.160.100"
	 };
	
	
	public void updateRobotAvatarAndNickName(MongoDBClient mongoClient, List<User> robotUsers) {
		
		ServerLog.info(0, "<RobotInfoUpdater> Start to update robot avatar and nick name ! Totoal robots: " + robotUsers.size());
		
		// prepare images
		List<String> femaleImgs = new ArrayList<String>();
		List<String> maleImgs = new ArrayList<String>();
		String queryResults = null;
		for ( Gender gender : Gender.values()) {
			queryResults = doQueryImg(gender);
			if ( queryResults == null ) { 
				ServerLog.info(0, "<RobotInfoUpdater> Query google for images fails. Stop updating!!!");
				return;
			}
			parseJSON(queryResults, gender.isMale()? maleImgs : femaleImgs);
		}
		Collections.shuffle(maleImgs);
		Collections.shuffle(femaleImgs);
		ServerLog.info(0, "<RobotInfoUpdater> Query google for images done ! Male images : total " + maleImgs.size() + ", female Images : total " + femaleImgs.size());
		
		// prepare nicknames
		List<String> maleNames = Arrays.asList(RobotNickName.MALE_NICK_NAME);
		List<String> femaleNames = Arrays.asList(RobotNickName.FEMALE_NICK_NAME);
		Collections.shuffle(maleNames);
		Collections.shuffle(femaleNames);
		ServerLog.info(0, "<RobotInfoUpdater> Prepare nicknames done ! Male names : total " + maleNames.size() + ", female names : total " + femaleNames.size());
		
		String newAvatar = null;
		String newNickName = null;
		int maleIndex = 0;
		int femaleIndex = 0;
		for(User user : robotUsers) {
			ServerLog.info(0, "<RobotInfoUpdater> Start to update robot " + user.getUserId());
			DBObject obj = new BasicDBObject();
			String gender = user.getGender();
			if ( gender.equals("m")) { 
				newAvatar = maleImgs.get(maleIndex);
				newNickName = maleNames.get(maleIndex);
				maleIndex++;
			} else {
				newAvatar = femaleImgs.get(femaleIndex);
				newNickName = femaleNames.get(femaleIndex);
				femaleIndex++;
			}
			// 如果为空，就不再继续更新，返回
			if ( newAvatar == null || newNickName == null ) {
				ServerLog.info(0, "<RobotInfoUpdater> new avatar or newNickName is null ,stop update. Total updated robot : " + (maleIndex+femaleIndex));
				return;
			}
			
			
			obj.put(DBConstants.F_AVATAR, newAvatar);
			obj.put(DBConstants.F_NICKNAME, newNickName);
			String userId = user.getUserId();
			User updatedUser = UserManager.updateUserByDBObject(mongoClient, userId, obj);
			if (updatedUser != null ) {
				ServerLog.info(0, "<RobotInfoUpdater> " + (maleIndex+femaleIndex) +": Successfully update robot " + userId );
			} else {
				ServerLog.info(0, "<RobotInfoUpdater> " + (maleIndex+femaleIndex) +" Fail to  update robot " + userId );
			}
		}
		
	}

	
	/**
	 *  请求一页(RESULTS_PER_PAGE张)图片
	 */
	 private String doQueryImg(Gender gender)  {
		 
		 String result = null;
		 
		 String line = null;
		 StringBuilder builder = new StringBuilder();
		 BufferedReader reader = null;
		 InputStream inputStream = null;
		 Random rdm = new Random();
		 
		 /**
		  *  For more details, plz refer to https://developers.google.com/image-search/v1/jsondevguide !
		  */
		 String urlString = IMG_SEARCH_API_BASE_URL
								+ "&q="+gender.encodedString()   
								+ "&imgsz=small"
				 				+ "&imgtype=face"
				            + "&rsz="+RESULTS_PER_PAGE // the number of images per page returned
				            + "&start="+RESULTS_PER_PAGE * rdm.nextInt(5) // start index of the first search result
				            + "&userip="+USER_IP[rdm.nextInt(USER_IP.length)];
//		 Serverlog.info(0, "<RobotInfoUpdater> Query for " + gender.toString() + ", urlString = " + urlString);
		 
		 boolean success = true;
		 try {
				 URL url = new URL(urlString);
				 URLConnection connection = url.openConnection();
				 connection.addRequestProperty("Referer",REFERER_URL);
				 connection.setConnectTimeout(30*1000);
			 
				 inputStream = connection.getInputStream();
				 reader = new BufferedReader(new InputStreamReader(inputStream));
				 while((line = reader.readLine()) != null) {
					 builder.append(line);	
				 }

				 
			 } catch(MalformedURLException me) {
				 ServerLog.info(0, "<RobotInfoUpdater> Wrong query URL for img search, please check: " + urlString);
				 success = false;
			 } catch(IOException ie) {
				 ServerLog.info(0, "<RobotInfoUpdater> Query for "+ gender.toString() + ", fails due to " + ie.toString());
				 success = false;
			 } catch (Exception e) {
				 ServerLog.info(0, "<RobotInfoUpdater> Query for "+ gender.toString() + ", fails due to " + e.toString());
				 success = false;
			}finally {
				 try {  
					 if (reader != null) {  
						 reader.close();  
					 }	  
				 } catch (IOException ex) {  
					 ex.printStackTrace();  
				 }  
				 
				 if (inputStream != null){
					 try {
						inputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				 }
		}

		if ( success )
		    result = builder.toString();
		 
		 return result;
	 }
	 
	 private void parseJSON(String str, List<String> list) {
		 
		 String tmp = null; 
		 
		 JSONObject queryResults = JSONObject.fromObject(str);
		 JSONObject responseData = (JSONObject) queryResults.get("responseData");
		 JSONArray imgArray = responseData.getJSONArray("results");
		 if ( imgArray != null ) {
			 for ( Object o: imgArray ) {
				 tmp = (String) ((JSONObject)o).get("url");
				 list.add(tmp);
			 }
		 }
	 }
}
