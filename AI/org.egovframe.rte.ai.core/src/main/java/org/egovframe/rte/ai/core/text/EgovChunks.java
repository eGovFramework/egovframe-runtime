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
package org.egovframe.rte.ai.core.text;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.egovframe.rte.ai.core.text.EgovKoreanSentences.Span;

/**
 * 청킹 결과 — 원문 offset 구간 목록과 <b>한도 도달 여부</b>.
 *
 * <p>최대 청크 수에 걸려 뒷부분이 처리되지 않았으면 {@link #truncated()} 가 참이다. 참고 샘플은 이 경우 WARN 로그만
 * 남기고 문서를 "색인 완료"로 기록해 버려진 뒷부분이 파일을 고치기 전까지 다시 처리되지 않았다 — 호출 측이 결과에서
 * 이 사실을 읽어 예외를 내거나 문서를 나누도록 값으로 드러낸다.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.03  실행환경팀     최초 생성 (과제 23 add-ai-core)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovChunks {

	private final String source;
	private final List<Span> spans;
	private final boolean truncated;

	EgovChunks(String source, List<Span> spans, boolean truncated) {
		this.source = Objects.requireNonNull(source, "source must not be null");
		this.spans = List.copyOf(spans);
		this.truncated = truncated;
	}

	/** 청크 구간(원문 offset, 오름차순). */
	public List<Span> spans() {
		return spans;
	}

	/** 청크 텍스트 목록 — 각 항목은 원문의 연속 부분문자열이다. */
	public List<String> texts() {
		List<String> texts = new ArrayList<>(spans.size());
		for (Span span : spans) {
			texts.add(span.text(source));
		}
		return List.copyOf(texts);
	}

	/** 청크 수. */
	public int size() {
		return spans.size();
	}

	/** 비어 있는가. */
	public boolean isEmpty() {
		return spans.isEmpty();
	}

	/** 최대 청크 수에 도달해 뒷부분이 처리되지 않았는가. */
	public boolean truncated() {
		return truncated;
	}

	@Override
	public String toString() {
		return "EgovChunks[size=" + spans.size() + ", truncated=" + truncated + "]";
	}
}
