package com.brunofontenele.ems.school.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.brunofontenele.ems.school.domain.School;

@Repository
@Transactional
public interface SchoolRepository extends JpaRepository<School, Long> {

	Optional<School> findByCode(String code);

	boolean existsByCode(String code);
}
