package com.brunofontenele.ems.exam.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import com.brunofontenele.ems.exam.domain.Exam;
import com.brunofontenele.ems.school.domain.School;

import com.brunofontenele.ems.person.domain.Person;

@Repository
@Transactional
public interface ExamRepository extends JpaRepository<Exam, Long> {

    List<Exam> findBySubjectCodeAndStatus(String subjectCode, Exam.ExamStatus status);

	Optional<Exam> findById(long id);

    List<Exam> findByStudentId(long studentId);

    List<Exam> findBySchoolIdAndSubjectCodeAndStatus(long schoolId, String subjectCode, Exam.ExamStatus status);

    List<Exam> findBySchoolAndSubject(School school, com.brunofontenele.ems.subject.domain.Subject subject);

	boolean existsById(long id);

    List<Exam> findByStatusAndReleaseDateBefore(Exam.ExamStatus status, LocalDateTime thresholdDate);

    List<Exam> findBySchool(School school);

    List<Exam> findByStudentAndStatusIn(Person student, List<Exam.ExamStatus> statuses);
    
    boolean existsBySchoolIdAndSubjectCodeAndStatusIn(long schoolId, String subjectCode, List<Exam.ExamStatus> statuses);

    boolean existsBySubjectCodeAndStatusIn(String subjectCode, List<Exam.ExamStatus> statuses);

}
