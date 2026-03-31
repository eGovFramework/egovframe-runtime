package org.egovframe.rte.fdl.cmmn.setter;

import org.egovframe.rte.fdl.cmmn.config.SetterConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SetterConfig.class)
public class SetterTest {

    @Autowired
    private SetterBar setterBar;

    @Test
    public void test() {
        assertTrue(setterBar.setMessage("I'm a big fan of yours.").equals("return_I'm a big fan of yours."));
    }

}
