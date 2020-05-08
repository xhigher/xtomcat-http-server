package com.cheercent.xtomcat.httpserver;

import java.io.InputStream;
import java.util.Properties;

import com.cheercent.xtomcat.httpserver.base.XServer;

public class XStarter {
	
	private static String configFile = "/application.properties";

	public static void main(String[] args) {
		try{
			Properties properties = new Properties();
			InputStream is = Object.class.getResourceAsStream(configFile);
			properties.load(is);
			if (is != null) {
				is.close();
			}
			
			final XServer xServer = new XServer(properties);
			
			Runtime.getRuntime().addShutdownHook(new Thread(){
				@Override
				public void run() {
					xServer.stop();
				}
			});
			
			xServer.start();
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
