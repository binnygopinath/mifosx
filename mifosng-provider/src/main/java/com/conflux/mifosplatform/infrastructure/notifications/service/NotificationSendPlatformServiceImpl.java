package com.conflux.mifosplatform.infrastructure.notifications.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.conflux.mifosplatform.infrastructure.notifications.api.NotificationApiConstants;
import com.conflux.mifosplatform.infrastructure.notifications.data.NotificationDataValidator;
import com.conflux.mifosplatform.infrastructure.notifications.service.email.EmailSender;

@Service
public class NotificationSendPlatformServiceImpl implements NotificationSendPlatformService {

	private final PlatformSecurityContext context;
	private final NotificationDataValidator fromApiJsonDeserializer;
	private final EmailSender emailSender;
	
	@Autowired
	public NotificationSendPlatformServiceImpl(
			final PlatformSecurityContext context, 
			final NotificationDataValidator fromApiJsonDeserializer,
			final EmailSender emailSender) {
		this.context = context;
		this.fromApiJsonDeserializer = fromApiJsonDeserializer;
		this.emailSender = emailSender;
	}
	
	@Override
	public CommandProcessingResult sendNotification(JsonCommand command) {
		// TODO Auto-generated method stub
		final AppUser currentUser = this.context.authenticatedUser();

        this.fromApiJsonDeserializer.validateForSend(command.json());
        
        final String type = command.stringValueOfParameterNamed(NotificationApiConstants.type);
        final String target = command.stringValueOfParameterNamed(NotificationApiConstants.target);
        final String subject = command.stringValueOfParameterNamed(NotificationApiConstants.subject);
        final String message = command.stringValueOfParameterNamed(NotificationApiConstants.message);
        
        if (type.equalsIgnoreCase("email")) {
        	emailSender.sendEmail(target, subject, message);
        } else if(type.equalsIgnoreCase("sms")) {
        	SMSSender smsSender = NotificationsConfiguration.getInstance().getSenderForSMSProvider();
        	smsSender.send(target, message);
        }

		return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .build();
	}

}
