package org.egovframe.rte.bat.sample.example.support;

import org.egovframe.rte.bat.sample.domain.trade.Trade;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Trade 관련 Data 를 쓰는 Writer
 *
 * @author 배치실행개발팀
 * @see <pre>
 * == 개정이력(Modification Information) ==
 * 수정일               수정자               수정내용
 * ------      --------     ---------------------------
 * 2012. 07.30  배치실행개발팀    최초 생성
 * </pre>
 * @since 2012. 07.30
 */
public class EgovItemTrackingTradeItemWriter implements ItemWriter<Trade> {

    private List<Trade> items = new ArrayList<>();
    private String writeFailureISIN;
    private JdbcTemplate jdbcTemplate;

    /**
     * dataSource 셋팅
     */
    public void setDataSource(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * writeFailureISIN 셋팅
     */
    public void setWriteFailureISIN(String writeFailureISIN) {
        this.writeFailureISIN = writeFailureISIN;
    }

    /**
     * item getter
     */
    public List<Trade> getItems() {
        return items;
    }

    /**
     * items 셋팅
     */
    public void setItems(List<Trade> items) {
        this.items = items;
    }

    /**
     * items 의 요소를 삭제
     */
    public void clearItems() {
        this.items.clear();
    }

    /**
     * TRADE 테이블에 items 의 정보를 update 하는 Write
     */
    @Override
    public void write(Chunk<? extends Trade> chunk) throws Exception {
        List<Trade> items = (List<Trade>) chunk.getItems();
        List<Trade> newItems = new ArrayList<>();
        for (Trade t : items) {
            if (t.getIsin().equals(this.writeFailureISIN)) {
                throw new IOException("write failed");
            }
            newItems.add(t);
            if (jdbcTemplate != null) {
                jdbcTemplate.update("UPDATE TRADE set VERSION=? where ID=? and version=?", t.getVersion() + 1, t.getId(), t.getVersion());
            }
        }
        this.items.addAll(newItems);
    }

}
