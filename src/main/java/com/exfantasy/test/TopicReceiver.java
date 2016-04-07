package com.exfantasy.test;

import javax.jms.Connection;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;

public class TopicReceiver implements ExceptionListener, MessageListener {

	private String mClientId;
	private String mNameUsedToIdentifyThisSubscription;
	private String mMessageSelector;
	
	private Connection connection;
	private Session session;
	private MessageConsumer consumer;

	private String TOPIC_NAME = Constant.TEST_T;

	private void start() {
		try {
			String name = System.getProperty("name");
			if (name == null) {
				System.err.println("Please check your VM arguments has property -Dname");
				System.exit(1);
			}
			
			mClientId = name + "_T_Receiver_ClientId";
			switch (name) {
				case "Tommy":
					mMessageSelector = "news='sport'";
					mNameUsedToIdentifyThisSubscription = "sport_news";
					break;
					
				case "Alice":
					mMessageSelector = "news='shopping'";
					mNameUsedToIdentifyThisSubscription = "shopping_news";
					break;
			}
			
			createConnection();
		} 
		catch (JMSException e) {
			System.err.println("Creating connection with JMSException raised, err-msg: " + e.getMessage());
			stop();
		}
	}

	private void createConnection() throws JMSException {
		// Create a ConnectionFactory
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(Constant.BROKER_URL);

		// Create a Connection
		connection = connectionFactory.createConnection();

		/** [DRUABLE] if you want durable you should configure client id */
		connection.setClientID(mClientId);
		connection.setExceptionListener(this);
		connection.start();
		System.out.println("Create connection succeed");
		
		// Create a Session
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		System.out.println("Create session succeed");

		// Create the destination (Topic or Queue)
		Topic topic = session.createTopic(TOPIC_NAME);

		consumer = session.createDurableSubscriber(topic, mNameUsedToIdentifyThisSubscription, mMessageSelector, true);
		consumer.setMessageListener(this);
		
		System.out.println("Create consumer to topic: <" + TOPIC_NAME + "> with name: <" + mNameUsedToIdentifyThisSubscription + "> by messageSelector: <" + mMessageSelector + "> succeed");
	}
	
	public void onMessage(Message message) {
		try {
			if (message instanceof TextMessage) {
				TextMessage textMessage = (TextMessage) message;
				System.out.println("<<<<< Topic TextMessage received: <" + textMessage.getText() + ">");
			} else {
				System.out.println("<<<<< Topic message received: <" + message + ">");
			}
		} catch (JMSException e) {
			System.err.println("Receiving message with JMSException raised, err-msg: " + e.getMessage());
		}
	}
	
	private void stop() {
		try {
			if (consumer != null) {
				consumer.close();
				consumer = null;
			}
			if (session != null) {
				session.close();
				session = null;
			}
			if (connection != null) {
				connection.close();
				connection = null;
			}
		} catch (JMSException e) {
			System.err.println("Stoping related components with JMSException raised, err-msg: " + e.getMessage());
		}
	}

	public void onException(JMSException e) {
		System.err.println("Connection onException with JMSException raised, err-msg: " + e.getMessage());
	}
	
	public static void main(String[] args) {
		TopicReceiver receiver = new TopicReceiver();
		receiver.start();
	}
}