package org.egovframe.rte.bat.sample.example.support;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * BATCH_STAGING에 processed 값 업데이트
 *
 * @param <T> item type
 * @see EgovStagingItemReader
 * @see EgovStagingItemWriter
 * @see EgovProcessIndicatorItemWrapper
 * <p>
 * <p>
 * == 개정이력(Modification Information) ==
 * <p>
 * 수정일               수정자               수정내용
 * -------      --------     ---------------------------
 * 2017.07.06   장동한         SimpleJdbcTemplate(Deprecated) > NamedParameterJdbcTemplate 변경
 */
@SuppressWarnings("deprecation")
public class EgovStagingItemProcessor<T> implements ItemProcessor<EgovProcessIndicatorItemWrapper<T>, T>, InitializingBean {

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    /**
     * DataSource 세팅
     */
    public void setDataSource(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    /**
     * 설정확인
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(namedParameterJdbcTemplate, "Either jdbcTemplate or dataSource must be set");
    }

    /**
     * BATCH_STAGING에 processed 값 업데이트
     */
    @Override
    public T process(EgovProcessIndicatorItemWrapper<T> wrapper) throws Exception {

        Map<String, Object> namedParameters = new HashMap<String, Object>();
        namedParameters.put("processed1", EgovStagingItemWriter.DONE);
        namedParameters.put("id", wrapper.getId());
        namedParameters.put("processed2", EgovStagingItemWriter.NEW);

        String query = "UPDATE BATCH_STAGING SET PROCESSED=:processed1 WHERE ID=:id AND PROCESSED=:processed2";
        int count = namedParameterJdbcTemplate.update(query, namedParameters);

        if (count != 1) {
            throw new OptimisticLockingFailureException("The staging record with ID=" + wrapper.getId() + " was updated concurrently when trying to mark as complete (updated "
                    + count + " records.");
        }
        return wrapper.getItem();
    }

}
