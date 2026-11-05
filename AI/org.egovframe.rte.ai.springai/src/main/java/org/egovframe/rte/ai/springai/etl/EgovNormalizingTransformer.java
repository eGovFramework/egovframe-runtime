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
package org.egovframe.rte.ai.springai.etl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.egovframe.rte.ai.core.text.EgovNormalized;
import org.egovframe.rte.ai.core.text.EgovTextNormalizer;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentTransformer;

/**
 * 텍스트 정규화 변환기 — 기반 모듈 {@link EgovTextNormalizer} 위임.
 *
 * <p>본문이 바뀐 문서만 같은 id·메타데이터({@code content_length} 는 새 길이)로 다시 만들고, 바뀌지 않은 문서는 같은 인스턴스를
 * 통과시킨다. 기본 정규화기는 글자를 지우지 않으므로 공문서 표기(한자·가운뎃점·낫표)가 보존된다 — 샘플의 특수문자 삭제
 * (검토 4장 #1)는 옵션으로도 제공하지 않는다.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.03  실행환경팀     최초 생성 (과제 23 add-ai-springai)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovNormalizingTransformer implements DocumentTransformer {

	private final EgovTextNormalizer normalizer;

	private EgovNormalizingTransformer(EgovTextNormalizer normalizer) {
		this.normalizer = Objects.requireNonNull(normalizer, "normalizer must not be null");
	}

	/**
	 * 정규화기를 지정한 변환기.
	 *
	 * @param normalizer 정규화기
	 * @return 변환기
	 */
	public static EgovNormalizingTransformer of(EgovTextNormalizer normalizer) {
		return new EgovNormalizingTransformer(normalizer);
	}

	/** 기본 정규화기({@link EgovTextNormalizer#DEFAULT} — 제어·서식 문자 제거와 빈 줄 정리만). */
	public static EgovNormalizingTransformer defaults() {
		return of(EgovTextNormalizer.DEFAULT);
	}

	/** 위임 정규화기. */
	public EgovTextNormalizer normalizer() {
		return normalizer;
	}

	@Override
	public List<Document> apply(List<Document> documents) {
		Objects.requireNonNull(documents, "documents must not be null");
		List<Document> out = new ArrayList<>(documents.size());
		for (Document document : documents) {
			String text = document.getText();
			if (text == null) {
				out.add(document);
				continue;
			}
			EgovNormalized normalized = normalizer.normalize(text);
			out.add(normalized.changed() ? EgovDocuments.withText(document, normalized.text(), Map.of()) : document);
		}
		return out;
	}
}
