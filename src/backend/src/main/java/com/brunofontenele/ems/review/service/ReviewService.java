package com.brunofontenele.ems.review.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Comparator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.brunofontenele.ems.auditlog.service.AuditLogService;
import com.brunofontenele.ems.notification.service.NotificationService;
import com.brunofontenele.ems.correction.domain.Correction;
import com.brunofontenele.ems.exam.domain.Exam;
import com.brunofontenele.ems.exceptions.EmsException;
import com.brunofontenele.ems.exceptions.ErrorMessage;
import com.brunofontenele.ems.person.domain.Person;
import com.brunofontenele.ems.review.domain.Review;
import com.brunofontenele.ems.review.repository.ReviewRepository;
import com.brunofontenele.ems.review.dto.SubmitReviewDto;
import com.brunofontenele.ems.exam.repository.ExamRepository;
import com.brunofontenele.ems.question.domain.Question;
import com.brunofontenele.ems.question.repository.QuestionRepository;
import com.brunofontenele.ems.correction.repository.CorrectionRepository;
import com.brunofontenele.ems.review.dto.ReviewDto;
import com.brunofontenele.ems.person.repository.PersonRepository;

@Service
@Transactional
public class ReviewService {
	@Autowired
	private ReviewRepository reviewRepository;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private CorrectionRepository correctionRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private NotificationService notificationService;

