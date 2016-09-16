package com.blastedstudios.entente;

import java.net.Socket;

import com.google.protobuf.Message;

/**
 * Listener for a particular type of message
 */
public interface IMessageListener<T extends Message> {
	/**
	 * Upon reception of message via network, callback will be triggered with
	 * originations socket @param origin
	 * @param object: protobuf message to receive
	 * @param origin: socket from which message was received
	 */
	void receive(T object, Socket origin);
}
