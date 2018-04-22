package org.seckill.exception;

/**
 * 秒杀异常（运行期异常）
 * @author honghao
 *
 */
@SuppressWarnings("serial")
public class SeckillException extends RuntimeException{

	public SeckillException(String message) {
		super(message);
	}
	
	public SeckillException(String message,Throwable cause) {
		super(message,cause);
	}
}
