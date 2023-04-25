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
package org.egovframe.rte.fdl.security.intercept;

import org.egovframe.rte.fdl.security.securedobject.EgovSecuredObjectService;
import org.springframework.beans.factory.support.MethodReplacer;
import org.springframework.security.access.ConfigAttribute;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 보호자원의 url 요청정보에 대한 권한을 매핑하는 클래스
 * 
 * <p><b>NOTE:</b> 매 request 마다 요청 url 에 대한 best matching 보호자원-권한 맵핑 정보를 DB 기반으로 찾기 위해
 * DefaultFilterInvocationDefinitionSource 의 lookupAttributes 메서드를 가로채어 수행하기 위한 MethodReplacer 이다.</p>
 * 
 * @author ByungHun Woo
 * @since 2009.06.01
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2009.06.01   윤성종             최초 생성
 * 2014.01.22   한성곤             Spring Security 3.2.X 업그레이드 적용
 * </pre>
 */
public class LookupAttributesMethodReplacer implements MethodReplacer {

    private EgovSecuredObjectService securedObjectService;

    /**
     * <p>
     * SecuredObjectService를 설정하는 메소드이다.
     * </p>
     * @param securedObjectService <code>EgovSecuredObjectService</code>
     */
    public void setSecuredObjectService(EgovSecuredObjectService securedObjectService) {
        this.securedObjectService = securedObjectService;
    }

    /**
     * <p>
     * best matching 보호자원-권한 맵핑 정보를 DB에서 조회한다.
     * </p>
     * @param target <code>Object</code>
     * @param method <code>Method</code>
     * @param args <code>Object[]</code>
     * @return
     * @throws Exception
     * @see org.springframework.beans.factory.support.MethodReplacer#reimplement(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     */
    public Object reimplement(Object target, Method method, Object[] args) throws Exception {
        List<ConfigAttribute> attributes = null;
        // DB 검색
        attributes = securedObjectService.getMatchedRequestMapping((String) args[0]);
        return attributes;
    }

}
