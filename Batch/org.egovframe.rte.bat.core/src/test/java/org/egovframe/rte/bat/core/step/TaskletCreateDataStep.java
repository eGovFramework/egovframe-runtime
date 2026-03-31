package org.egovframe.rte.bat.core.step;

import org.egovframe.rte.bat.domain.CustomerCredit;
import org.egovframe.rte.bat.util.ApplicationContextProvider;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class TaskletCreateDataStep implements Tasklet, InitializingBean {

    @Override
    public void afterPropertiesSet() {
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        ExecutionContext executionContext = new ExecutionContext();
        FlatFileItemWriter<CustomerCredit> writer = ApplicationContextProvider.getBean("delimitedToDelimitedJob-CreateData-delimitedItemWriter", FlatFileItemWriter.class);
        List<CustomerCredit> listDataInfo = new ArrayList<CustomerCredit>();
        writer.open(executionContext);

        Random random = new Random();
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
        Date now = new Date();
        int commitInterval = 10000;
        int commitCount = 0;

        // 테스트 데이타를 생성한다.
        int createCount = 10000;
        for (int ii = 0; ii < createCount; ii++) {
            now = new Date();
            CustomerCredit customerCredit = new CustomerCredit();
            customerCredit.setId(createCount + ii);
            customerCredit.setName("name" + (createCount + ii));
            customerCredit.setCredit(new BigDecimal(createCount));
            listDataInfo.add(customerCredit);
            commitCount++;

            if (commitInterval == commitCount) {
                writer.write(Chunk.of(listDataInfo.toArray(new CustomerCredit[0])));
                commitCount = 0;
                listDataInfo.clear();
            }
        }
        writer.close();

        return RepeatStatus.FINISHED;
    }

}
