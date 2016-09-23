package com.blastedstudios.chat;

import java.net.Socket;

import com.blastedstudios.chat.ChatMessages.Text;
import com.blastedstudios.entente.BaseNetwork;
import com.blastedstudios.entente.Client;
import com.blastedstudios.entente.IMessageListener;

public class ChatClient {
	public static void main(String[] args) {
		BaseNetwork.registerMessageOrigin(ChatMessages.class);
		final Client network = new Client("localhost", 43216);
		network.subscribe(Text.class, new IMessageListener<Text>(){
			public void receive(Text message, Socket origin){
				System.out.println("Received text with content: " + message.getContent());
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
