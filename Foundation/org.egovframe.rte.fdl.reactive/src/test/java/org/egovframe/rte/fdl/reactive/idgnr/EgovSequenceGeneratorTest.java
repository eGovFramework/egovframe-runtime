package org.egovframe.rte.fdl.reactive.idgnr;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * EgovSequenceGenerator 테스트.
 *
 * <p>종전 테스트는 PowerMock 으로 대상 클래스를 정적 mock 한 뒤 호출만 하고 검증하지 않았다. PowerMock 2.x 는
 * JDK 16 이상에서 {@code java.lang} 내부 접근이 막혀 테스트 JVM 이 기동 단계에서 실패하므로, mock 없이 실제
 * 동작(해시 형식·유일성·잘못된 알고리즘 처리)을 검증하도록 바꿨다.</p>
 */
public class EgovSequenceGeneratorTest {

    @Test
    public void generatorSequenceTest() {
        String sequence = EgovSequenceGenerator.generateSequence("SHA-1");
        assertTrue(sequence, sequence.matches("[0-9a-f]{40}"));
    }

    @Test
    public void sha256SequenceTest() {
        String sequence = EgovSequenceGenerator.generateSequence("SHA-256");
        assertTrue(sequence, sequence.matches("[0-9a-f]{64}"));
    }

    @Test
    public void sequencesAreDistinct() {
        Set<String> sequences = new HashSet<String>();
        for (int i = 0; i < 1000; i++) {
            sequences.add(EgovSequenceGenerator.generateSequence("SHA-256"));
        }
        assertEquals(1000, sequences.size());
    }

    @Test
    public void unknownAlgorithmReturnsEmpty() {
        assertEquals("", EgovSequenceGenerator.generateSequence("NO-SUCH-ALGORITHM"));
    }

}
