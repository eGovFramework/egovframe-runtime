package org.egovframe.rte.ptl.reactive.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Service;

import java.lang.annotation.*;

/**
 * Indicates that an annotated class is a Service
 *
 * <p>Desc.: Indicates that an annotated class is a Service</p>
 *
 * @author 유지보수
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2023.08.31   유지보수            최초 생성
 * </pre>
 * @since 2023.08.31
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Service
public @interface EgovService {

    @AliasFor(annotation = Service.class)
    String value() default "";

}
