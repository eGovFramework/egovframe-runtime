package org.egovframe.rte.psl.data.mongodb.repository;

import java.util.List;

import org.egovframe.rte.psl.data.mongodb.domain.Address;
import org.egovframe.rte.psl.data.mongodb.domain.Person;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface PersonRepository extends MongoRepository<Person, String> {
	// Query methods
	List<Person> findByLastname(String lastname);

	Page<Person> findByFirstname(String firstname, Pageable pageable);

	Person findByAddress(Address address);

	// Delete methods
	List<Person> deleteByLastname(String lastname);

	Long deletePersonByLastname(String lastname);

	// Geo-spatial methods
	List<Person> findByLocationNear(Point location, Distance distance);

	// JSON based query methods
	@Query("{ 'firstname' : ?0 }")
	List<Person> findByThePersonsFirstname(String firstname);
}
