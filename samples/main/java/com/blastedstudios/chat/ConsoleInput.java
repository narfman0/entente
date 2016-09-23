package com.blastedstudios.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Class to read input from the console in a non-blocking way.
 */
public class ConsoleInput {
	private final Thread thread;
	private final List<String> lines = Collections.synchronizedList(new LinkedList<>());
	
	public ConsoleInput(){
		thread = new Thread(() -> {while(true){ readInput(); }}, ConsoleInput.class.getSimpleName() + "Thread");
		thread.setDaemon(true);
		thread.start();
	}
	
	private void readInput(){
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			lines.add(br.readLine());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @return list of lines that have been typed since the last fetch
	 */
	public List<String> fetch(){
		List<String> result = new ArrayList<>(lines);
		lines.clear();
		return result;
	}
}
