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
package egovframework.brte.sample.example.support;

import java.util.List;

import org.springframework.batch.item.ItemWriter;

/**
 * 실제 Write 하지 않고, 정해진 시간만큼 대기하는 클래스
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
 * </pre>
 */
public class EgovDummyItemWriter implements ItemWriter<Object> {
	/**
	 * 바로 Wirte 하지 않고 대기 함
	 */
	public void write(List<? extends Object> item) throws Exception {
		// NO-OP
		Thread.sleep(500);
	}

}
