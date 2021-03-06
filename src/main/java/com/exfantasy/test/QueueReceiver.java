package com.exfantasy.test;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

public class QueueReceiver implements ExceptionListener {
	
	private String mClientId;
	private String mMessageSelector;

	private Connection connection;
	private Session session;
	private MessageConsumer consumer;

	private String QUEUE_NAME = Constant.TEST_Q;

	private void start() {
		try {
			String name = System.getProperty("name");
			if (name == null) {
				System.err.println("Please check your VM arguments has property -Dname");
				System.exit(1);
			}
			
			mClientId = name + "_Q_Receiver_ClientId";
			switch (name) {
				case "Tommy":
					mMessageSelector = "news='sport'";
					break;
					
				case "Alice":
					mMessageSelector = "news='shopping'";
					break;
			}
			
			createConnection();
			
		} catch (JMSException e) {
			System.err.println("Creating connection with JMSException raised, err-msg: " + e.getMessage());
			stop();
		}

		startToReceiveMessage();
	}

	private void createConnection() throws JMSException {
		// Create a ConnectionFactory
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(Constant.BROKER_URL);

		// Create a Connection
		connection = connectionFactory.createConnection();
		connection.setClientID(mClientId);
		connection.setExceptionListener(this);
		connection.start();
		System.out.println("Create connection succeed");

		// Create a Session
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		System.out.println("Create session succeed");

		// Create the destination (Topic or Queue)
		Destination destination = session.createQueue(QUEUE_NAME);

		// Create a MessageConsumer from the Session to the Topic or Queue
//		consumer = session.createConsumer(destination);
		consumer = session.createConsumer(destination, mMessageSelector);

		System.out.println("Create consumer to queue: " + QUEUE_NAME + " with message selector: " + mMessageSelector + " succeed");
	}

	private void startToReceiveMessage() {
		try {
			System.out.println(">>>>> Staring to receive message");
			
			// Wait for a message
			while (true) {
//				Message message = consumer.receive(5000);
				Message message = consumer.receive();
				if (message instanceof TextMessage) {
					TextMessage textMessage = (TextMessage) message;
					String text = textMessage.getText();
					System.out.println("<<<<< Queue TextMessage received: <" + text + ">");
				} else {
					System.out.println("<<<<< Queue message received: <" + message + ">");
				}
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
		QueueReceiver receiver = new QueueReceiver();
		receiver.start();
	}
}