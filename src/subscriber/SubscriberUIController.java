package subscriber;

import java.io.IOException;

import common.TopicMessage;


/**
 * Created by Dhruv Naik on 4/8/2018.
 */

public interface SubscriberUIController {

    /**
     * Initiate request to get published topics from the server
     * @throws IOException 
     * @throws InterruptedException 
     */
    void getPublishedTopics() throws IOException, InterruptedException;

    /**
     * Initiate request to get subscribed topics from the server
     * @throws IOException 
     */
    void getSubscribedTopics() throws IOException;

    /**
     * Initiate request to subscribe to a topic
     * @param topic
     * @throws IOException 
     */
    void subscribeTopic(String topic) throws IOException;

    /**
     * Initiate request to unsubscribe to a topic
     * @param topic
     * @throws IOException 
     */
    void unsubscribeTopic(String topic) throws IOException;

    /**
     * Handle topic message received
     * @param topicMessage
     */
    void onReceivedTopicMessage(TopicMessage topicMessage);

}
