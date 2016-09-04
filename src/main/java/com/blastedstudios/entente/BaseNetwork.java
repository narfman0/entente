package com.blastedstudios.entente;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import com.badlogic.gdx.net.Socket;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.Message;

@SuppressWarnings({ "rawtypes", "unchecked" })
public abstract class BaseNetwork {
	private static final HashMap<Integer, Method> idToParseMethod = new HashMap<>();
	private static final HashMap<Integer, Class<?>> idToMessage = new HashMap<>();
	private static final HashMap<Class<?>, Integer> messageToID = new HashMap<>();
	protected final LinkedList<MessageStruct> sendQueue = new LinkedList<>();
	private final HashMap<Class<?>, List<IMessageListener>> listeners = new HashMap<>();
	private static int nextMessageIdentifier = 1;
	
	/**
	 * Register a protocol buffer generated class as something to be ingested across the network
	 * @param parentClass: generated class from protobuf
	 */
	public static void registerMessageOrigin(Class<?> parentClass){
		for(Class<?> clazz : parentClass.getClasses())
			try {
				if(!Message.class.isAssignableFrom(clazz))
					continue;
				Method parseMethod = clazz.getMethod("parseFrom", byte[].class);
				messageToID.put(clazz, nextMessageIdentifier);
				idToMessage.put(nextMessageIdentifier, clazz);
				idToParseMethod.put(nextMessageIdentifier++, parseMethod);
			} catch (Exception e) {
				Logger.getLogger(BaseNetwork.class.getName()).warning("Exception caching parse method for: " + clazz);
			}
	}
	
	/**
	 * Distribute message to all listeners
	 * a.k.a. receive, heed, execute, send
	 */
	public void receiveMessage(Message message, Socket origin){
		List<IMessageListener> msgListeners = listeners.get(message.getClass());
		if(msgListeners != null)
			for(IMessageListener listener : msgListeners)
				listener.receive(message, origin);
	}

	/**
	 * Send a network message of the given type to connected host(s)
	 */
	public void send(Message message, List<Socket> destinations) {
		sendQueue.add(new MessageStruct(message, destinations));
	}

	public void send(Message message) {
		sendQueue.add(new MessageStruct(message, null));
	}
	
	public void subscribe(Class<?> clazz, IMessageListener listener){
		if(!listeners.containsKey(clazz))
			listeners.put(clazz, new LinkedList<>());
		listeners.get(clazz).add(listener);
	}
	
	public void unsubscribe(Class<?> clazz, IMessageListener listener){
		if(listeners.containsKey(clazz))
			listeners.get(clazz).remove(listener);
	}
	
	public void unsubscribe(IMessageListener listener){
		for(List<IMessageListener> listenerList : listeners.values())
			listenerList.remove(listener);
	}
	
	public void clearListeners(){
		listeners.clear();
	}

	public abstract void dispose();
	public abstract boolean isConnected();
	public abstract void update();
	
	protected static void sendMessages(List<MessageStruct> messages, HostStruct target) throws IOException{
		for(MessageStruct sendStruct : messages){
			if(sendStruct.destinations == null || sendStruct.destinations.contains(target.socket)){
				try {
					target.outStream.writeUInt32NoTag(messageToID.get(sendStruct.message.getClass()));
					target.outStream.writeUInt32NoTag(sendStruct.message.getSerializedSize());
					target.outStream.writeRawBytes(sendStruct.message.toByteArray());
					Logger.getLogger(BaseNetwork.class.getName()).fine("Sent message successfully: " + sendStruct.message.getClass().getSimpleName());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		target.outStream.flush();
	}
	
	protected static List<MessageStruct> receiveMessages(CodedInputStream stream, Socket socket){
		List<MessageStruct> messages = new LinkedList<>();
		try {
			while(socket.getInputStream().available() > 0 && socket.isConnected()){
				int messageID = stream.readUInt32();
				int length = stream.readUInt32();
				byte[] buffer = stream.readRawBytes(length);
				Method messageParseID = idToParseMethod.get(messageID);
				Message message = (Message)messageParseID.invoke(idToMessage.get(messageID), buffer);
				messages.add(new MessageStruct(message, Arrays.asList(socket)));
				Logger.getLogger(BaseNetwork.class.getName()).fine("Received from: " + socket.getRemoteAddress());
			}
		} catch (Exception e) {
			Logger.getLogger(BaseNetwork.class.getName()).severe("Exception reading message, message: " + e.getMessage());
			e.printStackTrace();
		}
		return messages;
	}
}
