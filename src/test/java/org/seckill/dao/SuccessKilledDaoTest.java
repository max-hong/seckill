package org.seckill.dao;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.entity.SuccessKilled;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class SuccessKilledDaoTest {
	
	 //注入Dao实现类依赖
    @Resource
    private SuccessKilledDao successKilledDao;
    
    @Resource
    private SeckillDao seckillDao;
	
	@Test
	public void testInsertSuccessKilled() {
		
		System.out.println(successKilledDao.insertSuccessKilled(1000, 17688768671L));
	}

	@Test
	public void testQueryByIdWithSeckill() {
		SuccessKilled successKilled=successKilledDao.queryByIdWithSeckill(1000, 17688768671L);
		System.out.println(successKilled.getSeckill().toString());
	}
}
