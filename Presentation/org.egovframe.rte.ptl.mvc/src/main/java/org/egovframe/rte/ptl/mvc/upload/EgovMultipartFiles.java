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
package org.egovframe.rte.ptl.mvc.upload;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.egovframe.rte.fdl.filehandling.EgovFiles;
import org.egovframe.rte.fdl.filehandling.upload.EgovUploadPolicy;
import org.springframework.web.multipart.MultipartFile;

/**
 * {@link MultipartFile} 을 {@link EgovUploadPolicy} 로 검증하는 얇은 어댑터.
 *
 * <p><b>NOTE:</b> 검증 규칙 자체는 {@code fdl.filehandling} 의 정책 객체에 있다. 이 클래스는
 * 서블릿 자료형에서 <b>파일명과 크기를 꺼내 정책에 넘기는 일</b>만 한다 — 규칙을 웹 계층에
 * 복제하지 않기 위해서다(공통컴포넌트 원본은 같은 화이트리스트 로직을 유틸과 리졸버에
 * 두 벌 두고 있었다).</p>
 *
 * <pre>
 * &#64;PostMapping("/upload")
 * public String upload(&#64;RequestParam("files") List&lt;MultipartFile&gt; files) {
 *     EgovMultipartFiles.validate(POLICY, files);      // 하나라도 위반하면 예외
 *     ...
 * }
 * </pre>
 *
 * <p>브라우저가 파일명에 경로를 붙여 보내는 경우가 있어(구형 클라이언트) <b>이름만 추출해서</b>
 * 정책에 넘긴다. 따라서 정상 업로드가 경로 구분자 때문에 거부되지 않는다.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.01  실행환경팀     최초 생성
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovMultipartFiles {

	private EgovMultipartFiles() {
	}

	/**
	 * 파일 하나를 검증하고 위반 사유를 반환한다. <b>예외를 던지지 않는다.</b>
	 *
	 * @param policy 적용할 정책
	 * @param file   검증할 파일({@code null} 이면 {@code NO_FILENAME})
	 * @return 위반 사유. 통과하면 {@link Optional#empty()}
	 */
	public static Optional<EgovUploadPolicy.Reason> check(EgovUploadPolicy policy, MultipartFile file) {
		if (file == null) {
			return Optional.of(EgovUploadPolicy.Reason.NO_FILENAME);
		}
		return policy.check(fileNameOf(file), file.getSize());
	}

	/**
	 * 파일 하나를 검증하고, 위반이면 예외를 던진다.
	 *
	 * @param policy 적용할 정책
	 * @param file   검증할 파일
	 * @throws org.egovframe.rte.fdl.filehandling.upload.EgovUploadRejectedException 정책 위반인 경우
	 */
	public static void validate(EgovUploadPolicy policy, MultipartFile file) {
		if (file == null) {
			policy.validate(null, 0L);
			return;
		}
		policy.validate(fileNameOf(file), file.getSize());
	}

	/**
	 * 여러 파일을 검증하고, 하나라도 위반이면 예외를 던진다(개수 제한 포함).
	 *
	 * <p><b>채우지 않은 입력칸(파일명 없음·0 바이트)은 개수에서 제외</b>한다 — 화면이 파일
	 * 입력칸을 여러 개 두고 일부만 채워 보내는 것이 보통이기 때문이다. 반면 <b>파일명이 있는
	 * 0 바이트 파일은 제출된 것</b>이라 정책이 판정한다(기본 거부, {@code allowEmptyFile} 로
	 * 허용하면 확장자 검사를 받는다). 크기가 0 이라는 이유로 정책을 건너뛰면 확장자
	 * 화이트리스트와 개수 상한이 빈 파일로 우회된다.</p>
	 *
	 * @param policy 적용할 정책
	 * @param files  검증할 파일 목록({@code null} 허용)
	 * @throws org.egovframe.rte.fdl.filehandling.upload.EgovUploadRejectedException 정책 위반인 경우
	 */
	public static void validateAll(EgovUploadPolicy policy, Collection<MultipartFile> files) {
		List<MultipartFile> submitted = submittedOnly(files);
		policy.validateCount(submitted.size());
		for (MultipartFile file : submitted) {
			validate(policy, file);
		}
	}

	/**
	 * 여러 파일을 검증하고 <b>위반한 것만</b> 골라 반환한다. 예외를 던지지 않는다.
	 *
	 * <p>화면에서 파일별로 오류를 표시해야 할 때 쓴다. 개수 초과는 키가 {@code null} 인
	 * 항목으로 담긴다.</p>
	 *
	 * @param policy 적용할 정책
	 * @param files  검증할 파일 목록({@code null} 허용)
	 * @return 위반한 파일과 사유(입력 순서 유지). 전부 통과하면 빈 맵
	 */
	public static Map<MultipartFile, EgovUploadPolicy.Reason> checkAll(
			EgovUploadPolicy policy, Collection<MultipartFile> files) {

		Map<MultipartFile, EgovUploadPolicy.Reason> violations = new LinkedHashMap<>();
		List<MultipartFile> submitted = submittedOnly(files);

		policy.checkCount(submitted.size()).ifPresent(reason -> violations.put(null, reason));

		for (MultipartFile file : submitted) {
			check(policy, file).ifPresent(reason -> violations.put(file, reason));
		}
		return violations;
	}

	/**
	 * 실제로 제출된 파일만 골라낸다 — 내용이 있거나 <b>파일명이 있는</b> 항목. 파일명도 내용도
	 * 없는 항목은 채우지 않은 입력칸이므로 제외한다.
	 *
	 * @param files 원본 목록({@code null} 허용)
	 * @return 제출된 파일 목록
	 */
	public static List<MultipartFile> submittedOnly(Collection<MultipartFile> files) {
		List<MultipartFile> submitted = new ArrayList<>();
		if (files == null) {
			return submitted;
		}
		for (MultipartFile file : files) {
			if (file != null && (!file.isEmpty() || hasFileName(file))) {
				submitted.add(file);
			}
		}
		return submitted;
	}

	/** 파일명이 있는 항목인가 — 크기가 0 이어도 사용자가 파일을 골라 보낸 것이다. */
	private static boolean hasFileName(MultipartFile file) {
		String name = file.getOriginalFilename();
		return name != null && !name.trim().isEmpty();
	}

	/**
	 * 업로드 파일에서 <b>경로를 제외한 이름</b>을 얻는다 — 구분자({@code /}·{@code \}) 앞부분과
	 * Windows 드라이브 접두({@code C:})를 뗀다.
	 *
	 * @param file 업로드 파일
	 * @return 파일명(없으면 {@code null})
	 */
	public static String fileNameOf(MultipartFile file) {
		String original = file.getOriginalFilename();
		if (original == null) {
			return null;
		}
		String trimmed = original.trim();
		if (trimmed.isEmpty()) {
			return trimmed;
		}
		// 드라이브 상대 표기("C:name")의 드라이브 접두는 구분자 없이도 경로다 — 함께 뗀다
		if (trimmed.length() >= 2 && trimmed.charAt(1) == ':' && Character.isLetter(trimmed.charAt(0))) {
			trimmed = trimmed.substring(2).trim();
			if (trimmed.isEmpty()) {
				return trimmed;
			}
		}
		String baseName = EgovFiles.getBaseName(trimmed);
		String extension = EgovFiles.getExtension(trimmed);
		return extension.isEmpty() ? baseName : baseName + "." + extension;
	}
}
