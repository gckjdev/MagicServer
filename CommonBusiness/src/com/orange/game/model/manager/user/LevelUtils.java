package com.orange.game.model.manager.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;



public class LevelUtils {

	static Logger logger = Logger.getLogger("LevelUtils");
	
	private final static int MAX_LEVEL 					= 99; 
	private final static double FIRST_LEVEL_EXP   	= 60.0f;
	private final static double EXP_INC_RATE  		= 1.08;

	final static List<Long> levelExpList = new ArrayList<Long>();
	final static HashMap<Integer, Long> levelExpMap = initLevelExpMap();
		
	private static HashMap<Integer, Long> initLevelExpMap(){
		
		HashMap<Integer, Long> map = new HashMap<Integer, Long>();
		
	    double exp = 0.0f;
	    double lastLevelUpExp = 0.0f;
	    
	    for (int i = 0; i <= MAX_LEVEL; i++) {
	        if (i <= 5) {            
	            lastLevelUpExp = FIRST_LEVEL_EXP*i;
	            exp = exp+lastLevelUpExp;
	        } else if (i > 90) {
	            lastLevelUpExp = lastLevelUpExp*2.0f;
	            exp = exp+lastLevelUpExp;
	        } else {
	            lastLevelUpExp = lastLevelUpExp*EXP_INC_RATE;
	            exp = exp+lastLevelUpExp;
	        }
	        
	        map.put(i+1, Long.valueOf((long)exp));
	        levelExpList.add((long)exp);
	    }
	    	    
	    logger.info("<initLevelExpMap> map="+map.toString());
	    return map;
	}
	
	public static long getExpByLevel(int level){
		return levelExpList.get(level);
	}
	
	public static int getLevelByExp(long exp){
	    long maxExp = levelExpList.get(levelExpList.size()-1);
	    if (exp >= maxExp) {
	        return MAX_LEVEL;
	    }
	    
	    for (int i = 1; i < MAX_LEVEL-1; i ++) {
	    	
	    	long low = levelExpList.get(i-1);
	    	long high = levelExpList.get(i);	    	
	        if (exp >= low && exp <high) {
	            return i;
	        }
	    }
	    return 1;
	}
	
	
}
