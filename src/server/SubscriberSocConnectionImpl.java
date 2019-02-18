package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Set;

//import edu.njit.cs602.s2018.assignments.Server;
import common.ClientServerConnection;
import common.ServerCommandMessage;
import common.ServerCommandMessage.MessageType;
import common.TopicMessage;

/**
 * 
 * Created by Dhruv Naik on 4/8/2018.
 *
 */

public class SubscriberSocConnectionImpl implements Runnable, ClientServerConnection {

	private final Socket connSock;
	protected Thread receiveThread;
	private final ServerController controller;
	private final ServerConnectionManager connMgr;
	private final int clientId;
	private boolean loggedIn = false;
	private String subscriberName;
	private SubscriberSocConnectionImpl thisObject;
	private DataOutputStream dos = null;

	public SubscriberSocConnectionImpl(int clientId, Socket clientSock, ServerController controller,
			ServerConnectionManager connMgr) {
		this.clientId = clientId;
		this.connSock = clientSock;
		this.controller = controller;
		this.connMgr = connMgr;
		this.loggedIn = true;
		this.thisObject = this;
	}

	public String getSubscriberName() {
		return this.subscriberName;
	}

	@Override
	public void start() {
		receiveThread = new Thread();
		receiveThread.start();

	}

	@Override
	public boolean isAlive() {
		return false;
	}

	@Override
	public boolean login(String clientId) {
		boolean loggedIn = this.controller.addConnection(clientId, this);
		if (loggedIn) {
			this.subscriberName = clientId;
		}
		return loggedIn;
	}

	@Override
	public Set<String> getPublishedTopics() {
		Set<String> publishedTopics = this.controller.getAllPublishedTopics();
		return publishedTopics;
	}

	@Override
	public Set<String> getSubscribedTopics() {
		Set<String> subscribedTopics = this.controller.getSubscribedTopics(subscriberName);
		return subscribedTopics;
	}

	@Override
	public boolean subscribe(String topic) {
		boolean response = this.controller.addTopic(subscriberName, topic);
		return response;

	}

	@Override
	public boolean unsubscribe(String topic) {
		boolean response = this.controller.removeTopic(subscriberName, topic);
		return response;
	}

	@Override
	public void sendTopicMessages(List<TopicMessage> topicMessages) {
		try {
			dos = new DataOutputStream(connSock.getOutputStream());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		new Thread() {
			public void run() {
				for (TopicMessage topicMessage : topicMessages) {
					try {
						thisObject.send(new ServerCommandMessage(MessageType.TOPIC_MESSAGE, topicMessage.toString()),
								dos);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}

	@Override
	public void stop() {
		this.receiveThread = null;
		this.loggedIn = false;
	}

	@Override
	public void run() {
		DataInputStream dis = null;
		DataOutputStream dos = null;
		try {
			dis = new DataInputStream(connSock.getInputStream());
			dos = new DataOutputStream(connSock.getOutputStream());
			boolean response = false;
			for (;;) {

				ServerCommandMessage msg = ServerCommandMessage.get(dis);
				// The various conditions for the various commands sent by the subscriber
				switch (msg.getMessageType()) {
				case SUBSCRIBER_LOGIN_REQ:
					response = this.login(msg.getMessage());
					if (response) {
						this.send(new ServerCommandMessage(MessageType.SUBSCRIBER_LOGIN_RESP_OK, ""), dos);

					} else {
						this.send(new ServerCommandMessage(MessageType.SUBSCRIBER_LOGIN_RESP_ERR, ""), dos);
					}
					break;
				case GET_PUBLISHED_TOPICS_REQ:
					Set<String> publishedTopics = this.getPublishedTopics();
					for (String topicName : publishedTopics) {
						this.send(new ServerCommandMessage(MessageType.GET_PUBLISHED_TOPICS_RESP, topicName), dos);
					}
					this.send(new ServerCommandMessage(MessageType.RESP_END, ""), dos);
					break;
				case GET_SUBSCRIBED_TOPICS_REQ:
					Set<String> subscribedTopics = this.getSubscribedTopics();
					for (String topicName : subscribedTopics) {
						this.send(new ServerCommandMessage(MessageType.GET_SUBSCRIBED_TOPICS_RESP, topicName), dos);
					}
					this.send(new ServerCommandMessage(MessageType.RESP_END, ""), dos);
					break;
				case SUBSCRIBE_REQ:
					response = this.subscribe(msg.getMessage());
					if (response) {
						this.send(new ServerCommandMessage(MessageType.SUBSCRIBE_RESP, ServerCommandMessage.SUBSCRIBED),
								dos);
					} else {
						this.send(new ServerCommandMessage(MessageType.SUBSCRIBE_RESP,
								ServerCommandMessage.ALREADY_SUBSCRIBED), dos);
					}
					break;
				case UNSUBSCRIBE_REQ:
					response = this.unsubscribe(msg.getMessage());
					if (response) {
						this.send(new ServerCommandMessage(MessageType.UNSUBSCRIBE_RESP,
								ServerCommandMessage.UNSUBSCRIBED), dos);
					} else {
						this.send(new ServerCommandMessage(MessageType.UNSUBSCRIBE_RESP,
								ServerCommandMessage.NEVER_SUBSCRIBED), dos);
					}
					break;
				default:
					break;
				}
			}
		} catch (EOFException eof) {
			System.out.println("Subscriber " + clientId + " closed");
		} catch (Exception e) {
		} finally {
			try {
				connSock.close();
				connMgr.removeConnection(clientId);
				if (subscriberName != null) {
					this.controller.removeConnection(subscriberName);
				}
				this.stop();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public void send(ServerCommandMessage msg, DataOutputStream dos) throws IOException {
		msg.put(dos);
		dos.flush();
	}

}