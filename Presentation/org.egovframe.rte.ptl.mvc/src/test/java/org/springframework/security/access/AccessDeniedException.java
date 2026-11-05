package org.springframework.security.access;

/**
 * 테스트 전용 대역 — ptl.mvc 는 Spring Security 에 의존하지 않으므로, EgovRestExceptionHandler 가
 * 클래스 이름으로 인가 예외를 판별하는지 검증하기 위해 같은 이름의 클래스를 테스트 범위에만 둔다.
 */
public class AccessDeniedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AccessDeniedException(String message) {
        super(message);
    }

}
