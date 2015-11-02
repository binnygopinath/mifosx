package com.conflux.mifosplatform.infrastructure.notifications.handler;

import org.mifosplatform.commands.annotation.CommandType;
import org.mifosplatform.commands.handler.NewCommandSourceHandler;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import com.conflux.mifosplatform.infrastructure.notifications.service.NotificationSendPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@CommandType(entity = "NOTIFICATION", action = "SEND")
public class SendNotificationCommandHandler implements NewCommandSourceHandler {

	private final NotificationSendPlatformService notificationSendPlatformService;
	
	@Autowired
	public SendNotificationCommandHandler (final NotificationSendPlatformService notificationSendPlatformService){
		this.notificationSendPlatformService = notificationSendPlatformService;
	}
	
	@Override
	public CommandProcessingResult processCommand(JsonCommand command) {
		// TODO Auto-generated method stub
		System.out.println(command.json());
		return notificationSendPlatformService.sendNotification(command);
		
	}

}
