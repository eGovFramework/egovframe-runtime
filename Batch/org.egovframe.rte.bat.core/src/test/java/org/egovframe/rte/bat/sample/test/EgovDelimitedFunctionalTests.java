package org.egovframe.rte.bat.sample.test;

import org.egovframe.rte.bat.config.jobs.DelimitedIoJob;
import org.egovframe.rte.bat.sample.domain.trade.CustomerCredit;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Delimited 방식으로 데이터처리를 수행하는 테스트
 *
 * @author 배치실행개발팀
 * @version 1.0
 * @see
 * @since 2012. 06.27
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = DelimitedIoJob.class)
public class EgovDelimitedFunctionalTests extends EgovAbstractIoSampleTests {

    /**
     * 배치결과를 다시 읽을 때  reader 설정하는 메소드
     */
    @Override
    protected void pointReaderToOutput(ItemReader<CustomerCredit> reader) {
        // 출력 파일을 읽을 때는 FileSystemResource를 사용해야 하므로 reader의 resource를 직접 변경
        if (reader instanceof org.springframework.batch.item.file.FlatFileItemReader) {
            org.springframework.batch.item.file.FlatFileItemReader<CustomerCredit> fileReader =
                    (org.springframework.batch.item.file.FlatFileItemReader<CustomerCredit>) reader;
            fileReader.setResource(new org.springframework.core.io.FileSystemResource("./target/test-outputs/delimitedOutput.csv"));
        }
    }

    /**
     * 잡파라미터를 설정하기 위한 메소드
     */
    @Override
    protected JobParameters getUniqueJobParameters() {
        return new JobParametersBuilder(super.getUniqueJobParameters())
                .addString("inputFile", "META-INF/spring/delimited.csv")
                .addString("outputFile", "./target/test-outputs/delimitedOutput.csv").toJobParameters();
    }

}
