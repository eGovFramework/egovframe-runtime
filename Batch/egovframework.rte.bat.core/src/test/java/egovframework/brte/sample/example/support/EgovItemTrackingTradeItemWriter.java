/*
 * Copyright 2006-2007 the original author or authors. *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package egovframework.brte.sample.example.support;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import egovframework.brte.sample.common.domain.trade.Trade;

import javax.sql.DataSource;

import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Trade 관련 Data 를 쓰는 Writer
 *
 * @author 배치실행개발팀
 * @since 2012. 07.30
 * @see <pre>
 * << 개정이력(Modification Information) >>
 * 수정일               수정자               수정내용
 * ------      --------     ---------------------------
 * 2012. 07.30  배치실행개발팀    최초 생성
 * </pre>
 */

public class EgovItemTrackingTradeItemWriter implements ItemWriter<Trade> {
	// items
	private List<Trade> items = new ArrayList<Trade>();
	// writeFailureISIN
	private String writeFailureISIN;
	// jdbcTemplate
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
	 * items 셋팅
	 */
	public void setItems(List<Trade> items) {
		this.items = items;
	}

	/**
	 * item getter
	 */
	public List<Trade> getItems() {
		return items;
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
	public void write(List<? extends Trade> items) throws Exception {
		List<Trade> newItems = new ArrayList<Trade>();
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
