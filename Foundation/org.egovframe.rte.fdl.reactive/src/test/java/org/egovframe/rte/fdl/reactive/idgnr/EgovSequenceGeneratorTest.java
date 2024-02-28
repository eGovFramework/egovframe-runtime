package org.egovframe.rte.fdl.reactive.idgnr;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.UUID;

@RunWith(PowerMockRunner.class)
public class EgovSequenceGeneratorTest {

    @Test
    public void generatorSequenceTest() {
        PowerMock.mockStatic(EgovSequenceGenerator.class);
        String sequence = EgovSequenceGenerator.generateSequence("SHA-1");
        System.out.println("##### EgovSequenceGeneratorTest SHA1 >>> " + sequence);
        System.out.println("##### EgovSequenceGeneratorTest UUID >>> " + UUID.randomUUID());
    }

}
