package org.egovframe.rte.psl.data.jpa.repository;

import org.egovframe.rte.psl.data.jpa.domain.User;
import org.egovframe.rte.psl.data.jpa.domain.UserPK;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends CrudRepository<User, UserPK> {

	User findByUsername(String username);

	List<User> findByLastname(String lastname);

	@Query("select u from User u where u.firstname = ?1")
	List<User> findByFirstname(String firstname);

	@Query("select u from User u where u.firstname = :name or u.lastname = :name")
	List<User> findByFirstnameOrLastname(@Param("name") String name);

}
