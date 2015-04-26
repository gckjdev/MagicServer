
package com.orange.common.api;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.orange.common.api.service.ServiceHandler;

public abstract class CommonApiServer extends AbstractHandler
{
	public static final Logger log = Logger.getLogger(CommonApiServer.class.getName());		
	
	public abstract String getSpringContextFile();
	public abstract String getLog4jFile();	
	public abstract String getAppNameVersion();
	public abstract ServiceHandler getServiceHandler();	
	public abstract int getPort();		

	// not used so far
	private void readConfig(String filename){

		   InputStream inputStream = null;		   
		   try {
			   inputStream = new FileInputStream(filename);
		   } catch (FileNotFoundException e) {
			   log.info("configuration file "+filename+"not found exception");
			   e.printStackTrace();
		   }
		   Properties p = new Properties();   
		   try {   
			   p.load(inputStream);   
		   } catch (IOException e1) {   
			   log.error("read configuration file exception, exception="+e1.toString(), e1);
		   }
	}
		
	public static void initSpringContext(String... context) {
		try {
			new ClassPathXmlApplicationContext(
					context );
		} catch (Exception e) {
			log.info("initSpringContext exception");
			e.printStackTrace();
		}
	}
	
    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response) 
        throws IOException, ServletException
    {
        baseRequest.setHandled(true);        
		try{			
			
	        getServiceHandler().handlRequest(request, response);	        
                       
		} catch (Exception e){
			log.error("<handleHttpServletRequest> catch Exception="+e.toString(), e);		
		} finally {
		}		
    }

	public void startServer() throws Exception {
    	//init the spring context
		String[] springFiles = new String[] { getSpringContextFile() };
    	initSpringContext(springFiles);
    	
		log.info(getAppNameVersion());

        // preformance setttings, refer to http://wiki.eclipse.org/Jetty/Howto/High_Load
        // http://wiki.eclipse.org/Jetty/Howto/Garbage_Collection

        // create thread pool
        QueuedThreadPool pool = new QueuedThreadPool();
        pool.setMaxThreads(300);
        pool.setMinThreads(20);
        pool.setIdleTimeout(60000);
        pool.setDetailedDump(false);

		Server server = new Server(pool);

//		for (Connector connector : server.getConnectors()) {
//			connector.setRequestHeaderSize(1024 * 30);
//		}

        ServerConnector http = new ServerConnector(server);
        http.setPort(getPort());
        http.setIdleTimeout(30000);

        server.addConnector(http);
		server.setHandler(getHandler());

        server.setStopAtShutdown(true);
        server.start();
        server.join();
	}
	
	public Handler getHandler(){
		return this;
	}
}
