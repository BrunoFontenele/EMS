package com.brunofontenele.ems.question.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

import com.brunofontenele.ems.auditlog.service.AuditLogService;
import com.brunofontenele.ems.notification.service.NotificationService;
import com.brunofontenele.ems.exceptions.EmsException;
import com.brunofontenele.ems.exceptions.ErrorMessage;
import com.brunofontenele.ems.person.domain.Person;
import com.brunofontenele.ems.question.domain.Question;
import com.brunofontenele.ems.question.repository.QuestionRepository;
import com.brunofontenele.ems.question.dto.QuestionDto;
import com.brunofontenele.ems.exam.domain.Exam;
import com.brunofontenele.ems.exam.repository.ExamRepository;
import com.brunofontenele.ems.subject.domain.Subject;
import com.brunofontenele.ems.subject.repository.SubjectRepository;
import com.brunofontenele.ems.correction.repository.CorrectionRepository;
import com.brunofontenele.ems.person.repository.PersonRepository;
import com.brunofontenele.ems.review.repository.ReviewRepository;

import com.brunofontenele.ems.exam.service.ExamService;

@Service
@Transactional
public class QuestionService {
    @Autowired
    private PersonRepository personRepository;

	@Autowired
	private QuestionRepository questionRepository;

    @Autowired
    private ExamRepository examRepository;

	@Autowired
	private SubjectRepository subjectRepository;

    @Autowired
    private CorrectionRepository correctionRepository;

    @Autowired
    private ExamService examService;

    @Autowired
    private ReviewRepository reviewRepository;

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

	private Question fetchQuestionOrThrow(long id) {
		return questionRepository.findById(id)
				.orElseThrow(() -> new EmsException(ErrorMessage.NO_SUCH_QUESTION, Long.toString(id)));
	}

    @Transactional(readOnly = true)
    public List<QuestionDto> getQuestions(String userEmail) {
        Person user = personRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EmsException(ErrorMessage.NO_SUCH_PERSON, userEmail));

