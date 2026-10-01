package com.brunofontenele.ems.question.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import com.brunofontenele.ems.person.domain.Person;
import com.brunofontenele.ems.question.domain.Question;
import com.brunofontenele.ems.school.domain.School;

@Repository
@Transactional
public interface QuestionRepository extends JpaRepository<Question, Long> {

	Optional<Question> findById(Long id);

    List<Question> findByExamId(Long examId);

    List<Question> findByExamIdIn(List<Long> examIds);

    List<Question> findByExam_School(School school);

    List<Question> findByExam_Student(Person student);

	boolean existsById(Long id);
}
