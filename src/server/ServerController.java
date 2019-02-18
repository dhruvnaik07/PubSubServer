package server;

import common.ClientServerConnection;
import common.TopicMessage;

import java.util.Set;

/**
 * Created by Dhruv Naik on 4/8/2018.
 */

public interface ServerController {

	/**
	 * Add connection for subscriber id
	 * 
	 * @param subscriberId
	 * @param connection
	 * @return true if a new client and false otherwise
	 */
	boolean addConnection(String subscriberId, ClientServerConnection connection);

	/**
	 * Remove connection for subscriber id
	 * 
	 * @param subscriberId
	 * @return true if connection existed and removed and false otherwise
	 */
	boolean removeConnection(String subscriberId);

	/**
	 * Get connection for subscriber id
	 * 
	 * @param subscriberId
	 * @return connection
	 */
	ClientServerConnection getSubscriberConnection(String subscriberId);

	/**
	 * Get all published topics
	 * 
	 * @return
	 */
	Set<String> getAllPublishedTopics();

	/**
	 * Get all subscribed topics for a subscriber
	 * 
	 * @param subscriberId
	 * @return
	 */
	Set<String> getSubscribedTopics(String subscriberId);

	/**
	 * Add topic for a subscriber
	 * 
	 * @param subscriberId
	 * @return true if a new topic false if already subscribed
	 */
	boolean addTopic(String subscriberId, String topic);

	/**
	 * Remove topic for a subscriber
	 * 
	 * @param subscriberId
	 * @return true if subscribed false if not subscribed
	 */
	boolean removeTopic(String subscriberId, String topic);

	/**
	 * Handle topic message received from publisher
	 * 
	 * @param message
	 */
	void onReceivedTopicMessage(TopicMessage message);

}
