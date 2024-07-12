package org.egovframe.rte.ptl.reactive.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Repository;

import java.lang.annotation.*;

/**
 * Indicates that an annotated class is a Repository
 *
 * <p>Desc.: Indicates that an annotated class is a Repository</p>
 *
 * @author ESFC
 * @since 2023.08.31
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2023.08.31   ESFC            최초 생성
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repository
public @interface EgovRepository {

    @AliasFor(annotation = Repository.class)
    String value() default "";

}
