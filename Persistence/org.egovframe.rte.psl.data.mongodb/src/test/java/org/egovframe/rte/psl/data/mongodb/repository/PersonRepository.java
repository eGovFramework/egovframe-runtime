package org.egovframe.rte.psl.data.mongodb.repository;

import org.egovframe.rte.psl.data.mongodb.domain.Address;
import org.egovframe.rte.psl.data.mongodb.domain.Person;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface PersonRepository extends MongoRepository<Person, String> {

	List<Person> findByName(String name);

	Page<Person> findByName(String name, Pageable pageable);

	Person findByAddress(Address address);

	List<Person> deleteByName(String name);

	Long deletePersonByName(String lastname);

	List<Person> findByLocationNear(Point location, Distance distance);

	@Query("{ 'name' : ?0 }")
	List<Person> findByPersonName(String name);

}
