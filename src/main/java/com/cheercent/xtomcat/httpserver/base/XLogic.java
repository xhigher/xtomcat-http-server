package com.cheercent.xtomcat.httpserver.base;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cheercent.xtomcat.httpserver.conf.DataKey;
import com.cheercent.xtomcat.httpserver.util.CommonUtils;

/*
 * @copyright (c) xhigher 2020
 * @author xhigher    2020-5-1
 */
public abstract class XLogic extends HttpServlet implements Cloneable {

	private static final long serialVersionUID = 1L;

	protected static final Logger logger = LoggerFactory.getLogger(XLogic.class);
	
	private XLogicConfig logicConfig = null;
	
	protected XLogicSession logicSession = null;
	protected JSONObject logicParameters = null;
	private XContext context = null;
	
	private boolean initialized = false;
	
	public void setConfig(XLogicConfig config){
		this.logicConfig = config;
	}
	
	public boolean hasParameter(String name){
		return logicParameters.containsKey(name);
	}

	public String getString(String name){
		return logicParameters.getString(name);
	}
	
	public Integer getInteger(String name){
		return logicParameters.getInteger(name);
	}
	
	public Long getLong(String name){
		return logicParameters.getLong(name);
	}
	
	public Double getDouble(String name){
		return logicParameters.getDouble(name);
	}
	
	public <T extends Enum<T>> T getEnum(Class<T> enumType, String name) {
		try {
			return Enum.valueOf(enumType, logicParameters.getString(name));
		}catch(Exception e){}
		return null;
	}
	
	public JSONArray getJSONArray(String name){
		return logicParameters.getJSONArray(name);
	}
	
	public JSONObject getJSONObject(String name){
		return logicParameters.getJSONObject(name);
	}
	
	public String getClientIP(){
		return this.getString(DataKey.CLIENT_IP);
	}
	
	public String getClientVersion(){
		return this.getString(DataKey.CLIENT_VERSION);
	}
	
	public String getClientDevice(){
		return this.getString(DataKey.CLIENT_DEVICE);
	}
	
	public void startTransaction(){
		if(context == null){
			context = new XContext();
		}
		context.startTransaction();
	}
	
	public XContext getContext(){
		if(context == null){
			context = new XContext();
		}
		return context;
	}
	
	public boolean submitTransaction(){
		return context.submitTransaction();
	}
	
	protected boolean requireSession(){
		return true;
	}
	
	protected boolean requireAccountBound(){
		return false;
	}
	
	protected abstract String prepare();
	
	protected abstract String execute();
	
	private XLogic init(JSONObject parameters) {
		this.logicParameters = parameters;
		this.logicSession = new XLogicSession(this.getString(DataKey.PEERID), this.getString(DataKey.SESSIONID));
		this.initialized = true;
		return this;
	}
	
	private String outputResult(){
		try{
			if(!this.initialized) {
				return XLogicResult.errorInternal();
			}

			String prepareResult = this.prepare();
			if(prepareResult != null){
				return prepareResult;
			}

			if(this.requireSession()){
				String sessionResult = this.logicSession.checkSession(this.requireAccountBound());
				if(sessionResult != null){
					return sessionResult;
				}
			}
			
			String executeResult = this.execute();
			if(executeResult == null) {
				executeResult = XLogicResult.success();
			}
			return executeResult;
		}catch(Exception e){
			logger.error(this.getClass().getSimpleName(), e);
			return XLogicResult.errorInternal();
		}finally{
			if(this.context != null) {
				this.context.endTransaction(false);
			}
		}
	}
	
	@Override
	public XLogic clone() {
		try{
			return (XLogic) super.clone();
		}catch(CloneNotSupportedException e){
		}
		return null;
	}
	
