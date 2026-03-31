package org.egovframe.rte.bat.sample.test;

import org.egovframe.rte.bat.config.SimpleJobLauncherConfig;
import org.egovframe.rte.bat.config.jobs.RestartFileSampleJob;
import org.egovframe.rte.bat.sample.domain.trade.CustomerCredit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RestartFileSampleJob.class, SimpleJobLauncherConfig.class})
public class EgovRestartFileSampleFunctionalTests {

    @Autowired
    private FileSystemResource outputResource;

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Test
    public void runTest() throws Exception {
        JobParameters jobParameters = jobLauncherTestUtils.getUniqueJobParameters();

        JobExecution je1 = jobLauncherTestUtils.launchJob(jobParameters);
        assertEquals(BatchStatus.FAILED, je1.getStatus());
        assertLineCount(10, outputResource);

        JobExecution je2 = jobLauncherTestUtils.launchJob(jobParameters);
        assertEquals(BatchStatus.COMPLETED, je2.getStatus());
        assertLineCount(20, outputResource);
    }

    private void assertLineCount(long expectedLineCount, Resource resource) throws Exception {
        Path path = resource.getFile().toPath();
        long actualLineCount = Files.lines(path).count();
        assertEquals(expectedLineCount, actualLineCount, "Line count does not match.");
    }

    public static class CustomerCreditFlatFileItemWriter extends FlatFileItemWriter<CustomerCredit> {
        private boolean failed = false;

        @Override
        public void write(Chunk<? extends CustomerCredit> items) throws Exception {
            for (CustomerCredit cc : items) {
                if (!failed && cc.getName().equals("customer13")) {
                    failed = true;
                    throw new RuntimeException("customer13 is failed");
                }
            }
            super.write(items);
        }
    }

}
