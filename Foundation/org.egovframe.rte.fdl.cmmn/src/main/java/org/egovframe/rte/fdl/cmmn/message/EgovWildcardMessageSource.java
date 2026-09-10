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
package org.egovframe.rte.fdl.cmmn.message;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * basename 에 <b>와일드카드 패턴</b>을 쓸 수 있는 {@code MessageSource}.
 *
 * <p><b>NOTE:</b> Spring 의 {@link ReloadableResourceBundleMessageSource} 는 basename 을
 * 하나씩 나열해야 한다. 모듈이 늘 때마다 설정에 줄이 늘고, 빠뜨리면 그 모듈의 메시지만
 * 조용히 비어 버린다. 프로퍼티 로딩은 이미 {@code classpath*:} 패턴을 지원하는데
 * 메시지만 지원하지 않는 <b>비대칭</b>이 있었다.</p>
 *
 * <pre>
 * &lt;bean id="messageSource"
 *       class="org.egovframe.rte.fdl.cmmn.message.EgovWildcardMessageSource"&gt;
 *     &lt;property name="basenames"&gt;
 *         &lt;list&gt;
 *             &lt;value&gt;classpath*:/messages/**&#47;*.properties&lt;/value&gt;
 *         &lt;/list&gt;
 *     &lt;/property&gt;
 *     &lt;property name="defaultEncoding" value="UTF-8" /&gt;
 * &lt;/bean&gt;
 * </pre>
 *
 * <p>패턴을 만나면 실제 리소스를 찾아 <b>로케일 접미와 확장자를 떼어낸</b> basename 목록으로
 * 펼친 뒤 상위 클래스에 넘긴다. 위 설정 하나로 {@code messages/common_ko.properties} ·
 * {@code messages/board/board_en.properties} 가 모두 잡힌다.</p>
 *
 * <p><b>패턴이 아닌 basename 은 그대로 전달</b>하므로 기존 설정을 섞어 쓸 수 있다.
 * 상위 클래스의 {@code setBasenames} 를 그대로 오버라이드하므로 <b>클래스명만 바꾸면
 * 드롭인 교체</b>가 된다.</p>
 *
 * <p><b>로케일 접미는 형태로 판별한다.</b> {@code _ko}·{@code _ko_KR} 처럼 언어 코드
 * 형태일 때만 떼어내므로 {@code user_info.properties} 의 {@code _info} 는 보존된다
 * (파일명의 밑줄을 모두 로케일 구분자로 보면 basename 이 잘려 메시지를 못 찾는다).</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.01  실행환경팀     최초 생성 (A-23: 공통컴포넌트
 *                            EgovWildcardReloadableResourceBundleMessageSource 의 착상만
 *                            차용하고 구현은 재작성 — 원본은 로케일 접미가 없는 파일에서
 *                            substring(0, -1) 로 예외가 나고, 파일명의 모든 밑줄을 로케일
 *                            구분자로 보며, IOException 을 debug 로그로 삼키고, 리소스 URI 를
 *                            "/classes/"·".jar!/" 문자열 가공으로 역산해 취약했다. 코드 이식 아님)
 *  2026.09.07  실행환경팀     접두 디렉터리 이름이 하위 경로에 반복될 때 상대 경로가 안쪽 일치를
 *                            잡아 메시지가 유실되던 것을 루트 URL 접두 비교로 정정
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public class EgovWildcardMessageSource extends ReloadableResourceBundleMessageSource {

	/** 프로퍼티 확장자. */
	private static final String PROPERTIES_SUFFIX = ".properties";

	/**
	 * 로케일 접미 패턴 — {@code _ko} · {@code _ko_KR} · {@code _zh_Hans_CN} · {@code _en_US_POSIX}.
	 *
	 * <p>언어는 소문자 2~3자, 스크립트는 {@code Hans} 형태(대문자 1 + 소문자 3),
	 * 국가는 대문자 2자 또는 숫자 3자(UN M.49), 마지막은 variant 다. <b>형태가 정확히
	 * 맞을 때만</b> 로케일로 본다 — {@code user_info} 의 {@code _info}(소문자 4자)는
	 * 언어 코드가 아니므로 보존된다.</p>
	 */
	private static final Pattern LOCALE_SUFFIX = Pattern.compile(
			"_[a-z]{2,3}(_[A-Z][a-z]{3})?(_([A-Z]{2}|[0-9]{3}))?(_[A-Za-z0-9]+)?$");

	private final ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

	/**
	 * basename 을 지정한다. 와일드카드가 포함된 항목은 실제 리소스로 펼쳐진다.
	 *
	 * @param basenames basename 또는 리소스 패턴
	 * @throws UncheckedIOException 패턴 해석 중 입출력 오류가 난 경우
	 */
	@Override
	public void setBasenames(String... basenames) {
		if (basenames == null) {
			super.setBasenames();
			return;
		}
		Set<String> expanded = new LinkedHashSet<>();
		for (String basename : basenames) {
			if (basename == null || basename.trim().isEmpty()) {
				continue;
			}
			String trimmed = basename.trim();
			if (isPattern(trimmed)) {
				expanded.addAll(expand(trimmed));
			} else {
				expanded.add(trimmed);
			}
		}
		super.setBasenames(expanded.toArray(new String[0]));
	}

	/**
	 * 패턴 하나를 basename 목록으로 펼친다.
	 *
	 * @param pattern 리소스 패턴
	 * @return basename 목록(순서 유지)
	 */
	protected List<String> expand(String pattern) {
		String prefixDir = fixedDirectoryOf(pattern);
		List<String> basenames = new ArrayList<>();
		Set<String> seen = new LinkedHashSet<>();

		Resource[] resources;
		try {
			resources = resourcePatternResolver.getResources(pattern);
		} catch (IOException e) {
			// 원본은 이 지점을 debug 로그로 삼켰다 — 메시지가 비어도 원인을 찾을 수 없었다.
			throw new UncheckedIOException("메시지 리소스 패턴을 해석하지 못했습니다: " + pattern, e);
		}

		List<String> rootUrls = rootUrlsOf(prefixDir);
		for (Resource resource : resources) {
			String relative = relativeNameOf(resource, prefixDir, rootUrls);
			if (relative == null || !relative.endsWith(PROPERTIES_SUFFIX)) {
				continue;
			}
			String withoutSuffix = relative.substring(0, relative.length() - PROPERTIES_SUFFIX.length());
			String basename = "classpath:" + prefixDir + stripLocaleSuffix(withoutSuffix);
			if (seen.add(basename)) {
				basenames.add(basename);
			}
		}
		return basenames;
	}

	/**
	 * 파일명에서 로케일 접미를 떼어낸다({@code common_ko_KR} → {@code common}).
	 *
	 * <p>언어 코드 형태일 때만 떼어내므로 {@code user_info} 는 그대로 남는다.</p>
	 *
	 * @param name 확장자를 제거한 이름
	 * @return 로케일 접미를 제거한 이름
	 */
	protected String stripLocaleSuffix(String name) {
		// 패턴이 접미 전체(언어+스크립트+국가+variant)를 한 번에 잡으므로 반복하지 않는다 —
		// 반복하면 basename 자체가 로케일 형태일 때(예: "ko_KR") 과도하게 잘린다.
		String stripped = LOCALE_SUFFIX.matcher(name).replaceFirst("");
		return stripped.isEmpty() ? name : stripped;
	}

	/**
	 * 패턴에서 <b>와일드카드가 나오기 전까지의 디렉터리</b>를 잘라낸다.
	 *
	 * <p>리소스 URL 을 통째로 문자열 가공하지 않고 이 값을 기준으로 상대 경로만 취한다 —
	 * 파일시스템·jar 어느 쪽이든 같은 방식으로 동작한다.</p>
	 *
	 * @param pattern 리소스 패턴
	 * @return {@code /messages/} 같은 접두 디렉터리(항상 {@code /} 로 끝난다)
	 */
	protected String fixedDirectoryOf(String pattern) {
		String path = pattern;
		int schemeEnd = path.indexOf(':');
		if (schemeEnd >= 0) {
			path = path.substring(schemeEnd + 1);
		}
		int wildcard = firstWildcardIndex(path);
		String head = (wildcard < 0) ? path : path.substring(0, wildcard);
		int lastSlash = head.lastIndexOf('/');
		if (lastSlash < 0) {
			return "/";
		}
		String dir = head.substring(0, lastSlash + 1);
		return dir.startsWith("/") ? dir : "/" + dir;
	}

	private static int firstWildcardIndex(String path) {
		int star = path.indexOf('*');
		int question = path.indexOf('?');
		if (star < 0) {
			return question;
		}
		if (question < 0) {
			return star;
		}
		return Math.min(star, question);
	}

	/**
	 * 접두 디렉터리 자체를 클래스패스 전체에서 찾은 루트 URL 목록({@code classpath*:} 해석 결과, 항상
	 * {@code /} 로 끝난다). 상대 경로를 이 루트와의 접두 비교로 구해야, 접두 디렉터리 이름이 하위 경로에
	 * 반복돼도({@code mm/sub/mm/x}) 안쪽 일치를 잡아 basename 이 틀어지지 않는다.
	 */
	private List<String> rootUrlsOf(String prefixDir) {
		List<String> urls = new ArrayList<>();
		try {
			for (Resource root : resourcePatternResolver.getResources("classpath*:" + prefixDir)) {
				String url = root.getURL().toString();
				urls.add(url.endsWith("/") ? url : url + "/");
			}
		} catch (IOException e) {
			// 루트를 얻지 못하면 relativeNameOf 가 종전 방식(마지막 일치)으로 폴백한다
		}
		return urls;
	}

	/**
	 * 리소스 URL 에서 접두 디렉터리 이후의 상대 경로를 얻는다 — 접두 디렉터리 루트 URL 과의 접두 비교가
	 * 우선이고, 루트에 속하지 않는 리소스(URL 표기 차이 등)만 마지막 일치로 폴백한다.
	 *
	 * @param resource  찾은 리소스
	 * @param prefixDir 접두 디렉터리
	 * @param rootUrls  {@link #rootUrlsOf(String)} 결과
	 * @return 상대 경로. 접두를 찾지 못하면 {@code null}
	 */
	private String relativeNameOf(Resource resource, String prefixDir, List<String> rootUrls) {
		String url;
		try {
			url = resource.getURL().toString();
		} catch (IOException e) {
			throw new UncheckedIOException("메시지 리소스 위치를 확인하지 못했습니다: " + resource, e);
		}
		for (String root : rootUrls) {
			if (url.startsWith(root)) {
				return url.substring(root.length());
			}
		}
		int index = url.lastIndexOf(prefixDir);
		return (index < 0) ? null : url.substring(index + prefixDir.length());
	}

	private static boolean isPattern(String basename) {
		return basename.indexOf('*') >= 0 || basename.indexOf('?') >= 0;
	}
}
