package com.blastedstudios.entente;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Holds information for hosts, including the socket and streams
 */
class HostStruct {
	public final Socket socket;
	public final DataOutputStream outStream;
	public final DataInputStream inStream;
	
	public HostStruct(Socket socket){
		this.socket = socket;
		DataInputStream istream = null;
		DataOutputStream ostream = null;
		try {
			istream = new DataInputStream(socket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			ostream = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		inStream = istream;
		outStream = ostream;
	}
	
	/**
	 * @return true if socket is connected to remote
	 */
	public boolean isConnected(){
		return socket != null && socket.isConnected();
	}
	
	public String toString(){
		return socket.toString();
	}
}
