package org.egovframe.rte.bat.exception;

import java.math.BigDecimal;

import javax.sql.DataSource;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import org.egovframe.rte.bat.exception.EgovBatchException;
import org.egovframe.brte.sample.common.domain.trade.CustomerCredit;

public class CustomerCreditIncreaseProcessor implements ItemProcessor<CustomerCredit, CustomerCredit> {

	// 증가할 수
	public static final BigDecimal FIXED_AMOUNT = new BigDecimal("5");
	
	@Autowired
    private DataSource dataSource;

	/**
	 * FIXED_AMOUNT만큼 증가 시킨 후 return
	 */
	@Override
	public CustomerCredit process(CustomerCredit item) throws Exception {
		
		try{
			
			int i = 1/0;
			
		}catch(Exception e){
			throw new EgovBatchException(dataSource,"EGOVBATCH000001");
		}
		
		return item.increaseCreditBy(FIXED_AMOUNT);
	}
}
