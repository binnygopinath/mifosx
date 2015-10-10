package com.conflux.mifosplatform.infrastructure.notifications.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

public abstract class AbstractSMSSender {
	
	protected static Properties smsConfigs;
	
	protected AbstractSMSSender() {
		this.init();
	}
	
	private void init() {
		if (smsConfigs == null) {
			InputStream in = this.getClass().getClassLoader().getResourceAsStream(
					"config/SMS.properties");
			smsConfigs = new Properties();
			try {
				smsConfigs.load(in);
			} catch (IOException e) {
		          e.printStackTrace();
			} 
		}
	}
	
	protected String getSMSConfig (String key) {
		return (String) smsConfigs.get("com.conflux.mifosplatform.notifications.sms." + key); 
	}
	
	protected boolean checkIfSMSConfigEnabled (String key) {
		return 
			((String) smsConfigs.get("com.conflux.mifosplatform.notifications.sms." + key))
				.equals("yes"); 
	}
	
	// HTTP GET request
	protected void sendHttpGet(String urlStr) throws Exception {
		
		URL url = new URL(urlStr);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();

		// optional default is GET
		con.setRequestMethod("GET");

		//add request header
		con.setRequestProperty("User-Agent", "Mozilla/5.0");

		System.out.println("\nSending 'GET' request to URL : " + urlStr);
		
		int responseCode = con.getResponseCode();
		System.out.println("Response Code : " + responseCode);

		if (responseCode == HttpURLConnection.HTTP_OK) { // success
			BufferedReader in = new BufferedReader(
			        new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
	
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
	
			//print result
			System.out.println(response.toString());
		} else {
			System.out.println("GET request for sending SMS did not work.");
			BufferedReader in = new BufferedReader(
			        new InputStreamReader(con.getErrorStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
	
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
	
			//print result
			System.out.println(response.toString());
		}

	}
}
