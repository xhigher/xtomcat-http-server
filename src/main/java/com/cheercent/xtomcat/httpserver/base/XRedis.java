package com.cheercent.xtomcat.httpserver.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

/*
 * @copyright (c) xhigher 2015 
 * @author xhigher    2015-3-26 
 */
public class XRedis {

	private static Logger logger = LoggerFactory.getLogger(XRedis.class);

	private final static Map<String,JedisPool> redisPoolNodeList = new HashMap<>();
	
	public static void init(Properties properties) {
		if(properties.containsKey("redis.status") && 1==Integer.parseInt(properties.getProperty("redis.status").trim())) {
			JedisPoolConfig poolConfig = new JedisPoolConfig();
			poolConfig.setMaxTotal(Integer.parseInt(properties.getProperty("redis.pool.maxActive").trim()));
			poolConfig.setMaxIdle(Integer.parseInt(properties.getProperty("redis.pool.maxIdle").trim()));
			poolConfig.setMaxWaitMillis(Long.parseLong(properties.getProperty("redis.pool.maxWait").trim()));
			poolConfig.setTestOnBorrow(Boolean.parseBoolean(properties.getProperty("redis.pool.testOnBorrow").trim()));
			poolConfig.setTestOnReturn(Boolean.parseBoolean(properties.getProperty("redis.pool.testOnReturn").trim()));
			poolConfig.setBlockWhenExhausted(false);
			
			String name = null;
			String host = null;
			Integer port  = null;
			String pass = null;
			int db = 0;
			int nodeSize = Integer.parseInt(properties.getProperty("redis.node.size").trim());
			for (int id = 1; id <= nodeSize; id++) {
				name = properties.getProperty("redis.node"+id+".name").trim();
				host = properties.getProperty("redis.node"+id+".host").trim();
				port = Integer.valueOf(properties.getProperty("redis.node"+id+".port").trim());
				pass = properties.getProperty("redis.node"+id+".pass").trim();
				db = 0;
				if(properties.containsKey("redis.node"+id+".db")) {
					db = Integer.parseInt(properties.getProperty("redis.node"+id+".db").trim());
				}
				redisPoolNodeList.put(name, new JedisPool(poolConfig, host, port, Protocol.DEFAULT_TIMEOUT, pass, db));
			}
			
			logger.info("redisPoolNodeList: names="+redisPoolNodeList.keySet().toString());
		}
	}

	private synchronized static Jedis getResource(String name) {
		Jedis jedis = null;
		JedisPool jedisPool = redisPoolNodeList.get(name);
		if (jedisPool != null) {
			try{
				jedis = jedisPool.getResource();
			}catch(Exception e){
				logger.error("getResource.Exception:", e);
			}
		}
		return jedis;
	}

	public static void close() {
		for (JedisPool jedisPool : redisPoolNodeList.values()) {
			jedisPool.close();
		}
	}
	
