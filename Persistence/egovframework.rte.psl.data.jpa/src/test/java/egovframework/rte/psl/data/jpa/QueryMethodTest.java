package egovframework.rte.psl.data.jpa;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import egovframework.rte.psl.data.jpa.domain.User;
import egovframework.rte.psl.data.jpa.repository.UserDetailRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:META-INF/spring/context-*.xml")
@Transactional
public class QueryMethodTest {

	@Autowired
	UserDetailRepository repository;
	
	private static final String[] NAME = {"Gildong Alice", "Gildong Bob", "Gilseo Charles", "Gilseo Dennis", "Gilbook Emily", "Gilbook Frank ", "Gilnam George", "Gilnam Henry"};
	
	User[] users = null;

	@Before
	public void setUp() {
		User[] users = getTestList();
		
		for (int i = 0; i < users.length; i++) {
			users[i] = repository.save(users[i]);
		}
	}

	@Test
	public void testQueryMethod() {
		
		List<User> list = null;
		
		list = repository.findByLastnameAndFirstname("Alice", "Gildong");
		assertEquals(1, list.size());
		
		list = repository.findByLastnameOrFirstname("Alice", "Gildong");
		assertEquals(2, list.size());
		
		list = repository.findByStartDateBetween(getDate("2013-01-01"), getDate("2013-01-31"));
		assertEquals(4, list.size());
		
		list = repository.findByAgeLessThan(10);
		assertEquals(0, list.size());
		
		list = repository.findByAgeGreaterThan(50);
		assertEquals(3, list.size());
		
		list = repository.findByStartDateAfter(getDate("2013-02-01"));
		assertEquals(4, list.size());
		
		list = repository.findByStartDateBefore(getDate("2013-01-31"));
		assertEquals(3, list.size());
		
		list = repository.findByAgeIsNull();
		assertEquals(0, list.size());
		
		list = repository.findByAgeIsNotNull();
		assertEquals(8, list.size());
		
		list = repository.findByFirstnameLike("%dong");
		assertEquals(2, list.size());
		
		list = repository.findByFirstnameNotLike("%dong");
		assertEquals(6, list.size());
		
		list = repository.findByFirstnameStartingWith("Gil");
		assertEquals(8, list.size());
		
		list = repository.findByFirstnameEndingWith("nam");
		assertEquals(2, list.size());
		
		list = repository.findByFirstnameContaining("book");
		assertEquals(2, list.size());
		
		list = repository.findByAgeOrderByLastnameDesc(10);
		assertEquals(1, list.size());
		
		list = repository.findByLastnameNot("Alice");
		assertEquals(7, list.size());
		
		List<Integer> ages = new ArrayList<Integer>();
		ages.add(15);
		ages.add(20);
		ages.add(30);
		
		list = repository.findByAgeIn(ages);
		assertEquals(2, list.size());
		
		list = repository.findByAgeNotIn(ages);
		assertEquals(6, list.size());
		
		list = repository.findByActiveTrue();
		assertEquals(4, list.size());
		
		list = repository.findByActiveFalse();
		assertEquals(4, list.size());
	}
	
	private Date getDate(String dateString, int add) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		
		try {
			Date current = format.parse(dateString);
			
			Calendar cal = Calendar.getInstance();
			cal.setTime(current);
			cal.add(Calendar.DATE, add);
			
			return cal.getTime();
		} catch (ParseException pe) {
			throw new RuntimeException(pe);
		}
	}
	
	private Date getDate(String dateString) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		
		try {
			return format.parse(dateString);
		} catch (ParseException pe) {
			throw new RuntimeException(pe);
		}
	}
	
	private User[] getTestList() {
		List<User> users = new ArrayList<User>();
		
		User user = null;
		for (int i = 0; i < NAME.length; i++) {
			user = new User();
			
			String[] names = NAME[i].split(" ");
			user.setFirstname(names[0]);
			user.setLastname(names[1]);
			
			user.setStartDate(getDate("2013-01-01", i * 10));
			user.setAge(i * 10 + 10);
			
			if (i % 2 ==0) {
				user.setActive(true);
			} else {
				user.setActive(false);
				
			}
			
			users.add(user);
		}
		
		return users.toArray(new User[0]);
	}
}
