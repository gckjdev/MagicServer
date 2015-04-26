package com.orange.game.api;

import com.orange.barrage.model.chat.OnlineAgentManager;
import com.orange.common.api.CommonApiServer;
import com.orange.common.api.service.ServiceHandler;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.common.redis.RedisClient;
import com.orange.game.api.service.GameServiceFactory;
import com.orange.game.model.service.DBService;

public class ChatCoreServer extends CommonApiServer {
	
	public static final String VERSION_STRING = ChatCoreServer.class.getName() + " Version 0.1";
	public static final String SPRING_CONTEXT_FILE = "classpath:/com/orange/game/api/applicationContext.xml";	
	public static final String LOG4J_FLE = "classpath:/log4j.properties";
//	public static final String MONGO_SERVER = "localhost";
//	public static final String MONGO_DB_NAME = "groupbuy";
//	public static final String MONGO_USER = "";
//	public static final String MONGO_PASSWORD = "";
	public static final GameServiceFactory serviceFactory = new GameServiceFactory();
	
	private static final MongoDBClient mongoClient = DBService.getInstance().getMongoDBClient(); //new MongoDBClient(DBConstants.D_GAME);
//	private static final MongoDBClient mongoClientForBlack = new MongoDBClient(DBConstants.D_GAME);

	@Override
	public String getAppNameVersion() {
		return VERSION_STRING;
	}

	@Override
	public String getLog4jFile() {
		return LOG4J_FLE;
	}

	@Override
	public ServiceHandler getServiceHandler() {
		return ServiceHandler.getServiceHandler(mongoClient, serviceFactory);
	}

	@Override
	public String getSpringContextFile() {
		return SPRING_CONTEXT_FILE;
	}
	
	@Override
	public int getPort() {
		String port = System.getProperty("server.port");
		if (port != null && !port.isEmpty()){
			return Integer.parseInt(port);
		}
		return 8100;
	}
	
    public static void main(String[] args) throws Exception{



        final ChatCoreServer server = new ChatCoreServer();

        if (OnlineAgentManager.getInstance().getOnlineAgents().size() == 0) {
            OnlineAgentManager.getInstance().initAgents();
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {

                try {
                    server.stop();
                } catch (Exception e) {
                    log.error("catch exception while stop JETTY server, exception = " + e.toString(), e);
                }

                log.info("===================== SHUTDOWN HOOK CATCH =====================");


                DBService.getInstance().shutdown();
				RedisClient.getInstance().destroyPool();

				log.info("===================== SHUTDOWN HOOK COMPLETE =====================");
			} 
		});

//        InviteCodeManager.getInstance().createAllCandidateCode();

//        IndexMonitorManager.getInstance().resetOngoingIndex();

		// This code is to initiate the listener.
//		ServerMonitor.getInstance().start();

//        QiuNiuService.getInstance().getUpToken();

		server.startServer();
    }



}


