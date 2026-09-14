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
import java.util.List;
import java.util.Map;

import org.egovframe.rte.fdl.filehandling.upload.EgovUploadPolicy;
import org.egovframe.rte.fdl.filehandling.upload.EgovUploadRejectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 업로드 정책을 컨트롤러 진입 전에 일괄 적용하는 {@code MultipartResolver}.
 *
 * <p><b>NOTE:</b> 컨트롤러마다 검증을 반복해 넣다 보면 한 곳이 빠지고, 그 한 곳이 통로가 된다.
 * 리졸버 단계에 두면 <b>모든 multipart 요청이 같은 정책을 거친다.</b></p>
 *
 * <pre>
 * &#64;Bean
 * public EgovUploadPolicy uploadPolicy() {
 *     return EgovUploadPolicy.builder().allowExtensions("png", "pdf").maxFileCount(5).build();
 * }
 *
 * // 메서드 이름이 곧 빈 이름이다 — DispatcherServlet 이 "multipartResolver" 로 이름 조회를 한다
 * &#64;Bean
 * public MultipartResolver multipartResolver(EgovUploadPolicy uploadPolicy) {
 *     return new EgovValidatingMultipartResolver(uploadPolicy);
 * }
 * </pre>
 *
 * <p><b>빈 이름을 바꾸면 동작하지 않는다.</b> {@code DispatcherServlet} 은
 * {@code MULTIPART_RESOLVER_BEAN_NAME}(= {@code "multipartResolver"})으로 조회하므로,
 * 이름이 다르면 빈은 만들어지지만 요청 경로에 끼지 않는다.</p>
 *
 * <p><b>정책은 생성자로 받으며 필수다.</b> 기본 생성자를 두지 않은 이유는, 정책이 없는 상태를
 * 허용하면 설정 누락이 곧 무검증으로 이어지기 때문이다(공통컴포넌트 원본은 화이트리스트
 * 프로퍼티가 비어 있으면 <b>모든 확장자를 통과</b>시켰다).</p>
 *
 * <h3>이 단계에서 막지 <b>못하는</b> 것</h3>
 *
 * <p>서블릿 컨테이너가 요청 본문을 이미 파싱한 뒤에 검증이 이뤄지므로, <b>임시 파일은 한 번
 * 생성된다.</b> 검증에 걸리면 즉시 정리하지만 디스크 쓰기 자체를 없애지는 못한다.
 * 과도한 크기의 요청을 <b>받기 전에</b> 끊으려면 컨테이너 설정을 함께 건다.</p>
 *
 * <pre>
 * &lt;multipart-config&gt;
 *     &lt;max-file-size&gt;10485760&lt;/max-file-size&gt;
 *     &lt;max-request-size&gt;52428800&lt;/max-request-size&gt;
 * &lt;/multipart-config&gt;
 * </pre>
 *
 * <p>정책 위반은 {@link MultipartException} 으로 감싸 던진다. 사유는 cause 로 실린
 * {@link EgovUploadRejectedException#getReason()} 에서 꺼낸다 — 크기 초과는 413,
 * 확장자 불허는 400 처럼 구분해 응답하려면 예외 핸들러에서 사유로 분기한다.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.01  실행환경팀     최초 생성 (공통컴포넌트 EgovMultipartResolver 의 배치와
 *                            검증 항목을 차용하되, 화이트리스트 미설정 시 전부 허용·
 *                            SecurityException 이 MultipartException catch 를 빠져나가
 *                            500 이 되는 문제·검증 실패 시 임시 파일 미정리·요청마다
 *                            프로퍼티 재조회를 재구성 — 코드 이식 아님)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public class EgovValidatingMultipartResolver extends StandardServletMultipartResolver {

	private static final Logger LOGGER = LoggerFactory.getLogger(EgovValidatingMultipartResolver.class);

	private final EgovUploadPolicy policy;

	/**
	 * @param policy 적용할 업로드 정책(필수)
	 */
	public EgovValidatingMultipartResolver(EgovUploadPolicy policy) {
		if (policy == null) {
			throw new IllegalArgumentException("upload policy must not be null");
		}
		this.policy = policy;
	}

	/**
	 * 적용 중인 정책을 반환한다.
	 *
	 * @return 정책
	 */
	public EgovUploadPolicy getPolicy() {
		return policy;
	}

	/**
	 * multipart 요청을 파싱한 뒤 정책을 적용한다.
	 *
	 * <p>위반이면 <b>임시 파일을 정리한 뒤</b> {@link MultipartException} 을 던진다.</p>
	 *
	 * @param request 요청
	 * @return 파싱된 multipart 요청
	 * @throws MultipartException 파싱 실패 또는 정책 위반
	 */
	@Override
	public MultipartHttpServletRequest resolveMultipart(HttpServletRequest request) throws MultipartException {
		MultipartHttpServletRequest multipartRequest = super.resolveMultipart(request);
		try {
			applyPolicy(multipartRequest);
		} catch (EgovUploadRejectedException ex) {
			cleanupMultipart(multipartRequest);
			LOGGER.warn("업로드 거부 [{}] uri={}", ex.getReason(), request.getRequestURI());
			throw new MultipartException("Upload rejected by policy: " + ex.getReason(), ex);
		}
		return multipartRequest;
	}

	/**
	 * 요청에 담긴 모든 파일에 정책을 적용한다.
	 *
	 * @param multipartRequest 파싱된 multipart 요청
	 * @throws EgovUploadRejectedException 정책 위반인 경우
	 */
	protected void applyPolicy(MultipartHttpServletRequest multipartRequest) {
		Map<String, List<MultipartFile>> fileMap = multipartRequest.getMultiFileMap();
		List<MultipartFile> all = new ArrayList<>();
		for (List<MultipartFile> files : fileMap.values()) {
			all.addAll(files);
		}
		EgovMultipartFiles.validateAll(policy, all);
	}
}
