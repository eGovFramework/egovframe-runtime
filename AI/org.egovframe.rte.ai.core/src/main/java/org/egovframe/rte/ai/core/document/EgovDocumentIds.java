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
import java.util.regex.Pattern;

/**
 * 안정 문서 식별자 — 같은 파일에 대해 실행마다 같은 값(재색인 시 이전 청크 삭제 키).
 *
 * <p>형식: <code>&lt;형식 접두&gt;-&lt;정제 파일명&gt;</code>, 페이지가 있으면 <code>_&lt;페이지&gt;</code>. 정제는 확장자 제거 →
 * 경로 위험 문자(<code>\ / : * ? " &lt; &gt; |</code>) 제거 → 공백 연속을 <code>-</code> 로.</p>
 *
 * <p><b>보안 주의(2026-09-08 보안 재점검, CWE-639)</b>: 파일명에서 파생한 식별자는 <b>클라이언트가 고를 수 있는 키</b>다.
 * 재색인 라이터는 이 값으로 기존 세그먼트를 지운 뒤 다시 넣으므로, 여러 사용자·부서가 같은 저장소를 쓰는 앱에서는
 * 다른 사용자가 같은 파일명으로 업로드하는 것만으로 남의 색인을 덮어쓸 수 있다. 호출자는 식별자에 소유자·테넌트
 * 접두({@code tenantId + ":" + fileName})를 붙이거나 저장소를 분리해야 한다.</p>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovDocumentIds {

	private static final Pattern UNSAFE = Pattern.compile("[\\\\/:*?\"<>|]");
	private static final Pattern WHITESPACE = Pattern.compile("\\s+");

	private EgovDocumentIds() {
	}

	/** <code>&lt;type&gt;-&lt;정제명&gt;</code>. */
	public static String of(String type, String fileName) {
		return requireType(type) + "-" + sanitize(fileName);
	}

	/** <code>&lt;type&gt;-&lt;정제명&gt;_&lt;page&gt;</code>. */
	public static String of(String type, String fileName, int page) {
		if (page <= 0) {
			throw new IllegalArgumentException("page 는 1 이상이어야 합니다: " + page);
		}
		return of(type, fileName) + "_" + page;
	}

	/**
	 * 파일명을 정제한다 — 확장자 제거, 경로 위험 문자 제거, 공백은 <code>-</code>.
	 *
	 * @throws IllegalArgumentException 정제 결과가 비어 있음
	 */
	public static String sanitize(String fileName) {
		Objects.requireNonNull(fileName, "fileName must not be null");
		String base = fileName;
		int slash = Math.max(base.lastIndexOf('/'), base.lastIndexOf('\\'));
		if (slash >= 0) {
			base = base.substring(slash + 1);
		}
		int dot = base.lastIndexOf('.');
		if (dot > 0) {
			base = base.substring(0, dot);
		}
		String cleaned = WHITESPACE.matcher(UNSAFE.matcher(base).replaceAll("").trim()).replaceAll("-");
		if (cleaned.isEmpty()) {
			throw new IllegalArgumentException("식별자로 쓸 수 있는 파일명이 아닙니다: " + fileName);
		}
		return cleaned;
	}

	private static String requireType(String type) {
		if (type == null || type.isBlank()) {
			throw new IllegalArgumentException("type 은 비어 있을 수 없습니다");
		}
		return type;
	}
}
