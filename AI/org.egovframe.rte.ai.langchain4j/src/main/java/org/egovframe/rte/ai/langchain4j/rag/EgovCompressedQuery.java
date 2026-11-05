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
package org.egovframe.rte.ai.langchain4j.rag;

import java.util.Objects;

import dev.langchain4j.rag.query.Metadata;
import dev.langchain4j.rag.query.Query;

/**
 * 압축 상태를 함께 노출하는 질의 — LangChain4j {@link Query} 에는 자유 컨텍스트가 없어 하위 타입으로 상태와 원 질의 텍스트를 싣는다.
 * LangChain4j 는 {@code text()}·{@code metadata()} 만 읽으므로 검색기·증강기에 그대로 넘길 수 있다.
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.03  실행환경팀     최초 생성 (과제 23 add-ai-langchain4j)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovCompressedQuery extends Query {

	private final String status;
	private final String originalText;

	private EgovCompressedQuery(String text, String status, String originalText) {
		super(text);
		this.status = Objects.requireNonNull(status, "status must not be null");
		this.originalText = originalText;
	}

	private EgovCompressedQuery(String text, Metadata metadata, String status, String originalText) {
		super(text, metadata);
		this.status = Objects.requireNonNull(status, "status must not be null");
		this.originalText = originalText;
	}

	/**
	 * 상태를 실은 질의를 만든다 — LangChain4j {@code Query(text, metadata)} 는 null 메타데이터를 거부하므로 없으면 {@code Query(text)} 경로를 쓴다.
	 *
	 * @param text 변환된 질의 텍스트
	 * @param metadata 원 질의의 메타데이터(null 가능)
	 * @param status 상태({@link EgovConversationQueryTransformer#STATUS_APPLIED} 등)
	 * @param originalText 원 질의 텍스트
	 * @return 질의
	 */
	public static EgovCompressedQuery of(String text, Metadata metadata, String status, String originalText) {
		return metadata == null ? new EgovCompressedQuery(text, status, originalText)
				: new EgovCompressedQuery(text, metadata, status, originalText);
	}

	/** 압축 상태 — {@code applied}·{@code skipped}·{@code fallback}. */
	public String status() {
		return status;
	}

	/** 원 질의 텍스트. */
	public String originalText() {
		return originalText;
	}

	/** 압축 결과가 채택되어 텍스트가 바뀌었는가. */
	public boolean changed() {
		return EgovConversationQueryTransformer.STATUS_APPLIED.equals(status) && !Objects.equals(originalText, text());
	}
}
