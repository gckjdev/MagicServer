package com.orange.game.traffic.robot.client;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.orange.common.log.ServerLog;

public class RobotService {	

	// thread-safe singleton implementation
    private static RobotService service = new RobotService();     
    private RobotService() {} 	    
    public static RobotService getInstance() { 
    	return service; 
     }
	
//    ExecutorService executor = Executors.newFixedThreadPool(AbstractRobotManager.MAX_ROBOT_USER);
    ExecutorService executor = Executors.newCachedThreadPool();
    final private ScheduledExecutorService scheduleService = Executors.newScheduledThreadPool(1);
    
    AbstractRobotManager robotManager;
	
    public void initRobotManager(AbstractRobotManager robotManager){
    	this.robotManager = robotManager;
    }
    
    public boolean isEnableRobot() {
		String robot = System.getProperty("config.robot");
		if (robot != null && !robot.isEmpty()){
			return (Integer.parseInt(robot) == 1);
		}
		return false; // default
	}	
	
    public void startOneRobot(int sessionId) {
    	startRobot(sessionId, 1);
	}
    
    public void startRobot(int sessionId, int robotCount) {
    	if (!isEnableRobot()){
    		ServerLog.info(sessionId, "Robot not enabled for launch");
    		return;
    	}
    	
    	for (int i=0; i<robotCount; i++){
    		AbstractRobotClient client = robotManager.allocNewClient(sessionId); 
	    	if (client == null){
	    		ServerLog.info(sessionId, "start new robot but no robot client available");
	    		return;
	    	}

	    	executor.execute(client);
    	}
    	
	}
    
    public void startNewRobot(int sessionId) {
    	if (!isEnableRobot()){
    		ServerLog.info(sessionId, "Robot not enabled for launch");
    		return;
    	}
    	
    	int robotCount = getRobotCountPerTime();
    	
    	for (int i=0; i<robotCount; i++){
    		AbstractRobotClient client = robotManager.allocNewClient(sessionId); 
	    	if (client == null){
	    		ServerLog.info(sessionId, "start new robot but no robot client available");
	    		return;
	    	}
	    	
	    	postStartRobot(client);
	    	
	    	executor.execute(client);
    	}
    	
	}
    
	private void postStartRobot(AbstractRobotClient robotClient) {
		robotClient.chargeBalance();
	}
	
	private int getRobotCountPerTime() {
		String robot = System.getProperty("config.robot_count");
		if (robot != null && !robot.isEmpty()){
			return Integer.parseInt(robot);
		}
		return 1; // default
	}
	
	public void finishRobot(final AbstractRobotClient robotClient) {		
		executor.execute(new Runnable(){
			@Override
			public void run() {
				robotManager.deallocClient(robotClient);
				robotClient.stopClient();
			}		
		});
	} 	
	
	public ScheduledFuture<Object> schedule(Callable<Object> callable, int delaySeconds){
		return scheduleService.schedule(callable, delaySeconds, TimeUnit.SECONDS);
	}

}
