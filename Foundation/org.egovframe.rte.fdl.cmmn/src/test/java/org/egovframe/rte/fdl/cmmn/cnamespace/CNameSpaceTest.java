package org.egovframe.rte.fdl.cmmn.cnamespace;

import org.egovframe.rte.fdl.cmmn.config.CNamespaceConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = CNamespaceConfig.class)
public class CNameSpaceTest {

    @Autowired
    private Foo foo;

    @Test
    public void test() {
        assertTrue(foo.getEmail().equals("foo@bar.com"));
    }

}
