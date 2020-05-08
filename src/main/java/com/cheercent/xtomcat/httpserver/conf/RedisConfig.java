package com.cheercent.xtomcat.httpserver.conf;

import com.cheercent.xtomcat.httpserver.base.XRedis;
import com.cheercent.xtomcat.httpserver.base.XRedis.RedisKeyBuilder;

public interface RedisConfig {
	
	int EXPIRE_DAY_30 = 2592000;
	int EXPIRE_DAY_7 = 604800;
	int EXPIRE_DAY_1 = 86400;
	int EXPIRE_DAY_3 = 86400 * 3;
	int EXPIRE_MIN_1 = 60;
	int EXPIRE_MIN_2 = 120;
	int EXPIRE_MIN_5 = 300;
	int EXPIRE_MIN_10 = 600;
	int EXPIRE_MIN_30 = 1800;
	int EXPIRE_HOUR_1 = 3600;
	int EXPIRE_HOUR_6 = 21800;
	int EXPIRE_SEC_10 = 10;
	
	
	String NODE_BUSINESS = "business";
	

	RedisKeyBuilder CONFIG_INFO = new RedisKeyBuilder(NODE_BUSINESS, "config_info", 0);
	RedisKeyBuilder CONFIG_DICT = new RedisKeyBuilder(NODE_BUSINESS, "config_dict", 0);
	RedisKeyBuilder CONFIG_INFO_CHECKSUM = new RedisKeyBuilder(NODE_BUSINESS, "config_info_checksum", 0);
	RedisKeyBuilder CONFIG_DICT_CHECKSUM = new RedisKeyBuilder(NODE_BUSINESS, "config_dict_checksum", 0);
	
	

	RedisKeyBuilder SESSIONID = new RedisKeyBuilder(NODE_BUSINESS, "sessionid", EXPIRE_DAY_30);
	RedisKeyBuilder SESSION_INFO = new RedisKeyBuilder(NODE_BUSINESS, "session_info", EXPIRE_DAY_30);
	RedisKeyBuilder USER_INFO = new RedisKeyBuilder(NODE_BUSINESS, "user_info", 0);
	RedisKeyBuilder USER_ASSET = new RedisKeyBuilder(NODE_BUSINESS, "user_asset", EXPIRE_DAY_30);
	RedisKeyBuilder USER_VIP = new RedisKeyBuilder(NODE_BUSINESS, "user_vip", EXPIRE_DAY_1);

	
}


