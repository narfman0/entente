package com.blastedstudios.entente;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.logging.Logger;

public class Client extends BaseNetwork {
	private HostStruct hostStruct;
	
	public boolean connect(String host, int port){
		try {
			Socket socket = new Socket(host, port);
			hostStruct = new HostStruct(socket);
			Logger.getLogger(this.getClass().getName()).fine("Connected to server: " + socket.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return isConnected();
	}
	
	@Override public void update(){
		if(!isConnected())
			return;
		try {
			List<MessageStruct> messages = receiveMessages(hostStruct.inStream, hostStruct.socket);
			for(MessageStruct message : messages){
				receiveMessage(message.message, hostStruct.socket);
				Logger.getLogger(this.getClass().getName()).fine("Message received: " + message.message);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
			dispose();
		}
		try{
			sendMessages(sendQueue, hostStruct);
		}catch(IOException e){
			e.printStackTrace();
			dispose(); //TODO send message internally telling client we disconnected. :(
		}
		sendQueue.clear();
	}
	
	@Override public void dispose(){
		if(hostStruct != null && hostStruct.socket != null)
			try {
				hostStruct.socket.close();
			} catch (IOException e) {}
		hostStruct = null;
	}

	@Override public boolean isConnected() {
		return hostStruct != null && hostStruct.socket != null && hostStruct.socket.isConnected();
	}
}
