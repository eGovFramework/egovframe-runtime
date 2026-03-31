/*
 * Copyright 2008-2024 MOIS(Ministry of the Interior and Safety).
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
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;

import java.util.List;

/**
 * CompositeReader를 통해 ItemReaderList에 등록된 아이템을 읽어들임
 *
 * @author 배치실행개발팀
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2012.10.20	배치실행개발팀		최초 생성
 * </pre>
 * @since 2012.07.30
 */
public class EgovCompositeFileReader<T> implements ItemStreamReader<T> {

    private List<ItemStreamReader<?>> itemReaderList;
    private EgovItemsMapper<T> itemsMapper;
    private String returnType;

    public T read() throws Exception {
        Object[] items = new Object[itemReaderList.size()];
        int count = 0;
        int flagCount = 0;

        if (returnType.equalsIgnoreCase("READER")) {
            for (ItemStreamReader<?> itemReader : itemReaderList) {
                items[count] = itemReader;
                count++;
            }
        } else {
            for (ItemStreamReader<?> itemReader : itemReaderList) {
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
     */
    public void setItemsMapper(EgovItemsMapper<T> mapper) {
        this.itemsMapper = mapper;
    }

    /**
     * 설정파일에서 ItemReaderList에 등록한 값을  Setting
     */
    public void setItemReaderList(List<ItemStreamReader<?>> itemReaderList) {
        this.itemReaderList = itemReaderList;
    }

    /**
     * 설정파일에서  등록한 returnType을 Setting
     */
    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

}
