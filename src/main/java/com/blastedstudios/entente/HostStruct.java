package com.blastedstudios.entente;

import com.badlogic.gdx.net.Socket;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;

public class HostStruct {
	public final Socket socket;
	public final CodedOutputStream outStream;
	public final CodedInputStream inStream;
	
	public HostStruct(Socket socket){
		this.socket = socket;
		inStream = CodedInputStream.newInstance(socket.getInputStream());
		outStream = CodedOutputStream.newInstance(socket.getOutputStream());
	}
	
	public boolean isConnected(){
		return socket != null && socket.isConnected();
	}
	
	public String toString(){
		return socket.getRemoteAddress();
	}
}
