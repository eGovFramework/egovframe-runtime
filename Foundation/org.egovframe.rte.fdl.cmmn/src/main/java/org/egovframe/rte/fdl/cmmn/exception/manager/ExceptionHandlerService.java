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
package org.egovframe.rte.fdl.cmmn.exception.manager;

import org.egovframe.rte.fdl.cmmn.exception.handler.ExceptionHandler;
import org.springframework.util.PathMatcher;

/**
 * ExceptionHandlerService 인터페이스.
 * 구현시 run(Exception exception) 만 구현한다.
 *
 * @author Judd Cho (horanghi@gmail.com)
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2009.05.30	Judd Cho			최초 생성
 * 2015.01.31	Vincent Han			코드 품질 개선
 * </pre>
 * @since 2009.06.01
 */
public interface ExceptionHandlerService {

    /**
     * 패키지, 클래스 이름으로 패턴등록(Ant형식의 매칭).
     *
     * @param patterns 패턴리스트
     */
    public void setPatterns(String[] patterns);

    /**
     * ExceptionHandler 리스트 등록.
     *
     * @param handlers handler리스트
     */
    public void setHandlers(ExceptionHandler[] handlers);

    /**
     * 비교할 클래스 정보.
     *
     * @param canonicalName 비교할 클래스명
     */
    public void setPackageName(String canonicalName);

    /**
     * setException 메소드.
     *
     * @param be Exception
     */
    public void setException(Exception be);

    /**
     * setReqExpMatcher 메소드.
     *
     * @param pm 별도의 PathMatcher
     */
    public void setReqExpMatcher(PathMatcher pm);

    /**
     * run 메소드 .
     *
     * @param exception 발생한 Exception
     * @return boolean 실행성공여부
     */
    public boolean run(Exception exception) throws Exception;

    /**
     * 발생 위치를 인자로 받아 실행한다.
     *
     * <p>싱글톤 빈의 상태를 {@link #setPackageName(String)} 으로 바꾼 뒤 {@link #run(Exception)} 을 호출하면
     * 동시 요청 간에 발생 위치가 섞인다. 기본 구현은 하위 호환을 위해 두 호출을 이 객체로 동기화하며,
     * 구현체는 상태를 바꾸지 않도록 재정의할 수 있다({@link DefaultExceptionHandleManager} 참조).</p>
     *
     * @param exception   발생한 Exception
     * @param packageName 발생 위치(클래스 FQCN.메서드명)
     * @return boolean 실행성공여부
     */
    default boolean run(Exception exception, String packageName) throws Exception {
        synchronized (this) {
            setPackageName(packageName);
            return run(exception);
        }
    }

    /**
     * PathMatcher 가 있는지 여부 반환.
     *
     * @return boolean true|false
     */
    public boolean hasReqExpMatcher();

}
