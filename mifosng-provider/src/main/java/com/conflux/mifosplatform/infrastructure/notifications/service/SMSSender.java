package com.conflux.mifosplatform.infrastructure.notifications.service;

import java.util.Properties;

public interface SMSSender {
	public void send (String to, String message);
}
