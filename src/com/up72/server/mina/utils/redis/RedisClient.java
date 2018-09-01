package com.up72.server.mina.utils.redis;

import java.util.Map;

import com.up72.game.constant.Cnst;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisClient {
	private JedisPool pool;
	private Map<String, String> params;

	public RedisClient(Map<String, String> params) {
		this.params = params;
		this.init();
	}
	
	private void init() {
		String redisHost = Cnst.REDIS_HOST;
		int redisPort = Integer.parseInt(Cnst.REDIS_PORT);
		int maxActive = 3000;
		int maxIdle = 200;
		int maxWait = 100000;
		boolean testOnBorrow = true;

		JedisPoolConfig conf = new JedisPoolConfig();
		conf.setMaxIdle(maxIdle);
//		conf.setMaxTotal(maxActive);
//		conf.setMaxWaitMillis(maxWait);
		conf.setMaxActive(maxActive);
		conf.setMaxWait(maxWait);
		conf.setTestOnBorrow(testOnBorrow);
		
		if (Cnst.REDIS_PASSWORD!=null&&!Cnst.REDIS_PASSWORD.equals("")) {
			this.pool = new JedisPool(conf, redisHost, redisPort,1000000,Cnst.REDIS_PASSWORD);
		}else{
			this.pool = new JedisPool(conf, redisHost, redisPort,1000000);
		}
		
	}

	public Jedis getJedis() {
		return (Jedis)this.pool.getResource();
	}

	public void returnJedis(Jedis jedis) {
		if(jedis != null) {
			this.pool.returnResource(jedis);
//			jedis.close();
		}

	}
	
	public void returnBrokenJedis(Jedis jedis) {
		if(jedis != null) {
			this.pool.returnBrokenResource(jedis);
//			jedis.close();
		}
		
	}

	public void setParams(Map<String, String> params) {
		this.params = params;
	}
}
