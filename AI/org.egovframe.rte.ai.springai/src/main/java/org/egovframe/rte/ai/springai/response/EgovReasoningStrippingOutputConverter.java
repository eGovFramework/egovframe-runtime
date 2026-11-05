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
package org.egovframe.rte.ai.springai.response;

import java.util.Objects;

import org.egovframe.rte.ai.core.response.EgovModelOutputs;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.converter.StructuredOutputConverter;

/**
 * 추론 태그 제거 출력 변환기 — 모델 출력에서 {@code <think>…</think>} 블록을 지우고(옵션: 첫 JSON 객체만 잘라) 위임 변환기에 넘긴다.
 *
 * <p>JSON 추출은 {@link BeanOutputConverter}·{@code MapOutputConverter} 처럼 JSON 을 기대하는 변환기용이며 {@code ListOutputConverter}
 * (CSV)에는 부적합해 옵션이다. 형식 지시문({@link #getFormat()})은 위임 변환기의 것을 그대로 돌려주고, 정리 후 위임 변환이 실패하면
 * 예외를 숨기지 않는다.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.03  실행환경팀     최초 생성 (과제 23 add-ai-springai — 샘플 EgovThinkTagOutputConverter 재구성)
 * </pre>
 *
 * @param <T> 변환 결과 타입
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovReasoningStrippingOutputConverter<T> implements StructuredOutputConverter<T> {

	private final StructuredOutputConverter<T> delegate;
	private final boolean extractJson;

	private EgovReasoningStrippingOutputConverter(StructuredOutputConverter<T> delegate, boolean extractJson) {
		this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
		this.extractJson = extractJson;
	}

	/**
	 * 위임 변환기를 감싼다.
	 *
	 * @param delegate 위임 변환기
	 * @param extractJson 첫 JSON 객체만 잘라 넘길지
	 * @param <T> 결과 타입
	 * @return 변환기
	 */
	public static <T> EgovReasoningStrippingOutputConverter<T> wrap(StructuredOutputConverter<T> delegate, boolean extractJson) {
		return new EgovReasoningStrippingOutputConverter<>(delegate, extractJson);
	}

	/**
	 * 빈 변환기({@link BeanOutputConverter}) + JSON 추출 켬.
	 *
	 * @param type 결과 타입
	 * @param <T> 결과 타입
	 * @return 변환기
	 */
	public static <T> EgovReasoningStrippingOutputConverter<T> forBean(Class<T> type) {
		return wrap(new BeanOutputConverter<>(type), true);
	}

	/** 위임 변환기. */
	public StructuredOutputConverter<T> delegate() {
		return delegate;
	}

	@Override
	public T convert(String source) {
		String cleaned = EgovModelOutputs.stripReasoning(source);
		if (extractJson) {
			cleaned = EgovModelOutputs.extractJsonObject(cleaned);
		}
		return delegate.convert(cleaned);
	}

	@Override
	public String getFormat() {
		return delegate.getFormat();
	}
}
