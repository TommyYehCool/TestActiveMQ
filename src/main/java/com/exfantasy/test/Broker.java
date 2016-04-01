package com.exfantasy.test;

import org.apache.activemq.broker.BrokerService;

public class Broker {

	public static void main(String[] args) {
		BrokerService broker = new BrokerService();
		try {
			broker.addConnector(Constant.BROKER_URL);

			broker.setPersistent(true);

			broker.setOfflineDurableSubscriberTimeout(30000);

			broker.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}