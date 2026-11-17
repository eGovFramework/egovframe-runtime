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
package org.egovframe.rte.ai.core.document;

import java.util.Objects;

/**
 * 추출 자원 예산 초과 — 어느 한도를 얼마나 넘었는지를 담은 {@link EgovExtractionException} 의 하위 형.
 *
 * <p>타입은 여전히 {@link EgovExtractionException} 이므로 기존 호출자의 처리 경로는 그대로이고, 예산을 구분해야 하는 호출자만
 * 이 형을 잡아 {@link #getLimit()} 로 나눈다. 잘린 본문 같은 부분 결과는 돌려주지 않는다(부분 처리를 성공으로 보고하지 않는다).</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.18  실행환경팀     최초 생성 (R51-06 파싱 자원 예산)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public class EgovExtractionBudgetExceededException extends EgovExtractionException {

	private static final long serialVersionUID = 1L;

	/** 넘은 한도의 종류. */
	public enum Limit {
		/** 파일 바이트 수. */
		FILE_BYTES("파일 크기 한도"),
		/** PDF 페이지 수. */
		PAGES("페이지 한도"),
		/** 추출 문자 수(한 파일의 결과 전체). */
		CHARS("문자 수 한도"),
		/** zip 컨테이너 엔트리 수. */
		ZIP_ENTRIES("zip 엔트리 수 한도"),
		/** zip 컨테이너 해제 바이트(개별·누적). */
		ZIP_BYTES("zip 해제 크기 한도");

		private final String description;

		Limit(String description) {
			this.description = description;
		}

		/** 사유 설명(한국어). */
		public String getDescription() {
			return description;
		}
	}

	private final Limit limit;
	private final long allowed;
	private final long actual;
	private final String fileName;

	/**
	 * @param limit    넘은 한도
	 * @param allowed  한도 값
	 * @param actual   실측 값(한도를 넘은 시점의 값 — 스트리밍 검사는 넘은 순간의 값)
	 * @param fileName 파일명
	 * @param message  메시지(사유 설명·파일명을 담는다)
	 */
	public EgovExtractionBudgetExceededException(Limit limit, long allowed, long actual, String fileName, String message) {
		super(message);
		this.limit = Objects.requireNonNull(limit, "limit must not be null");
		this.allowed = allowed;
		this.actual = actual;
		this.fileName = fileName;
	}

	/** 표준 메시지로 만든다: {@code <파일명>: <한도 설명> <한도> 초과(실측 <값>)}. */
	public static EgovExtractionBudgetExceededException of(Limit limit, long allowed, long actual, String fileName) {
		return new EgovExtractionBudgetExceededException(limit, allowed, actual, fileName,
				fileName + ": " + limit.getDescription() + " " + allowed + " 초과(실측 " + actual + ")");
	}

	public Limit getLimit() {
		return limit;
	}

	public long getAllowed() {
		return allowed;
	}

	public long getActual() {
		return actual;
	}

	public String getFileName() {
		return fileName;
	}
}
