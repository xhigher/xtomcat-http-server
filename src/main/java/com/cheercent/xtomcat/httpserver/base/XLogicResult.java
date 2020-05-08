package com.cheercent.xtomcat.httpserver.base;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.cheercent.xtomcat.httpserver.conf.ErrorCode;

/*
 * @copyright (c) xhigher 2020
 * @author xhigher    2020-5-1
 */
public final class XLogicResult {


	public static final String KEY_ERRCODE = "errcode";
	public static final String KEY_ERRINFO = "errinfo";
	public static final String KEY_DATA = "data";
	
	public static String output(int code,String info, Object obj){
		if(info == null){
			info = "";
		}
		if(obj == null){
			obj = new JSONObject();
		}
		JSONObject result = new JSONObject();
		result.put(KEY_ERRCODE, code);
		result.put(KEY_ERRINFO, info);
		result.put(KEY_DATA, obj);
		return JSONObject.toJSONString(result, SerializerFeature.DisableCircularReferenceDetect);
	}
	
	public static String success(){
		return output(ErrorCode.OK, null, null);
	}
	
	public static String success(JSONObject data){
		return output(ErrorCode.OK, null, data);
	}
	
	public static String success(JSONArray data){
		return output(ErrorCode.OK, null, data);
	}
	
	public static String error(String info, JSONObject data){
		return output(ErrorCode.NOK, info, data);
	}
	
	public static String error(int code, JSONObject data){
		return output(code, null, data);
	}
	
	public static String error(int code, String info){
		return output(code, info, null);
	}
	
	public static String error(String info){
		return output(ErrorCode.NOK, info, null);
	}
	
	public static String error(){
		return output(ErrorCode.NOK, null, null);
	}

	public static String errorInternal(){
		return output(ErrorCode.INTERNAL_ERROR, "INTERNAL_ERROR", null);
	}
	
	public static String staticOutput(int code,String info, Object obj){
		if(info == null){
			info = "";
		}
		if(obj == null){
			obj = new JSONObject();
		}
		JSONObject result = new JSONObject();
		result.put(KEY_ERRCODE, code);
		result.put(KEY_ERRINFO, info);
		result.put(KEY_DATA, obj);
		return JSONObject.toJSONString(result, SerializerFeature.DisableCircularReferenceDetect);
	}
	
	public static String errorRequest(){
		return staticOutput(ErrorCode.REQEUST_ERROR, "REQUEST_ERROR", null);
	}
	
	public static String errorMethod(){
		return staticOutput(ErrorCode.METHOD_ERROR, "METHOD_ERROR", null);
	}
	
	public static String errorParameter(String info){
		return staticOutput(ErrorCode.PARAMETER_ERROR, info, null);
	}

	public static String errorValidation(){
		return staticOutput(ErrorCode.VALIDATION_ERROR, "VALIDATION_ERROR", null);
	}
}
