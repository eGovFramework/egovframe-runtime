package org.egovframe.rte.bat.core.step;

import java.io.File;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;

import org.egovframe.rte.bat.domain.CustomerCredit;
import org.egovframe.rte.bat.util.ApplicationContextProvider;

public class TaskletCreateDataStep implements Tasklet, InitializingBean {

	private static final Logger LOGGER = LoggerFactory.getLogger(TaskletCreateDataStep.class);
	
	@Override
	public void afterPropertiesSet() throws Exception {
		// Assert.notNull(directory, "directory must be set");
	}

	@Override
	public RepeatStatus execute(StepContribution contribution,
			ChunkContext chunkContext) throws Exception {

		LOGGER.debug("TaskletCreateDataStep execute START ===");

		ExecutionContext executionContext = new ExecutionContext();
		FlatFileItemWriter<CustomerCredit> writer = ApplicationContextProvider.getBean("delimitedToDelimitedJob-CreateData-delimitedItemWriter",FlatFileItemWriter.class);
		List<CustomerCredit> listDataInfo = new ArrayList<CustomerCredit>();
		writer.open(executionContext);

		Random random = new Random();
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
	    Date now = new Date();
	    int commitInterval = 10000;
	    int commitCount = 0;
	    
		// 테스트 데이타를 생성한다.
		int createCount = 1*10000;
		for ( int ii=0; ii<createCount; ii++) {
			
			now = new Date();
			CustomerCredit customerCredit = new CustomerCredit();
			
			customerCredit.setId(createCount+ii);
			customerCredit.setName("name"+(createCount+ii));
			customerCredit.setCredit(new BigDecimal(createCount));
			//customerCreditMore.setSerial("ABCD\"TEST\"\"1,000\"\"\""+(createCount+ii));
			//customerCreditMore.setTax(new BigDecimal(random.nextInt()));
			//customerCreditMore.setAmount(random.nextInt());
			//customerCreditMore.setCreateDate(sdfDate.format(now));
			//customerCreditMore.setChangeDate(sdfDate.format(now));
			listDataInfo.add(customerCredit);
			commitCount++;
			if (commitInterval == commitCount) {
				writer.write(listDataInfo);
				commitCount = 0;
				//for(int dd=0;dd<listDataInfo.size(); dd++) listDataInfo.set(dd, null);
				listDataInfo.clear();
				//listDataInfo = null;
				//listDataInfo = new ArrayList<CustomerCreditMore>();
			}
			
		}
		
		writer.close();

		return RepeatStatus.FINISHED;
	}

}
