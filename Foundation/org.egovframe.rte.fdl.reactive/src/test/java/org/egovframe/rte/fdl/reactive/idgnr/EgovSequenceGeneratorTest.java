package org.egovframe.rte.fdl.reactive.idgnr;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class EgovSequenceGeneratorTest {

    @Test
    public void generateSequence() {
        try (MockedStatic<EgovSequenceGenerator> mockedStatic = mockStatic(EgovSequenceGenerator.class)) {
            // generateSequence("SHA-1") 호출 시 "mocked-sequence" 반환하도록 설정
            mockedStatic.when(() -> EgovSequenceGenerator.generateSequence("SHA-1")).thenReturn("mocked-sequence");

            // 실제 테스트
            String sequence = EgovSequenceGenerator.generateSequence("SHA-1");

            // 결과 검증
            assertNotNull(sequence);
            assertEquals("mocked-sequence", sequence);

            // 호출 검증: generateSequence("SHA-1") 한 번 호출되었는지
            mockedStatic.verify(() -> EgovSequenceGenerator.generateSequence("SHA-1"), times(1));
        }
    }

    /**
     * 모킹 없는 실동작 — SHA-256 시퀀스는 64자 소문자 16진수이고 호출마다 다르다.
     */
    @Test
    public void generateSequenceProducesUniqueHexDigests() {
        Set<String> sequences = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            String sequence = EgovSequenceGenerator.generateSequence("SHA-256");
            assertEquals(64, sequence.length());
            assertTrue(sequence.matches("[0-9a-f]{64}"), "16진수 형식이어야 한다: " + sequence);
            sequences.add(sequence);
        }
        assertEquals(1000, sequences.size(), "같은 밀리초에 만들어도 시퀀스가 겹치지 않아야 한다");
    }

    /**
     * 랜덤 문자열은 시각(17자) 뒤에 허용 문자 집합의 5자가 붙는다.
     */
    @Test
    public void generateRandomStringUsesAllowedCharactersOnly() {
        for (int i = 0; i < 200; i++) {
            String value = EgovSequenceGenerator.generateRandomString();
            assertEquals(17 + 5, value.length(), value);
            assertTrue(value.matches("\\d{17}[A-Za-z0-9]{5}"), "허용 문자 집합 밖의 문자가 있다: " + value);
        }
    }

}
