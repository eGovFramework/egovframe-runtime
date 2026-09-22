package org.egovframe.rte.fdl.string;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@Isolated("Temporarily changes the class loading allowlist")
class EgovObjectUtilClassLoaderTest {

    private ClassLoader originalLoader;
    private Set<String> originalAllowedClasses;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void saveClassLoadingConfiguration() {
        originalLoader = Thread.currentThread().getContextClassLoader();
        originalAllowedClasses = (Set<String>) ReflectionTestUtils.getField(EgovObjectUtil.class, "allowedClassNames");
        EgovObjectUtil.setAllowedClassNames(null);
    }

    @AfterEach
    void restoreClassLoadingConfiguration() {
        Thread.currentThread().setContextClassLoader(originalLoader);
        EgovObjectUtil.setAllowedClassNames(originalAllowedClasses);
    }

    @Test
    void usesExistingContextClassLoader() throws ClassNotFoundException {
        AtomicBoolean called = new AtomicBoolean();
        Thread.currentThread().setContextClassLoader(new ClassLoader(originalLoader) {
            @Override
            public Class<?> loadClass(String name) throws ClassNotFoundException {
                called.set(true);
                return super.loadClass(name);
            }
        });
        assertSame(String.class, EgovObjectUtil.loadClass("java.lang.String"));
        assertTrue(called.get());
    }

    @Test
    void loadsBootstrapClassWithoutContextClassLoader() throws ClassNotFoundException {
        Thread.currentThread().setContextClassLoader(null);
        assertSame(String.class, EgovObjectUtil.loadClass("java.lang.String"));
    }

    @Test
    void loadsApplicationClassWithoutContextClassLoader() throws ClassNotFoundException {
        Thread.currentThread().setContextClassLoader(null);
        assertSame(EgovObjectUtil.class, EgovObjectUtil.loadClass(EgovObjectUtil.class.getName()));
    }

    @Test
    void instantiatesWithoutContextClassLoader() {
        Thread.currentThread().setContextClassLoader(null);
        assertEquals("", EgovObjectUtil.instantiate("java.lang.String"));
    }

    @Test
    void instantiatesWithArgumentsWithoutContextClassLoader() {
        Thread.currentThread().setContextClassLoader(null);
        Object value = EgovObjectUtil.instantiate("java.lang.StringBuilder", new String[]{"java.lang.String"}, new Object[]{"egov"});
        assertEquals("egov", value.toString());
    }

    @Test
    void reportsMissingClassWithoutContextClassLoader() {
        Thread.currentThread().setContextClassLoader(null);
        assertThrows(ClassNotFoundException.class, () -> EgovObjectUtil.loadClass("review.MissingClass"));
    }

    @Test
    void doesNotBypassExistingContextClassLoader() {
        Thread.currentThread().setContextClassLoader(new ClassLoader(originalLoader) {
            @Override
            public Class<?> loadClass(String name) throws ClassNotFoundException {
                throw new ClassNotFoundException(name);
            }
        });
        assertThrows(ClassNotFoundException.class, () -> EgovObjectUtil.loadClass("java.lang.String"));
    }

    @Test
    void enforcesAllowlistWithoutContextClassLoader() throws ClassNotFoundException {
        Thread.currentThread().setContextClassLoader(null);
        EgovObjectUtil.setAllowedClassNames(Set.of("java.lang.String"));
        assertSame(String.class, EgovObjectUtil.loadClass("java.lang.String"));
        assertThrows(SecurityException.class, () -> EgovObjectUtil.loadClass("java.lang.StringBuilder"));
    }
}
