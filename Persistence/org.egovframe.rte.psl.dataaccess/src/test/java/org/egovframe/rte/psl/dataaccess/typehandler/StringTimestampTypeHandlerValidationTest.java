package org.egovframe.rte.psl.dataaccess.typehandler;

import com.ibatis.sqlmap.client.extensions.ParameterSetter;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.format.DateTimeParseException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StringTimestampTypeHandlerValidationTest {

    @Test
    void rejectsDatesThatDoNotExist() {
        for (String input : new String[]{"20230229010203", "20240230010203", "20230431010203",
                "19000229010203", "21000229010203"}) {
            assertInvalidTimestamp(input);
        }
    }

    @Test
    void rejectsInvalidTimesWithoutRollingIntoNextDay() {
        for (String input : new String[]{"20240101240000", "20240101250000", "20240101006000", "20240101000060"}) {
            assertInvalidTimestamp(input);
        }
    }

    @Test
    void preservesParseCauseForMalformedInputs() {
        for (String input : new String[]{"invalid", "", "20240101", "20241301000000", "00000101000000"}) {
            assertInvalidTimestamp(input);
        }
    }

    @Test
    void acceptsValidLeapDaysAndMonthEnds() throws SQLException {
        String[][] inputs = {
                {"20000229010203", "2000-02-29 01:02:03"},
                {"20240229010203", "2024-02-29 01:02:03"},
                {"20230430235959", "2023-04-30 23:59:59"}
        };
        for (String[] input : inputs) {
            AtomicReference<Timestamp> captured = new AtomicReference<>();
            ParameterSetter setter = setter((method, args) -> {
                assertEquals("setTimestamp", method);
                captured.set((Timestamp) args[0]);
            });

            new StringTimestampTypeHandler().setParameter(setter, input[0]);

            assertEquals(Timestamp.valueOf(input[1]), captured.get());
        }
    }

    @Test
    void nullInputStillBindsSqlNull() throws SQLException {
        AtomicInteger nullCalls = new AtomicInteger();
        ParameterSetter setter = setter((method, args) -> {
            assertEquals("setNull", method);
            nullCalls.incrementAndGet();
        });

        new StringTimestampTypeHandler().setParameter(setter, null);

        assertEquals(1, nullCalls.get());
    }

    @Test
    void preservesSqlExceptionFromParameterSetter() {
        SQLException original = new SQLException("binding failed");
        ParameterSetter setter = setter((method, args) -> { throw original; });

        SQLException actual = assertThrows(SQLException.class,
                () -> new StringTimestampTypeHandler().setParameter(setter, "20240102030405"));

        assertSame(original, actual);
    }

    private void assertInvalidTimestamp(String input) {
        AtomicInteger bindCalls = new AtomicInteger();
        ParameterSetter setter = setter((method, args) -> bindCalls.incrementAndGet());

        SQLException exception = assertThrows(SQLException.class,
                () -> new StringTimestampTypeHandler().setParameter(setter, input), input);

        assertInstanceOf(DateTimeParseException.class, exception.getCause(), input);
        assertEquals(0, bindCalls.get(), input);
    }

    private ParameterSetter setter(Binding binding) {
        return (ParameterSetter) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{ParameterSetter.class}, (proxy, method, args) -> {
                    binding.apply(method.getName(), args);
                    return null;
                });
    }

    private interface Binding {
        void apply(String method, Object[] args) throws SQLException;
    }
}
