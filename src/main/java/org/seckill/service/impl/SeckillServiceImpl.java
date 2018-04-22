package org.seckill.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.seckill.dao.SeckillDao;
import org.seckill.dao.SuccessKilledDao;
import org.seckill.dao.cache.RedisDao;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.entity.SuccessKilled;
import org.seckill.enums.SeckillStatEnum;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

//@Component @Service @Dao @Controller
@Service
public class SeckillServiceImpl implements SeckillService {
	private Logger logger=LoggerFactory.getLogger(this.getClass());
	
	/**
	 * 注入Dao实现类依赖
	 */
	@Autowired
	private SeckillDao seckillDao;
	
    @Autowired
    private SuccessKilledDao successKilledDao;
    
	@Autowired
	private RedisDao redisDao;
   
    /**
     * md5盐值字符串，用于混淆MD5
     */
    private String slat="asdfwebfsargdgfjwrta123￥#@￥&#*";
	
	@Override
	public List<Seckill> getSeckillList() {
		return seckillDao.queryAll(0, 4);
	}

	@Override
	public Seckill queryById(long seckillId) {
		return seckillDao.queryById(seckillId);
	}

	@Override
	public Exposer exportSeckillUrl(long seckillId) {
		//优化点：缓存优化:超时的基础上维护一致性
		//1.访问redis
		Seckill seckill=redisDao.getSeckill(seckillId);
		if(seckill==null) {//秒杀不存在
			//2.访问数据库
			seckill=seckillDao.queryById(seckillId);
			if(seckill==null){
				return new Exposer(false, seckillId);
			}else {
				//3.放入redis
				redisDao.putSeckill(seckill);
			}
		}
		
		Date startTime = seckill.getStartTime();
		Date endTime = seckill.getEndTime();
		//系统当前时间
		Date nowTime=new Date();
		if(startTime.getTime()>nowTime.getTime() || nowTime.getTime()>endTime.getTime()) {//秒杀未开始
			return new Exposer(false, seckillId, nowTime.getTime(), startTime.getTime(), endTime.getTime());
		}
		//转化特定字符串的过程，不可逆
		String md5=getMD5(seckillId);
		return new Exposer(true, md5, seckillId);
	}
	
	private String getMD5(long seckillId) {
		String base=seckillId+"/"+slat;
		String md5=DigestUtils.md5DigestAsHex(base.getBytes());
		return md5;
	}

	@Override
	@Transactional
	/**
	 * 使用注解控制事务方法的要点
	 * 1.开发团队达成一致约定，明确标注事务方法的变成风格
	 * 2.保证事务方法的执行时间尽可能短，不要穿插其他的网络操作，RPC/HTTP请求或者剥离到事务方法外部
	 * 3.不需要所有的方法都需要事务，如只有一条修改操作，只读操作不需要事务控制
	 */
	public SeckillExecution exexuteSeckill(long seckillId, long userPhone, String md5)
			throws SeckillException, RepeatKillException, SeckillCloseException {
		
		if(md5==null || !md5.equals(getMD5(seckillId))) {
			throw new SeckillException("seckill data rewrite 秒杀被篡改");
		}
		Date killTime=new Date();
		try {
				//记录购买行为
				int insertState=successKilledDao.insertSuccessKilled(seckillId, userPhone);
				//唯一：seckillId,userPhone
				if(insertState<=0) {
					//重复秒杀
					throw new RepeatKillException("seckill repeated 重复秒杀");
				}else {
					//减库存
					int reduceState=seckillDao.reduceNumber(seckillId, killTime);
					if(reduceState<=0) {
						throw new SeckillException("seckill is closed 秒杀关闭");
					}else {
						//秒杀成功 commit
						SuccessKilled successKilled=successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
						return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS, successKilled);
					}
				}
		}catch (SeckillCloseException e1) {
			throw e1;
		}catch (RepeatKillException e2) {
			throw e2;
		}catch (Exception e) {
			logger.error(e.getMessage(),e);
			//所有编译器异常 转化成运行时异常
			throw new SeckillException("seckill inner error:"+e.getMessage());
		}
	}
	
	@Override
	public SeckillExecution exexuteSeckillProcedure(long seckillId, long userPhone, String md5){
		if( md5 ==null || !md5.equals(getMD5(seckillId))) {
			return new SeckillExecution(seckillId, SeckillStatEnum.DATA_REWRITE);
		}
		Date killTime=new Date();
		Map<String, Object> map=new HashMap<String, Object>();
		map.put("seckillId", seckillId);
		map.put("phone", userPhone);
		map.put("killTime", killTime);
		map.put("result", null);
		//执行存储过程，result被复制
		try {
			seckillDao.killByProcedure(map);
			int result=MapUtils.getInteger(map, "result",-2);
			if(result==1) {
				SuccessKilled sk=successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
				return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS, sk);
			}else {
				return new SeckillExecution(seckillId, SeckillStatEnum.stateOf(result));
			}
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			return new SeckillExecution(seckillId, SeckillStatEnum.INNER_ERROR);
		}
	}
}
