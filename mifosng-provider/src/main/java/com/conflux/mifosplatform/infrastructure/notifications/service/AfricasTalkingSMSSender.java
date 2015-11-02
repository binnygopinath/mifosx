package com.conflux.mifosplatform.infrastructure.notifications.service;

import africastalking.sms.AfricasTalkingGateway;

public class AfricasTalkingSMSSender extends AbstractSMSSender implements SMSSender {
	
	protected AfricasTalkingSMSSender() {
	}

	@Override
	public void send(String to, String message) {
		
		// Uses Africa's talking JAVA classes to send SMS
		  
		String user = getSMSConfig("africastalking.user");
		String key = getSMSConfig("africastalking.key");
		
		try {
			
			AfricasTalkingGateway gwy = new AfricasTalkingGateway(user, key);
			System.out.println(gwy.sendMessage(to, message).toString());
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
