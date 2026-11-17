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
package org.egovframe.rte.ai.langchain4j.etl;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.egovframe.rte.ai.core.document.EgovExtractedDocument;
import org.egovframe.rte.ai.core.document.EgovExtractionResult;
import org.egovframe.rte.ai.core.document.EgovTextExtractors;

import dev.langchain4j.data.document.Document;

/**
 * 기반 모듈의 추출기 모음(HWP·HWPX·DOCX·PDF·Markdown)으로 파일을 읽어 LangChain4j {@link Document} 로 바꾸는 로더.
 *
 * <p>LangChain4j {@code DocumentParser} 는 {@code InputStream} 을 받아 파일 유형·경로를 모르고, 기반 추출기(HWP 파서 등)는 파일 경로가
 * 필요하다. 그래서 파서가 아니라 {@link Path} 목록 로더로 제공한다. 두 진입점 — {@link #load()} 는 <b>엄격</b>(한 파일이라도 실패하면
 * {@link EgovDocumentLoadException}), {@link #loadAll()} 은 성공 문서·실패 목록·빈 파일 목록을 함께 돌려준다. 실패를 로그만 남기고
 * 성공분만 돌려주는 동작은 없다. 메타데이터는 기반 키를 {@link EgovLangChain4jMetadata#toMetadata(java.util.Map)} 규칙으로 옮긴다.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.03  실행환경팀     최초 생성 (과제 23 add-ai-langchain4j — 샘플 리더 5종의 파일별 침묵 실패 교정)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovDocumentLoader {

	private final EgovTextExtractors extractors;
	private final List<Path> files;

	private EgovDocumentLoader(EgovTextExtractors extractors, Collection<Path> files) {
		this.extractors = Objects.requireNonNull(extractors, "extractors must not be null");
		this.files = List.copyOf(Objects.requireNonNull(files, "files must not be null"));
	}

	/**
	 * 기본 추출기 5종으로 읽는 로더.
	 *
	 * @param files 읽을 파일 목록
	 * @return 로더
	 */
	public static EgovDocumentLoader of(Collection<Path> files) {
		return new EgovDocumentLoader(EgovTextExtractors.defaults(), files);
	}

	/**
	 * 추출기 모음을 지정해 읽는 로더.
	 *
	 * @param extractors 추출기 모음
	 * @param files 읽을 파일 목록
	 * @return 로더
	 */
	public static EgovDocumentLoader of(EgovTextExtractors extractors, Collection<Path> files) {
		return new EgovDocumentLoader(extractors, files);
	}

	/** 읽을 파일 목록(불변). */
	public List<Path> files() {
		return files;
	}

	/**
	 * 엄격 읽기 — 한 파일이라도 실패하면 예외.
	 *
	 * @return 성공 문서 전부
	 * @throws EgovDocumentLoadException 실패한 파일이 있을 때(실패 전부를 담는다)
	 */
	public List<Document> load() {
		Result result = loadAll();
		if (result.hasFailures()) {
			throw new EgovDocumentLoadException(result.failures());
		}
		return result.documents();
	}

	/**
	 * 부분 읽기 — 성공 문서·실패 목록·빈 파일 목록을 함께 돌려준다(예외 없음).
	 *
	 * @return 결과
	 */
	public Result loadAll() {
		EgovExtractionResult extracted = extractors.extractAll(files);
		List<Document> documents = new ArrayList<>(extracted.succeeded().size());
		for (EgovExtractedDocument document : extracted.succeeded()) {
			documents.add(toDocument(document));
		}
		return new Result(documents, extracted.failed(), extracted.empty());
	}

	/**
	 * 기반 모듈 문서 → LangChain4j 문서(텍스트·메타데이터 그대로, 문서 id 는 메타데이터 {@code original_id} 로 이미 들어 있다).
	 *
	 * @param document 추출 문서
	 * @return LangChain4j 문서
	 */
	public static Document toDocument(EgovExtractedDocument document) {
		Objects.requireNonNull(document, "document must not be null");
		return Document.from(document.text(), EgovLangChain4jMetadata.toMetadata(document.metadata()));
	}

	/**
	 * 부분 읽기 결과.
	 *
	 * @param documents 성공 문서
	 * @param failures 실패(파일·사유·원인)
	 * @param empty 텍스트가 없던 파일
	 */
	public record Result(List<Document> documents, List<EgovExtractionResult.Failure> failures, List<Path> empty) {

		public Result {
			documents = List.copyOf(documents);
			failures = List.copyOf(failures);
			empty = List.copyOf(empty);
		}

		/** 실패가 있는가. */
		public boolean hasFailures() {
			return !failures.isEmpty();
		}
	}
}