        if (user.getType() == Person.PersonType.ADMINISTRATOR) {
            return questionRepository.findAll().stream()
                    .map(QuestionDto::new)
                    .toList();
        } 
        else if (user.getType() == Person.PersonType.SCHOOL_STAFF) {
            if (user.getSchool() == null) {
                return List.of();
            }
            return questionRepository.findByExam_School(user.getSchool()).stream()
                    .map(QuestionDto::new)
                    .toList();
        }
        else if (user.getType() == Person.PersonType.STUDENT) {
            return questionRepository.findByExam_Student(user).stream()
                    .map(QuestionDto::new)
                    .toList();
        }
        else {
            throw new EmsException(ErrorMessage.ACCESS_DENIED, userEmail);
        }
    }
    
    @Transactional(readOnly = true)
    public QuestionDto getQuestion(long id, String userEmail) {
        Person user = personRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EmsException(ErrorMessage.NO_SUCH_PERSON, userEmail));

        Question question = fetchQuestionOrThrow(id);
        Exam exam = question.getExam();

        examService.verifyExamAccess(user, exam);

        return new QuestionDto(question);
    }

    @Transactional(readOnly = true)
    public byte[] getQuestionImage(Long questionId, String userEmail) {
        Person user = personRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EmsException(ErrorMessage.NO_SUCH_PERSON, userEmail));

        Question question = questionRepository.findById(questionId)
            .orElseThrow(() -> new EmsException(ErrorMessage.NO_SUCH_QUESTION, Long.toString(questionId)));
        Exam exam = question.getExam();

        if (user.getType() == Person.PersonType.TEACHER) {
            boolean isCorrectionAssigned = correctionRepository.existsByProfessorEmailAndQuestionId(userEmail, questionId);
            boolean isReviewAssigned = reviewRepository.existsByAssignedTeacherEmailAndQuestionId(userEmail, questionId);
            
            if (!isCorrectionAssigned && !isReviewAssigned) {
                throw new EmsException(ErrorMessage.PROFESSOR_NOT_ASSIGNED_TO_QUESTION, userEmail, Long.toString(questionId));
            }
        } 
        else if (user.getType() == Person.PersonType.STUDENT) {
            if (exam.getStatus() != Exam.ExamStatus.RELEASED && exam.getStatus() != Exam.ExamStatus.CLOSED) {
                throw new EmsException(ErrorMessage.EXAM_NOT_RELEASED, Long.toString(exam.getId()));
            }
        } else {
            examService.verifyExamAccess(user, exam);
        }

        try {
            Path filePath = Paths.get(question.getFilePath());
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new EmsException(ErrorMessage.FAILED_TO_READ_FILE, question.getFilePath());
        }
    }
 
	@Transactional
	public QuestionDto createQuestion(QuestionDto questionDto, MultipartFile file) {
        if (file == null || file.isEmpty()){
            throw new EmsException(ErrorMessage.QUESTION_FILE_PATH_NOT_VALID);
        }
        
        String contentType = file.getContentType();

        if (contentType == null || (!contentType.equals("image/png") && !contentType.equals("image/jpeg"))) {
            throw new EmsException(ErrorMessage.INVALID_FILE_TYPE);
        }
        if (questionDto.questionNumber() == null || questionDto.questionNumber() <= 0) {
            throw new EmsException(ErrorMessage.QUESTION_NUMBER_NOT_VALID, Long.toString(questionDto.questionNumber()))  ;
        }
        if (questionDto.maxScore() == null || questionDto.maxScore() <= 0) {
            throw new EmsException(ErrorMessage.QUESTION_MAX_SCORE_NOT_VALID, Long.toString(questionDto.maxScore()));
        }
        if (questionDto.examId() == null || questionDto.examId() <= 0) {
            throw new EmsException(ErrorMessage.NO_SUCH_EXAM, Long.toString(questionDto.examId()));
        }
        if (questionDto.subjectCode() == null || questionDto.subjectCode().isBlank()) {
            throw new EmsException(ErrorMessage.NO_SUCH_SUBJECT, questionDto.subjectCode());
        }

        Exam exam = examRepository.findById(questionDto.examId()) 
                .orElseThrow(() -> new EmsException(ErrorMessage.NO_SUCH_EXAM, Long.toString(questionDto.examId())));

        if (exam.getStatus() != Exam.ExamStatus.UPLOAD_IN_PROGRESS) {
            throw new EmsException(ErrorMessage.EXAM_UPLOAD_CLOSED, Long.toString(questionDto.examId()));
        }

        Subject subject = subjectRepository.findByCode(questionDto.subjectCode()) 
                .orElseThrow(() -> new EmsException(ErrorMessage.NO_SUCH_SUBJECT, questionDto.subjectCode()));

        String filePath = saveFileToDisk(file);

        Question question = new Question(
                questionDto.questionNumber(),
                questionDto.maxScore(),
                filePath,
                exam,
                subject
        );
        Question saved = questionRepository.save(question);

        auditLogService.logAction(
            getCurrentUserEmail(),
            String.format("Segmentou a Pergunta %d (Cotação Máx: %d) para o exame ID %d (%s)", 
                saved.getQuestionNumber(), saved.getMaxScore(), exam.getId(), subject.getCode())
        );

		return new QuestionDto(saved);
	}

	@Transactional
	public QuestionDto updateQuestion(QuestionDto questionDto) { 
		Question question = fetchQuestionOrThrow(questionDto.id());
			
        if (questionDto.questionNumber() != null && questionDto.questionNumber() > 0) {
            question.setQuestionNumber(questionDto.questionNumber());
        }
        if (questionDto.maxScore() != null && questionDto.maxScore() > 0) {
            question.setMaxScore(questionDto.maxScore());
        }

		if (questionDto.examId() != null && questionDto.examId() > 0) {
            Exam exam = examRepository.findById(questionDto.examId())
                    .orElseThrow(() -> new EmsException(ErrorMessage.NO_SUCH_EXAM, questionDto.examId().toString()));
            question.setExam(exam);
        }

		if (questionDto.subjectCode() != null && !questionDto.subjectCode().isBlank()) {
            Subject subject = subjectRepository.findByCode(questionDto.subjectCode())
                    .orElseThrow(() -> new EmsException(ErrorMessage.NO_SUCH_SUBJECT, questionDto.subjectCode()));
            question.setSubject(subject);
    	}

        Question saved = questionRepository.save(question);

        auditLogService.logAction(
            getCurrentUserEmail(),
            String.format("Atualizou os parâmetros da Pergunta ID %d (Nº %d, Exame ID %d)", 
                saved.getId(), saved.getQuestionNumber(), saved.getExam().getId())
        );

		return new QuestionDto(saved);
    }

	@Transactional
	public void deleteQuestion(long id) {
		Question question = fetchQuestionOrThrow(id);
		deleteFileFromDisk(question.getFilePath());
		questionRepository.deleteById(id);

        auditLogService.logAction(
            getCurrentUserEmail(),
            String.format("Eliminou o fragmento da Pergunta ID %d (Exame ID %d)", id, question.getExam().getId())
        );
	}

    //file management methods

    public String saveFileToDisk(MultipartFile file) {
        try {
            String uploadDir = "uploads/questions/";
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) 
                Files.createDirectories(uploadPath);
            
            String originalFileName = file.getOriginalFilename();
            String uniqueFileName = UUID.randomUUID().toString() + "_" + originalFileName;

            Path filePath = uploadPath.resolve(uniqueFileName);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return filePath.toString();
        } catch (IOException e) {
            throw new EmsException(ErrorMessage.FAILED_TO_STORE_FILE);
        }
    }

    public void deleteFileFromDisk(String filePath) {
        try {
            Path path = Paths.get(filePath);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new EmsException(ErrorMessage.FAILED_TO_DELETE_FILE);
        }
    }

    public void updateQuestionFilePath(long questionId, String newFilePath) {
        Question question = fetchQuestionOrThrow(questionId);
        deleteFileFromDisk(question.getFilePath());
        question.setFilePath(newFilePath);
        questionRepository.save(question);

        auditLogService.logAction(
            getCurrentUserEmail(),
            String.format("Atualizou a imagem de recorte da Pergunta ID %d", questionId)
        );
    }
}