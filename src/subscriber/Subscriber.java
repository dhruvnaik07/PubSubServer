package subscriber;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import common.ClientServerConnection;
import common.ServerCommandMessage;
import common.TopicMessage;
import server.PublisherSocConnectionImpl;

/**
 * Created by Dhruv Naik on 4/8/2018.
 */

public class Subscriber implements SubscriberUIController {

	private final SubscriberServerConnection subServerConnection;
	private final SubscriberGUI userInterface;
	private String subscriberID;
	private ArrayList<String> subscribedTopics;
	private static Socket cs;
	private final DataInputStream dis = null;
	private final DataOutputStream dos = null;
	// private ConcurrentSkipListSet<String> topicMessages;
	private ServerCommandMessage topic;

	public Subscriber(ClientServerConnection serverConnection, SubscriberUserInterface userInterface) {
		this.userInterface = (SubscriberGUI) userInterface;
		this.subServerConnection = (SubscriberServerConnection) serverConnection;
		this.userInterface.setUIController(this);
	}

	// return user interface
	public SubscriberGUI getUserInterface() {
		return this.userInterface;
	}

	// alternative constructor to test GUI
	/*
	 * public Subscriber(SubscriberUserInterface userInterface) { this.userInterface
	 * = userInterface; this.subServerConnection = null; }
	 */

	@Override
	/**
	 * Initiate request to get published topics from the server
	 */
	public void getPublishedTopics() {
		// System.out.println("getPublishedTopics method called");
		Set<String> pubTopics = subServerConnection.getPublishedTopics();
		// Iterator<String> subbedTopics =
		// userInterface.getSubbedTopicsIterator();
		if (pubTopics.isEmpty()) {
			// System.out.println("published topics list is emptyyyyyy");
			return;
		} else {
			// System.out.println("published topics has items");
		}
		/*
		 * for (String subbedTopic : ) { if (subbedTopics.contains(topic)){
		 * pubTopics.remove(topic); } }
		 */
		this.userInterface.setPublishedTopics(pubTopics);
	}

	@Override
	/**
	 * Initiate request to get subscribed topics from the server
	 */
	public void getSubscribedTopics() {
		Set<String> subTopics = subServerConnection.getSubscribedTopics();
		if (subTopics.isEmpty()) {
			System.out.println("subscribed topics list is emptyyyyyy");
			return;
		} else {
			System.out.println("subscribed topics has items");
		}
		for (String sub : subTopics) {
			System.out.println(sub);
		}
		this.userInterface.setSubscribedTopics(subTopics);
	}

	@Override
	/**
	 * Initiate request to subscribe to a topic
	 * 
	 * @param topic
	 */
	public void subscribeTopic(String topic) {
		boolean subscribed = subServerConnection.subscribe(topic);
		System.out.println("Returned from subscribe call. subscribed value is: " + subscribed);
		if (subscribed) {
			this.userInterface.postServerMessage("Subscribed to " + topic);
		} else {
			this.userInterface.postServerMessage("Could not subscribe to " + topic);
		}
	}

	@Override
	/**
	 * Initiate request to unsubscribe to a topic
	 * 
	 * @param topic
	 */
	public void unsubscribeTopic(String topic) {
		boolean unsubscribed = subServerConnection.unsubscribe(topic);
		System.out.println("Returned from unsubscribe call. unsubscribed value is: " + unsubscribed);
		if (unsubscribed) {
			this.userInterface.postServerMessage("Unsubscribed from " + topic);
		} else {
			this.userInterface.postServerMessage("Could not unsubscribe from " + topic);
		}
	}

	@Override
	public void onReceivedTopicMessage(TopicMessage topicMessage) {
		System.out.println(topicMessage.toString());
	}

	public void setSubscriberID(String subscriberID) {
		this.subscriberID = subscriberID;
	}

	public static void main(String[] args) {
		// display server status before opening gui
		// create clientserverconnection implementation on subscriber side as
		// well with diff implementation (just have socket connection args as
		// parameters)
		ServerCommandMessage topic;
		SubscriberServerConnection conn = null;
		try {
			conn = new SubscriberServerConnection(args[0], Integer.parseInt(args[1]));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// e.printStackTrace();
		}
		if (conn.isAlive()) {
			System.out.println(ErrorMessage.SERVER_UP.toString());
			System.out.println("Subscriber connected to server " + args[0] + ": " + Integer.parseInt(args[1]));
			Scanner scanner = new Scanner(System.in);
			System.out.println("Enter user id: ");
			String subscriberID = scanner.nextLine();
			System.out.println("Please wait...");
			SubscriberGUI subGUI = new SubscriberGUI();
			Subscriber test1 = new Subscriber(conn, subGUI);
			test1.userInterface.setUIController(test1);

			boolean loggedIn = test1.subServerConnection.login(subscriberID);

			if (loggedIn) {
				System.out.println("Login Successful");
				subGUI.startUser(subscriberID);
				subGUI.setServerStatus(ErrorMessage.SERVER_UP);
			} else {
				System.out.println("Already Connected");
				System.exit(0);
			}

			test1.subServerConnection.setSubscriber(test1);

			test1.subServerConnection.start();

			test1.getSubscribedTopics();

			test1.getPublishedTopics();

			try {
				while (true) {
				}
			} catch (Exception e) {
				test1.userInterface.setServerStatus(ErrorMessage.SERVER_DOWN);
				test1.userInterface.disableButtons();
			}

		}

	}
}