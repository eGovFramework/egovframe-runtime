package org.springframework.security.access;

/**
 * 테스트 전용 대역 클래스. EgovRestExceptionHandler의 클래스 이름 기반 인가 예외 판별을 검증한다.
 */
public class AccessDeniedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AccessDeniedException(String message) {
        super(message);
    }

}
