package com.cheercent.xtomcat.httpserver.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.SimpleTimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CommonUtils {
	
	private static final String __numberChars = "0123456789";
	private static final String __diffChars = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
	private static final String __randChars = "0123456789abcdefghigklmnopqrstuvtxyzABCDEFGHIGKLMNOPQRSTUVWXYZ";
  
    public static final Charset ENCODING_UTF8 = StandardCharsets.UTF_8;
    
	private final static Random __random = new Random(System.currentTimeMillis());

	private final static char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	public static String randomNumbers(int length) {
		StringBuilder hash = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			hash.append(__numberChars.charAt(__random.nextInt(10)));
		}
		return hash.toString();
	}
	
	public static String randomString(int length, boolean isLowerCase) {
		int size = isLowerCase ? 36 : 62;
		StringBuilder hash = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			hash.append(__randChars.charAt(__random.nextInt(size)));
		}
		return hash.toString();
	}

	public static long randomLong(long min, long max) {
		return min + (long) (Math.random() * (max - min));
	}
	
	public final static String md5(String s) {
		try {
			byte[] btInput = s.getBytes();
			MessageDigest mdInst = MessageDigest.getInstance("MD5");
			mdInst.update(btInput);
			byte[] md = mdInst.digest();
			return toHex(md);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public final static String getCheckcode(int length){
		int size = __diffChars.length();
		StringBuilder hash = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			hash.append(__diffChars.charAt(__random.nextInt(size)));
		}
		return hash.toString();
	}
	
	public final static String getMsgcode(int length){
		return randomNumbers(length);
	}

	public static int randomInt(int min, int max) {
		return min + (int) (Math.random() * (max - min));
	}
	
    public static int FNVHash1(String data){   
        final int p = 16777619;   
        int hash = (int)2166136261L;
        for(int i=0;i<data.length();i++)   
            hash = (hash ^ data.charAt(i)) * p;
        hash += hash << 13;   
        hash ^= hash >> 7;   
        hash += hash << 3;   
        hash ^= hash >> 17;   
        hash += hash << 5;
        return hash;   
    }
    
    public static String hashNum(String data, int range){
    	CRC32 crc32 = new CRC32();
    	crc32.update(data.getBytes());
    	long hash = crc32.getValue();
    	hash = hash % range; 
		return String.valueOf(hash);
    }
    
    public static String hashTableId(String data, int range){
    	CRC32 crc32 = new CRC32();
    	crc32.update(data.getBytes());
    	long hash = crc32.getValue();
    	hash = hash % range + 1; 
		return String.valueOf(hash);
    }
    
    public static String hashTableId(long data, int range){
    	long hash = (data % range + 1);
    	return String.valueOf(hash);
    }
    
    public static String getHmacSHA1Encrypt(String encryptText, String encryptKey) throws Exception {           
        byte[] keyBytes = encryptKey.getBytes(ENCODING_UTF8);
        byte[] textBytes = encryptText.getBytes(ENCODING_UTF8);
        SecretKey secretKey = new SecretKeySpec(keyBytes, "HmacSHA1");
        Mac mac = Mac.getInstance("HmacSHA1");   
        mac.init(secretKey);
        return new String(Base64.getEncoder().encodeToString(mac.doFinal(textBytes)));  
    }
    
	private static String toHex(byte[] src) {
		int len = src.length;
		char[] chs = new char[len * 2];
		int j = 0;
		for (int i = 0; i < len; i++) {
			chs[j++] = hexDigits[src[i] >> 4 & 0xF];
			chs[j++] = hexDigits[src[i] >> 0 & 0xF];
		}
		return new String(chs);
	}

	public static String encodeBySHA1(String str) {
		try{
			MessageDigest sha1 = MessageDigest.getInstance("SHA1");
			sha1.update(str.getBytes()); 
			byte[] digest = sha1.digest();
			return toHex(digest);
		}catch(Exception e){
			return null;
		}
	}
	
	private static final String IV_STRING = "eDeSDFX73hwxi7Xg";

	public static String encryptAESData(String privateKey, String text) {
	   try{
		  SecretKeySpec secretKey = new SecretKeySpec(privateKey.getBytes(ENCODING_UTF8), "AES");
		  Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		  byte[] initParam = IV_STRING.getBytes(ENCODING_UTF8);
		  IvParameterSpec ivParameterSpec = new IvParameterSpec(initParam);
		  cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);  
		  return Base64.getEncoder().encodeToString(cipher.doFinal(text.getBytes(ENCODING_UTF8)));
	   } catch (Exception e) {
		   e.printStackTrace();
	   }
	   return text;
	}

	public static String decryptAESData(String privateKey, String encryptText) {
	   try{
		  SecretKeySpec secretKey = new SecretKeySpec(privateKey.getBytes(ENCODING_UTF8), "AES");
		  Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		  byte[] initParam = IV_STRING.getBytes(ENCODING_UTF8);
		  IvParameterSpec ivParameterSpec = new IvParameterSpec(initParam);
		  cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
		  return new String(cipher.doFinal(Base64.getDecoder().decode(encryptText)), ENCODING_UTF8);
	   }catch (Exception e){
		   e.printStackTrace();
	   }
	   return null;
	}
	
	public static boolean checkPhoneNo(String phoneno){
		Pattern pattern = Pattern.compile("^(1[0-9]{10})$");
		Matcher matcher = pattern.matcher(phoneno);
		return matcher.matches();
	}
	
	/************************************** DateTime Begin *********************************/
	
    public static final String YYYY_MM_DD_HH_MM_SS_SSS = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String YYYY_MM_DD_T_HH_MM_SS_Z = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    
    public static final String YYYYMMDD = "yyyyMMdd";
    public static final String YYYYMMDDHH = "yyyyMMddHH";
    public static final String YYYYMMDDHHMM = "yyyyMMddHHmm";
    
    public static final String YYYY_MM = "yyyy-MM";
    public static final String YYYYMM = "yyyyMM";
    
    public static final String HH_MM = "HH:mm";
    
    public static final String YYYY_MM_DD_HH_MM = "yyyy-MM-dd HH:mm";
    public static final String YYYYMMDDHHMMSSSSS = "yyyyMMddHHmmssSSS";
    public static final String YYMMDDHHMMSSSSS = "yyMMddHHmmssSSS";
    public static final String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";
    
    public static String getYMDTHMSZ(){
		final SimpleDateFormat df = new SimpleDateFormat(YYYY_MM_DD_T_HH_MM_SS_Z);
		df.setTimeZone(new SimpleTimeZone(0, "GMT"));
		return df.format(new Date(System.currentTimeMillis()));
	}
    
    public static String getCurrentYMDHMS(){
		final SimpleDateFormat df = new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS);
		return df.format(new Date(System.currentTimeMillis()));
	}
    
    public static String getCurrentYMDHMS(long millis){
		final SimpleDateFormat df = new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS);
		return df.format(new Date(millis));
	}
    
    public static String getCurrentYMDHM(){
		final SimpleDateFormat df = new SimpleDateFormat(YYYY_MM_DD_HH_MM);
		return df.format(new Date(System.currentTimeMillis()));
	}
    
    public static String getCurrentYMDHM(long millis){
		final SimpleDateFormat df = new SimpleDateFormat(YYYY_MM_DD_HH_MM);
		return df.format(new Date(millis));
	}
 
    public static String getCurrentYMDHMSS(){
		final SimpleDateFormat df = new SimpleDateFormat(YYYYMMDDHHMMSSSSS);
		return df.format(new Date(System.currentTimeMillis()));
	}
    
    public static String getCurrentYMDHMSS2(){
		final SimpleDateFormat df = new SimpleDateFormat(YYMMDDHHMMSSSSS);
		return df.format(new Date(System.currentTimeMillis()));
	}
    
    public static String getCurrentYMDHMS2(){
		final SimpleDateFormat df = new SimpleDateFormat(YYYYMMDDHHMMSS);
		return df.format(new Date(System.currentTimeMillis()));
	}
    
    public static String getTodayYMD(){
		final SimpleDateFormat df = new SimpleDateFormat(YYYY_MM_DD);
		return df.format(new Date(System.currentTimeMillis()));
	}
    
    public static String getCurrentYMDH2(){
		final SimpleDateFormat df = new SimpleDateFormat(YYYYMMDDHH);
		return df.format(new Date(System.currentTimeMillis()));
	}
    public static String getCurrentYMDHM2(){
		final SimpleDateFormat df = new SimpleDateFormat(YYYYMMDDHHMM);
		return df.format(new Date(System.currentTimeMillis()));
	}
    
    public static String getTodayYMD2(){
		final SimpleDateFormat df = new SimpleDateFormat(YYYYMMDD);
		return df.format(new Date(System.currentTimeMillis()));
	}
    
    public static String getAfterDaysYMD(int days, String startDate){
    	String endDate = null;
    	try{
    		SimpleDateFormat format = new SimpleDateFormat(YYYY_MM_DD);
    		Calendar calendar = Calendar.getInstance();
    		if(startDate != null){
    			Date date = format.parse(startDate);
        		calendar.setTime(date);
    		}
    		calendar.add(Calendar.DATE, days);
        	endDate = format.format(calendar.getTime());
    	}catch(Exception e){
    	}
    	return endDate;
    }
    
    public static String getAfterMonthsYMD(int months, String startDate){
    	String endDate = null;
    	try{
    		SimpleDateFormat format = new SimpleDateFormat(YYYY_MM_DD);
    		Calendar calendar = Calendar.getInstance();
    		if(startDate != null){
    			Date date = format.parse(startDate);
        		calendar.setTime(date);
    		}
    		calendar.add(Calendar.MONTH, months);
        	endDate = format.format(calendar.getTime());
    	}catch(Exception e){
    	}
    	return endDate;
    }

	public static String getAfterMonthsYM(int months){
		String endDate = null;
		try{
			SimpleDateFormat format = new SimpleDateFormat(YYYY_MM);
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.MONTH, months);
			endDate = format.format(calendar.getTime());
		}catch(Exception e){
		}
		return endDate;
	}

	public static String getAfterMinutesYMDHMS(int minutes){
		String endDate = null;
		try{
			SimpleDateFormat format = new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS);
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.MINUTE, minutes);
			endDate = format.format(calendar.getTime());
		}catch(Exception e){
		}
		return endDate;
	}

    
    public static String getAfterDaysYMD(int days){
    	String endDate = null;
    	try{
    		SimpleDateFormat format = new SimpleDateFormat(YYYY_MM_DD);
    		Calendar calendar = Calendar.getInstance();
    		calendar.add(Calendar.DATE, days);
        	endDate = format.format(calendar.getTime());
    	}catch(Exception e){
    	}
    	return endDate;
    }
    
    public static long getDaysBetweenDates(String ymdhm1, String ymdhm2){
    	try{
    		Calendar calendar = Calendar.getInstance();
    		SimpleDateFormat format = new SimpleDateFormat(YYYY_MM_DD_HH_MM);
    		Date date1 = format.parse(ymdhm1);
    		Date date2 = format.parse(ymdhm2);
    		calendar.setTime(date1);
    		long time1 = calendar.getTimeInMillis();
    		calendar.setTime(date2);
    		long time2 = calendar.getTimeInMillis();
    		return Math.abs((time1-time2)/86400000);
    	}catch(Exception e){
    	}
    	return 0;
    }
    
    public static String getCurrentHHmm(){
    	String endTime = null;
    	try{
    		SimpleDateFormat format = new SimpleDateFormat(HH_MM);
    		Calendar calendar = Calendar.getInstance();
    		endTime = format.format(calendar.getTime());
    	}catch(Exception e){
    	}
    	return endTime;
    }
    
    public static String getAfterHoursTime(int hours, Date date){
    	String endTime = null;
    	try{
    		SimpleDateFormat format = new SimpleDateFormat(YYYY_MM_DD_HH_MM);
    		Calendar calendar = Calendar.getInstance();
    		if(date != null){
        		calendar.setTime(date);
    		}
    		calendar.add(Calendar.HOUR, hours);
    		endTime = format.format(calendar.getTime());
    	}catch(Exception e){
    	}
    	return endTime;
    }
    
    public static String getYestodayYMD(){
		final SimpleDateFormat df = new SimpleDateFormat(YYYY_MM_DD);
		return df.format(new Date(System.currentTimeMillis()-86400000L));
	}
    
    public static int getDayOfWeek(){
    	Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(System.currentTimeMillis()));
        return cal.get(Calendar.DAY_OF_WEEK);
    }
    
	public static boolean checkformatYMD(String date){
		Pattern pattern = Pattern.compile("^(19|20)\\d{2}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])$");
		return pattern.matcher(date).matches();
	}
	
	/************************************** DateTime End *********************************/
}
	