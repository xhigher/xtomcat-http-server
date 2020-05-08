package com.cheercent.xtomcat.httpserver.base;

/*
 * @copyright (c) xhigher 2015 
 * @author xhigher    2015-3-26 
 */
public final class XContext {

	private XTransaction transaction = null;

	public void startTransaction(){
		transaction = new XTransaction();
	}
	
	public XTransaction getTransaction(){
		return transaction;
	}

	public boolean endTransaction(boolean success){
		boolean flag = false;
		if(transaction != null && !transaction.isEnded()) {
			flag = transaction.end(success);
			transaction = null;
		}
		return flag;
	}
	
	public boolean submitTransaction(){
		boolean flag = false;
		if(transaction != null && !transaction.isEnded()) {
			flag = transaction.end(true);
			transaction = null;
		}
		return flag;
	}
	

}
