package org.egovframe.boot.security;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.List;
import java.util.Map;

/**
 * 기동 시 적재한 URL 보호자원-권한 매핑을 보관하는 캐시.
 *
 * @author 유지보수
 * @version 5.1
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일       수정자    수정내용
 * ------------------------------------------------------------
 * 2025.06.01   유지보수  최초 생성
 * </pre>
 * @since 2025.06.01
 */
@Slf4j
public class EgovSecurityMetadataCache {

    @Setter
    @Getter
    private static Map<RequestMatcher, List<ConfigAttribute>> requestMap;

}
