package com.orange.game.traffic.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import org.apache.log4j.Logger;

public class ServerMonitor {

	private static final Logger logger = Logger.getLogger(ServerMonitor.class
			.getName());

	// don't change this constant, it needs to align with nagios configuration
	private static final String ALIVE_CONSTANT = "alive";
	private static final String TOO_SLOW_CONSTANT = "slow";

	// Server socket
	private ServerSocket srv;

	// Flag that indicates whether the poller is running or not.
	volatile private boolean isRunning = true;

	private static ServerMonitor serverMonitor = new ServerMonitor();

	private ServerMonitor() {
	}

	public static ServerMonitor getInstance() {
		return serverMonitor;
	}

	// Method for terminating the listener
	public void terminate() {
		this.isRunning = false;
	}

	private int getPort() {
		String port = System.getProperty("server_monitor.port");
		if (port != null && !port.isEmpty()) {
			return Integer.parseInt(port);
		}
		return 6000; // default
	}

	public void start() {
		int port = getPort();
		try {
			srv = new ServerSocket(port);
			logger.info("Start Nagios Monitor At Port " + port);
		} catch (Exception e) {
			logger.error("Can't start server monitor at port+" + port
					+ ", exception=" + e.toString(), e);
		}
		this.isRunning = true;
		
		Thread thread = new Thread(new Runnable() {
			
			@Override
			/**
			 * This method start the thread and performs all the operations.
			 */
			public void run() {
				
				logger.info("Server Monitor Thread Starts...");
				
				try {
					// Wait for connection from client.
					while (isRunning) {
						Socket socket = srv.accept();

						// Open a reader to receive (and ignore the input)
						BufferedReader rd = new BufferedReader(new InputStreamReader(
								socket.getInputStream()));

						// Write the status message to the outputstream
						try {
							BufferedWriter wr = new BufferedWriter(
									new OutputStreamWriter(socket.getOutputStream()));
							
							if (GameServerHandler.isProcessMessageTooSlow()){
								wr.write(TOO_SLOW_CONSTANT);
							}
							else{
								wr.write(ALIVE_CONSTANT); // don't change this constant, it
															// needs to align with nagios
															// configuration
							}
							wr.flush();
							
							Thread.sleep(10*1000);
							
							wr.close();
						} catch (Exception e) {
							logger.error("<ServerMonitor>Caught an exception: "
									+ e.getMessage()
									+ ". Can't check the server status!", e);													
						}
						
						// Close the inputstream since we really don't care about it
						rd.close();
						socket.close();
						
					} // end while
				} catch (Exception e) {
					logger.error("<ServerMonitor>Caught an exception: "
							+ e.getMessage() + ". Can't check the server status!");
				}
			} // end run()
		});
		
		thread.start();
	}

	
}
