package com.conflux.mifosplatform.infrastructure.notifications.service;

import java.util.HashSet;
import java.util.Map;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.hibernate.mapping.Set;
import org.mifosplatform.portfolio.common.BusinessEventNotificationConstants.BUSINESS_ENTITY;
import org.mifosplatform.portfolio.common.BusinessEventNotificationConstants.BUSINESS_EVENTS;
import org.mifosplatform.portfolio.common.service.BusinessEventListner;
import org.mifosplatform.portfolio.common.service.BusinessEventNotifierService;
import org.mifosplatform.portfolio.loanaccount.domain.Loan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.conflux.mifosplatform.infrastructure.notifications.exception.NotificationsException;

@Component
public class NotificationsSender {
	
	private final BusinessEventNotifierService businessEventNotifierService;
	
	private static Properties notificationConfigs;
	
	@Autowired
    public NotificationsSender (
            final BusinessEventNotifierService businessEventNotifierService
            ) {
        this.businessEventNotifierService = businessEventNotifierService;
        this.init();
    }
	
	private void init() {
		if (notificationConfigs == null) {
			InputStream in = this.getClass().getClassLoader().getResourceAsStream(
					"config/Notifications.properties");
				notificationConfigs = new Properties();
			try {
				notificationConfigs.load(in);
			} catch (IOException e) {
		          e.printStackTrace();
			} 
		}
	}
	
	private String getNotificationConfig (String key) {
		return (String) notificationConfigs.get("com.conflux.mifosplatform.notifications." + key); 
	}
	
	private boolean checkIfNotificationConfigEnabled (String key) {
		return 
			((String) notificationConfigs.get("com.conflux.mifosplatform.notifications." + key))
				.equals("yes"); 
	}
	
	@PostConstruct
    public void addListners() {
		// Read from Properties file to check which events need notifications to be sent out
		
		if (checkIfNotificationConfigEnabled("enablesms")) {
			if (checkIfNotificationConfigEnabled("sendsms.loan.approval")) {
				this.businessEventNotifierService.addBusinessEventPostListners(
						BUSINESS_EVENTS.LOAN_APPROVED, 
						new SMSSender(BUSINESS_EVENTS.LOAN_APPROVED));
			}
			if (checkIfNotificationConfigEnabled("sendsms.loan.disbursal")) {
				this.businessEventNotifierService.addBusinessEventPostListners(
						BUSINESS_EVENTS.LOAN_DISBURSAL, 
						new SMSSender(BUSINESS_EVENTS.LOAN_DISBURSAL));
			}
			if (checkIfNotificationConfigEnabled("sendsms.loan.repayment")) {
				this.businessEventNotifierService.addBusinessEventPostListners(
						BUSINESS_EVENTS.LOAN_MAKE_REPAYMENT, 
						new SMSSender(BUSINESS_EVENTS.LOAN_MAKE_REPAYMENT));
			}
		}
    }
	
	private class SMSSender implements BusinessEventListner {
		
		BUSINESS_EVENTS event;
		
		SMSSender (BUSINESS_EVENTS event) {
			this.event = event;
		}

		@Override
		public void businessEventToBeExecuted(
				Map<BUSINESS_ENTITY, Object> businessEventEntity) {
			// do nothing
			
		}

		@Override
		public void businessEventWasExecuted(
				Map<BUSINESS_ENTITY, Object> businessEventEntity) {
			// TODO Auto-generated method stub, call to the relevant SMS class
			if (businessEventEntity.containsKey(BUSINESS_ENTITY.LOAN)) {
				Loan loan = (Loan) businessEventEntity.get(BUSINESS_ENTITY.LOAN);
				System.out.println ("Send SMS: " + event.getValue() + ":" + 
						loan.getId() + ":" +
						loan.getAccountNumber() + ":" +
						loan.getClient().getDisplayName() + ":" +
						loan.getClient().mobileNo()
						);
				if (loan.getClient().mobileNo() != null) {
					String message = "Completed " + this.event.getValue() + ": " +
						"Loan No: " + loan.getAccountNumber() + ", " +
						"Amount disbursed: " + loan.getDisburseAmountForTemplate() + ", " +
						"for Client: " + loan.getClient().getDisplayName();
					String to = loan.getClient().mobileNo();
					
					if (getNotificationConfig("smsprovider").equals("mVaayoo")) {
						System.out.println ("via mVayoo");
						MVayooSMSSender smsSender = new MVayooSMSSender();
						smsSender.send(to, message);
						
					} else if (getNotificationConfig("smsprovider").equals("smsZone")) {
						System.out.println ("via smsZone");
					} else {
						System.out.println ("Unknown SMS Provider");
					}
					
				}
			
			} else {
				System.out.println ("Send SMS: Could not process");
			}
		}
    }

	private class EmailSender implements BusinessEventListner {

		@Override
		public void businessEventToBeExecuted(
				Map<BUSINESS_ENTITY, Object> businessEventEntity) {
			// do nothing
			
		}

		@Override
		public void businessEventWasExecuted(
				Map<BUSINESS_ENTITY, Object> businessEventEntity) {
			// TODO Auto-generated method stub, call to the relevant Email class
			System.out.println ("Send Email");
		}
    }

}