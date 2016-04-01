package com.exfantasy.test;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

public class QueueSender {

	private Connection connection;
	private Session session;
	private MessageProducer producer;

	private String QUEUE_NAME = Constant.TEST_Q;

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
		Destination destination = session.createQueue(QUEUE_NAME);

		// Create a MessageProducer from the Session to the Topic or Queue
		producer = session.createProducer(destination);
		producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
	}

	private void startToSendMessage() throws JMSException {
		int processId = CommonUtil.getProcessId();

		long seq = 0;
		while (seq < 99999) {
			String sMsg = processId + "-" + String.valueOf(seq++);
			TextMessage message = session.createTextMessage(sMsg);
			producer.send(message);
			System.out.println(">>>>> Sent message: <" + sMsg + "> to queue: <" + QUEUE_NAME + ">");
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
		QueueSender sender = new QueueSender();
		sender.start();
	}
}