package com.orange.game.model.service.opus;

import com.orange.game.model.dao.opus.Opus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OpusNotificationService {

	// thread-safe singleton implementation
	private static OpusNotificationService service = new OpusNotificationService();     
	private OpusNotificationService(){		
		super();
	} 	    	
	public static OpusNotificationService getInstance() { 
		return service; 
	}

    ExecutorService executor = Executors.newFixedThreadPool(10);
	final CopyOnWriteArrayList<OpusNotificationInterface> observerList = new CopyOnWriteArrayList<OpusNotificationInterface>();
	
	public void registerNotification(OpusNotificationInterface obj){
		if (observerList.indexOf(obj) == -1){
			return;
		}
		
		observerList.add(obj);
	}
	
	public void unregisterNotification(OpusNotificationInterface obj){
		observerList.remove(obj);
	}
	
	public void notifyOpusCreateOrUpdate(String userId, String opusId, double opusScore){
	}

    public void notifyOpusCreate(final String userId, final Opus opus) {
        for (final OpusNotificationInterface obj : observerList){
            if (obj.processNotificationAtBackground()){
                executor.execute(new Runnable(){
                    public void run() {
                        obj.notifyOpusCreate(userId, opus);
                    }
                });
            }
            else{
                obj.notifyOpusCreate(userId, opus);
            }
        }
    }


}
