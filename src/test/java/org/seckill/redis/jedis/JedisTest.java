package org.seckill.redis.jedis;

import org.junit.Test;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class JedisTest {

	@Test
	public void testJedis() {
		Jedis jedis = new Jedis("39.108.59.106",6379);
		jedis.auth("honghao");
		//jedis.set("name", "imooc");
		String name = jedis.get("name");
		System.out.println(name);
		jedis.close();
	}
	
	/**
	 * 连接池方式连接
	 */
	@Test
	public void testJedisPool() {
		//获取连接池配置对象
		JedisPoolConfig jedisPoolConfig=new JedisPoolConfig();
		//设置最大连接数
		jedisPoolConfig.setMaxTotal(30);
		//最大空闲连接数
		jedisPoolConfig.setMaxIdle(10);
		
		//获取连接池
		JedisPool jedisPool=new JedisPool(jedisPoolConfig, "39.108.59.106",6379,2000,"honghao");
		
		//获得核心对象
		Jedis jedis=null;
		try {
			jedis=jedisPool.getResource();
			//jedis.set("name", "28");
			System.out.println(jedis.get("name"));
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			if(jedis!=null) {
				jedis.close();
			}
			if(jedisPool!=null) {
				jedisPool.close();
			}
		}
		
	}
}
