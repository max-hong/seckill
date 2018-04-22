package org.seckill.service;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml",
	"classpath:spring/spring-service.xml"})
public class SeckillServiceTest{

	private final Logger logger=LoggerFactory.getLogger(this.getClass());
	
    @Autowired
    private SeckillService seckillService;

    @Test
    public void testGetSeckillList() throws Exception {
    	List<Seckill> seckillList=seckillService.getSeckillList();
    	logger.info("seckillList={}",seckillList);
    }

    @Test
    public void testGetById() throws Exception {
    	Seckill seckill = seckillService.queryById(1000);
    	logger.info("seckill={}",seckill);
    }

    /**
     * 集成测试代码完整逻辑，注意可重复执行
     * @throws Exception
     */
    @Test
    public void testExportSeckillLogic() throws Exception {
        long seckillId=1001;
        Exposer exposer=seckillService.exportSeckillUrl(seckillId);
        if(exposer.isExposed()) {
        	logger.info("exposer={}",exposer);
        	long phone=17688768671L;
        	String md5=exposer.getMd5();
        	try {
        		SeckillExecution seckillExecution=seckillService.exexuteSeckill(seckillId, phone, md5);
        		logger.info("seckillExecution={}",seckillExecution);
    		} catch (RepeatKillException e) { 
    			logger.error(e.getMessage());
    		} catch (SeckillCloseException e) { 
    			logger.error(e.getMessage());
    		}catch (SeckillException e) { 
    			logger.error(e.getMessage());
    		}
        }else {
        	//秒杀未开启
        	logger.warn("exposer={}",exposer);
        }
       /* exposed=true,md5=91b9ec6600d94308050134a6de8050ab,seckillId=1000,now=0,start=0,end=0*/
    }

    @Test
    public void executeSeckillProcedure() {
    	long seckillId=1001;
    	long phone=17688768672L;
    	Exposer exposer=seckillService.exportSeckillUrl(seckillId);
    	if(exposer.isExposed()) {
    		String md5=exposer.getMd5();
    		SeckillExecution seckillExecution=seckillService.exexuteSeckillProcedure(seckillId, phone, md5);
    		logger.info("seckillExecution={}",seckillExecution);
    	}else {
        	//秒杀未开启
        	logger.warn("exposer={}",exposer);
        }
    	
    	
    	
    }
    
}