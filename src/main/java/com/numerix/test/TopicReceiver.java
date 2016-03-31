package com.numerix.test;

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
			
			mClientId = name + "_id";
			switch (name) {
				case "Tommy":
					mMessageSelector = "action='sport'";
					mNameUsedToIdentifyThisSubscription = name + "_sport_news";
					break;
					
				case "Alice":
					mMessageSelector = "action='shopping'";
					mNameUsedToIdentifyThisSubscription = name + "_shopping_news";
					break;
			}
			
			createConnection();
		} 
		catch (JMSException e) {
			System.err.println("JMSException raised while creating connection, err-msg: " + e.toString());
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
		
		System.out.println("Create consumer to topic: " + TOPIC_NAME + " succeed");
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
			System.err.println("JMSException raised while stoping related components, err-msg: " + e.toString());
		}
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
			System.err.println("JMSException raised while receiving message, err-msg: " + e.toString());
		}
	}

	public void onException(JMSException ex) {
		System.err.println("JMSException raised, err-msg: " + ex.toString());
	}
	
	public static void main(String[] args) {
		TopicReceiver receiver = new TopicReceiver();
		receiver.start();
	}
}