package com.brunofontenele.ems.person.repository;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import com.brunofontenele.ems.person.domain.Person;
import com.brunofontenele.ems.person.domain.Person.PersonType;
import com.brunofontenele.ems.school.domain.School;
import com.brunofontenele.ems.subject.domain.Subject;

@Repository
@Transactional
public interface PersonRepository extends JpaRepository<Person, Long> {
	Optional<Person> findById(long id);

	Optional<Person> findByEmail(String email);

	List<Person> findBySchool(School school);

	List<Person> findBySubjectCode(String subjectCode);

	List<Person> findBySubjectAndType(Subject subject, PersonType type);

	boolean existsByEmail(String email);
}
