package com.blastedstudios.entente;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

/**
 * Host class to distribute messages to client(s) 
 */
public class Host extends BaseNetwork{
	private final List<HostStruct> clients = Collections.synchronizedList(new LinkedList<>());
	private ServerSocket serverSocket;
	private Timer timer;
	
	public Host(int port){
		try {
			serverSocket = new ServerSocket(port);
			timer = new Timer("Server accept thread");
			timer.schedule(new TimerTask() {
				@Override public void run() {
					try{
						Socket socket = serverSocket.accept();
						HostStruct client = new HostStruct(socket); 
						clients.add(client);
						Logger.getLogger(this.getClass().getName()).fine("Added client: " + socket.toString());
					}catch(Exception e){
						Logger.getLogger(this.getClass().getName()).warning("Exception received, aborting host thread. Message: " + e.getMessage());
						this.cancel();
					}
				}
			}, 0, 100);
			Logger.getLogger(this.getClass().getName()).info("Network created, listening for conenctions on port: " + port);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	/**
	 * Receive messages from all clients and ingest to a queue for later disbursement. Send what messages have
	 * been prepared.
	 */
	@Override public boolean update(){
		synchronized (clients) {
			for(Iterator<HostStruct> iter = clients.iterator(); iter.hasNext();){
				HostStruct client = iter.next();
				if(!client.socket.isConnected()){
					Logger.getLogger(this.getClass().getName()).info("Disconnected client: " + client);
					iter.remove();
					closeClient(client);
				}else{
					try{
						receiveMessages(client.inStream, client.socket);
						sendMessages(client);
					} catch (Exception e) {
						Logger.getLogger(this.getClass().getName()).info("Disconnected client: " + client + " from exception with message: " + e.getMessage());
						iter.remove();
						closeClient(client);
						e.printStackTrace();
					}
				}
			}
		}
		return true;
	}
	
	@Override public void dispose(){
		if(serverSocket != null)
			try {
				serverSocket.close();
			} catch (IOException e) {}
		serverSocket = null;
		for(HostStruct client : clients)
			closeClient(client);
		if(timer != null)
			timer.cancel();
		timer = null;
	}
	
	private static void closeClient(HostStruct client){
		try {
			client.socket.close();
		} catch (IOException e) {}
	}

	@Override public boolean isConnected() {
		return serverSocket != null;
	}
}
