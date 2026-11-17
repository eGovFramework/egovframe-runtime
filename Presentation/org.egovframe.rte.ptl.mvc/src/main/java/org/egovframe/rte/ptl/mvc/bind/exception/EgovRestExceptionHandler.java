/*
 * Copyright 2008-2026 MOIS(Ministry of the Interior and Safety).
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
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * REST 응답용 기본 예외 핸들러 — <b>동작하는 기본 구현</b> + RFC 7807/9457(Problem Details) 형식 응답.
 *
 * <p>기존 {@link AbstractAnnotationExceptionHandler}는 전 메서드가 abstract 인 시그니처 골격이라
 * 사용자가 전부 직접 구현해야 했고, 구현이 빠지면 컨테이너 기본 오류 페이지·스택트레이스가 노출될 수 있었다.
 * 본 클래스는 실행환경 예외 계층을 {@code application/problem+json} 응답으로 매핑하는 기본 구현을 제공한다.
 * (Spring 5.3 에는 {@code ProblemDetail} 이 없으므로 같은 필드 구성의 {@code Map} 본문을 쓴다.)</p>
 * <ul>
 *   <li>{@link EgovBizException}(업무 예외) → 400 + 예외 메시지 + {@code messageKey} 확장 필드</li>
 *   <li><b>제약 위반</b>({@code @Valid} 본문·바인딩 {@link BindException} 계열, {@code @Validated} 파라미터
 *       {@code ConstraintViolationException}) → 400 + {@code errors} 확장 필드(필드명·메시지 — <b>거부된 값은 싣지
 *       않는다</b>: 입력 반사·민감값 노출 방지)</li>
 *   <li><b>형 변환 실패</b>·<b>읽을 수 없는 본문</b> → 400 + 일반화 메시지(입력값·자바 타입명·파서 상세 미노출)</li>
 *   <li><b>자기 HTTP 상태를 가진 Spring 표준 예외</b>(405·415·406·404·413·필수 파라미터 누락 등)와
 *       {@link ResponseStatusException}·{@link ResponseStatus} 예외 → 그 상태(405 는 {@code Allow} 헤더 포함)와
 *       상태별 일반화 문구</li>
 *   <li><b>인증·인가 예외</b>(Spring Security {@code AccessDeniedException}·{@code AuthenticationException} 계열)
 *       → 응답을 쓰지 않고 <b>같은 예외를 다시 던져</b> 보안 계층이 401·403·로그인 유도로 처리하게 한다.
 *       판별은 클래스 이름으로 하므로 본 모듈은 Spring Security 에 의존하지 않는다</li>
 *   <li>{@link FdlException}/{@link BaseException}/그 외 → 500 + <b>일반화 메시지</b>
 *       (내부 정보 노출 방지 — CWE-209, 원본은 서버 로그)</li>
 * </ul>
 *
 * <p>사용: 그대로 빈 등록이 아니라 <b>애플리케이션에서 {@code @RestControllerAdvice}를 붙인
 * 하위 클래스로 등록</b>한다(적용 범위·추가 예외 매핑을 앱이 결정). 개별 응답 커스터마이징은
 * {@code protected} 메서드 오버라이드로 한다.</p>
 * <pre>{@code
 * @RestControllerAdvice(basePackages = "egovframework.example")
 * public class ExampleExceptionHandler extends EgovRestExceptionHandler { }
 * }</pre>
 *
 * @author 실행환경 개발팀
 * @since 4.3.1
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일        수정자           수정내용
 * ----------  --------------  ---------------------------
 * 2026.10.25  실행환경 개발팀    최초 생성 (5.1.0 EgovRestExceptionHandler 의 응답 정책을 Spring 5.3 기준으로 반영)
 * </pre>
 */
