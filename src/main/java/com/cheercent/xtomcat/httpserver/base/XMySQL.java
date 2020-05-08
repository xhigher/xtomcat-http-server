package com.cheercent.xtomcat.httpserver.base;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariDataSource;

public class XMySQL {

	private static Logger logger = LoggerFactory.getLogger(XMySQL.class);

	private static final Map<String, HikariDataSource> dataSourceMap = new HashMap<String, HikariDataSource>();

	public static void init(Properties properties) {
		if(properties.containsKey("mysql.status") && 1==Integer.valueOf(properties.getProperty("mysql.status").trim())) {
			int size = Integer.valueOf(properties.getProperty("mysql.dataSource.size").trim());
			for (int i = 1; i <= size; i++) {
				HikariDataSource dataSource = new HikariDataSource();
				dataSource.setDriverClassName(properties.getProperty("mysql.dataSource" + i + ".driverClassName"));
				dataSource.setPoolName(properties.getProperty("mysql.dataSource" + i + ".name"));
				dataSource.setJdbcUrl(properties.getProperty("mysql.dataSource" + i + ".url"));
				dataSource.setUsername(properties.getProperty("mysql.dataSource" + i + ".user"));
				dataSource.setPassword(properties.getProperty("mysql.dataSource" + i + ".password"));
				dataSource.addDataSourceProperty("cachePrepStmts", properties.getProperty("mysql.dataSource" + i + ".cachePrepStmts"));
				dataSource.addDataSourceProperty("prepStmtCacheSize", properties.getProperty("mysql.dataSource" + i + ".prepStmtCacheSize"));
				dataSource.addDataSourceProperty("prepStmtCacheSqlLimit", properties.getProperty("mysql.dataSource" + i + ".prepStmtCacheSqlLimit"));
				dataSource.setMaximumPoolSize(Integer.valueOf(properties.getProperty("mysql.dataSource" + i + ".maximumPoolSize")));
				
				dataSourceMap.put(dataSource.getPoolName(), dataSource);
				checkDataSource(dataSource);
			}
		}
	}
	
	private static void checkDataSource(HikariDataSource dataSource){
		try {
			Connection conn = dataSource.getConnection();
			if(conn != null){
				conn.close();
			}
		} catch (SQLException e) {
			logger.error("XMySQL.checkDataSource.SQLException:", e);
		}
	}

	public static Connection getConnection(String dsName) {
		try {
			return dataSourceMap.get(dsName).getConnection();
		} catch (SQLException e) {
			logger.error("XMySQL.getConnection.SQLException:", e);
		}
		return null;
	}

	public static void releaseConnection(Connection conn) {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				logger.error("XMySQL.releaseConnection.SQLException:", e);
			}
		}
	}

}
