package org.egovframe.rte.bat.sample.example.support;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.SerializationUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.ListIterator;

/**
 * 데이터 처리후 BATCH_STAGING 에 Insert 하는 라이터
 *
 * @author 배치실행개발팀
 * @version 1.0
 * @see <pre>
 *      개정이력(Modification Information)
 *
 *   수정일      수정자           수정내용
 *  ------- -------- ---------------------------
 *  2012. 07.25  배치실행개발팀     최초 생성
 *  </pre>
 * @since 2012. 07.25
 */
public abstract class EgovStagingItemWriter<T> extends JdbcDaoSupport implements StepExecutionListener, ItemWriter<T> {

    public static final String NEW = "N";
    public static final String DONE = "Y";
    public static final Object WORKING = "W";

    private DataFieldMaxValueIncrementer incrementer;
    private StepExecution stepExecution;

    /**
     * 설정 확인
     */
    protected void initDao() throws Exception {
        super.initDao();
        Assert.notNull(incrementer, "DataFieldMaxValueIncrementer is required - set the incrementer property in the " + ClassUtils.getShortName(EgovStagingItemWriter.class));
    }

    /**
     * incrementer 설정
     */

    public void setIncrementer(DataFieldMaxValueIncrementer incrementer) {
        this.incrementer = incrementer;
    }

    /**
     * BATCH_STAGING에 write함
     */
    public void write(final List<? extends T> items) {
        final ListIterator<? extends T> itemIterator = items.listIterator();
        getJdbcTemplate().batchUpdate("INSERT into BATCH_STAGING (ID, JOB_ID, VALUE, PROCESSED) values (?,?,?,?)", new BatchPreparedStatementSetter() {

            public int getBatchSize() {
                return items.size();
            }

            public void setValues(PreparedStatement ps, int i) throws SQLException {

                long id = incrementer.nextLongValue();
                long jobId = stepExecution.getJobExecution().getJobId();

                Assert.state(itemIterator.nextIndex() == i, "Item ordering must be preserved in batch sql update");

                byte[] blob = SerializationUtils.serialize(itemIterator.next());

                ps.setLong(1, id);
                ps.setLong(2, jobId);
                ps.setBytes(3, blob);
                ps.setString(4, NEW);
            }
        });
    }

    /**
     * ExitStatus를 null로 줌
     */
    public ExitStatus afterStep(StepExecution stepExecution) {
        return null;
    }

    /**
     * stepExecution 설정
     */
    public void beforeStep(StepExecution stepExecution) {
        this.stepExecution = stepExecution;
    }

}