    private JSONObject cleanRequestParameters(Map<String, String[]> parameters){
    	JSONObject requestParameters = new JSONObject();
    	String[] tpv = null;
		String pv = null;
		JSONArray item = null;
		for(String pn : parameters.keySet()){
			tpv = parameters.get(pn);
			if(tpv.length > 1){
				item = new JSONArray(Arrays.asList(tpv));
				pv = item.toJSONString();
			}else if(parameters.get(pn).length == 1) {
				pv = parameters.get(pn)[0].trim();
			}else{
				pv = "";
			}
			requestParameters.put(pn, pv);
		}
		return requestParameters;
    }
    
	public String checkRequiredParameters(JSONObject parameters, final String[] requires){
		if(requires != null && requires.length > 0){
			String pn = null;
			List<String> lackedParams = new ArrayList<String>();
			for(int i=0,n=requires.length; i<n; i++){
				pn = requires[i];
				if(!parameters.containsKey(pn)){
					lackedParams.add(pn);
				}
			}
			if(lackedParams.size() > 0){
				return "PARAMETER_LACKED"+lackedParams.toString();
			}
		}
		return null;
	}
	
	@Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String requestIP = request.getHeader(DataKey.HEADER_IP);
        if (requestIP == null) {
        	requestIP = request.getRemoteAddr();
		}
		String[] ipList = this.logicConfig.allow();
		boolean isForbidden = false;
		if(ipList.length > 0){
			isForbidden = true;
			for(int i=0,n=ipList.length; i<n; i++){
				if(ipList[i].equals(requestIP)){
					isForbidden = false;
					break;
				}
			}
			if(isForbidden){
				logger.error("IP_FORBIDDEN:"+requestIP);
				response.getWriter().write(XLogicResult.errorRequest());
				return;
			}
		}
		String requestMethod = request.getMethod().toUpperCase();
		if(!this.logicConfig.method().toString().equals(requestMethod)){
			response.getWriter().write(XLogicResult.errorMethod());
			return;
		}
		
		JSONObject requestParameters = cleanRequestParameters(request.getParameterMap());
		
        if(!requestParameters.containsKey(DataKey.PEERID)){
        	requestParameters.put(DataKey.PEERID, request.getHeader(DataKey.HEADER_PEERID));
        }
        if(!requestParameters.containsKey(DataKey.SESSIONID)){
        	requestParameters.put(DataKey.SESSIONID, request.getHeader(DataKey.HEADER_SESSIONID));
        }
        requestParameters.put(DataKey.CLIENT_VERSION, request.getHeader(DataKey.HEADER_VERSION));
        requestParameters.put(DataKey.CLIENT_DEVICE, request.getHeader(DataKey.HEADER_DEVICE));
        requestParameters.put(DataKey.CLIENT_IP, requestIP);
        
		if(this.logicConfig.requiredPeerid()){
			String peerid = requestParameters.getString(DataKey.PEERID);
			if(!checkPeerid(peerid)){
				response.getWriter().write(XLogicResult.errorValidation());
				return;
			}
		}
		
		if(this.logicConfig.requiredParameters().length > 0){
			String errinfo = this.checkRequiredParameters(requestParameters, this.logicConfig.requiredParameters());
			if(errinfo != null){
				response.getWriter().write(XLogicResult.errorParameter(errinfo));
				return;
			}
		}
		
		response.getWriter().write(this.clone().init(requestParameters).outputResult());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response);
    }
	
	public static String createPeerid(char clientType) {
        long ts = System.currentTimeMillis();
        long rn = (long) Math.floor(Math.random() * 9) + 1;
        long mn = ts % rn;
        return clientType + CommonUtils.randomString(3, true) + rn + Long.toString(ts, 36) + mn + CommonUtils.randomString(6, true);
    }
	
	public static boolean checkPeerid(String peerid) {
		try {
			if(peerid!=null && peerid.length() == 20){
				int rn = Integer.parseInt(peerid.substring(4, 5));
				int mn = Integer.parseInt(peerid.substring(13, 14));
				String ts36 = peerid.substring(5, 13);
				long ts = Long.valueOf(ts36, 36);
				if(ts % rn == mn) {
					return true;
				}
			}
		}catch(Exception e){	
		}
		return false;
	}

}
