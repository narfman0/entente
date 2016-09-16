package com.blastedstudios.entente;

import java.net.Socket;
import java.util.List;

import com.google.protobuf.Message;

/**
 * Holder for messages to send to target socket(s)
 */
public class MessageStruct{
	public final Message message;
	public final List<Socket> destinations;
	
	public MessageStruct(Message message, List<Socket> destinations){
		this.message = message;
		this.destinations = destinations;
	}
	
	@Override public String toString(){
		return "[MessageStruct message: " + message.toString() + "]";
	}
}