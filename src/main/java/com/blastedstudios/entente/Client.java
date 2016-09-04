package com.blastedstudios.entente;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.Protocol;
import com.badlogic.gdx.net.Socket;

public class Client extends BaseNetwork {
	private HostStruct hostStruct;
	
	public boolean connect(String host, int port){
		Socket socket = Gdx.net.newClientSocket(Protocol.TCP, host, port, null);
		hostStruct = new HostStruct(socket);
		Logger.getLogger(this.getClass().getName()).fine("Connected to server: " + socket.getRemoteAddress());
		return isConnected();
	}
	
	@Override public void update(){
		if(!isConnected())
			return;
		List<MessageStruct> messages = receiveMessages(hostStruct.inStream, hostStruct.socket);
		for(MessageStruct message : messages){
			receiveMessage(message.message, hostStruct.socket);
			Logger.getLogger(this.getClass().getName()).fine("Message received: " + message.message);
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
			hostStruct.socket.dispose();
		hostStruct = null;
	}

	@Override public boolean isConnected() {
		return hostStruct != null && hostStruct.socket != null && hostStruct.socket.isConnected();
	}
}
