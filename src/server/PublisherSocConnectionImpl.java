package server;

import common.ClientServerConnection;
import common.TopicMessage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Set;

/**
 * Created by Dhruv Naik on 4/8/2018.
 */

public class PublisherSocConnectionImpl implements Runnable, ClientServerConnection {
	private final Socket connSock;
	protected Thread receiveThread;
	private final ServerController controller;
	private final ServerConnectionManager connMgr;
	private final int clientId;

	public PublisherSocConnectionImpl(int clientId, Socket clientSock, ServerController controller,
			ServerConnectionManager connMgr) {
		this.clientId = clientId;
		this.connSock = clientSock;
		this.controller = controller;
		this.connMgr = connMgr;
	}

	@Override
	public void start() {
		receiveThread = new Thread(this);
		receiveThread.start();
	}

	@Override
	public boolean isAlive() {
		return false;
	}

	@Override
	public boolean login(String clientId) {
		return false;
	}

	@Override
	public Set<String> getPublishedTopics() {
		return null;
	}

	@Override
	public Set<String> getSubscribedTopics() {
		return null;
	}

	@Override
	public boolean subscribe(String topic) {
		return false;
	}

	@Override
	public boolean unsubscribe(String topic) {
		return false;
	}

	@Override
	public void sendTopicMessages(List<TopicMessage> topicMessages) {

	}

	@Override
	public void stop() {

	}

	public int getClientId() {
		return this.clientId;
	}

	@Override
	public void run() {
		try {
			DataInputStream is = new DataInputStream(connSock.getInputStream());
			DataOutputStream os = new DataOutputStream(connSock.getOutputStream());
			for (;;) {
				TopicMessage msg = TopicMessage.get(is);
				this.controller.onReceivedTopicMessage(msg);
			}
		} catch (EOFException e) {
			System.out.println("Publisher " + clientId + " closed");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				connSock.close();
				connMgr.removeConnection(clientId);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}