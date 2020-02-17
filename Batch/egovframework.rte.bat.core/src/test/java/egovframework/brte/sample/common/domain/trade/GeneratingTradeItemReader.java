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

package egovframework.brte.sample.common.domain.trade;

import java.math.BigDecimal;
import org.springframework.batch.item.ItemReader;

import egovframework.brte.sample.common.domain.trade.Trade;

/**
 * 원천소스에서 read 하는 것이 아닌, items 를 사용자 설정만큼 생성하는 클래스
 * 
 * @author 배치실행개발팀
 * @since 2012. 07.30
 * @version 1.0
 * @see
 * 
 *  * <pre>
 * << 개정이력(Modification Information) >>
 *   
 *   수정일               수정자               수정내용
 *  -------      --------     ---------------------------
 *  2012. 07.30  배치실행개발팀    최초 생성
 *
 * </pre>
 */
public class GeneratingTradeItemReader implements ItemReader<Trade> {

	private int limit = 1;

	private int counter = 0;

	public Trade read() throws Exception {
		if (counter < limit) {
			counter++;
			return new Trade("isin" + counter, counter, new BigDecimal(counter), "customer" + counter);
		}
		return null;
	}

	/**
	 * @param limit number of items that will be generated
	 * (null returned on consecutive calls).
	 */
	public void setLimit(int limit) {
		this.limit = limit;
	}

	public int getCounter() {
		return counter;
	}

	public int getLimit() {
		return limit;
	}

	public void resetCounter() {
		this.counter = 0;
	}
}
