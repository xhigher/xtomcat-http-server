package com.cheercent.xtomcat.httpserver.base;


import com.alibaba.fastjson.JSONObject;
import com.cheercent.xtomcat.httpserver.base.XRedis.RedisKey;
import com.cheercent.xtomcat.httpserver.conf.DataKey;
import com.cheercent.xtomcat.httpserver.conf.ErrorCode;
import com.cheercent.xtomcat.httpserver.conf.RedisConfig;

/*
 * @copyright (c) xhigher 2020
 * @author xhigher    2020-5-1
 */
public final class XLogicSession {

	protected String peerid = null;
	protected String sessionid = null;
	protected String userid = null;
	protected String username = null;
	protected Integer type = null;
	
	public XLogicSession(String peerid, String sessionid) {
		this.peerid = peerid;
		this.sessionid = sessionid;
	}
	
	public String getPeerid(){
		return this.peerid;
	}

	public String getSessionid() {
		return sessionid;
	}

	public String getUserid() {
		return userid;
	}

	public String getUsername() {
		return username;
	}

	public Integer getType() {
		return type;
	}

	public String checkSession(boolean acountBound){
		if(this.sessionid == null){
			return XLogicResult.errorParameter("SESSIONID_NULL");
		}
		RedisKey redisKey = RedisConfig.SESSION_INFO.build().append(this.sessionid);
		String redisData = XRedis.get(redisKey);
		if(redisData == null){
			return XLogicResult.error(ErrorCode.SESSION_INVALID, "SESSION_INVALID");
		}
		//logger.info("sessionInfo="+redisData);
		JSONObject sessionInfo = JSONObject.parseObject(redisData);
		if(!this.peerid.equals(sessionInfo.getString(DataKey.PEERID))){
			XRedis.del(redisKey);
			return XLogicResult.error(ErrorCode.SESSION_INVALID, "SESSION_INVALID");
		}

		this.userid = sessionInfo.getString(DataKey.USERID);
		this.type = sessionInfo.getInteger(DataKey.TYPE);
		this.username = sessionInfo.getString(DataKey.USERNAME);

		Long liveTime = XRedis.ttl(redisKey);
		if(liveTime == null) {
			return XLogicResult.errorInternal();
		}
		if(liveTime > 0 && liveTime < RedisConfig.EXPIRE_DAY_1) {
			XRedis.expire(redisKey);
			XRedis.expire(RedisConfig.SESSIONID.build().append(this.username));
		}

		if(acountBound){
			if(this.username == null){
				return XLogicResult.error(ErrorCode.ACCOUNT_UNBOUND, "ACCOUNT_UNBOUND");
			}
		}
		
		return null;
	}

}
