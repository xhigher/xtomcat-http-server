package com.cheercent.xtomcat.httpserver.base;

import java.io.File;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XServer {
	
	private static Logger logger = LoggerFactory.getLogger(XServer.class);
	
	private final String defaultHost;
	private final int defaultPort;
	
	private final String logicPackage;
	private final String logicModules;
	
	private final Tomcat tomcat;
	
	public XServer(Properties properties) {
		
		defaultHost = properties.getProperty("server.host").trim();
		defaultPort = Integer.parseInt(properties.getProperty("server.port").trim());
		
		logicPackage = properties.getProperty("logic.package").trim();
		logicModules = properties.getProperty("logic.modules").trim();
		
		XRedis.init(properties);
		XMySQL.init(properties);
		
		tomcat = new Tomcat();
        tomcat.setBaseDir(".");
        tomcat.setHostname(defaultHost);
        tomcat.setPort(defaultPort);
        tomcat.getConnector();

        initModules();
	}
	
	public void start() {
		try {

            tomcat.start();
            tomcat.getServer().await();

		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void stop() {
		try {
			XRedis.close();
			tomcat.stop();
			tomcat.destroy();
		} catch (LifecycleException e) {
			logger.error("stop.exception", e);
		}
	}
	
	
	private void initModules() {
		if(this.logicModules != null && !logicModules.isEmpty()) {
			String[] modules = this.logicModules.split(",");
			for(int i=0; i<modules.length; i++){
				loadModule(modules[i].trim());
			}
		}
	}
	
	private static String getVersionAction(XLogicConfig logicConfig) {
		return logicConfig.name() + "@" + logicConfig.version();
	}
	
	private void loadModule(String module){
		File file = null;
		List<String> classFiles = null;
		Class<?> clazz = null;
		XLogicConfig logicConfig = null;
		
        Context context = tomcat.addContext("/"+module, null);
        
		String modulePackage = this.logicPackage + "." + module;
		String modulePath = modulePackage.replace(".", "/");
		String versionAction = null;
		String urlPattern = null;
		try{
			URL url = null;
			JarURLConnection jarConnection = null;
			JarFile jarFile = null;
			Enumeration<JarEntry> jarEntryEnumeration = null;
			String jarEntryName = null;
			String fullClazz = null;
			Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(modulePath);
			while (urls.hasMoreElements()) {
				url = urls.nextElement();
				if ("jar".equalsIgnoreCase(url.getProtocol())) {
					jarConnection = (JarURLConnection) url.openConnection();
					if (jarConnection != null) {
						jarFile = jarConnection.getJarFile();
						if (jarFile != null) {
							jarEntryEnumeration = jarFile.entries();
							while (jarEntryEnumeration.hasMoreElements()) {
								jarEntryName = jarEntryEnumeration.nextElement().getName();
								if (jarEntryName.contains(".class") && jarEntryName.replace("/",".").startsWith(modulePackage)) {
									fullClazz = jarEntryName.substring(0, jarEntryName.lastIndexOf(".")).replace("/", ".");
									clazz = Class.forName(fullClazz);
									logicConfig = clazz.getAnnotation(XLogicConfig.class);
									if(logicConfig != null){
										versionAction = getVersionAction(logicConfig);
										urlPattern = "/" + logicConfig.name();
										if(logicConfig.version() > 1) {
											urlPattern = urlPattern + "/v" + logicConfig.version();
										}
										XLogic logic = (XLogic)clazz.newInstance();
										logic.setConfig(logicConfig);
										Tomcat.addServlet(context, versionAction, logic);
								        context.addServletMappingDecoded(urlPattern, versionAction);
									}
								}
							}
						}
					}
				}else{
					file = new File(url.toURI());
					if (file != null) {
						classFiles = new ArrayList<String>();
						listClassFiles(file, classFiles);
						for (String clz : classFiles) {
							fullClazz = clz.replaceAll("[/\\\\]", ".");
							fullClazz = fullClazz.substring(fullClazz.indexOf(modulePackage), clz.length() - 6);
							clazz = Class.forName(fullClazz);
							logicConfig = clazz.getAnnotation(XLogicConfig.class);
							if (logicConfig != null) {
								versionAction = getVersionAction(logicConfig);
								urlPattern = "/" + logicConfig.name();
								if(logicConfig.version() > 1) {
									urlPattern = urlPattern + "/v" + logicConfig.version();
								}
								XLogic logic = (XLogic)clazz.newInstance();
								logic.setConfig(logicConfig);
								Tomcat.addServlet(context, versionAction, logic);
						        context.addServletMappingDecoded(urlPattern, versionAction);
							}
						}
					}
				}
			}
		}catch(Exception e){
			logger.error("initModule.Exception", e);
		}
	}

	private static void listClassFiles(File file, List<String> classFiles){
		File tf = null;
		File[] files = file.listFiles();
		for(int i=0; i<files.length; i++){
			tf = files[i];
			if(tf.isDirectory()){
				listClassFiles(tf, classFiles);
			}else if(tf.isFile() && tf.getName().endsWith(".class")){
				classFiles.add(tf.getAbsolutePath());
			}
		}
	}

}
