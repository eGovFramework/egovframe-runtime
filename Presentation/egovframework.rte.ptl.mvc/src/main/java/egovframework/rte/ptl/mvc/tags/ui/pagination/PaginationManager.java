/*
 * Copyright 2008-2009 MOPAS(Ministry of Public Administration and Security).
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
package egovframework.rte.ptl.mvc.tags.ui.pagination;

/**
 * PaginationManager.java
 * <p/><b>NOTE:</b><pre> PaginationRenderer의 구현체를 빈설정 파일을 참조하여 반환한다. 
 * 이는 이미지, 텍스트등 여러 포맷으로 보여줄 필요성이 있을 경우, 각 type의 구현클래스를 사용자가 지정하고 
 * Tag사용시에 type을 자유롭게 정하여 사용하기 위함이다.
 *                
 * 아래의 예와 같이 프로퍼티 rendererType에 여러 PaginationRenderer을 설정하면,
 * 페이징을 위한 태그인 <ui:pagination type="..."/>에서 사용자가 사용하려는 PaginationRenderer의 key값을 type 속성값으로 주면 된다. 
 * </pre>
 * <pre class="code">
 * &lt;bean id="imageRenderer" class="com.easycompany.tag.ImagePaginationRenderer"/&gt;
 * &lt;bean id="textRenderer" class="com.easycompany.tag.TextPaginationRenderer"/&gt;	
 * &lt;bean id="paginationManager" class="egovframework.rte.ptl.mvc.tags.ui.pagination.DefaultPaginationManager"&gt;
 *     &lt;property name="rendererType"&gt;
 *        &lt;map&gt;
 *           &lt;entry key="image" value-ref="imageRenderer"/&gt;
 *           &lt;entry key="text" value-ref="textRenderer"/&gt;
 *        &lt;/map&gt;
 *     &lt;/property&gt;
 * &lt;/bean&gt;
 * </pre>
 * @author 실행환경 개발팀 함철
 * @since 2009.06.01
 * @version 1.0
 *
 * <pre>
 * == 개정이력(Modification Information) ==
 *   
 *   수정일      수정자           수정내용
 *  -------    --------    ---------------------------
 *   2009.05.30  함철            최초 생성
 *
 * </pre>
 */

public interface PaginationManager {

	public PaginationRenderer getRendererType(String type);
}
