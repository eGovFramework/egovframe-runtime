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
 * 2026.09.10	실행환경 개발팀		발생 위치를 파라미터로 받는 run(Exception, String) 추가 — 싱글톤 상태 변경 방식 deprecated
 * </pre>
 * @since 2009.06.01
 */
public interface ExceptionHandlerService {

    /**
     * 패키지, 클래스 이름으로 패턴등록(Ant형식의 매칭).
     *
     * @param patterns 패턴리스트
     */
    void setPatterns(String[] patterns);

    /**
     * ExceptionHandler 리스트 등록.
     *
     * @param handlers handler리스트
     */
    void setHandlers(ExceptionHandler[] handlers);

    /**
     * 비교할 클래스 정보.
     *
     * @param canonicalName 비교할 클래스명
     * @deprecated 싱글톤 빈의 상태를 요청마다 바꾸는 방식이라 동시 요청에서 다른 호출의
     * 발생 위치로 매칭될 수 있다. {@link #run(Exception, String)} 으로 발생 위치를 직접 전달한다.
     */
    @Deprecated
    void setPackageName(String canonicalName);

    /**
     * setException 메소드.
     *
     * @param be Exception
     */
    void setException(Exception be);

    /**
     * setReqExpMatcher 메소드.
     *
     * @param pm 별도의 PathMatcher
     */
    void setReqExpMatcher(PathMatcher pm);

    /**
     * run 메소드 .
     *
     * @param exception 발생한 Exception
     * @return boolean 실행성공여부
     * @deprecated {@link #setPackageName(String)} 으로 미리 넣어 둔 상태에 의존하므로 동시 요청에서
     * 오동작할 수 있다. {@link #run(Exception, String)} 으로 대체한다.
     */
    @Deprecated
    boolean run(Exception exception) throws Exception;

    /**
     * 발생 위치(패키지.클래스.메소드)를 파라미터로 받아 후처리 로직을 실행한다.
     * 실행환경의 {@code ExceptionTransfer} 는 이 메소드로 호출한다.
     *
     * <p>기본 구현은 하위 호환을 위해 구 방식({@link #setPackageName(String)} 뒤 {@link #run(Exception)})으로
     * 위임하되, 인스턴스 잠금으로 동시 호출 간의 발생 위치 혼선을 막는다. 구현체가 이 메소드를 직접
     * 재정의하면 잠금 없이 상태 변경 없는 처리가 가능하다({@code DefaultExceptionHandleManager} 참고).</p>
     *
     * @param exception   발생한 Exception
     * @param packageName 발생 위치(패키지.클래스.메소드)
     * @return boolean 실행성공여부
     * @throws Exception 후처리 중 발생한 예외
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
    boolean hasReqExpMatcher();

}
