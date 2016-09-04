package com.blastedstudios.entente;

import java.net.Socket;

import com.google.protobuf.Message;

public interface IMessageListener<T extends Message> {
	void receive(T object, Socket origin);
}
