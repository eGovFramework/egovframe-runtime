package org.egovframe.rte.fdl.cmmn.setter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/spring/context-setter.xml" })
public class SetterTest {

	@Autowired
	private SetterBar setterBar;

	@Test
	public void test() {
		assertTrue(setterBar.setMessage("I'm a big fan of yours.").equals("return_I'm a big fan of yours."));
	}


}
