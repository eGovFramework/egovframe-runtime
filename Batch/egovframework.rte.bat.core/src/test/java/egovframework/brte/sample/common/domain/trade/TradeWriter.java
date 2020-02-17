/*
 * Copyright 2006-2007 the original author or authors.
 *
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

package egovframework.brte.sample.common.domain.trade;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.annotation.AfterWrite;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamSupport;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.WriteFailedException;
import org.springframework.util.Assert;

/**
 * TradeWriter
 * 
 * @author 배치실행개발팀
 * @since 2012. 07.25
 * @version 1.0
 * @see <pre>
 *      개정이력(Modification Information)
 *   
 *   수정일      수정자           수정내용
 *  ------- -------- ---------------------------
 *  2012. 07.25  배치실행개발팀     최초 생성
 *  </pre>
 */

public class TradeWriter extends ItemStreamSupport implements ItemWriter<Trade> {

	private static final Logger LOGGER = LoggerFactory.getLogger(TradeWriter.class);

	public static final String TOTAL_AMOUNT_KEY = "TOTAL_AMOUNT";

	private TradeDao dao;

	private List<String> failingCustomers = new ArrayList<String>();

	private BigDecimal totalPrice = BigDecimal.ZERO;

	public void write(List<? extends Trade> trades) {

		for (Trade trade : trades) {

			LOGGER.debug("trade :: " + trade);

			dao.writeTrade(trade);

			Assert.notNull(trade.getPrice()); // There must be a price to total

			if (this.failingCustomers.contains(trade.getCustomer())) {
				throw new WriteFailedException("Something unexpected happened!");
			}
		}

	}

	@AfterWrite
	public void updateTotalPrice(List<Trade> trades) {
		for (Trade trade : trades) {
			this.totalPrice = this.totalPrice.add(trade.getPrice());
		}
	}

	@Override
	public void open(ExecutionContext executionContext) throws ItemStreamException {
		if (executionContext.containsKey(TOTAL_AMOUNT_KEY)) {
			this.totalPrice = (BigDecimal) executionContext.get(TOTAL_AMOUNT_KEY);
		} else {
			this.totalPrice = BigDecimal.ZERO;
		}
	}

	@Override
	public void update(ExecutionContext executionContext) {
		executionContext.put(TOTAL_AMOUNT_KEY, this.totalPrice);
	}

	public BigDecimal getTotalPrice() {
		return totalPrice;
	}

	public void setDao(TradeDao dao) {
		this.dao = dao;
	}

	public void setFailingCustomers(List<String> failingCustomers) {
		this.failingCustomers = failingCustomers;
	}
}
