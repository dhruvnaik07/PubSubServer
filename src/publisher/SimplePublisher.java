package publisher;

import common.TopicMessage;

import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.Scanner;

/**
 * Created by Dhruv Naik on 4/8/2018.
 * 
 */

public class SimplePublisher {

	private final Socket cs;
	private final DataInputStream dis;
	private final DataOutputStream dos;

	public SimplePublisher(String serverHost, int serverPort) throws IOException {
		this.cs = new Socket(serverHost, serverPort);
		dis = new DataInputStream(cs.getInputStream());
		dos = new DataOutputStream(cs.getOutputStream());
		System.out.println("Publisher connected to server " + serverHost + ":" + serverPort);
	}

	public void send(TopicMessage msg) throws IOException {
		msg.put(dos);
		dos.flush();
	}

	public static void main(String[] args) {
		try {
			Scanner scanner = new Scanner(System.in);
			SimplePublisher publisher = new SimplePublisher(args[0], Integer.parseInt(args[1]));
			for (;;) {
				String topic = scanner.nextLine();
				String msg = scanner.nextLine();
				publisher.send(new TopicMessage(topic, msg, new Date(System.currentTimeMillis())));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}