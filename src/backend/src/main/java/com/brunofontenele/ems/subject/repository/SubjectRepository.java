package com.brunofontenele.ems.subject.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.brunofontenele.ems.subject.domain.Subject;

@Repository
@Transactional
public interface SubjectRepository extends JpaRepository<Subject, Long> {

	Optional<Subject> findByCode(String code);

	boolean existsByCode(String code);

	boolean existsById(Long id);
}
