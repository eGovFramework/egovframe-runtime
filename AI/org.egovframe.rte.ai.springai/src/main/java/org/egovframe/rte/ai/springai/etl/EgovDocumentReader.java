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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

import org.egovframe.rte.ai.core.document.EgovExtractedDocument;
import org.egovframe.rte.ai.core.document.EgovExtractionResult;
import org.egovframe.rte.ai.core.document.EgovTextExtractors;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;

/**
 * 기반 모듈의 추출기 모음(HWP·HWPX·DOCX·PDF·Markdown)으로 파일을 읽어 Spring AI {@link Document} 로 바꾸는 리더.
 *
 * <p>두 진입점을 둔다. {@link #get()} 은 Spring AI 파이프라인 관용구({@code writer.accept(splitter.apply(reader.get()))})용으로
 * <b>엄격</b>하다 — 한 파일이라도 실패하면 {@link EgovDocumentReadException}. 부분 결과가 필요한 배치 색인기는
 * {@link #readAll()} 로 성공 문서·실패 목록·빈 파일 목록을 함께 받아 실패를 자기 손으로 처리한다. 실패를 로그만 남기고
 * 성공분만 돌려주는 동작은 없다.</p>
 *
 * <p>문서 id·텍스트·메타데이터({@code original_id}·{@code file_name}·{@code source}·{@code type}·{@code content_length}·
 * {@code page_number} 등)는 기반 모듈이 만든 값을 그대로 옮긴다.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.03  실행환경팀     최초 생성 (과제 23 add-ai-springai — 샘플 리더 5종을 추출기 위임 1종으로)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovDocumentReader implements DocumentReader {

	private final EgovTextExtractors extractors;
	private final List<Path> files;

	private EgovDocumentReader(EgovTextExtractors extractors, Collection<Path> files) {
		this.extractors = Objects.requireNonNull(extractors, "extractors must not be null");
		this.files = List.copyOf(Objects.requireNonNull(files, "files must not be null"));
	}

	/**
	 * 기본 추출기 5종으로 읽는 리더.
	 *
	 * @param files 읽을 파일 목록
	 * @return 리더
	 */
	public static EgovDocumentReader of(Collection<Path> files) {
		return new EgovDocumentReader(EgovTextExtractors.defaults(), files);
	}

	/**
	 * 추출기 모음을 지정해 읽는 리더.
	 *
	 * @param extractors 추출기 모음
	 * @param files 읽을 파일 목록
	 * @return 리더
	 */
	public static EgovDocumentReader of(EgovTextExtractors extractors, Collection<Path> files) {
		return new EgovDocumentReader(extractors, files);
	}

	/** 읽을 파일 목록(불변). */
	public List<Path> files() {
		return files;
	}

	/**
	 * 엄격 읽기 — 한 파일이라도 실패하면 예외.
	 *
	 * @return 성공 문서 전부
	 * @throws EgovDocumentReadException 실패한 파일이 있을 때(실패 전부를 담는다)
	 */
	@Override
	public List<Document> get() {
		Result result = readAll();
		if (!result.failures().isEmpty()) {
			throw new EgovDocumentReadException(result.failures());
		}
		return result.documents();
	}

	/**
	 * 부분 읽기 — 성공 문서·실패 목록·빈 파일 목록을 함께 돌려준다(예외 없음).
	 *
	 * @return 결과
	 */
	public Result readAll() {
		EgovExtractionResult extracted = extractors.extractAll(files);
		List<Document> documents = new ArrayList<>(extracted.succeeded().size());
		for (EgovExtractedDocument document : extracted.succeeded()) {
			documents.add(toDocument(document));
		}
		return new Result(documents, extracted.failed(), extracted.empty());
	}

	/**
	 * 기반 모듈 문서 → Spring AI 문서(id·텍스트·메타데이터 그대로).
	 *
	 * @param document 추출 문서
	 * @return Spring AI 문서
	 */
	public static Document toDocument(EgovExtractedDocument document) {
		Objects.requireNonNull(document, "document must not be null");
		return Document.builder()
				.id(document.id())
				.text(document.text())
				.metadata(new LinkedHashMap<>(document.metadata()))
				.build();
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
