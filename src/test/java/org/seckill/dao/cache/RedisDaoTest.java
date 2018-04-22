package org.seckill.dao.cache;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.dao.SeckillDao;
import org.seckill.entity.Seckill;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author honghao
 */
@RunWith(SpringJUnit4ClassRunner.class)
//告诉junit spring的配置文件
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class RedisDaoTest{
	
	private long seckillId=1000;
	
	@Autowired
	private RedisDao redisDao;

    //注入Dao实现类依赖
	@Autowired
    private SeckillDao seckillDao;

    @Test
    public void testSeckill() throws Exception {
    		//get and put
    	Seckill seckill=redisDao.getSeckill(seckillId);
    	if(seckill ==null) {
    		seckill = seckillDao.queryById(seckillId);
    		if(seckill != null) {
    			String result=redisDao.putSeckill(seckill);
    			System.out.println(result);
    			seckill=redisDao.getSeckill(seckillId);
    			System.out.println(seckill);
    		}
    	}
    }


}