public class EgovRestExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovRestExceptionHandler.class);

    /** 응답 Content-Type */
    protected static final MediaType PROBLEM_JSON = MediaType.APPLICATION_PROBLEM_JSON;

    /** 내부 오류 응답에 사용할 일반화 메시지(원본 메시지는 서버 로그에만 남긴다) */
    private static final String INTERNAL_ERROR_DETAIL = "An internal server error has occurred.";

    /** 제약 위반 응답의 detail(개별 사유는 errors 확장 필드) */
    private static final String VALIDATION_ERROR_DETAIL = "Request validation failed.";

    /** 형 변환 실패 필드에 쓸 일반화 메시지 — Spring 기본 메시지는 거부된 입력값과 자바 타입명을 담는다 */
    private static final String BINDING_FAILURE_DETAIL = "Invalid value.";

    /** 읽을 수 없는 요청 본문 응답의 detail(파서 상세는 싣지 않는다) */
    private static final String UNREADABLE_BODY_DETAIL = "Request body could not be read.";

    /** 보안 계층에 넘길 예외 타입(이름으로 판별 — 본 모듈은 Spring Security 에 의존하지 않는다) */
    private static final String[] SECURITY_EXCEPTION_TYPES = {
            "org.springframework.security.access.AccessDeniedException",
            "org.springframework.security.core.AuthenticationException"
    };

    /** Bean Validation 파라미터 제약 위반 예외(이름으로 판별 — 본 모듈은 javax.validation 에 의존하지 않는다) */
    private static final String CONSTRAINT_VIOLATION_EXCEPTION = "javax.validation.ConstraintViolationException";

    private MessageSource messageSource;

    /**
     * 위반 메시지의 코드 해석에 쓸 {@link MessageSource}(opt-in — 미연동이면 제약의 기본 메시지로 완결된다).
     */
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * 제약 위반(@Valid 본문·바인딩) — 400 + 필드 오류 목록. 거부된 값은 싣지 않는다.
     * {@code MethodArgumentNotValidException} 은 {@link BindException} 의 하위라 두 경로를 함께 처리한다.
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Map<String, Object>> handleBindException(BindException e) {
        LOGGER.debug("Validation failure handled: {} error(s)", e.getErrorCount());
        List<Map<String, String>> errors = new ArrayList<Map<String, String>>();
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
        return toResponse(validationProblem(errors), null);
    }

    /**
     * 업무 예외 — 사용자 대상 메시지이므로 그대로 전달하고 messageKey 를 확장 필드로 싣는다.
     */
    @ExceptionHandler(EgovBizException.class)
    public ResponseEntity<Map<String, Object>> handleEgovBizException(EgovBizException e) {
        LOGGER.debug("Business exception handled: {}", e.getMessage());
        Map<String, Object> problem = baseProblem(HttpStatus.BAD_REQUEST, "Business Error", e.getMessage());
        if (e.getMessageKey() != null) {
            problem.put("messageKey", e.getMessageKey());
        }
        return toResponse(problem, null);
    }

    /**
     * 단일 값 형 변환 실패({@code @PathVariable}·{@code @RequestParam} 등) — 400 + 일반화 메시지.
     */
    @ExceptionHandler(TypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(TypeMismatchException e) {
        String name = (e instanceof MethodArgumentTypeMismatchException)
                ? ((MethodArgumentTypeMismatchException) e).getName() : e.getPropertyName();
        if (name == null) {
            name = "value";
        }
        LOGGER.debug("Type mismatch handled for '{}': {}", name, e.getMessage());
        List<Map<String, String>> errors = new ArrayList<Map<String, String>>();
        errors.add(errorEntry(name, resolveTypeMismatchMessage(name, e.getRequiredType())));
        return toResponse(validationProblem(errors), null);
    }

    /**
     * 읽을 수 없는 요청 본문(JSON 문법 오류 등) — 400. 파서가 준 상세(클래스명·위치)는 싣지 않는다.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleNotReadable(HttpMessageNotReadableException e) {
        LOGGER.debug("Unreadable request body handled: {}", e.getMessage());
        return toResponse(baseProblem(HttpStatus.BAD_REQUEST, "Malformed Request", UNREADABLE_BODY_DETAIL), null);
    }

    /**
     * 실행환경 확장모듈 예외 — 내부 오류로 일반화한다.
     */
    @ExceptionHandler(FdlException.class)
    public ResponseEntity<Map<String, Object>> handleFdlException(FdlException e) {
        return internalError(e);
    }

    /**
     * 그 외 실행환경 checked 예외 — 내부 오류로 일반화한다.
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<Map<String, Object>> handleBaseException(BaseException e) {
        return internalError(e);
    }

    /**
     * 미분류 예외 — 내부 오류로 일반화한다(원본 메시지·스택은 서버 로그로만).
     *
     * <p>단 다음은 미분류가 아니다. ① 인증·인가 예외는 같은 예외를 다시 던져 보안 계층에 넘긴다.
     * ② Bean Validation 파라미터 제약 위반은 400 필드 오류로 응답한다. ③ 자기 HTTP 상태를 가진 Spring 표준 예외는
     * {@link #standardError(HttpStatus, HttpHeaders, Exception)} 로 그 상태를 지킨다.</p>
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        if (isSecurityException(e)) {
            LOGGER.debug("Security exception delegated to the security layer: {}", e.getClass().getName());
            throw (RuntimeException) e;
        }
        if (isTypeOf(e, CONSTRAINT_VIOLATION_EXCEPTION)) {
            return toResponse(validationProblem(constraintViolationErrors(e)), null);
        }
        HttpHeaders headers = new HttpHeaders();
        HttpStatus status = standardStatus(e, headers);
        if (status != null) {
            return standardError(status, headers, e);
        }
        return internalError(e);
    }

    /**
     * Spring 표준 예외 공통 처리 — 예외가 뜻하는 HTTP 상태와 헤더를 쓰고, detail 은 상태별 일반화 문구로 둔다
     * (Spring 기본 메시지는 요청 경로·파라미터명 등 입력을 되비출 수 있다).
     */
    protected ResponseEntity<Map<String, Object>> standardError(HttpStatus status, HttpHeaders headers, Exception e) {
        if (status.is5xxServerError()) {
            LOGGER.error("Standard exception mapped to status {}", status.value(), e);
        } else {
            LOGGER.debug("Standard exception mapped to status {}: {}", status.value(), e.getMessage());
        }
        return toResponse(baseProblem(status, status.getReasonPhrase(), standardDetail(status)), headers);
    }

    /**
     * 내부 오류 공통 처리 — 원본은 로그, 응답은 일반화 메시지(CWE-209 방지).
     */
    protected ResponseEntity<Map<String, Object>> internalError(Exception e) {
        LOGGER.error("Unhandled exception mapped to internal server error", e);
        return toResponse(baseProblem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", INTERNAL_ERROR_DETAIL), null);
    }

    /**
     * 공통 Problem Details 골격 — type·title·status·detail·timestamp.
     */
    protected Map<String, Object> baseProblem(HttpStatus status, String title, String detail) {
        Map<String, Object> problem = new LinkedHashMap<String, Object>();
        problem.put("type", "about:blank");
        problem.put("title", title);
        problem.put("status", status.value());
        problem.put("detail", detail);
        problem.put("timestamp", OffsetDateTime.now().toString());
        return problem;
    }

    /** 제약 위반 공통 골격 — 400 + errors 확장 필드. */
    protected Map<String, Object> validationProblem(List<Map<String, String>> errors) {
        Map<String, Object> problem = baseProblem(HttpStatus.BAD_REQUEST, "Validation Failed", VALIDATION_ERROR_DETAIL);
        problem.put("errors", errors);
        return problem;
    }

    /**
     * 위반 메시지 해석 — {@link MessageSource} 연동 시 오류 코드 해석 우선, 미연동이면 제약의 기본 메시지.
     * 형 변환 실패는 기본 메시지(입력값·타입명 포함)로 폴백하지 않는다.
     */
    protected String resolveMessage(ObjectError error) {
        boolean bindingFailure = (error instanceof FieldError) && ((FieldError) error).isBindingFailure();
        if (messageSource != null) {
            try {
                MessageSourceResolvable resolvable = bindingFailure
                        ? new DefaultMessageSourceResolvable(error.getCodes(), error.getArguments())
                        : error;
                return messageSource.getMessage(resolvable, LocaleContextHolder.getLocale());
            } catch (NoSuchMessageException noMessage) {
                // 코드·기본 메시지 모두 없음 — 아래 기본 메시지 경로로 폴백
                LOGGER.debug("No message for codes of '{}'", error.getObjectName());
            }
        }
        return bindingFailure ? BINDING_FAILURE_DETAIL : error.getDefaultMessage();
    }

    /** 단일 값 형 변환 실패 메시지 해석 — 바인딩 경로와 같은 코드 단계·필드명 인자를 쓴다. */
    private String resolveTypeMismatchMessage(String name, Class<?> requiredType) {
        if (messageSource == null) {
            return BINDING_FAILURE_DETAIL;
        }
        List<String> codes = new ArrayList<String>(3);
        codes.add("typeMismatch." + name);
        if (requiredType != null) {
            codes.add("typeMismatch." + requiredType.getName());
        }
        codes.add("typeMismatch");
        MessageSourceResolvable resolvable = new DefaultMessageSourceResolvable(
                codes.toArray(new String[0]),
                new Object[]{new DefaultMessageSourceResolvable(new String[]{name}, name)},
                BINDING_FAILURE_DETAIL);
        return messageSource.getMessage(resolvable, LocaleContextHolder.getLocale());
    }

    /**
     * Spring 표준 예외의 HTTP 상태(대응이 없으면 null). 405·415 는 허용 값을 헤더로 함께 싣는다.
     */
    protected HttpStatus standardStatus(Exception e, HttpHeaders headers) {
        if (e instanceof HttpRequestMethodNotSupportedException) {
            HttpRequestMethodNotSupportedException ex = (HttpRequestMethodNotSupportedException) e;
            if (!CollectionUtils.isEmpty(ex.getSupportedHttpMethods())) {
                headers.setAllow(ex.getSupportedHttpMethods());
            }
            return HttpStatus.METHOD_NOT_ALLOWED;
        }
        if (e instanceof HttpMediaTypeNotSupportedException) {
            List<MediaType> mediaTypes = ((HttpMediaTypeNotSupportedException) e).getSupportedMediaTypes();
            if (!CollectionUtils.isEmpty(mediaTypes)) {
                headers.setAccept(mediaTypes);
            }
            return HttpStatus.UNSUPPORTED_MEDIA_TYPE;
        }
        if (e instanceof HttpMediaTypeNotAcceptableException) {
            return HttpStatus.NOT_ACCEPTABLE;
        }
        if (e instanceof MissingPathVariableException) {
            // 경로 변수 누락은 매핑 선언 오류(서버 결함)이므로 Spring 기본과 같이 500
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        if (e instanceof ServletRequestBindingException || e instanceof MissingServletRequestPartException) {
            return HttpStatus.BAD_REQUEST;
        }
        if (e instanceof NoHandlerFoundException) {
            return HttpStatus.NOT_FOUND;
        }
        if (e instanceof MaxUploadSizeExceededException) {
            return HttpStatus.PAYLOAD_TOO_LARGE;
        }
        if (e instanceof AsyncRequestTimeoutException) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        }
        if (e instanceof ResponseStatusException) {
            ResponseStatusException ex = (ResponseStatusException) e;
            headers.addAll(ex.getResponseHeaders());
            return HttpStatus.resolve(ex.getRawStatusCode());
        }
        ResponseStatus responseStatus = AnnotatedElementUtils.findMergedAnnotation(e.getClass(), ResponseStatus.class);
        if (responseStatus != null) {
            return responseStatus.code();
        }
        return null;
    }

    /** 표준 예외의 상태별 일반화 detail — 요청 경로·요청 값 같은 입력을 담지 않는 고정 문구다. */
    private static String standardDetail(HttpStatus status) {
        switch (status.value()) {
            case 400: return "The request is invalid.";
            case 401: return "Authentication is required.";
            case 403: return "Access is denied.";
            case 404: return "The requested resource was not found.";
            case 405: return "The request method is not supported for this resource.";
            case 406: return "The requested representation is not available.";
            case 413: return "The request payload is too large.";
            case 415: return "The request media type is not supported.";
            case 503: return "The service is temporarily unavailable.";
            default:
                return status.is5xxServerError() ? INTERNAL_ERROR_DETAIL : "The request could not be processed.";
        }
    }

    /**
     * Bean Validation 제약 위반 목록(필드명·메시지). javax.validation 에 컴파일 의존하지 않도록 리플렉션으로 읽는다.
     */
    private static List<Map<String, String>> constraintViolationErrors(Exception e) {
        List<Map<String, String>> errors = new ArrayList<Map<String, String>>();
        try {
            Method getViolations = e.getClass().getMethod("getConstraintViolations");
            Object violations = getViolations.invoke(e);
            if (violations instanceof Collection) {
                for (Object violation : (Collection<?>) violations) {
                    String path = String.valueOf(violation.getClass().getMethod("getPropertyPath").invoke(violation));
                    int lastDot = path.lastIndexOf('.');
                    String field = (lastDot < 0) ? path : path.substring(lastDot + 1);
                    Object message = violation.getClass().getMethod("getMessage").invoke(violation);
                    errors.add(errorEntry(field, String.valueOf(message)));
                }
            }
        } catch (ReflectiveOperationException | RuntimeException ex) {
            LOGGER.debug("Constraint violations could not be read: {}", ex.getMessage());
        }
        LOGGER.debug("Constraint violation handled: {} violation(s)", errors.size());
        return errors;
    }

    /** 예외 클래스 계층에 보안 계층이 처리할 타입이 있는지 — 이름으로만 비교한다. */
    private static boolean isSecurityException(Throwable e) {
        if (!(e instanceof RuntimeException)) {
            return false;
        }
        for (String securityType : SECURITY_EXCEPTION_TYPES) {
            if (isTypeOf(e, securityType)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isTypeOf(Throwable e, String className) {
        for (Class<?> type = e.getClass(); type != null && type != Object.class; type = type.getSuperclass()) {
            if (className.equals(type.getName())) {
                return true;
            }
        }
        return false;
    }

    private static Map<String, String> errorEntry(String field, String message) {
        Map<String, String> entry = new LinkedHashMap<String, String>(2);
        entry.put("field", field);
        entry.put("message", message);
        return entry;
    }

    private static ResponseEntity<Map<String, Object>> toResponse(Map<String, Object> problem, HttpHeaders headers) {
        ResponseEntity.BodyBuilder builder = ResponseEntity.status((Integer) problem.get("status")).contentType(PROBLEM_JSON);
        if (headers != null && !headers.isEmpty()) {
            builder.headers(headers);
        }
        return builder.body(problem);
    }

}
