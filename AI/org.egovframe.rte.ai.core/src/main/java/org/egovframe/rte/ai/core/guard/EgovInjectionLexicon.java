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
package org.egovframe.rte.ai.core.guard;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

/**
 * 프롬프트 인젝션 렉시콘 — 시드(동봉) + 운영자 확장(파일)의 불변 병합.
 *
 * <p><b>로드 실패는 예외</b>다: 시드 리소스가 없거나 지정한 확장 파일이 없으면 렉시콘(따라서 가드)이 만들어지지 않는다.
 * 참고 샘플은 경고만 남기고 빈 렉시콘으로 기동해 가드가 조용히 무력화됐다.</p>
 *
 * <p>파일 형식: 한 줄에 패턴 하나, <code>#</code> 주석·빈 줄 무시, UTF-8. 패턴은 판정 시 질의와 같은 정규화를 거치므로
 * 표기(전각·공백)에 민감하지 않다.</p>
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
public final class EgovInjectionLexicon {

	/** 동봉 시드 렉시콘의 클래스패스 위치. */
	public static final String SEED_RESOURCE = "egov/ai/guard/injection-lexicon.txt";

	private final List<String> patterns;

	private EgovInjectionLexicon(Collection<String> patterns) {
		this.patterns = List.copyOf(new LinkedHashSet<>(patterns));
	}

	/**
	 * 동봉 시드 렉시콘.
	 *
	 * @throws IllegalStateException 시드 리소스가 없거나 읽을 수 없음
	 */
	public static EgovInjectionLexicon seed() {
		ClassLoader loader = EgovInjectionLexicon.class.getClassLoader();
		try (InputStream in = loader.getResourceAsStream(SEED_RESOURCE)) {
			if (in == null) {
				throw new IllegalStateException("시드 렉시콘 리소스가 없습니다: " + SEED_RESOURCE);
			}
			List<String> lines = read(new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)));
			if (lines.isEmpty()) {
				throw new IllegalStateException("시드 렉시콘이 비어 있습니다: " + SEED_RESOURCE);
			}
			return new EgovInjectionLexicon(lines);
		} catch (IOException e) {
			throw new IllegalStateException("시드 렉시콘을 읽지 못했습니다: " + SEED_RESOURCE, e);
		}
	}

	/**
	 * 패턴 목록으로 만든다(테스트·프로그램 구성용).
	 *
	 * @throws IllegalArgumentException 비어 있음
	 */
	public static EgovInjectionLexicon of(Collection<String> patterns) {
		Objects.requireNonNull(patterns, "patterns must not be null");
		List<String> cleaned = new ArrayList<>();
		for (String pattern : patterns) {
			if (pattern != null && !pattern.isBlank()) {
				cleaned.add(pattern.trim());
			}
		}
		if (cleaned.isEmpty()) {
			throw new IllegalArgumentException("렉시콘 패턴이 비어 있습니다");
		}
		return new EgovInjectionLexicon(cleaned);
	}

	/**
	 * 운영자 확장 파일을 병합한 새 렉시콘.
	 *
	 * @param extension UTF-8 텍스트 파일
	 * @throws IllegalArgumentException 파일이 없음
	 * @throws UncheckedIOException 읽기 실패
	 */
	public EgovInjectionLexicon withExtension(Path extension) {
		Objects.requireNonNull(extension, "extension must not be null");
		if (!Files.isRegularFile(extension)) {
			throw new IllegalArgumentException("확장 렉시콘 파일이 없습니다: " + extension);
		}
		try (BufferedReader reader = Files.newBufferedReader(extension, StandardCharsets.UTF_8)) {
			List<String> merged = new ArrayList<>(patterns);
			merged.addAll(read(reader));
			return new EgovInjectionLexicon(merged);
		} catch (IOException e) {
			throw new UncheckedIOException("확장 렉시콘을 읽지 못했습니다: " + extension, e);
		}
	}

	/** 패턴 목록(불변, 등록 순서·중복 제거). */
	public List<String> patterns() {
		return patterns;
	}

	/** 패턴 수. */
	public int size() {
		return patterns.size();
	}

	private static List<String> read(BufferedReader reader) throws IOException {
		List<String> lines = new ArrayList<>();
		String line;
		while ((line = reader.readLine()) != null) {
			String trimmed = line.strip();
			if (trimmed.startsWith("﻿")) {
				trimmed = trimmed.substring(1).strip();
			}
			if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
				lines.add(trimmed);
			}
		}
		return lines;
	}
}
