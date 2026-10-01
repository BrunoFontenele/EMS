package com.brunofontenele.ems.correction.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import com.brunofontenele.ems.correction.domain.Correction;
import com.brunofontenele.ems.exam.domain.Exam;
import com.brunofontenele.ems.person.domain.Person;

@Repository
@Transactional
public interface CorrectionRepository extends JpaRepository<Correction, Long> {
	Optional<Correction> findById(Long id);

    List<Correction> findByProfessorId(Long professorId);

    boolean existsByProfessorEmailAndQuestionId(String professorEmail, Long questionId);

    boolean existsByQuestionExamIdAndScoreIsNull(Long examId);

    List<Correction> findByQuestionExamId(Long examId);

    Optional<Correction> findByQuestionId(Long questionId);

    List<Correction> findByProfessor(Person professor);

    List<Correction> findByQuestion_Exam(Exam exam);

	boolean existsById(Long id);
}
