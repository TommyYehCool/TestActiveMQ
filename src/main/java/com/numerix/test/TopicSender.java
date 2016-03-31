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
		 * [PERSISTANCE] 
		 * 1. In non persistance case, the data will lose while the Broker is shutdown 
		 * 2. Default is Persistent
		 */
		// producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
	}
	
	private void startToSendMessage() throws JMSException {
		int processId = CommonUtil.getProcessId();

		long seq = 0;
		while (seq < 99999) {
			String sMsg = processId + "-" + String.valueOf(seq++);
			TextMessage message = session.createTextMessage(sMsg);
			producer.send(message);
			System.out.println(">>>>> Sent message: <" + sMsg + "> to topic: <" + TOPIC_NAME + ">");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
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