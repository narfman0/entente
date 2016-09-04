package com.blastedstudios.entente;

import java.io.IOException;
import java.net.Socket;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;

public class HostStruct {
	public final Socket socket;
	public final CodedOutputStream outStream;
	public final CodedInputStream inStream;
	
	public HostStruct(Socket socket){
		this.socket = socket;
		CodedInputStream istream = null;
		CodedOutputStream ostream = null;
		try {
			istream = CodedInputStream.newInstance(socket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			ostream = CodedOutputStream.newInstance(socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		inStream = istream;
		outStream = ostream;
	}
	
	public boolean isConnected(){
		return socket != null && socket.isConnected();
	}
	
	public String toString(){
		return socket.toString();
	}
}
