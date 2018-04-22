package org.seckill.dao.cache;

import org.seckill.entity.Seckill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.runtime.RuntimeSchema;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisDao {
	private Logger logger=LoggerFactory.getLogger(this.getClass());
	private final JedisPool jedisPool;
	
	public RedisDao(String ip,int port,int timeout,String password) {
		//获取连接池配置对象
				JedisPoolConfig jedisPoolConfig=new JedisPoolConfig();
				//设置最大连接数
				jedisPoolConfig.setMaxTotal(30);
				//最大空闲连接数
				jedisPoolConfig.setMaxIdle(10);
				//获取连接池
				jedisPool=new JedisPool(jedisPoolConfig,ip,port,timeout,password);
	}
	
	private RuntimeSchema<Seckill> schema=RuntimeSchema.createFrom(Seckill.class);
	
	public Seckill getSeckill(long seckillId) {
		//redis操作逻辑
		try {
			Jedis jedis = jedisPool.getResource();
			String key="seckill:"+seckillId;
			//并没有实现内部序列化操作
			//get-> byte[] -> 反序列化 -> Object(Seckill)
			//protostuff:pojo
			byte[] bytes=jedis.get(key.getBytes());
			//缓存重新获取到
			if(bytes !=null) {
				//空对象
				Seckill seckill=schema.newMessage();
				ProtobufIOUtil.mergeFrom(bytes, seckill, schema);
				//seckill被反序列化
				return seckill;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		return null;
	}
	
	public String putSeckill(Seckill seckill) {
		//set Object(Seckill) -> 序列化 -> byte[]
		try {
			Jedis jedis=jedisPool.getResource();
			try {
				String key="seckill:"+seckill.getSeckillId();
				byte[] bytes=ProtostuffIOUtil.toByteArray(seckill, schema, LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
				//超时缓存
				int timeout=60*60;
				String result=jedis.setex(key.getBytes(), timeout, bytes);
				return result;
			} finally {
				jedis.close();
			}
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		return null;
	}

}
