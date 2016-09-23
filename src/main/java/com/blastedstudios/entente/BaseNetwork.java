package com.blastedstudios.entente;

import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

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
	 * Send a network message of the given type to connected host(s)
	 */
	public void send(Message message, List<Socket> destinations) {
		sendQueue.add(new MessageStruct(message, destinations));
	}

	/**
	 * @see BaseNetwork#send(Message, List)
	 */
	public void send(Message message) {
		sendQueue.add(new MessageStruct(message, null));
	}
	
	/**
	 * Listen to a particular message
	 * @param clazz: protobuf message class to which we will listen
	 * @param listener: callback upon reception of message of type @param clazz
	 */
	public void subscribe(Class<?> clazz, IMessageListener listener){
		if(!listeners.containsKey(clazz))
			listeners.put(clazz, new LinkedList<>());
		listeners.get(clazz).add(listener);
	}
	
	/**
	 * Remove @param listener from receiving messages of type @param clazz 
	 */
	public void unsubscribe(Class<?> clazz, IMessageListener listener){
		if(listeners.containsKey(clazz))
			listeners.get(clazz).remove(listener);
	}
	
	/**
	 * Unsubscribe listener from receiving any messages
	 */
	public void unsubscribe(IMessageListener listener){
		for(List<IMessageListener> listenerList : listeners.values())
			listenerList.remove(listener);
	}
	
	/**
	 * Remove all listeners
	 */
	public void clearListeners(){
		listeners.clear();
	}

	/**
	 * Close any sockets and cleanly disconnect from remote(s)
	 */
	public abstract void dispose();
	
	/**
	 * @return true if connected to a remote
	 */
	public abstract boolean isConnected();
	
	/**
	 * Tick the socket to receive and send messages
	 * @return true if we are still connected
	 */
	public abstract boolean update();
	
	/**
	 * Send each message in queue to @param target hosts
	 * @param target: Destination host to which we shall send @param messages
	 */
	protected void sendMessages(HostStruct target) throws IOException{
		// Build new list of messages to send this frame. Grab messages initially, don't check queue again!
		ArrayList<MessageStruct> currentQueue = new ArrayList<>(sendQueue);
		// "but jrob, thats a queue that could be modified between copying and clearing, you should iterate..."
		// GTFO /uninstall /uninstall /uninstall
		// no but you're right... *shrugs*
		sendQueue.clear();
		for(MessageStruct sendStruct : currentQueue){
			if(sendStruct.destinations == null || sendStruct.destinations.contains(target.socket)){
				target.outStream.writeInt(messageToID.get(sendStruct.message.getClass()));
				target.outStream.writeInt(sendStruct.message.getSerializedSize());
				target.outStream.write(sendStruct.message.toByteArray());
				Logger.getLogger(BaseNetwork.class.getName()).fine("Sent message successfully: " + sendStruct.message.getClass().getSimpleName());
			}
		}
		target.outStream.flush();
	}
	
	protected void receiveMessages(DataInputStream stream, Socket socket) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException{
		List<MessageStruct> messages = new LinkedList<>();
		while(socket.getInputStream().available() > 0 && socket.isConnected()){
			int messageID = stream.readInt();
			int length = stream.readInt();
			byte[] buffer = new byte[length];
			stream.read(buffer);
			Method messageParseID = idToParseMethod.get(messageID);
			Message message = (Message)messageParseID.invoke(idToMessage.get(messageID), buffer);
			messages.add(new MessageStruct(message, Arrays.asList(socket)));
			Logger.getLogger(BaseNetwork.class.getName()).fine("Received from: " + socket.toString());
		}
		for(MessageStruct message : messages){
			List<IMessageListener> msgListeners = listeners.get(message.message.getClass());
			if(msgListeners != null)
				for(IMessageListener listener : msgListeners)
					listener.receive(message.message, socket);
		}
	}
}
