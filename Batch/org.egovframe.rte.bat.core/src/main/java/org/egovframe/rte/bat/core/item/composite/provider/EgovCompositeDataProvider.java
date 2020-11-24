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
package org.egovframe.rte.bat.core.item.composite.provider;

/**
 * CompositeReader를 통해 ItemReaderList에 등록된 아이템을 읽어들임 
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
public class EgovCompositeDataProvider {

	Object[] mapItems = null;

	/**
	 * Object[]값 Getter
	 */
	public Object[] getMapItems() {
		return mapItems;
	}

	/**
	 * 여러 개의 Object[]값 Setter
	 * @param mapItems
	 */
	public void setMapItems(Object[] mapItems) {
		this.mapItems = mapItems;
	}

}
