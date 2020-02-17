package egovframework.rte.psl.data.jpa.repository;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import egovframework.rte.psl.data.jpa.domain.User;

import org.springframework.data.repository.CrudRepository;

public interface UserDetailRepository extends CrudRepository<User, Long> {

	List<User> findByLastnameAndFirstname(String lastname, String firstname);
	
	List<User> findByLastnameOrFirstname(String lastname, String firstname);
	
	List<User> findByStartDateBetween(Date start, Date end);
	
	List<User> findByAgeLessThan(int age);
	
	List<User> findByAgeGreaterThan(int age);
	
	List<User> findByStartDateAfter(Date start);
	
	List<User> findByStartDateBefore(Date end);
	
	List<User> findByAgeIsNull();
	
	List<User> findByAgeIsNotNull();
	
	List<User> findByFirstnameLike(String firstname);
	
	List<User> findByFirstnameNotLike(String firstname);
	
	List<User> findByFirstnameStartingWith(String firstname);
	
	List<User> findByFirstnameEndingWith(String firstname);
	
	List<User> findByFirstnameContaining(String firstname);
	
	List<User> findByAgeOrderByLastnameDesc(int age);
	
	List<User> findByLastnameNot(String lastname);
	
	List<User> findByAgeIn(Collection<Integer> ages);
	
	List<User> findByAgeNotIn(Collection<Integer> ages);
	
	List<User> findByActiveTrue();
	
	List<User> findByActiveFalse();
	
}
