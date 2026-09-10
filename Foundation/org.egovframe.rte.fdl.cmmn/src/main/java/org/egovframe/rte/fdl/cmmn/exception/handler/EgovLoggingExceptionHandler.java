/*
 * Copyright 2009-2026 MOIS(Ministry of the Interior and Safety).
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
package org.egovframe.rte.fdl.cmmn.exception.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 발생한 예외를 <b>로그로 남기는</b> 기본 {@link ExceptionHandler} 구현체.
 *
 * <p><b>NOTE:</b> {@code ExceptionHandler} 는 인터페이스만 제공되어, "일단 로그만 남기면
 * 되는" 가장 흔한 경우에도 프로젝트마다 같은 클래스를 새로 만들어야 했다. 예외 추적
 * ({@code TraceHandler})은 기본 구현이 제공되는데 예외 처리는 아니었던 <b>비대칭</b>을 없앤다.</p>
 *
 * <pre>
 * &lt;bean id="defaultExceptionHandler"
 *       class="org.egovframe.rte.fdl.cmmn.exception.handler.EgovLoggingExceptionHandler" /&gt;
 *
 * &lt;bean id="exceptionHandleManager"
 *       class="org.egovframe.rte.fdl.cmmn.exception.manager.DefaultExceptionHandleManager"&gt;
 *     &lt;property name="patterns"&gt;&lt;list&gt;&lt;value&gt;**service.impl.*&lt;/value&gt;&lt;/list&gt;&lt;/property&gt;
 *     &lt;property name="handlers"&gt;&lt;list&gt;&lt;ref bean="defaultExceptionHandler" /&gt;&lt;/list&gt;&lt;/property&gt;
 * &lt;/bean&gt;
 * </pre>
 *
 * <p><b>로거를 발생 위치 이름으로 잡는다.</b> {@code occur} 에 넘어오는 {@code packageName}
 * 으로 로거를 얻으므로 {@code org.example.board} 처럼 <b>패키지별로 로그 레벨을 조정</b>할 수
 * 있다. 핸들러 클래스 이름 하나로 로거를 고정하면(공통컴포넌트 원본이 그랬다) 어느 업무에서
 * 난 예외든 같은 로거로 찍혀 걸러낼 수 없다.</p>
 *
 * <p>기본 레벨은 {@code ERROR} 이며 {@link #setLogLevel(Level)} 로 낮출 수 있다 —
 * 업무 예외처럼 정상 흐름의 일부인 경우 {@code WARN} 이 적절할 때가 있다.</p>
 *
 * <p><b>로그 위조(CWE-117) 경계.</b> 이 핸들러는 예외 메시지의 개행(CR/LF)을 중화하지 않는다 — 예외를
 * throwable 로 넘기므로 중화 지점은 로그 레이아웃이다. 사용자 입력이 예외 메시지에 실릴 수 있는 배치라면
 * 패턴 레이아웃에 {@code %enc{%m}{CRLF}} 와 throwable 평탄화({@code %replace{%xEx}{[\r\n]+}{ | }})를 둔다.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.01  실행환경팀     최초 생성 (공통컴포넌트 EgovComExcepHndlr ·
 *                            EgovComOthersExcepHndlr 의 역할만 차용 — 원본은 두 클래스가
 *                            사실상 같은 한 줄(LOGGER.error)이고 메일 발송 코드는 전부
 *                            주석 처리된 채였으며, 로거가 핸들러 클래스로 고정되어
 *                            발생 위치별 로그 제어가 불가능했다. 코드 이식 아님)
 *  2026.09.07  실행환경팀     로그 위조(CWE-117) 경계 명시 — CR/LF 중화는 레이아웃 책임
 *                            (코드 변경 없음)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public class EgovLoggingExceptionHandler implements ExceptionHandler {

	/** {@code packageName} 이 비어 있을 때 쓰는 로거 이름. */
	private static final String FALLBACK_LOGGER_NAME = EgovLoggingExceptionHandler.class.getName();

	private Level logLevel = Level.ERROR;

	/**
	 * 예외를 기록한다.
	 *
	 * <p>인자가 어떤 상태든 <b>예외를 다시 던지지 않는다</b> — 예외 처리 중에 또 예외가 나면
	 * 원래 예외가 묻히기 때문이다.</p>
	 *
	 * @param exception   발생한 예외({@code null} 허용)
	 * @param packageName 발생 위치(로거 이름으로 쓴다, {@code null} 허용)
	 */
	@Override
	public void occur(Exception exception, String packageName) {
		Logger logger = LoggerFactory.getLogger(loggerNameOf(packageName));
		String message = (packageName == null || packageName.trim().isEmpty())
				? "예외가 발생했습니다" : packageName;

		switch (logLevel) {
			case WARN:
				logger.warn(message, exception);
				break;
			case INFO:
				logger.info(message, exception);
				break;
			case DEBUG:
				logger.debug(message, exception);
				break;
			case ERROR:
			default:
				logger.error(message, exception);
		}
	}

	/**
	 * 기록 레벨을 지정한다(기본 {@code ERROR}).
	 *
	 * @param logLevel 레벨({@code null} 이면 {@code ERROR})
	 */
	public void setLogLevel(Level logLevel) {
		this.logLevel = (logLevel == null) ? Level.ERROR : logLevel;
	}

	/**
	 * 기록 레벨을 반환한다.
	 *
	 * @return 레벨
	 */
	public Level getLogLevel() {
		return logLevel;
	}

	private static String loggerNameOf(String packageName) {
		return (packageName == null || packageName.trim().isEmpty())
				? FALLBACK_LOGGER_NAME : packageName.trim();
	}

	/**
	 * 기록 레벨.
	 */
	public enum Level {

		/** 오류 — 기본값. */
		ERROR,

		/** 경고 — 업무 예외처럼 정상 흐름의 일부일 때. */
		WARN,

		/** 정보. */
		INFO,

		/** 디버그. */
		DEBUG
	}
}