	public static Long ttl(RedisKey key) {
		if (key != null) {
			Jedis jedis = null;
			try {
				jedis = getResource(key.builder.node);
				if (jedis == null) {
					logger.error("getResource.null: node=" + key.builder.node);
					return null;
				}
				return jedis.ttl(key.name());
			} catch (Exception e) {
				logger.error("ttl.Exception:", e);
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		}
		return 0L;
	}
	
	public static Long expire(RedisKey key) {
	    if(key != null) {
	    	Jedis jedis = null;
	    	try{
	    		jedis = getResource(key.builder.node);
	    		if(jedis == null) {
	    			logger.error("getResource.null: node="+key.builder.node);
	    			return null;
	    		}
	    		return jedis.expire(key.name(), key.builder.expireTime);
			}catch(Exception e){
				logger.error("expire.Exception:", e);
			}finally {
				if (jedis != null) {
					jedis.close();
				}
			}
	    }
	    return 0L;
	}

	public static Long expireAt(RedisKey key, long unixTime) {
		if(key != null) {
			Jedis jedis = null;
			try{
				jedis = getResource(key.builder.node);
				if(jedis == null) {
					logger.error("getResource.null: node="+key.builder.node);
					return null;
				}
				return jedis.expireAt(key.name(), unixTime);
			}catch(Exception e){
				logger.error("expireAt.Exception:", e);
			}finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		}
		return 0L;
	}

	
	public static void set(RedisKey key, String value) {
	    if(key != null) {
	    	Jedis jedis = null;
	    	try{
	    		jedis = getResource(key.builder.node);
	    		if(jedis == null) {
	    			logger.error("getResource.null: node="+key.builder.node);
	    			return;
	    		}
				String keyName = key.name();
				jedis.set(keyName, value);
	    		if(key.builder.expireTime > 0) {
	    			jedis.expire(keyName, key.builder.expireTime);
	    		}
			}catch(Exception e){
				logger.error("set.Exception:", e);
			}finally {
				if (jedis != null) {
					jedis.close();
				}
			}
	    }
	}
	
	public static String get(RedisKey key) {
		if (key != null) {
			Jedis jedis = null;
			try {
				jedis = getResource(key.builder.node);
				if (jedis == null) {
					logger.error("getResource.null: node=" + key.builder.node);
					return null;
				}
				return jedis.get(key.name());
			} catch (Exception e) {
				logger.error("get.Exception:", e);
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		}
		return null;
	}

	public static List<String> mget(final RedisKey... keys) {
		if (keys != null && keys.length > 0) {
			Jedis jedis = null;
			try {
				jedis = getResource(keys[0].builder.node);
				if (jedis == null) {
					logger.error("getResource.null: node=" + keys[0].builder.node);
					return null;
				}
				String[] keyList = new String[keys.length];
				for(int i=0; i < keys.length; i++) {
					keyList[i] = keys[i].name();
				}
				return jedis.mget(keyList);
			} catch (Exception e) {
				logger.error("mget.Exception:", e);
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
			return null;
		}
		return Collections.emptyList();
	}

	public static boolean mset(final Map<RedisKey, String> data) {
		if (data != null && data.size() > 0) {
			Map<String, JSONArray> keyGroup = new HashMap<String, JSONArray>();
			Map<String, Map<String, Integer>> keyExpires = new HashMap<String, Map<String, Integer>>();
 			JSONArray keysvalues = null;
			String keyName = null;
			for(RedisKey key : data.keySet()) {
				if(!keyGroup.containsKey(key.builder.node)) {
					keyGroup.put(key.builder.node, new JSONArray());
				}
				keyName = key.name();
				keysvalues = keyGroup.get(key.builder.node);
				keysvalues.add(keyName);
				keysvalues.add(data.get(key));
				
				if(!keyExpires.containsKey(key.builder.node)) {
					keyExpires.put(key.builder.node, new HashMap<String, Integer>());
				}
				keyExpires.get(key.builder.node).put(keyName, key.builder.expireTime);
			}
			for(String node : keyGroup.keySet()) {
				if(!nodeMset(node, keyExpires.get(node), keyGroup.get(node).toArray(new String[0]))) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	private static boolean nodeMset(String node, Map<String, Integer> expireTimes, String... keysvalues) {
		Jedis jedis = null;
		try {
			jedis = getResource(node);
			if (jedis == null) {
				logger.error("getResource.null: node=" + node);
				return false;
			}
			jedis.mset(keysvalues);
			int expireTime = 0;
			for (String key : expireTimes.keySet()) {
				expireTime = expireTimes.get(key);
				if(expireTime > 0) {
					jedis.expire(key, expireTime);
				}
			}
			return true;
		} catch (Exception e) {
			logger.error("nodeDel.Exception:", e);
		} finally {
			if (jedis != null) {
				jedis.close();
			}
		}
		return false;
	}

	public static Long setnx(final RedisKey key, String value) {
		if (key != null) {
			Jedis jedis = null;
			try {
				jedis = getResource(key.builder.node);
				if (jedis == null) {
					logger.error("getResource.null: node=" + key.builder.node);
					return null;
				}
				return jedis.setnx(key.name(), value);
			} catch (Exception e) {
				logger.error("setnx.Exception:", e);
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		}
		return 0L;
	}


	public static Long del(final RedisKey... keys) {
		if (keys != null && keys.length > 0) {
			Map<String, List<String>> keyGroup = new HashMap<String, List<String>>();
			RedisKey key = null;
			for(int i=0; i<keys.length; i++) {
				key = keys[i];
				if(!keyGroup.containsKey(key.builder.node)) {
					keyGroup.put(key.builder.node, new ArrayList<String>());
				}
				keyGroup.get(key.builder.node).add(key.name());
			}
			for(String node : keyGroup.keySet()) {
				nodeDel(node, keyGroup.get(node).toArray(new String[0]));
			}
		}
		return 0L;
	}
	
	private static Long nodeDel(String node, String... keys) {
		Jedis jedis = null;
		try {
			jedis = getResource(node);
			if (jedis == null) {
				logger.error("getResource.null: node=" + node);
				return null;
			}
			return jedis.del(keys);
		} catch (Exception e) {
			logger.error("nodeDel.Exception:", e);
		} finally {
			if (jedis != null) {
				jedis.close();
			}
		}
		return 0L;
	}


	
	public static Long del(RedisKey key) {
		if (key != null) {
			Jedis jedis = null;
			try {
				jedis = getResource(key.builder.node);
				if (jedis == null) {
					logger.error("getResource.null: node=" + key.builder.node);
					return null;
				}
				return jedis.del(key.name());
			} catch (Exception e) {
				logger.error("del.Exception:", e);
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		}
		return 0L;
	}

	public static Long hdel(final RedisKey key, final String... fields) {
		if (key != null) {
			Jedis jedis = null;
			try {
				jedis = getResource(key.builder.node);
				if (jedis == null) {
					logger.error("getResource.null: node=" + key.builder.node);
					return null;
				}
				return jedis.hdel(key.name(), fields);
			} catch (Exception e) {
				logger.error("hdel.Exception:", e);
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		}
		return 0L;
	}
	
	public static Long incr(final RedisKey key) {
		if (key != null) {
			Jedis jedis = null;
			try {
				jedis = getResource(key.builder.node);
				if (jedis == null) {
					logger.error("getResource.null: node=" + key.builder.node);
					return null;
				}
				String keyName = key.name();
				Long result = jedis.incr(keyName);
				if(key.builder.expireTime > 0) {
					jedis.expire(keyName, key.builder.expireTime);
				}
				return result;
			} catch (Exception e) {
				logger.error("incr.Exception:", e);
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		}
		return 0L;
	}

	public static Long incrBy(final RedisKey key, long value) {
		if (key != null) {
			Jedis jedis = null;
			try {
				jedis = getResource(key.builder.node);
				if (jedis == null) {
					logger.error("getResource.null: node=" + key.builder.node);
					return null;
				}
				String keyName = key.name();
				Long result = jedis.incrBy(keyName, value);
				if(key.builder.expireTime > 0) {
					jedis.expire(keyName, key.builder.expireTime);
				}
				return result;
			} catch (Exception e) {
				logger.error("incr.Exception:", e);
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		}
		return 0L;
	}
	
	public static Boolean exists(RedisKey key) {
		if (key != null) {
			Jedis jedis = null;
			try {
				jedis = getResource(key.builder.node);
				if (jedis == null) {
					logger.error("getResource.null: node=" + key.builder.node);
					return null;
				}
				return jedis.exists(key.name());
			} catch (Exception e) {
				logger.error("exists.Exception:", e);
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		}
		return false;
	}

	public static boolean hexists(RedisKey key, final String field) {
		if (key != null) {
			Jedis jedis = null;
			try {
				jedis = getResource(key.builder.node);
				if (jedis == null) {
					logger.error("getResource.null: node=" + key.builder.node);
					return false;
				}
				return jedis.hexists(key.name(), field);
			} catch (Exception e) {
				logger.error("hexists.Exception:", e);
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		}
		return false;
	}
	
	public static String hget(RedisKey key, final String field) {
		if (key != null) {
			Jedis jedis = null;
			try {
				jedis = getResource(key.builder.node);
				if (jedis == null) {
					logger.error("getResource.null: node=" + key.builder.node);
					return null;
				}
				return jedis.hget(key.name(), field);
			} catch (Exception e) {
				logger.error("hget.Exception:", e);
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		}
		return null;
	}

	public static Map<String, String> hgetAll(RedisKey key) {
		if (key != null) {
			Jedis jedis = null;
			try {
				jedis = getResource(key.builder.node);
				if (jedis == null) {
					logger.error("getResource.null: node=" + key.builder.node);
					return null;
				}
				return jedis.hgetAll(key.name());
			} catch (Exception e) {
				logger.error("hget.Exception:", e);
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		}
		return null;
	}

	public static Long hset(final RedisKey key, final String field, final String value) {
		if (key != null) {
			Jedis jedis = null;
			try {
				jedis = getResource(key.builder.node);
				if (jedis == null) {
					logger.error("getResource.null: node=" + key.builder.node);
					return null;
				}
				String keyName = key.name();
				Long result = jedis.hset(keyName, field, value);
				if(key.builder.expireTime > 0) {
					jedis.expire(keyName, key.builder.expireTime);
				}
				return result;
			} catch (Exception e) {
				logger.error("hset.Exception:", e);
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		}
		return null;
	}
	
	public static String hmset(RedisKey key, final Map<String, String> hash) {
		if (key != null) {
			Jedis jedis = null;
			try {
				jedis = getResource(key.builder.node);
				if (jedis == null) {
					logger.error("getResource.null: node=" + key.builder.node);
					return null;
				}
				String keyName = key.name();
				String result = jedis.hmset(keyName, hash);
				if(key.builder.expireTime > 0) {
					jedis.expire(keyName, key.builder.expireTime);
				}
				return result;
			} catch (Exception e) {
				logger.error("hmset.Exception:", e);
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		}
		return null;
	}
	
	public static Map<String, String> hmget(RedisKey key, final String... fields) {
		if (key != null) {
			Map<String, String> result = new HashMap<>();
			if (fields.length == 0){
				return result;
			}
			Jedis jedis = null;
			try {
				jedis = getResource(key.builder.node);
				if (jedis == null) {
					logger.error("getResource.null: node=" + key.builder.node);
					return null;
				}
				List<String> values = jedis.hmget(key.name(), fields);
				for (int i = 0; i < fields.length; i++) {
					result.put(fields[i], values.get(i));
				}
				return result;
			} catch (Exception e) {
				logger.error("hmget.Exception:", e);
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		}
		return null;
	}
	
	public static Long sadd(RedisKey key, final String... members) {
		if (key != null) {
			Jedis jedis = null;
			try {
				jedis = getResource(key.builder.node);
				if (jedis == null) {
					logger.error("getResource.null: node=" + key.builder.node);
					return null;
				}
				String keyName = key.name();
				Long result = jedis.sadd(keyName, members);
				if(key.builder.expireTime > 0) {
					jedis.expire(keyName, key.builder.expireTime);
				}
				return result;
			} catch (Exception e) {
				logger.error("sadd.Exception:", e);
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		}
		return 0L;
	}

	public static Set<String> smembers(final RedisKey key) {
		if (key != null) {
			Jedis jedis = null;
			try {
				jedis = getResource(key.builder.node);
				if (jedis == null) {
					logger.error("getResource.null: node=" + key.builder.node);
					return null;
				}
				return jedis.smembers(key.name());
			} catch (Exception e) {
				logger.error("smembers.Exception:", e);
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		}
		return null;
	}


	public static Long srem(final RedisKey key, final String... members) {
		if (key != null) {
			Jedis jedis = null;
			try {
				jedis = getResource(key.builder.node);
				if (jedis == null) {
					logger.error("getResource.null: node=" + key.builder.node);
					return null;
				}
				return jedis.srem(key.name(), members);
			} catch (Exception e) {
				logger.error("srem.Exception:", e);
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		}
		return 0L;
	}

	public static Long publish(final RedisKey channel, String message) {
		if (channel != null) {
			Jedis jedis = null;
			try {
				jedis = getResource(channel.builder.node);
				if (jedis == null) {
					logger.error("getResource.null: node=" + channel.builder.node);
					return null;
				}
				return jedis.publish(channel.name(), message);
			} catch (Exception e) {
				logger.error("publish.Exception:", e);
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		}
		return 0L;
	}


	public static class RedisKey {
		public final RedisKeyBuilder builder;
		
		private final List<Object> tags;
		private String name;
		
		public RedisKey(RedisKeyBuilder builder) {
			this.builder = builder;
			this.tags = new ArrayList<Object>();
			this.name = builder.prefix;
		}
		
		public RedisKey append(Object tag) {
			if(tag != null) {
				this.tags.add(tag);
			}
			return this;
		}
		
		public RedisKey reset() {
			this.tags.clear();
			return this;
		}
		
		public RedisKey append(Object[] tags) {
			if(tags != null) {
				for(int i=0; i<tags.length; i++) {
					this.tags.add(tags[i]);
				}
			}
			return this;
		}
		
		public String name() {
			if(this.tags.size() > 0) {
				StringBuilder sb = new StringBuilder();
				sb.append(builder.prefix);
				for(int i = 0; i < tags.size(); i++) {
					sb.append(":");
					sb.append(tags.get(i));
				}
				this.tags.clear();
				this.name = sb.toString();
			}
			return this.name;
		}
	}
	
	public static class RedisKeyBuilder {
		public final String node;
		
		public final String prefix;
		
		public final int expireTime;
		
		public RedisKeyBuilder(String node, String prefix, int expireTime) {
			this.node = node;
			this.prefix = prefix;
			this.expireTime = expireTime;
		}
		
		public RedisKey build() {
			return new RedisKey(this);
		}
		
	}

}
