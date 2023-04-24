/*
 * Copyright 2012-2014 MOSPA(Ministry of Security and Public Administration).
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
package org.egovframe.rte.bat.core.item.composite.reader;

import org.egovframe.rte.bat.core.item.composite.EgovItemsMapper;
import org.springframework.batch.item.*;
import org.springframework.batch.item.database.AbstractCursorItemReader;

import java.util.List;

/**
 * CompositeCursorReader를 통해 ItemReaderList에 등록된 DB아이템을 읽어들임 
 * 
 * @author 배치실행개발팀
 * @since 2012.07.30
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2012.10.20	배치실행개발팀		최초 생성
 * </pre>
 */
public class EgovCompositeCursorReader<T> implements ItemStreamReader<T> {

	private List<AbstractCursorItemReader<?>> itemReaderList;
	private EgovItemsMapper<T> itemsMapper;
	private String returnType;

	public T read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
		Object[] items = new Object[itemReaderList.size()];
		int count = 0;
		int flagCount = 0;

		if (returnType.toUpperCase().equals("READER")) {
			for (AbstractCursorItemReader<?> itemReader : itemReaderList) {
				items[count] = itemReader;
				count++;
			}
		} else {
			for (AbstractCursorItemReader<?> itemReader : itemReaderList) {
				Object o = itemReader.read();
				if (o == null) {
					flagCount = flagCount + 1;
				} else {
					items[count] = o;
				}

				count++;

				if (flagCount == itemReaderList.size()) {
					return null;
				}
			}
		}

		return itemsMapper.mapItems(items);
	}

	/**
	 * 변경된 사항을 등록된 모든 Reader에 update함 
	 * @see org.springframework.batch.item.ItemStream#update(ExecutionContext)
	 */
	public void update(ExecutionContext executionContext) {
		for (ItemStream itemStream : itemReaderList) {
			itemStream.update(executionContext);
		}
	}

	/**
	 * 모든 Reader에 close를 호출함 
	 */
	public void close() throws ItemStreamException {
		for (ItemStream itemStream : itemReaderList) {
			itemStream.close();
		}
	}

	/**
	 * 모든 Reader에 open을 호출함
	 */
	public void open(ExecutionContext executionContext) throws ItemStreamException {
		for (ItemStream itemStream : itemReaderList) {
			itemStream.open(executionContext);
		}
	}

	/**
	 * 설정파일에 세팅한 값을 ItemMapper에 Setting
	 * @param mapper
	 */
	public void setItemsMapper(EgovItemsMapper<T> mapper) {
		this.itemsMapper = mapper;
	}

	/**
	 * 설정파일에서 ItemReaderList에 등록한 값을  Setting
	 * @param itemReaderList
	 */
	public void setItemReaderList(List<AbstractCursorItemReader<?>> itemReaderList) {
		this.itemReaderList = itemReaderList;
	}

	/**
	 * 설정파일에서  등록한 returnType을 Setting
	 * @param returnType
	 */
	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}

}
