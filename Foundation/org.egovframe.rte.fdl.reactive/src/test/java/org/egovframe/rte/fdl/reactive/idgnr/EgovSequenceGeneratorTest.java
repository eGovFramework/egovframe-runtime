package org.egovframe.rte.fdl.reactive.idgnr;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

}
