package org.egovframe.rte.bat.core.item.file.transform;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EgovFieldExtractorBooleanTest {

    @Test
    void extractsTrueAndFalseFromBooleanGetter() {
        EgovFieldExtractor<Object> extractor = extractor("enabled");

        assertArrayEquals(new Object[]{true}, extractor.extract(new Flag(true)));
        assertArrayEquals(new Object[]{false}, extractor.extract(new Flag(false)));
    }

    @Test
    void extractsBooleanAndOtherFieldsInConfiguredOrder() {
        assertArrayEquals(new Object[]{"sample", true},
                extractor("name", "enabled").extract(new Flag(true)));
    }

    @Test
    void keepsExistingGetGetterWhenBothFormsExist() {
        assertArrayEquals(new Object[]{false}, extractor("enabled").extract(new BothGetters()));
    }

    @Test
    void keepsBoxedBooleanGetGetterIncludingNull() {
        EgovFieldExtractor<Object> extractor = extractor("enabled");

        assertArrayEquals(new Object[]{true}, extractor.extract(new BoxedFlag(true)));
        assertArrayEquals(new Object[]{false}, extractor.extract(new BoxedFlag(false)));
        assertArrayEquals(new Object[]{null}, extractor.extract(new BoxedFlag(null)));
    }

    @Test
    void supportsInheritedBooleanGetter() {
        assertArrayEquals(new Object[]{true}, extractor("enabled").extract(new InheritedFlag()));
    }

    @Test
    void choosesNoArgumentGetterWhenMethodsAreOverloaded() {
        assertArrayEquals(new Object[]{true}, extractor("enabled").extract(new OverloadedFlag()));
    }

    @Test
    void doesNotUseIsGetterWithBoxedReturnType() {
        assertThrows(RuntimeException.class, () -> extractor("enabled").extract(new BoxedIsFlag()));
    }

    @Test
    void doesNotUseIsGetterWithNonBooleanReturnType() {
        assertThrows(RuntimeException.class, () -> extractor("enabled").extract(new StringIsFlag()));
    }

    @Test
    void doesNotUseIsGetterRequiringAnArgument() {
        assertThrows(RuntimeException.class, () -> extractor("enabled").extract(new ArgumentIsFlag()));
    }

    private EgovFieldExtractor<Object> extractor(String... names) {
        EgovFieldExtractor<Object> extractor = new EgovFieldExtractor<>();
        extractor.setNames(names);
        extractor.afterPropertiesSet();
        return extractor;
    }

    public static class Flag {
        private final boolean enabled;

        public Flag(boolean enabled) { this.enabled = enabled; }
        public boolean isEnabled() { return enabled; }
        public String getName() { return "sample"; }
    }

    public static class BothGetters {
        public boolean getEnabled() { return false; }
        public boolean isEnabled() { return true; }
    }

    public static class BoxedFlag {
        private final Boolean enabled;

        public BoxedFlag(Boolean enabled) { this.enabled = enabled; }
        public Boolean getEnabled() { return enabled; }
    }

    public static class InheritedFlag extends Flag {
        public InheritedFlag() { super(true); }
    }

    public static class OverloadedFlag {
        public boolean getEnabled(String ignored) { return false; }
        public boolean getEnabled() { return true; }
    }

    public static class BoxedIsFlag {
        public Boolean isEnabled() { return true; }
    }

    public static class StringIsFlag {
        public String isEnabled() { return "true"; }
    }

    public static class ArgumentIsFlag {
        public boolean isEnabled(String ignored) { return true; }
    }
}
