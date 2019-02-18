package server;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import common.ClientServerConnection;
import common.TopicMessage;

/**
 * Created by Dhruv Naik on 4/8/2018.
 */

public class PubSubServer implements ServerController {

	private final ServerConnectionManager connManager;
	private ConcurrentHashMap<String, ClientServerConnection> listOfConnections;
	private ConcurrentHashMap<String, TreeSet<String>> subscriberTopics;
	private ConcurrentSkipListSet<String> publishedTopics;

	// keep subscriber id-connection bindings, data structures for subscriptions,
	// messages etc

	public PubSubServer(ServerConnectionManager connManager) {
		this.connManager = connManager;
		this.connManager.setServerController(this);
		this.listOfConnections = new ConcurrentHashMap<>();
		this.subscriberTopics = new ConcurrentHashMap<>();
		this.publishedTopics = new ConcurrentSkipListSet<>();
	}

	@Override
	public boolean addConnection(String subscriberId, ClientServerConnection connection) {
		if (this.listOfConnections.containsKey(subscriberId)) {
			return false;
		} else {
			this.listOfConnections.put(subscriberId, connection);
			return true;
		}

	}

	@Override
	public boolean removeConnection(String subscriberId) {
		if (this.listOfConnections.containsKey(subscriberId)) {
			this.listOfConnections.remove(subscriberId);
			return true;
		} else {
			return false;
		}

	}

	@Override
	public ClientServerConnection getSubscriberConnection(String subscriberId) {
		return this.listOfConnections.get(subscriberId);
	}

	@Override
	public Set<String> getAllPublishedTopics() {
		return this.publishedTopics;
	}

	@Override
	public Set<String> getSubscribedTopics(String subscriberId) {
		if (this.subscriberTopics.containsKey(subscriberId)) {
			return this.subscriberTopics.get(subscriberId);
		} else {
			return new TreeSet<String>();
		}
	}

	@Override
	public boolean addTopic(String subscriberId, String topic) {
		if (this.subscriberTopics.containsKey(subscriberId)) {
			TreeSet<String> topics = this.subscriberTopics.get(subscriberId);
			if (topics.contains(topic)) {
				return false;
			} else {
				topics.add(topic);
				this.subscriberTopics.put(subscriberId, topics);
				return true;
			}
		} else {
			TreeSet<String> topics = new TreeSet<>();
			topics.add(topic);
			this.subscriberTopics.put(subscriberId, topics);
			return true;
		}
	}

	@Override
	public boolean removeTopic(String subscriberId, String topic) {
		if (this.subscriberTopics.containsKey(subscriberId)) {
			TreeSet<String> topics = this.subscriberTopics.get(subscriberId);
			if (topics.contains(topic)) {
				topics.remove(topic);
				this.subscriberTopics.put(subscriberId, topics);
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	@Override
	public void onReceivedTopicMessage(TopicMessage message) {
		synchronized (this) {
			this.publishedTopics.add(message.getTopic());
			List<TopicMessage> lst = new ArrayList<TopicMessage>();
			lst.add(message);
			Iterator<Entry<String, ClientServerConnection>> it = listOfConnections.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pair = (Map.Entry) it.next();
				SubscriberSocConnectionImpl conn = (SubscriberSocConnectionImpl) pair.getValue();
				String name = conn.getSubscriberName();
				TreeSet<String> listOfTopics = this.subscriberTopics.get(name);
				if (listOfTopics != null && listOfTopics.contains(message.getTopic())) {
					conn.sendTopicMessages(lst);
				}
			}
		}
		System.out.println("Received topic message:" + message.toString());
	}

	public static void main(String[] args) {
		ServerConnectionManager connMgr = null;
		Scanner scanner = new Scanner(System.in);
		int publisherPort = 0, subscriberPort = 0;
		boolean samePorts = true;
		try {
			while (samePorts) {
				System.out.println("Enter two port numbers. One for the publisher and another for the subscriber :");
				System.out.println("Enter one port and press Enter key and then enter second port");
				while (!scanner.hasNextInt()) {
					System.out.println("Enter an integer value for the publisher port");
					scanner.nextLine();
				}
				publisherPort = scanner.nextInt();
				if (publisherPort <= 0) {
					while (publisherPort <= 0) {
						System.out.println("Enter a value greater than 0 for the publisher port");
						publisherPort = scanner.nextInt();
					}
				}
				scanner.nextLine();
				while (!scanner.hasNextInt()) {
					System.out.println("Enter an integer value for the subscriber port");
					scanner.nextLine();
				}

				subscriberPort = scanner.nextInt();
				if (subscriberPort <= 0) {
					while (subscriberPort <= 0) {
						System.out.println("Enter a value greater than 0 for the publisher port");
						subscriberPort = scanner.nextInt();
					}
				}
				if (publisherPort != subscriberPort) {
					break;
				}
			}
			connMgr = new PubSubSeverConnectionManagerSocImpl(publisherPort, subscriberPort);
			PubSubServer server = new PubSubServer(connMgr);
			connMgr.start();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.out.println("Server closed");
			if (connMgr != null) {
				connMgr.stop();

			}
			scanner.close();
		}

	}

}
