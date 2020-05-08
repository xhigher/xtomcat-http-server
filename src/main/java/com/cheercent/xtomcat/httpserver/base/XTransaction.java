package com.cheercent.xtomcat.httpserver.base;

import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * @copyright (c) xhigher 2015 
 * @author xhigher    2015-3-26 
 */
public class XTransaction {

	private static Logger logger = LoggerFactory.getLogger(XModel.class);
	
	private Connection connnection;
	
	private boolean isEnded = false;
	
	public boolean setConnection(Connection conn) {
		if(connnection == null) {
			connnection = conn;
			try {
				connnection.setAutoCommit(false);
				return true;
			} catch (SQLException e) {
				logger.error("XModelTransaction.begin", e);
			}
		}
		return false;
	}
	
	public Connection getConnection() {
		return connnection;
	}
	
	public boolean isEnded() {
		return this.isEnded;
	}
	
	public boolean end(boolean success){
		if(!isEnded) {
			try {
				if(success) {
					connnection.commit();
				}else {
					connnection.rollback();
				}
				connnection.setAutoCommit(true);
				return true;
			} catch (SQLException e) {
				logger.error("XModelTransaction.end", e);
			} finally {
				XMySQL.releaseConnection(connnection);
				isEnded = true;
				connnection = null;
			}
		}
		return false;
	}

}

