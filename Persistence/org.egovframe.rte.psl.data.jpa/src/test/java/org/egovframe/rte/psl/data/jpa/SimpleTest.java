package org.egovframe.rte.psl.data.jpa;

import org.egovframe.rte.psl.data.jpa.domain.User;
import org.egovframe.rte.psl.data.jpa.domain.UserPK;
import org.egovframe.rte.psl.data.jpa.repository.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:META-INF/spring/context-*.xml")
@Transactional
public class SimpleTest {

	@Autowired
	UserRepository repository;

	User user;

	@Before
	public void setUp() {     
		user = new User();
		user.setUsername("foobar");
		user.setFirstname("firstname");
		user.setLastname("lastname");
	}

	@Test
	public void testInsert() {
		user = repository.save(user);
		System.out.println("user pk>"+user.getId());
		UserPK userPk = new UserPK();
		userPk.setId(user.getId());
		assertEquals(user, repository.findById(userPk).get());
	}

	@Test
	public void testFindByLastname() {
		user = repository.save(user);
		List<User> users = repository.findByLastname("lastname");
		assertNotNull(users);
		assertTrue(users.contains(user));
	}

	@Test
	public void testFindByName() {
		user = repository.save(user);
		List<User> users = repository.findByFirstnameOrLastname("lastname");
		assertTrue(users.contains(user));
	}

}
