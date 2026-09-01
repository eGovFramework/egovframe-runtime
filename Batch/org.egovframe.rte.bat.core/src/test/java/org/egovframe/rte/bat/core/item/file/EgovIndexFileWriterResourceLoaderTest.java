package org.egovframe.rte.bat.core.item.file;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link EgovIndexFileWriter}가 classpath 기반 indexResource를 해석할 수 있도록
 * ResourceLoader 주입 경로를 갖추었는지 검증한다.
 *
 * <p>형제 클래스 EgovIndexFileReader와 달리 Writer에는 setResourceLoader가 없어,
 * classpath 경로 사용 시 configureWriterIndexResouce()의 resourceLoader.getResource()에서
 * 항상 NullPointerException이 발생했다. 이 테스트는 주입 전(NPE)·후(NPE 아님)를 대조한다.</p>
 */
class EgovIndexFileWriterResourceLoaderTest {

    /** private configureWriterIndexResouce()를 호출하고 실제 원인 예외를 그대로 던진다. */
    private void invokeConfigure(EgovIndexFileWriter<?> writer) throws Throwable {
        Method m = EgovIndexFileWriter.class.getDeclaredMethod("configureWriterIndexResouce");
        m.setAccessible(true);
        try {
            m.invoke(writer);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    @Test
    @DisplayName("ResourceLoader 미주입 시 classpath indexResource 해석에서 NPE가 발생한다(버그 재현)")
    void withoutResourceLoaderThrowsNpe() {
        EgovIndexFileWriter<Object> writer = new EgovIndexFileWriter<>();
        // classpath 경로(/ ./ target/ 접두 없음, ':\' 없음) → resourceLoader.getResource() 분기로 진입
        writer.setIndexResource("egovframe/sample_NDX(1).txt");

        assertThrows(NullPointerException.class, () -> invokeConfigure(writer));
    }

    @Test
    @DisplayName("ResourceLoader 주입 시 classpath indexResource가 정상 해석되어 resource가 설정된다")
    void withResourceLoaderResolvesClasspathResource() throws Throwable {
        EgovIndexFileWriter<Object> writer = new EgovIndexFileWriter<>();
        // 테스트 classpath에 실존하는 파일시스템 디렉토리("egovidxtest")를 resourceDirectory로 사용한다.
        // (+1) 옵션·기존 파일 없음 → 초기 파일명을 resourceLoader.getResource()로 해석해 resource에 설정한다.
        writer.setIndexResource("egovidxtest/DATA_NDX(1).txt");
        writer.setResourceLoader(new DefaultResourceLoader());

        invokeConfigure(writer); // 예외 없이 완료되어야 한다

        assertNotNull(writer.getResource(),
                "주입된 ResourceLoader로 classpath 경로가 해석되어 resource가 설정되어야 한다");
    }

    @Test
    @DisplayName("setResourceLoader가 Reader와 동일하게 공개 메서드로 제공된다")
    void setterExists() throws NoSuchMethodException {
        Method setter = EgovIndexFileWriter.class.getMethod("setResourceLoader",
                org.springframework.core.io.ResourceLoader.class);
        assertNotNull(setter);
    }
}
