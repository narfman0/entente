package com.blastedstudios.entente;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.Protocol;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.Socket;

public class Host extends BaseNetwork{
	private final List<HostStruct> clients = Collections.synchronizedList(new LinkedList<HostStruct>());
	private ServerSocket serverSocket;
	private Timer timer;
	
	public Host(int port){
		serverSocket = Gdx.net.newServerSocket(Protocol.TCP, port, null);
		timer = new Timer("Server accept thread");
		timer.schedule(new TimerTask() {
			@Override public void run() {
				try{
					Socket socket = serverSocket.accept(null);
					HostStruct client = new HostStruct(socket); 
					clients.add(client);
					Logger.getLogger(this.getClass().getName()).fine("Added client: " + socket.getRemoteAddress());
				}catch(Exception e){
					Logger.getLogger(this.getClass().getName()).warning("Exception received, aborting host thread. Message: " + e.getMessage());
					this.cancel();
				}
			}
		}, 0, 100);
		Logger.getLogger(this.getClass().getName()).info("Network created, listening for conenctions on port: " + port);
	}
	
	@Override public void update(){
		// Build new list of messages to send this frame. Grab messages initially, don't check queue again!
		ArrayList<MessageStruct> currentQueue = new ArrayList<>(sendQueue);
		// "but jrob, thats a queue that could be modified between copying and clearing, you should iterate..."
		// GTFO /uninstall /uninstall /uninstall
		// no but you're right... *shrugs*
		sendQueue.clear();
		
		for(Iterator<HostStruct> iter = clients.iterator(); iter.hasNext();){
			HostStruct client = iter.next();
			if(!client.socket.isConnected()){
				Logger.getLogger(this.getClass().getName()).info("Disconnecting client: " + client.socket.getRemoteAddress());
				iter.remove();
			}else{
				List<MessageStruct> messages = receiveMessages(client.inStream, client.socket);
				for(MessageStruct struct : messages)
					receiveMessage(struct.message, client.socket);
				try{
					sendMessages(currentQueue, client);
				} catch (SocketException e1) {
					Logger.getLogger(this.getClass().getName()).info("Disconnected from server, removing client: " + client);
					iter.remove();
				} catch (IOException e) {
					Logger.getLogger(this.getClass().getName()).info("Disconnected from server?");
				}
			}
		}
	}
	
	@Override public void dispose(){
		if(serverSocket != null)
			serverSocket.dispose();
		serverSocket = null;
		for(HostStruct client : clients)
			client.socket.dispose();
		if(timer != null)
			timer.cancel();
		timer = null;
	}

	@Override public boolean isConnected() {
		return serverSocket != null;
	}
}
