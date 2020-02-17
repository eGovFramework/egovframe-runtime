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

package egovframework.rte.bat.core.item.composite;

import egovframework.rte.bat.core.item.composite.provider.EgovCompositeDataProvider;

/**
 * CompositeReader를 통해 읽어들인 각 아이템을 Object[]형태로 Mapping
 * 
 * @author 배치실행개발팀
 * @since 2012. 07.30
 * @version 1.0
 * @see
 * <pre>
 * << 개정이력(Modification Information) >>
 *   
 *   수정일               수정자               수정내용
 *  -------      --------     ---------------------------
 *  2012. 10.20  배치실행개발팀    최초 생성
 * 
 * </pre>
 */
public class EgovCompositeItemMapper<T> implements EgovItemsMapper<EgovCompositeDataProvider> {
	/**
	 * 읽어들인 아이템 목록을 Processor에 넘겨주기 전에 VO형태로 매핑함
	 * @param items     
	 * @return     
	 * @throws Exception      
	 */    
	public EgovCompositeDataProvider mapItems(Object[] items) throws Exception {
		if (items != null && items.length > 0) {
			EgovCompositeDataProvider ecm = new EgovCompositeDataProvider();
			ecm.setMapItems(items);
			return ecm;
		} else {
			return null;
		}
	}
}
