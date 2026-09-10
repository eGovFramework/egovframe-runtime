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
package org.egovframe.rte.ptl.mvc.bind.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.egovframe.rte.fdl.cmmn.exception.BaseException;
import org.egovframe.rte.fdl.cmmn.exception.EgovBizException;
import org.egovframe.rte.fdl.cmmn.exception.FdlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * REST 응답용 기본 예외 핸들러 — <b>동작하는 기본 구현</b> + RFC 9457(ProblemDetail) 정렬.
 *
 * <p>기존 {@link AbstractAnnotationExceptionHandler}는 전 메서드가 abstract 인 시그니처 골격이라
 * 사용자가 전부 직접 구현해야 했다(잔여 모듈 점검 8-B-4). 본 클래스는 실행환경 예외 계층
 * 을 RFC 9457 {@code application/problem+json} 응답으로 매핑하는 기본 구현을 제공한다:</p>
 *
 * <ul>
 *   <li>{@link EgovBizException}(업무 예외) → 400 + 예외 메시지 + {@code messageKey} 확장 필드</li>
 *   <li><b>제약 위반</b>({@code @Valid} 본문·바인딩 {@link BindException} 계열,
 *       {@code @Validated} 파라미터 {@link ConstraintViolationException}) → <b>400</b> +
 *       {@code errors} 확장 필드(필드명·메시지 — <b>거부된 값은 싣지 않는다</b>: 입력
 *       반사·민감값 노출 방지). 종전에는 {@code Exception} 폴백으로 500 이 되던 정합성
 *       결함의 수정이다(고지). 메시지는 {@link #setMessageSource(MessageSource)}
 *       연동 시 코드 해석 우선, 미연동이면 제약의 기본 메시지</li>
 *   <li><b>형 변환 실패</b>(바인딩 중 {@code typeMismatch} 필드 오류 · 단일 값 {@link TypeMismatchException})와
 *       <b>읽을 수 없는 본문</b>({@link HttpMessageNotReadableException}) → <b>400</b> + 일반화 메시지.
 *       Spring 의 기본 메시지는 거부된 입력값과 자바 타입명(또는 파서 상세)을 담으므로 응답에
 *       싣지 않는다 — {@link MessageSource} 에 {@code typeMismatch} 코드가 있으면 그 메시지를 쓴다</li>
 *   <li>{@link FdlException}/{@link BaseException}/그 외 → 500 + <b>일반화 메시지</b>
 *       (내부 정보 노출 방지 — CWE-209, 원본은 서버 로그)</li>
 * </ul>
 *
 * <p>사용: 그대로 빈 등록이 아니라 <b>애플리케이션에서 {@code @RestControllerAdvice}를 붙인
 * 하위 클래스로 등록</b>한다(적용 범위·추가 예외 매핑을 앱이 결정). 개별 응답 커스터마이징은
 * {@code protected} 메서드 오버라이드로 한다.</p>
 *
 * <pre>{@code
 * @RestControllerAdvice(basePackages = "egovframework.example")
 * public class ExampleExceptionHandler extends EgovRestExceptionHandler { }
 * }</pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일        수정자           수정내용
 * ----------  --------------  ---------------------------
 * 2026.08.16  실행환경 개발팀    최초 생성 (검증·응답 표준화)
 * 2026.09.02  실행환경 개발팀    제약 위반 400 응답·메시지 해석 추가(종전 500 폴백은 정합성 결함)
 * 2026.09.07  실행환경 개발팀    형 변환 실패·읽을 수 없는 본문을 400 으로 매핑, 거부된 입력값·타입명 미노출
 * </pre>
 */
public class EgovRestExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovRestExceptionHandler.class);

    /** 내부 오류 응답에 사용할 일반화 메시지(원본 메시지는 서버 로그에만 남긴다) */
    private static final String INTERNAL_ERROR_DETAIL = "An internal server error has occurred.";

    /** 제약 위반 응답의 detail(개별 사유는 errors 확장 필드) */
    private static final String VALIDATION_ERROR_DETAIL = "Request validation failed.";

    /**
     * 형 변환 실패 필드에 쓸 일반화 메시지 — Spring 의 기본 메시지는 거부된 입력값과 자바 타입명을
     * 담으므로(Failed to convert ... to required type ... For input string: ...) 응답에 싣지 않는다.
     */
    private static final String BINDING_FAILURE_DETAIL = "Invalid value.";

    /** 읽을 수 없는 요청 본문 응답의 detail(파서 상세는 싣지 않는다) */
    private static final String UNREADABLE_BODY_DETAIL = "Request body could not be read.";

    private MessageSource messageSource;

    /**
     * 위반 메시지의 코드 해석에 쓸 {@link MessageSource}(opt-in — 미연동이면 제약의 기본
     * 메시지로 완결된다).
     *
     * @since 5.1
     */
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * 제약 위반(@Valid 본문·바인딩) — 400 + 필드 오류 목록. 거부된 값은 싣지 않는다.
     *
     * <p>{@code MethodArgumentNotValidException} 은 {@link BindException} 의 하위라 본
     * 핸들러 하나로 본문·바인딩 두 경로를 흡수한다.</p>
     *
     * @since 5.1
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ProblemDetail> handleBindException(BindException e) {
        LOGGER.debug("Validation failure handled: {} error(s)", e.getErrorCount());
        List<Map<String, String>> errors = new ArrayList<>();
        for (FieldError fieldError : e.getFieldErrors()) {
            if (fieldError.isBindingFailure()) {
                // 원본(입력값·타입명 포함)은 서버 로그로만 남긴다
                LOGGER.debug("Binding failure on field '{}': {}", fieldError.getField(), fieldError.getDefaultMessage());
            }
            errors.add(errorEntry(fieldError.getField(), resolveMessage(fieldError)));
        }
        for (ObjectError globalError : e.getGlobalErrors()) {
            errors.add(errorEntry(globalError.getObjectName(), resolveMessage(globalError)));
        }
        return toResponse(validationProblem(errors));
    }

    /**
     * 제약 위반(@Validated 파라미터) — 본문 위반과 같은 400 형식으로 정규화한다.
     *
     * @since 5.1
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(ConstraintViolationException e) {
        LOGGER.debug("Constraint violation handled: {} violation(s)", e.getConstraintViolations().size());
        List<Map<String, String>> errors = new ArrayList<>();
        for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
            errors.add(errorEntry(lastPathNode(violation), violation.getMessage()));
        }
        return toResponse(validationProblem(errors));
    }

    /**
     * 업무 예외 — 사용자 대상 메시지이므로 그대로 전달하고 messageKey 를 확장 필드로 싣는다.
     */
    @ExceptionHandler(EgovBizException.class)
    public ResponseEntity<ProblemDetail> handleEgovBizException(EgovBizException e) {
        LOGGER.debug("Business exception handled: {}", e.getMessage());
        ProblemDetail problem = baseProblem(HttpStatus.BAD_REQUEST, e.getMessage());
        problem.setTitle("Business Error");
        if (e.getMessageKey() != null) {
            problem.setProperty("messageKey", e.getMessageKey());
        }
        return toResponse(problem);
    }

    /**
     * 단일 값 형 변환 실패({@code @PathVariable}·{@code @RequestParam} 등) — 400 + 일반화 메시지.
     * 종전에는 {@code Exception} 폴백으로 500 이 되고 스택이 ERROR 로 기록되던 클라이언트 오류다.
     *
     * @since 5.1
     */
    @ExceptionHandler(TypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleTypeMismatch(TypeMismatchException e) {
        String name = (e instanceof MethodArgumentTypeMismatchException)
                ? ((MethodArgumentTypeMismatchException) e).getName() : e.getPropertyName();
        if (name == null) {
            name = "value";
        }
        LOGGER.debug("Type mismatch handled for '{}': {}", name, e.getMessage());
        List<Map<String, String>> errors = new ArrayList<>();
        errors.add(errorEntry(name, BINDING_FAILURE_DETAIL));
        return toResponse(validationProblem(errors));
    }

    /**
     * 읽을 수 없는 요청 본문(JSON 문법 오류 등) — 400. 파서가 준 상세(클래스명·위치)는 싣지 않는다.
     *
     * @since 5.1
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleNotReadable(HttpMessageNotReadableException e) {
        LOGGER.debug("Unreadable request body handled: {}", e.getMessage());
        ProblemDetail problem = baseProblem(HttpStatus.BAD_REQUEST, UNREADABLE_BODY_DETAIL);
        problem.setTitle("Malformed Request");
        return toResponse(problem);
    }

    /**
     * 실행환경 확장모듈 예외 — 내부 오류로 일반화한다.
     */
    @ExceptionHandler(FdlException.class)
    public ResponseEntity<ProblemDetail> handleFdlException(FdlException e) {
        return internalError(e);
    }

    /**
     * 그 외 실행환경 checked 예외 — 내부 오류로 일반화한다.
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ProblemDetail> handleBaseException(BaseException e) {
        return internalError(e);
    }

    /**
     * 미분류 예외 — 내부 오류로 일반화한다(원본 메시지·스택은 서버 로그로만).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleException(Exception e) {
        return internalError(e);
    }

    /**
     * 내부 오류 공통 처리 — 원본은 로그, 응답은 일반화 메시지(CWE-209 방지).
     */
    protected ResponseEntity<ProblemDetail> internalError(Exception e) {
        LOGGER.error("Unhandled exception mapped to internal server error", e);
        ProblemDetail problem = baseProblem(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_ERROR_DETAIL);
        problem.setTitle("Internal Server Error");
        return toResponse(problem);
    }

    /**
     * 공통 ProblemDetail 골격 — timestamp 확장 필드를 포함한다.
     */
    protected ProblemDetail baseProblem(HttpStatus status, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setProperty("timestamp", OffsetDateTime.now().toString());
        return problem;
    }

    /** 제약 위반 공통 골격 — 400 + errors 확장 필드. */
    protected ProblemDetail validationProblem(List<Map<String, String>> errors) {
        ProblemDetail problem = baseProblem(HttpStatus.BAD_REQUEST, VALIDATION_ERROR_DETAIL);
        problem.setTitle("Validation Failed");
        problem.setProperty("errors", errors);
        return problem;
    }

    /**
     * 위반 메시지 해석 — {@link MessageSource} 연동 시 오류 코드 해석 우선(기본 메시지 폴백은
     * Spring 표준 {@code MessageSourceResolvable} 규칙), 미연동이면 제약의 기본 메시지.
     */
    protected String resolveMessage(ObjectError error) {
        boolean bindingFailure = (error instanceof FieldError) && ((FieldError) error).isBindingFailure();
        if (messageSource != null) {
            try {
                // 형 변환 실패는 코드만 해석한다 — 기본 메시지(입력값·타입명 포함)로 폴백하지 않기 위해서다
                MessageSourceResolvable resolvable = bindingFailure
                        ? new DefaultMessageSourceResolvable(error.getCodes(), error.getArguments())
                        : error;
                return messageSource.getMessage(resolvable, LocaleContextHolder.getLocale());
            } catch (NoSuchMessageException noMessage) {
                // 코드·기본 메시지 모두 없음 — 아래 기본 메시지 경로로 폴백
            }
        }
        // 형 변환 실패의 기본 메시지는 거부된 입력값과 자바 타입명을 담는다(입력 반사·내부 정보 노출) —
        // 응답에는 일반화 메시지를 싣고 원본은 handleBindException 이 debug 로그로 남긴다
        return bindingFailure ? BINDING_FAILURE_DETAIL : error.getDefaultMessage();
    }

    private static Map<String, String> errorEntry(String field, String message) {
        Map<String, String> entry = new LinkedHashMap<>(2);
        entry.put("field", field);
        entry.put("message", message);
        return entry;
    }

    /** 파라미터 위반의 property path 말단을 필드명으로 정규화한다({@code selectOne.userId} → {@code userId}). */
    private static String lastPathNode(ConstraintViolation<?> violation) {
        String path = String.valueOf(violation.getPropertyPath());
        int lastDot = path.lastIndexOf('.');
        return (lastDot < 0) ? path : path.substring(lastDot + 1);
    }

    private static ResponseEntity<ProblemDetail> toResponse(ProblemDetail problem) {
        return ResponseEntity.status(problem.getStatus()).body(problem);
    }

}
