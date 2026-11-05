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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Markdown 텍스트 추출기 — 의존 0(UTF-8 읽기) + 구조 메타데이터(헤더·코드 블록·링크·이미지 유무, 줄 수).
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.03  실행환경팀     최초 생성 (과제 23 add-ai-core)
 *  2026.09.07  실행환경팀     보안 재점검(4차): 링크·이미지 정규식 길이 제한(적대 문서 2차 시간 폭발 차단)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovMarkdownTextExtractor implements EgovTextExtractor {

	/** 형식 이름. */
	public static final String TYPE = "markdown";

	/** 식별자 접두(md-…). */
	public static final String ID_PREFIX = "md";

	private static final Pattern HEADER = Pattern.compile("(?m)^#{1,6}\\s");
	// 링크·이미지 판정은 메타데이터용이라 길이를 묶는다 — 무한 [^\]]* 는 '[' 10만 개 문서에서 2차 시간 폭발
	// (Find Security Bugs REDOS 지적 → 실측 3초 초과, 2026-09-07 보안 재점검). 개행을 넘지 않고 텍스트 512자·URL 2,048자 이내.
	private static final Pattern LINK = Pattern.compile("\\[[^\\]\\n]{0,512}+\\]\\([^)\\n]{0,2048}+\\)");
	private static final Pattern IMAGE = Pattern.compile("!\\[[^\\]\\n]{0,512}+\\]\\([^)\\n]{0,2048}+\\)");

	@Override
	public String type() {
		return TYPE;
	}

	@Override
	public List<String> extensions() {
		return List.of("md", "markdown");
	}

	@Override
	public List<EgovExtractedDocument> extract(Path file) {
		Objects.requireNonNull(file, "file must not be null");
		String fileName = String.valueOf(file.getFileName());
		String content;
		try {
			content = Files.readString(file, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new EgovExtractionException("Markdown 파일을 읽지 못했습니다: " + fileName, e);
		}
		if (content.isBlank()) {
			return List.of();
		}
		String id = EgovDocumentIds.of(ID_PREFIX, fileName);
		Map<String, Object> metadata = EgovDocumentMetadata.base(TYPE, fileName, id, content.length());
		metadata.put(EgovDocumentMetadata.HAS_HEADERS, HEADER.matcher(content).find());
		metadata.put(EgovDocumentMetadata.HAS_CODE_BLOCKS, content.contains("```"));
		metadata.put(EgovDocumentMetadata.HAS_LINKS, LINK.matcher(content).find());
		metadata.put(EgovDocumentMetadata.HAS_IMAGES, IMAGE.matcher(content).find());
		metadata.put(EgovDocumentMetadata.LINE_COUNT, content.split("\n", -1).length);
		return List.of(new EgovExtractedDocument(id, content, metadata));
	}
}