    //------------------------------------------------------------------

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
            return authentication.getName();
        }
        return "SYSTEM";
    }

	private Review fetchReviewOrThrow(long id) {
		return reviewRepository.findById(id)
				.orElseThrow(() -> new EmsException(ErrorMessage.NO_SUCH_REVIEW, Long.toString(id)));
	}

    private void verifyReviewAccess(Person user, Review review) {
        if (user.getType() == Person.PersonType.ADMINISTRATOR) {
            return;
        } 
        else if (user.getType() == Person.PersonType.TEACHER) { 
            if (review.getAssignedTeacher() == null || 
                !review.getAssignedTeacher().getId().equals(user.getId())) {
                throw new EmsException(ErrorMessage.ACCESS_DENIED, user.getEmail());
            }
        } 
        else if (user.getType() == Person.PersonType.STUDENT) {
            Person student = review.getQuestion().getExam().getStudent();
            if (student == null || !student.getId().equals(user.getId())) {
                throw new EmsException(ErrorMessage.ACCESS_DENIED, user.getEmail());
            }
        } 
        else {
            throw new EmsException(ErrorMessage.ACCESS_DENIED, user.getEmail());
        }
    }

    @Transactional(readOnly = true)
    public ReviewDto getReview(long id, String userEmail) {
        Person user = personRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EmsException(ErrorMessage.NO_SUCH_PERSON, userEmail));

        Review review = fetchReviewOrThrow(id);

        verifyReviewAccess(user, review);

        ReviewDto dto = new ReviewDto(review);

        if(Person.PersonType.STUDENT.equals(user.getType())) {
            dto = new ReviewDto(
                review.getId(),
                review.getQuestion().getId(),
                review.getStudentJustification(),
                review.getOldScore(),
                review.getNewScore(),
                review.getCreatedAt(),
                review.getStatus(),
                null // Hide assigned teacher from student
            );
        }

        return dto;
    }

    @Transactional(readOnly = true)
    public List<ReviewDto> getReviews(String userEmail) {
        Person user = personRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EmsException(ErrorMessage.NO_SUCH_PERSON, userEmail));

        if (user.getType() == Person.PersonType.ADMINISTRATOR) {
            return reviewRepository.findAll().stream()
                    .map(ReviewDto::new)
                    .toList();
        } 
        else if (user.getType() == Person.PersonType.TEACHER) {
            return reviewRepository.findByAssignedTeacher(user).stream()
                    .map(ReviewDto::new)
                    .toList();
        } 
        else if (user.getType() == Person.PersonType.STUDENT) {
            return reviewRepository.findByQuestion_Exam_Student(user).stream()
                                .map(review -> new ReviewDto(
                                    review.getId(),
                                    review.getQuestion().getId(),
                                    review.getStudentJustification(),
                                    review.getOldScore(),
                                    review.getNewScore(),
                                    review.getCreatedAt(),
                                    review.getStatus(),
                                    null // Hide assigned teacher from student
                                ))
                                .toList();
                    }
        else {
            throw new EmsException(ErrorMessage.ACCESS_DENIED, userEmail);
        }
    }

    @Transactional
    public ReviewDto createReview(long examId, String studentEmail, SubmitReviewDto reviewDto) {
        Exam exam = examRepository.findById(examId)
            .orElseThrow(() -> new EmsException(ErrorMessage.NO_SUCH_EXAM, Long.toString(examId)));

        if (!exam.getStudent().getEmail().equals(studentEmail)) {
            throw new EmsException(ErrorMessage.ACCESS_DENIED, studentEmail);
        }

        if (exam.getStatus() != Exam.ExamStatus.RELEASED) {
            throw new EmsException(ErrorMessage.EXAM_NOT_RELEASED, Long.toString(examId));
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = exam.getReleaseDate().plusHours(48);

        if (now.isAfter(deadline)) {
            throw new EmsException(ErrorMessage.REVIEW_DEADLINE_PAST, Long.toString(examId));
        }

        Question question = questionRepository.findById(reviewDto.questionId())
            .orElseThrow(() -> new EmsException(ErrorMessage.NO_SUCH_QUESTION, Long.toString(reviewDto.questionId())));

        if (!question.getExam().getId().equals(exam.getId())) {
            throw new EmsException(ErrorMessage.QUESTION_NOT_ASSIGNED_TO_EXAM, Long.toString(reviewDto.questionId()));
        }

        if (reviewRepository.existsByQuestionId(question.getId())) {
            throw new EmsException(ErrorMessage.REVIEW_ALREADY_EXISTS, Long.toString(reviewDto.questionId()));
        }

        Correction correction = correctionRepository.findById(question.getId())
            .orElseThrow(() -> new EmsException(ErrorMessage.NO_SUCH_CORRECTION, Long.toString(reviewDto.questionId())));

        Person originalTeacher = correction.getProfessor();

        List<Person> teachers = personRepository.findBySubjectAndType(exam.getSubject(), Person.PersonType.TEACHER);

        Person chosenTeacher = teachers.stream()
            .filter(t -> !t.getId().equals(originalTeacher.getId()))
            .min(Comparator.comparingLong(t -> reviewRepository.countByAssignedTeacher(t)))
            .orElseThrow(() -> new EmsException(ErrorMessage.NO_AVAILABLE_TEACHER, Long.toString(question.getId())));

        Review review = new Review(question, reviewDto.justification(), correction.getScore());
        review.setAssignedTeacher(chosenTeacher);
        review.setStatus(Review.ReviewStatus.IN_REVIEW);

        Review savedReview = reviewRepository.save(review);
        
        auditLogService.logAction(
            studentEmail,
            String.format("Aluno submeteu pedido de revisão para a Pergunta ID %d (Exame ID %d). Revisor atribuído: %s", 
                question.getId(), exam.getId(), chosenTeacher.getEmail())
        );

        notificationService.sendNotification(
            chosenTeacher.getEmail(),
            String.format("Recebeste um pedido de revisão para avaliar na questão %d da disciplina %s.", 
                question.getQuestionNumber(), exam.getSubject().getCode())
        );

        return new ReviewDto(
            savedReview.getId(),
            savedReview.getQuestion().getId(),
            savedReview.getStudentJustification(),
            savedReview.getOldScore(),
            savedReview.getNewScore(),
            savedReview.getCreatedAt(),
            savedReview.getStatus(),
            null
        );
    }

    @Transactional
    public ReviewDto updateReview(ReviewDto dto, String userEmail) {
        if (dto.id() == null) {
            throw new EmsException(ErrorMessage.NO_SUCH_REVIEW, "null");
        }

        Review review = fetchReviewOrThrow(dto.id());

        Person user = personRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EmsException(ErrorMessage.NO_SUCH_PERSON, userEmail));

        if(user.getType() != Person.PersonType.ADMINISTRATOR) {
            throw new EmsException(ErrorMessage.ACCESS_DENIED, userEmail);
        }

        if (dto.assignedTeacherEmail() != null) {
            Person teacher = personRepository.findByEmail(dto.assignedTeacherEmail())
                    .orElseThrow(() -> new EmsException(ErrorMessage.NO_SUCH_PERSON, dto.assignedTeacherEmail()));
            review.setAssignedTeacher(teacher);

            notificationService.sendNotification(
                teacher.getEmail(),
                String.format("Foi-te atribuída a revisão da questão %d da disciplina %s.", 
                    review.getQuestion().getQuestionNumber(), review.getQuestion().getSubject().getCode())
            );
        }

        if (dto.studentJustification() != null) {
            review.setStudentJustification(dto.studentJustification());
        }

        if (dto.reviewStatus() != null) {
            review.setStatus(dto.reviewStatus());
        }

        if (dto.newScore() != null) {
            int maxScore = review.getQuestion().getMaxScore();
            if (dto.newScore() < 0 || dto.newScore() > maxScore) {
                throw new EmsException(ErrorMessage.INVALID_SCORE, Integer.toString(dto.newScore()));
            }
            review.setNewScore(dto.newScore());
        }

        Review saved = reviewRepository.save(review);

        auditLogService.logAction(
            userEmail,
            String.format("Administrador atualizou o pedido de revisão ID %d (Pergunta ID %d)", saved.getId(), saved.getQuestion().getId())
        );

        return new ReviewDto(saved);
    }

	@Transactional
	public void deleteReview(long id) {
		Review review = fetchReviewOrThrow(id); 

		reviewRepository.deleteById(id);

        auditLogService.logAction(
            getCurrentUserEmail(),
            String.format("Eliminou o pedido de revisão ID %d (Pergunta ID %d)", id, review.getQuestion().getId())
        );
	}

    @Transactional
    public ReviewDto reviewResponse(long reviewId, String userEmail, ReviewDto dto) {
        Person user = personRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EmsException(ErrorMessage.NO_SUCH_PERSON, userEmail));

        Review review = fetchReviewOrThrow(reviewId);

        if (user.getType() == Person.PersonType.STUDENT) {
            throw new EmsException(ErrorMessage.ACCESS_DENIED, userEmail);
        }

        verifyReviewAccess(user, review);

        if (review.getStatus() != Review.ReviewStatus.IN_REVIEW) {
            throw new EmsException(ErrorMessage.NOT_IN_REVIEW, Long.toString(reviewId));
        }

        if (dto.newScore() != null) {
            int maxScore = review.getQuestion().getMaxScore();
            if (dto.newScore() < 0 || dto.newScore() > maxScore) {
                throw new EmsException(ErrorMessage.INVALID_SCORE, String.valueOf(dto.newScore()), String.valueOf(maxScore));
            }
            review.setNewScore(dto.newScore());
        }

        review.setStatus(Review.ReviewStatus.REVIEWED);
        Review savedReview = reviewRepository.save(review);

        auditLogService.logAction(
            userEmail,
            String.format("Professor revisor concluiu a revisão ID %d para a Pergunta ID %d: nota alterada de %s para %s (Máx: %d)", 
                review.getId(), review.getQuestion().getId(), 
                review.getOldScore() != null ? review.getOldScore() : "Sem nota", 
                review.getNewScore(), review.getQuestion().getMaxScore())
        );

        notificationService.sendNotification(
            savedReview.getQuestion().getExam().getStudent().getEmail(),
            String.format("O teu pedido de revisão à questão %d foi concluído pelo professor revisor.", 
                savedReview.getQuestion().getQuestionNumber())
        );

        Exam exam = review.getQuestion().getExam();

        List<Review> examReviews = reviewRepository.findByQuestion_Exam(exam);

        boolean allReviewsCompleted = examReviews.stream()
                .allMatch(r -> r.getStatus() == Review.ReviewStatus.REVIEWED);

        if (allReviewsCompleted) {
            List<Correction> corrections = correctionRepository.findByQuestion_Exam(exam);

            int totalScore = 0;
            for (Correction c : corrections) {
                Review questionReview = examReviews.stream()
                        .filter(r -> r.getQuestion().getId().equals(c.getQuestion().getId()))
                        .findFirst()
                        .orElse(null);

                if (questionReview != null && questionReview.getNewScore() != null) {
                    totalScore += questionReview.getNewScore();
                } else if (c.getScore() != null) {
                    totalScore += c.getScore();
                }
            }

            exam.setFinalScore(totalScore);
            examRepository.save(exam);

            auditLogService.logAction(
                "SYSTEM",
                String.format("Todas as revisões do Exame ID %d foram concluídas. Nova nota final recalculada: %d", exam.getId(), totalScore)
            );

            notificationService.sendNotification(
                exam.getStudent().getEmail(),
                String.format("Todos os teus pedidos de revisão para o exame de %s foram finalizados. Nota final definitiva: %d", 
                    exam.getSubject().getCode(), totalScore)
            );
        }

        return new ReviewDto(savedReview);
    }
}