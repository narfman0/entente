package com.blastedstudios.entente;

import com.badlogic.gdx.net.Socket;
import com.google.protobuf.Message;

public interface IMessageListener {
	void receive(Message object, Socket origin);
}
