package com.blastedstudios.chat;

import java.net.Socket;

import com.blastedstudios.chat.ChatMessages.Text;
import com.blastedstudios.entente.BaseNetwork;
import com.blastedstudios.entente.Host;
import com.blastedstudios.entente.IMessageListener;

public class ChatServer {
	public static void main(String[] args) {
		BaseNetwork.registerMessageOrigin(ChatMessages.class);
		final Host network = new Host(43216);
		network.subscribe(Text.class, new IMessageListener<Text>(){
			public void receive(Text message, Socket origin){
				System.out.println("Received text with content: " + message.getContent());
				// If this were a multi-client server, we would relay the messages again with send here
			}
		});
		
		final ConsoleInput input = new ConsoleInput(); 
		while(true){
			network.update();
			for(String content : input.fetch()){
				Text.Builder builder = Text.newBuilder();
				builder.setContent(content);
				network.send(builder.build());
			}
		}
	}
}
