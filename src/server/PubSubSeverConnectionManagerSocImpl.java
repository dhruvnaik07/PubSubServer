package server;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentSkipListSet;

import common.ClientServerConnection;
import common.TopicMessage;

/**
 * 
 * @author Aditi Sharma
 *
 */
public class PubSubSeverConnectionManagerSocImpl implements ServerConnectionManager {

	public ServerSocket subscriberSocket = null;
	public ServerSocket publisherSocket = null;
	Socket publisher;
	Socket subscriber;
	ServerController controller = null;
	ConcurrentSkipListSet<Integer> connectionList = null;
	PubSubSeverConnectionManagerSocImpl connMgr = null;
	private static volatile int subsriberId = 0;
	private static volatile int publisherId = 0;
	ConcurrentSkipListSet<TopicMessage> topicList = null;

	public PubSubSeverConnectionManagerSocImpl(int publisherPort, int subscriberPort) {
		try {
			publisherSocket = new ServerSocket(publisherPort);
			subscriberSocket = new ServerSocket(subscriberPort);
			connMgr = this;
			this.topicList = new ConcurrentSkipListSet<>();
			this.connectionList = new ConcurrentSkipListSet<>();
		} catch (BindException e) {
			System.out.println("Socket already binded to the port. Try using another port no");
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void start() {
		System.out.println("Server running. Waiting for connections");
		while (true) {
			try {
				// New thread for the accepting the connections from the Publishers
				new Thread() {
					public void run() {
						while (true) {
							try {
								publisher = publisherSocket.accept();
								publisherId++;
								PublisherSocConnectionImpl pubSocConnectionImpl = (PublisherSocConnectionImpl) connMgr
										.createPublisherConnection(connMgr);
								System.out.println("1 Publisher connected");
								Thread t = new Thread(pubSocConnectionImpl);
								t.start();
								connMgr.addConnection(publisherId);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				}.start();
				// Different thread for accepting the connections from the Subscribers
				subscriber = subscriberSocket.accept();
				subsriberId++;
				SubscriberSocConnectionImpl subscriberSocConnectionImpl = (SubscriberSocConnectionImpl) this
						.createSubscriberConnection(this);
				System.out.println("1 Subscriber connected");
				Thread t = new Thread(subscriberSocConnectionImpl);
				t.start();
				connMgr.addConnection(subsriberId);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

	@Override
	public void setServerController(ServerController controller) {
		this.controller = controller;

	}

	@Override
	public ClientServerConnection createSubscriberConnection(Object connHandle) {
		SubscriberSocConnectionImpl subscriberSocConnectionImpl = new SubscriberSocConnectionImpl(subsriberId,
				subscriber, controller, (ServerConnectionManager) connHandle);
		return subscriberSocConnectionImpl;
	}

	@Override
	public ClientServerConnection createPublisherConnection(Object connHandle) {
		PublisherSocConnectionImpl pubSocConnectionImpl = new PublisherSocConnectionImpl(publisherId, publisher,
				controller, (ServerConnectionManager) connHandle);
		return pubSocConnectionImpl;
	}

	@Override
	public void removeConnection(int connectionId) {
		this.connectionList.remove(connectionId);

	}

	public void addConnection(int connectionId) {
		this.connectionList.add(connectionId);

	}

	@Override
	public void stop() {
		try {
			if (this.publisher != null) {
				this.publisher.close();
			}
			if (this.subscriber != null) {
				this.subscriber.close();
			}
			if (this.publisherSocket != null) {
				this.publisherSocket.close();
			}
			if (this.subscriberSocket != null) {
				this.subscriberSocket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void addTopic(TopicMessage t) {
		this.topicList.add(t);
	}
}
