package com.orange.game.traffic.robot.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.eclipse.jetty.util.ConcurrentHashSet;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.orange.common.log.ServerLog;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.User;

public abstract class AbstractRobotManager {

    public AbstractRobotManager(){
    	robotUserList = findRobots();
    	for (int i=0; i<robotUserList.size(); i++)
    		freeSet.add(i);
	} 	    

    static MongoDBClient mongoClient = new MongoDBClient(DBConstants.D_GAME);
    
    public static final Logger log = Logger.getLogger(AbstractRobotManager.class.getName()); 

//    public static final int MAX_ROBOT_USER = 8;

    public final static String ROBOT_USER_ID_PREFIX = "999999999999999999999";

    private static final String CUSTOMER_SERVER_ID = "88888888888888888888888";     
    
    ConcurrentHashSet<Integer> allocSet = new ConcurrentHashSet<Integer>();
    ConcurrentHashSet<Integer> freeSet  = new ConcurrentHashSet<Integer>();
    List<User> robotUserList = Collections.emptyList();
    Object allocLock = new Object();        
    
    public static boolean isRobotUser(String userId){
    	if (userId == null)
    		return false;
    	
    	return userId.contains(ROBOT_USER_ID_PREFIX);
    }
    
    public int allocIndex(){
    		if (freeSet.isEmpty() ||
    			freeSet.iterator() == null)
    			return -1;
    		
    		Random random = new Random();
    		random.setSeed(System.currentTimeMillis());
    		int randomCount = random.nextInt(freeSet.size());
    		Iterator<Integer> iter = freeSet.iterator();
    		
    		
    		int index = 0;
    		while (iter != null && iter.hasNext() && index < randomCount){
    			index++;
    			iter.next();
    		}
    		
    		if (iter != null && iter.hasNext()){
    			index = iter.next().intValue();
    		}
    		
    		if (index == -1)
    			return -1;
    		
    		allocSet.add(index);
    		freeSet.remove(index);

    		ServerLog.info(0, "alloc robot, alloc index="+index + ", active robot count = "+allocSet.size());
    		return index;
    }
    
    public void deallocIndex(int index){
    	if (!isValidIndex(index) && index < robotUserList.size()){
    		return;
    	}
    	
    		freeSet.add(index);
    		allocSet.remove(index);
    	
    	ServerLog.info(0, "dealloc robot, index="+index + ", active robot count = "+allocSet.size());
    }
    
    public abstract AbstractRobotClient createRobotClient(User robotUser, int sessionId, int index);
    	
    public AbstractRobotClient allocNewClient(int sessionId) {        	
    	int index = allocIndex();    	
    	if (!isValidIndex(index)){
    		ServerLog.info(sessionId, "<allocNewClient> allocating robot failed !");
    		return null;
    	}
    	
    	User robotUser = findRobotByIndex(index);
    	AbstractRobotClient client = null;
    	if (robotUser != null ) {
    		 ServerLog.info(sessionId, "<allocNewClient> alloc robot "+robotUser.getNickName()+", try to connect.");
    		 client = createRobotClient(robotUser, sessionId, index);
    	}
    	
		return client;
	}
	
   
	

	private boolean isValidIndex(int index) {
		return (index >= 0);
	}
	
	public void deallocClient(AbstractRobotClient robotClient) {
		if (robotClient == null)
			return;
		
		this.deallocIndex(robotClient.getClientIndex());				
	} 
	
	public synchronized User findRobotByIndex (int index) {
			if (robotUserList != null && !robotUserList.isEmpty() && index < robotUserList.size()) {
				return robotUserList.get(index);
			}
		return null;
	}
	
	public List<User> findRobots () {
		BasicDBObject query = new BasicDBObject(DBConstants.F_ISROBOT, 1);
//		BasicDBObject orderBy = new BasicDBObject(DBConstants.F_USERID, 1);
		List<User> list = new ArrayList<User>();
        DBCursor cursor = mongoClient.find(DBConstants.T_USER, query, null, 0, 100);
        if (cursor != null) {
			while (cursor.hasNext()) {
				DBObject dbObject = (DBObject) cursor.next();
				if ( ! dbObject.get(DBConstants.F_USERID).equals(CUSTOMER_SERVER_ID)) {
					list.add(new User(dbObject));
				}
			}
			cursor.close();
		}
        return list;
	}
	
	public void updateRobotAvatarAndNickName() {
//		RobotInfoUpdater.getInstance().updateRobotAvatarAndNickName(mongoClient, robotUserList);
	}
	
	// when update robots' infos in db, should call this method to reload the list.
	public synchronized void reloadRobotUserList() {
		robotUserList.clear();
		robotUserList.addAll(findRobots());
		ServerLog.info(0, "<AbstractRobotManager> Reload robot users list done !");
	}
}
