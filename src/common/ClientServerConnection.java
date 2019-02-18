package common;

import java.util.List;
import java.util.Set;

/**
 * Created by Dhruv Naik on 4/8/2018.
 */
public interface ClientServerConnection {

    /**
     * Start connection
     */
    void start();

    /**
     * Is connection active ?
     * @return
     */
    boolean isAlive();

    /**
     * Send log in request
     * @param clientId
     * @return true if a new client and false if already connected
     */
    boolean login(String clientId);


    /**
     * Get the set of published topics
     * @return set of topics
     */
    Set<String> getPublishedTopics();

    /**
     * Get the set of subscribed topics by the client
     * @return set of topics
     */
    Set<String> getSubscribedTopics();

    /**
     * Subscribe to a topic
     * @param topic
     * @return true if it a new topic, false already subscribed
     */
    boolean subscribe(String topic);

    /**
     * Unsubscribe to a topic
     * @param topic
     * @return true if it is a topic subscribed already, false if not yet subscribed
     */
    boolean unsubscribe(String topic);

    /**
     * Send topic messages (not needed for client side)
     * @param topicMessages
     */
    void sendTopicMessages(List<TopicMessage> topicMessages);

    /**
     * Stop connection
     */
    void stop();

}
