package com.numerix.test;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;

public class TopicSender {

	private Connection connection;
	private Session session;
	private MessageProducer producer;

	private String TOPIC_NAME = Constant.TEST_T;

	private void start() {
		try {
			createConnection();

			startToSendMessage();
		} catch (JMSException e) {
			System.err.println("JMSException raised, err-msg: " + e.toString());
			stop();
		}
	}

	private void createConnection() throws JMSException {
		// Create a ConnectionFactory
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(Constant.BROKER_URL);

		// Create a Connection
		connection = connectionFactory.createConnection();
		connection.start();

		// Create a Session
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

		// Create the destination (Topic or Queue)
		Topic topic = session.createTopic(TOPIC_NAME);

		// Create a MessageProducer from the Session to the Topic or Queue
		producer = session.createProducer(topic);
		/**
		 * [PERSISTANCE] 1. In non persistance case, the data will lose while
		 * the Broker is shutdown 2. Default is Persistent
		 */
		// producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
	}

	private void startToSendMessage() throws JMSException {
		for (int i = 0; i < 10; i++) {
			String sMsg = null;
			TextMessage message = null;
			if (i % 2 == 0) {
				sMsg = "Rocket Win-" + i;
				message = session.createTextMessage(sMsg);
				message.setStringProperty("action", "sport");
			}
			else {
				sMsg = "On Sale-" + i;
				message = session.createTextMessage(sMsg);
				message.setStringProperty("action", "shopping");
			}
			producer.send(message);
			
			System.out.println(">>>>> Sent message: <" + sMsg + "> to topic: <" + TOPIC_NAME + ">");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (i % 5 == 0) {
				pressAnyKeyToContinue();
			}
		}
	}

	private void pressAnyKeyToContinue() {
		System.out.println("Press any key to continue...");
		try {
			System.in.read();
		} catch (Exception e) {
		}
	}

	private void stop() {
		try {
			if (producer != null) {
				producer.close();
				producer = null;
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

	public static void main(String[] args) {
		TopicSender sender = new TopicSender();
		sender.start();
	}
}