package com.brunofontenele.ems.review.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import com.brunofontenele.ems.exam.domain.Exam;
import com.brunofontenele.ems.person.domain.Person;
import com.brunofontenele.ems.review.domain.Review;

@Repository
@Transactional
public interface ReviewRepository extends JpaRepository<Review, Long> {

	Optional<Review> findById(Long id);

	List<Review> findByQuestionExamStudentEmail(String studentEmail);

	@Query("SELECT r FROM Review r WHERE r.assignedTeacher.email = :teacherEmail")
    List<Review> findByTeacherEmail(@Param("teacherEmail") String teacherEmail);

	List<Review> findByQuestion_Exam_Student(Person student);

	List<Review> findByQuestionExamIdAndStatus(Long examId, Review.ReviewStatus status);

	List<Review> findByAssignedTeacher(Person teacher);

	List<Review> findByQuestion_Exam(Exam exam);

    long countByAssignedTeacher(Person teacher);

	boolean existsById(Long id);

	boolean existsByQuestionId(Long questionId);

	boolean existsByAssignedTeacherEmailAndQuestionId(String teacherEmail, Long questionId);
	
}
