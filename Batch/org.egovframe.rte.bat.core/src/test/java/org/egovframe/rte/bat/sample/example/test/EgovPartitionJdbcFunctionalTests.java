package org.egovframe.rte.bat.sample.example.test;

import org.egovframe.rte.bat.config.SimpleJobLauncherConfig;
import org.egovframe.rte.bat.config.jobs.PartitionJdbcJob;
import org.egovframe.rte.bat.sample.domain.trade.CustomerCredit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * DB를 gridSize 만큼 나누어 파티셔닝으로 처리하는 내용을 테스트
 *
 * @author 배치실행개발팀
 * @version 1.0
 * @see <pre>
 * == 개정이력(Modification Information) ==
 *
 *   수정일               수정자               수정내용
 *  -------      --------     ---------------------------
 *  2012. 07.30  배치실행개발팀    최초 생성
 *
 * </pre>
 * @since 2012. 07.30
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {PartitionJdbcJob.class, SimpleJobLauncherConfig.class})
public class EgovPartitionJdbcFunctionalTests implements ApplicationContextAware {

    @Autowired
    private ItemReader<CustomerCredit> inputReader;

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    private ApplicationContext applicationContext;

    private JdbcTemplate jdbcTemplate;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * datasource 설정
     */
    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * 배치작업 테스트 전에 DB관련 작업
     */
    @BeforeEach
    public void setUp() {
        jdbcTemplate.update("DELETE from CUSTOMER");
        jdbcTemplate.update("INSERT INTO CUSTOMER (ID, VERSION, NAME, CREDIT) VALUES  (1, 0, 'customer1', 100000)");
        jdbcTemplate.update("INSERT INTO CUSTOMER (ID, VERSION, NAME, CREDIT) VALUES  (2, 0, 'customer2', 100000)");
        jdbcTemplate.update("INSERT INTO CUSTOMER (ID, VERSION, NAME, CREDIT) VALUES  (3, 0, 'customer3', 100000)");
        jdbcTemplate.update("INSERT INTO CUSTOMER (ID, VERSION, NAME, CREDIT) VALUES  (4, 0, 'customer4', 100000)");
    }

    /**
     * 배치작업 테스트
     */
    @Test
    public void testUpdateCredit() throws Exception {
        assertTrue(applicationContext.containsBeanDefinition("outputTestReader"), "Define a prototype bean called 'outputTestReader' to check the output");

        open(inputReader);
        List<CustomerCredit> inputs = new ArrayList<CustomerCredit>(getCredits(inputReader));
        close(inputReader);

        JobExecution jobExecution = jobLauncherTestUtils.launchJob();
        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());

        // 출력 파일들을 읽기 위해 디렉토리에서 생성된 파일들을 찾아서 읽기
        List<CustomerCredit> outputs = readOutputFiles();

        assertEquals(inputs.size(), outputs.size());
        int itemCount = inputs.size();
        assertTrue(itemCount > 0);

        inputs.iterator();
        for (int i = 0; i < itemCount; i++) {
            assertEquals(inputs.get(i).getCredit().intValue(), outputs.get(i).getCredit().intValue());
        }
    }

    /**
     * 배치작업의 결과값을 set로 만드는 메소드
     */
    private Set<CustomerCredit> getCredits(ItemReader<CustomerCredit> reader) throws Exception {
        CustomerCredit credit;
        Set<CustomerCredit> result = new LinkedHashSet<CustomerCredit>();
        while ((credit = reader.read()) != null) {
            result.add(credit);
        }
        return result;
    }

    /**
     * reader를 open하는 메소드
     */
    private void open(ItemReader<?> reader) {
        if (reader instanceof ItemStream) {
            ((ItemStream) reader).open(new ExecutionContext());
        }
    }

    /**
     * reader를 close하는 메소드
     */
    private void close(ItemReader<?> reader) {
        if (reader instanceof ItemStream) {
            ((ItemStream) reader).close();
        }
    }

    /**
     * 출력 파일들을 읽어서 CustomerCredit 리스트로 반환하는 메서드
     */
    private List<CustomerCredit> readOutputFiles() throws Exception {
        List<CustomerCredit> result = new ArrayList<>();
        File outputDir = new File("./target/test-outputs/partition/db/");

        if (!outputDir.exists()) {
            outputDir.mkdirs();
            return result; // 디렉토리가 없으면 빈 리스트 반환
        }

        File[] csvFiles = outputDir.listFiles((dir, name) -> name.endsWith(".csv"));
        if (csvFiles == null || csvFiles.length == 0) {
            return result; // CSV 파일이 없으면 빈 리스트 반환
        }

        for (File csvFile : csvFiles) {
            FlatFileItemReader<CustomerCredit> reader = createFileReader(csvFile);
            open(reader);

            CustomerCredit item;
            while ((item = reader.read()) != null) {
                result.add(item);
            }

            close(reader);
        }

        return result;
    }

    /**
     * 파일을 읽기 위한 FlatFileItemReader를 생성하는 메서드
     */
    private FlatFileItemReader<CustomerCredit> createFileReader(File file) {
        FlatFileItemReader<CustomerCredit> reader = new FlatFileItemReader<>();
        reader.setResource(new FileSystemResource(file));

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setDelimiter(",");
        tokenizer.setNames("id", "name", "credit");

        BeanWrapperFieldSetMapper<CustomerCredit> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(CustomerCredit.class);

        DefaultLineMapper<CustomerCredit> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        reader.setLineMapper(lineMapper);
        return reader;
    }

}
