package com.brunofontenele.ems.exam.service;

import java.util.ArrayList;
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
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

import java.util.Objects;
import java.util.Comparator;

import com.brunofontenele.ems.auditlog.service.AuditLogService;
import com.brunofontenele.ems.notification.service.NotificationService;
import com.brunofontenele.ems.exceptions.EmsException;
import com.brunofontenele.ems.exceptions.ErrorMessage;
import com.brunofontenele.ems.correction.domain.Correction;
import com.brunofontenele.ems.exam.domain.Exam;
import com.brunofontenele.ems.exam.dto.ExamDto;
import com.brunofontenele.ems.exam.dto.FragmentScoreDto;
import com.brunofontenele.ems.exam.dto.StudentExamDetailsDto;
import com.brunofontenele.ems.exam.dto.StudentExamSummaryDto;
import com.brunofontenele.ems.exam.dto.StatisticsDto;
import com.brunofontenele.ems.exam.repository.ExamRepository;
import com.brunofontenele.ems.person.domain.Person;
import com.brunofontenele.ems.person.repository.PersonRepository;
import com.brunofontenele.ems.school.domain.School;
import com.brunofontenele.ems.school.repository.SchoolRepository;
import com.brunofontenele.ems.subject.domain.Subject;
import com.brunofontenele.ems.subject.repository.SubjectRepository;
import com.brunofontenele.ems.question.domain.Question;
import com.brunofontenele.ems.question.repository.QuestionRepository;
import com.brunofontenele.ems.correction.repository.CorrectionRepository;
import com.brunofontenele.ems.exam.dto.StudentsResultsDto;

@Service
@Transactional
public class ExamService {

	@Autowired
	private ExamRepository examRepository;

    @Autowired
    private PersonRepository personRepository;

	@Autowired
	private SchoolRepository schoolRepository;

	@Autowired
	private SubjectRepository subjectRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private CorrectionRepository correctionRepository;

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

	private Exam fetchExamOrThrow(long id) {
		return examRepository.findById(id)
				.orElseThrow(() -> new EmsException(ErrorMessage.NO_SUCH_EXAM, Long.toString(id)));
	}

    public void verifyExamAccess(Person user, Exam exam) {
        if (user.getType() == Person.PersonType.ADMINISTRATOR) {
            return; 
        } 
        else if (user.getType() == Person.PersonType.SCHOOL_STAFF) {
            if (user.getSchool() == null || exam.getSchool() == null || 
                !user.getSchool().getId().equals(exam.getSchool().getId())) {
                throw new EmsException(ErrorMessage.ACCESS_DENIED, user.getEmail());
            }
        } 
        else if (user.getType() == Person.PersonType.STUDENT) {
            if (exam.getStudent() == null || !exam.getStudent().getId().equals(user.getId())) {
                throw new EmsException(ErrorMessage.ACCESS_DENIED, user.getEmail());
            }
        } 
        else {
            throw new EmsException(ErrorMessage.ACCESS_DENIED, user.getEmail());
        }
    }

    @Transactional(readOnly = true)
    public List<ExamDto> getExams(String userEmail) {
        Person user = personRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EmsException(ErrorMessage.NO_SUCH_PERSON, userEmail));

        if (user.getType() == Person.PersonType.ADMINISTRATOR) {
            return examRepository.findAll().stream()
                    .map(ExamDto::new)
                    .toList();
        } 
        else if (user.getType() == Person.PersonType.SCHOOL_STAFF) {
            if (user.getSchool() == null) {
                return List.of();
            }
            
            return examRepository.findBySchool(user.getSchool()).stream()
                    .map(ExamDto::new)
                    .toList();
        } 
        else if (user.getType() == Person.PersonType.STUDENT) {
                    List<Exam.ExamStatus> visibleStatuses = List.of(
                        Exam.ExamStatus.CLOSED, 
                        Exam.ExamStatus.RELEASED
                    );
                    return examRepository.findByStudentAndStatusIn(user, visibleStatuses).stream()
                            .map(ExamDto::new)
                            .toList();
                }
        else {
            throw new EmsException(ErrorMessage.ACCESS_DENIED, userEmail);
        }
    }

    @Transactional(readOnly = true)
    public ExamDto getExam(long id, String userEmail) {
        Person user = personRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EmsException(ErrorMessage.NO_SUCH_PERSON, userEmail));

        Exam exam = fetchExamOrThrow(id);

        verifyExamAccess(user, exam);   

        return new ExamDto(exam);
    }

    @Transactional(readOnly = true)
    public byte[] getExamPdf(long examId, String userEmail) {
        Person user = personRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EmsException(ErrorMessage.NO_SUCH_PERSON, userEmail));

        Exam exam = fetchExamOrThrow(examId);

        if (user.getType() == Person.PersonType.STUDENT) {
             if (exam.getStatus() != Exam.ExamStatus.RELEASED && exam.getStatus() != Exam.ExamStatus.CLOSED) {
                throw new EmsException(ErrorMessage.EXAM_NOT_RELEASED, Long.toString(exam.getId()));
            }
        } else{
            verifyExamAccess(user, exam);
        }

        try {
            Path filePath = Paths.get(exam.getFilePath());
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new EmsException(ErrorMessage.FAILED_TO_READ_FILE, exam.getFilePath());
        }
    }

    @Transactional
    public ExamDto createExam(ExamDto examDto, MultipartFile file) {
        Person student = personRepository.findByEmail(examDto.studentEmail())
                .orElseThrow(() -> new EmsException(ErrorMessage.NO_SUCH_PERSON, examDto.studentEmail()));
        if (!student.isActive()) {
            throw new EmsException(ErrorMessage.INACTIVE_PERSON, examDto.studentEmail());
        }

        School school = schoolRepository.findByCode(examDto.schoolCode())
                .orElseThrow(() -> new EmsException(ErrorMessage.NO_SUCH_SCHOOL, examDto.schoolCode()));
        if (!school.isActive()) {
            throw new EmsException(ErrorMessage.INACTIVE_SCHOOL, examDto.schoolCode());
        }

        Subject subject = subjectRepository.findByCode(examDto.subjectCode()) 
                .orElseThrow(() -> new EmsException(ErrorMessage.NO_SUCH_SUBJECT, examDto.subjectCode()));
        if (!subject.isActive()) {
            throw new EmsException(ErrorMessage.INACTIVE_SUBJECT, examDto.subjectCode());
        }

        String contentType = file.getContentType();

        if (contentType == null || !contentType.equals("application/pdf")) {
            throw new EmsException(ErrorMessage.INVALID_FILE_TYPE);
        }

        String filePath = saveFileToDisk(file);

        Exam exam = new Exam(
                filePath,
                student,  
                school,   
                subject   
        );
        Exam saved = examRepository.save(exam);

        auditLogService.logAction(
            getCurrentUserEmail(),
            String.format("Carregou o exame ID %d para o aluno '%s' na disciplina '%s'", saved.getId(), student.getEmail(), subject.getCode())
        );

        notificationService.sendNotification(
            student.getEmail(),
            String.format("A tua prova digitalizada da disciplina %s foi submetida no sistema.", subject.getCode())
        );

        return new ExamDto(saved);
    }

	@Transactional
	public ExamDto updateExam(ExamDto examDto) { 
		Exam exam = fetchExamOrThrow(examDto.id());
			
        if (examDto.studentEmail() != null && !examDto.studentEmail().isBlank()) {
            Person student = personRepository.findByEmail(examDto.studentEmail())
                    .orElseThrow(() -> new EmsException(ErrorMessage.NO_SUCH_PERSON, examDto.studentEmail()));
            
            if (!student.isActive()) {
                throw new EmsException(ErrorMessage.INACTIVE_PERSON, examDto.studentEmail());
            }

            exam.setStudent(student);
        }

		if (examDto.schoolCode() != null && !examDto.schoolCode().isBlank()) {
            School school = schoolRepository.findByCode(examDto.schoolCode())
                    .orElseThrow(() -> new EmsException(ErrorMessage.NO_SUCH_SCHOOL, examDto.schoolCode()));

            if (!school.isActive()) {
                throw new EmsException(ErrorMessage.INACTIVE_SCHOOL, examDto.schoolCode());
            }

            exam.setSchool(school);
        }

		if (examDto.subjectCode() != null && !examDto.subjectCode().isBlank()) {
            Subject subject = subjectRepository.findByCode(examDto.subjectCode())
                    .orElseThrow(() -> new EmsException(ErrorMessage.NO_SUCH_SUBJECT, examDto.subjectCode()));

            if (!subject.isActive()) {
                throw new EmsException(ErrorMessage.INACTIVE_SUBJECT, examDto.subjectCode());
            }  

            exam.setSubject(subject);
    	}

        if (examDto.finalScore() != null) {
            exam.setFinalScore(examDto.finalScore());
        }

        if (examDto.viewRequested() != false) {
            exam.setViewRequested(true);
        }

        Exam saved = examRepository.save(exam);

        auditLogService.logAction(
            getCurrentUserEmail(),
            String.format("Atualizou os detalhes do exame ID %d", saved.getId())
        );

        return new ExamDto(saved);
    }

    @Transactional
    public ExamDto updateExamPdf(long id, MultipartFile file) {
        Exam exam = fetchExamOrThrow(id);

        if (file == null || file.isEmpty()) {
            throw new EmsException(ErrorMessage.FAILED_TO_STORE_FILE);
        }

        exam.setFilePath(saveFileToDisk(file));
        Exam saved = examRepository.save(exam);

        auditLogService.logAction(
            getCurrentUserEmail(),
            String.format("Atualizou o ficheiro PDF do exame ID %d", saved.getId())
        );

        return new ExamDto(saved);
    }

    // file handling methods

    public String saveFileToDisk(MultipartFile file) {
        try {
            String uploadDir = "uploads/exams/";
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

    @Transactional(readOnly = true)
    public StatisticsDto getSchoolStatisticsBySubject(String subjectCode, String staffEmail) {
        Person staff = personRepository.findByEmail(staffEmail)
                .orElseThrow(() -> new EmsException(ErrorMessage.NO_SUCH_PERSON, staffEmail));

        if (staff.getType() != Person.PersonType.SCHOOL_STAFF) {
            throw new EmsException(ErrorMessage.ACCESS_DENIED, staffEmail);
        }

        School school = staff.getSchool();
        if (school == null) {
            throw new EmsException(ErrorMessage.NO_SUCH_SCHOOL, staffEmail);
        }

        Subject subject = subjectRepository.findByCode(subjectCode)
                .orElseThrow(() -> new EmsException(ErrorMessage.NO_SUCH_SUBJECT, subjectCode));

        List<Exam> exams = examRepository.findBySchoolAndSubject(school, subject).stream()
                .filter(e -> e.getStatus() == Exam.ExamStatus.CLOSED || e.getStatus() == Exam.ExamStatus.RELEASED)
                .filter(e -> e.getFinalScore() != null)
                .toList();

        List<StudentsResultsDto> studentsResults = exams.stream()
                .map(exam -> new StudentsResultsDto(
                        exam.getId(),
                        exam.getStudent() != null ? exam.getStudent().getName() : "Sem Nome",
                        exam.getStudent() != null ? exam.getStudent().getEmail() : "-",
                        exam.getFinalScore()
                ))
                .toList();

        if (exams.isEmpty()) {
            return new StatisticsDto(
                    school.getName(),
                    school.getCode(),
                    subject.getCode(),
                    subject.getName(),
                    0,
                    0.0,
                    null,
                    null,
                    0,
                    0,
                    0.0,
                    List.of()
            );
        }

        long totalStudents = exams.size();
        
        double averageScore = exams.stream()
                .mapToInt(Exam::getFinalScore)
                .average()
                .orElse(0.0);
        
        averageScore = Math.round(averageScore * 100.0) / 100.0;

        Integer highestScore = exams.stream()
                .map(Exam::getFinalScore)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(null);

        Integer lowestScore = exams.stream()
                .map(Exam::getFinalScore)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(null);

        long approvedCount = exams.stream()
                .filter(e -> e.getFinalScore() >= 50)
                .count();

        long failedCount = totalStudents - approvedCount;
        
        double approvalRate = Math.round(((double) approvedCount / totalStudents * 100.0) * 100.0) / 100.0;

        return new StatisticsDto(
                school.getName(),
                school.getCode(),
                subject.getCode(),
                subject.getName(),
                totalStudents,
                averageScore,
                highestScore,
                lowestScore,
                approvedCount,
                failedCount,
                approvalRate,
                studentsResults
        );
    }

    @Transactional
    public void distributeExams(String subjectCode) {   
        if (subjectCode == null || subjectCode.isBlank() || !subjectRepository.existsByCode(subjectCode)) {
            throw new EmsException(ErrorMessage.NO_SUCH_SUBJECT, subjectCode);
        }

        List<Exam> examsToDistribute = examRepository.findBySubjectCodeAndStatus(
            subjectCode, Exam.ExamStatus.UPLOAD_IN_PROGRESS);

        if (examsToDistribute.isEmpty()) {
            throw new EmsException(ErrorMessage.NO_EXAMS_TO_DISTRIBUTE, subjectCode);
        }

        List<Person> teachers = personRepository.findBySubjectCode(subjectCode);

        if (teachers.isEmpty()) {
            throw new EmsException(ErrorMessage.NO_PROFESSORS_ASSIGNED_TO_SUBJECT, subjectCode);
        }
        
        List<Long> examIds = examsToDistribute.stream().map(Exam::getId).toList();
        List<Question> allQuestions = questionRepository.findByExamIdIn(examIds);

        if (allQuestions.isEmpty()) {
            throw new EmsException(ErrorMessage.NO_QUESTIONS_TO_DISTRIBUTE, subjectCode);
        }

        List<Correction> correctionsToSave = new ArrayList<>();
        int teacherIndex = 0;

        for (Question question : allQuestions) {
            Person assignedTeacher = teachers.get(teacherIndex % teachers.size());
            correctionsToSave.add(new Correction(question, assignedTeacher, null));
            teacherIndex++;
        }

        correctionRepository.saveAll(correctionsToSave);

        examsToDistribute.forEach(exam -> exam.setStatus(Exam.ExamStatus.IN_REVIEW));

        auditLogService.logAction(
            getCurrentUserEmail(),
            String.format("Iniciou a distribuição de %d questões para avaliação na disciplina '%s' entre %d professores", 
                allQuestions.size(), subjectCode, teachers.size())
        );

        for (Person teacher : teachers) {
            notificationService.sendNotification(
                teacher.getEmail(),
                String.format("Foram atribuídas novas questões da disciplina %s para correção.", subjectCode)
            );
        }
    }

    @Transactional
    public void requestExamView(Long examId, String studentEmail) {
        Exam exam = examRepository.findById(examId)
            .orElseThrow(() -> new EmsException(ErrorMessage.NO_SUCH_EXAM, Long.toString(examId)));

        if (!exam.getStudent().getEmail().equals(studentEmail)) {
            throw new EmsException(ErrorMessage.NO_SUCH_PERSON, studentEmail);
        }

        if (exam.getStatus() != Exam.ExamStatus.CLOSED) {
            throw new EmsException(ErrorMessage.EXAM_VIEWING_NOT_ALLOWED, Long.toString(examId));
        }

        exam.setViewRequested(true);
        examRepository.save(exam);

        auditLogService.logAction(
            studentEmail,
            String.format("O Aluno submeteu um pedido de consulta para o exame ID %d", examId)
        );

        notificationService.sendNotification(
            studentEmail,
            String.format("O teu pedido de consulta para a prova de %s foi submetido com sucesso.", exam.getSubject().getCode())
        );
    }

    @Transactional
    public void releaseExam(Long examId, String staffEmail) {
        Person staff = personRepository.findByEmail(staffEmail)
            .orElseThrow(() -> new EmsException(ErrorMessage.NO_SUCH_PERSON, staffEmail));

        Exam exam = examRepository.findById(examId)
            .orElseThrow(() -> new EmsException(ErrorMessage.NO_SUCH_EXAM, Long.toString(examId)));

        if (staff.getType() != Person.PersonType.ADMINISTRATOR) {
            
            if (staff.getSchool() == null) {
                throw new EmsException(ErrorMessage.NO_SUCH_SCHOOL, null);
            }

            if (!exam.getSchool().getId().equals(staff.getSchool().getId())) {
                throw new EmsException(ErrorMessage.ACCESS_DENIED, staffEmail);
            }
        }

        exam.setStatus(Exam.ExamStatus.RELEASED);
        exam.setReleaseDate(java.time.LocalDateTime.now());
        examRepository.save(exam);

        auditLogService.logAction(
            staffEmail,
            String.format("Disponibilizou a prova do exame ID %d para o aluno '%s'", exam.getId(), exam.getStudent().getEmail())
        );

        notificationService.sendNotification(
            exam.getStudent().getEmail(),
            String.format("A tua prova de %s já se encontra corrigida e disponível para consulta.", exam.getSubject().getCode())
        );
    }

    @Transactional
    public void bulkReleaseExams(String subjectCode, String staffEmail) {
        Person staff = personRepository.findByEmail(staffEmail)
            .orElseThrow(() -> new EmsException(ErrorMessage.NO_SUCH_PERSON, staffEmail));

        List<Exam.ExamStatus> pendingStatuses = List.of(
            Exam.ExamStatus.UPLOAD_IN_PROGRESS, 
            Exam.ExamStatus.IN_REVIEW
        );

        List<Exam> examsToRelease;

        if (staff.getType() == Person.PersonType.ADMINISTRATOR) {
            if (examRepository.existsBySubjectCodeAndStatusIn(subjectCode, pendingStatuses)) {
                throw new EmsException(ErrorMessage.PENDING_EXAMS_EXIST, subjectCode);
            }

            examsToRelease = examRepository.findBySubjectCodeAndStatus(subjectCode, Exam.ExamStatus.CLOSED);
        } else {
            if (staff.getSchool() == null) {
                throw new EmsException(ErrorMessage.NO_SUCH_SCHOOL, staffEmail);
            }

            Long schoolId = staff.getSchool().getId();

            if (examRepository.existsBySchoolIdAndSubjectCodeAndStatusIn(
                    schoolId, subjectCode, pendingStatuses)) {
                throw new EmsException(ErrorMessage.PENDING_EXAMS_EXIST, subjectCode);
            }

            examsToRelease = examRepository.findBySchoolIdAndSubjectCodeAndStatus(
                    schoolId, subjectCode, Exam.ExamStatus.CLOSED
            );
        }

        if (examsToRelease.isEmpty()) {
            throw new EmsException(ErrorMessage.NO_CLOSED_EXAMS, subjectCode);
        }

        for (Exam exam : examsToRelease) {
            exam.setStatus(Exam.ExamStatus.RELEASED);
            exam.setReleaseDate(java.time.LocalDateTime.now());

            notificationService.sendNotification(
                exam.getStudent().getEmail(),
                String.format("A tua prova de %s foi corrigida e está disponível para consulta!", exam.getSubject().getCode())
            );
        }

        examRepository.saveAll(examsToRelease);

        auditLogService.logAction(
            staffEmail,
            String.format("Executou a disponibilização em lote (Bulk Release) de %d exames da disciplina '%s'", 
                examsToRelease.size(), subjectCode)
        );
    }

    @Transactional
    public void submitRevisionRequest(Long examId, String studentEmail) {
        
        Exam exam = examRepository.findById(examId)
            .orElseThrow(() -> new EmsException(ErrorMessage.NO_SUCH_EXAM, Long.toString(examId)));

        if (!exam.getStudent().getEmail().equals(studentEmail)) {
            throw new EmsException(ErrorMessage.ACCESS_DENIED, studentEmail);
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = exam.getReleaseDate().plusHours(48);

        if (now.isAfter(deadline)) {
            throw new EmsException(ErrorMessage.REVIEW_DEADLINE_PAST, Long.toString(examId));
        }

        auditLogService.logAction(
            studentEmail,
            String.format("Submeteu pedido formal de revisão para o exame ID %d", examId)
        );

        notificationService.sendNotification(
            studentEmail,
            String.format("O teu pedido de revisão para o exame de %s foi registado com sucesso.", exam.getSubject().getCode())
        );
    }

    @Transactional(readOnly = true)
    public List<StudentExamSummaryDto> getStudentExamsSummary(String studentEmail) {
        Person student = personRepository.findByEmail(studentEmail)
            .orElseThrow(() -> new EmsException(ErrorMessage.NO_SUCH_PERSON, studentEmail));

        List<Exam> exams = examRepository.findByStudentId(student.getId());
        
        return exams.stream()
            .filter(exam -> exam.getStatus() == Exam.ExamStatus.RELEASED)
            .map(exam -> new StudentExamSummaryDto(
                exam.getId(),
                exam.getSubject().getCode(),
                exam.getReleaseDate(),
                exam.getFinalScore()
            ))
            .toList();
    }

    @Transactional(readOnly = true)
    public StudentExamDetailsDto getStudentExamDetails(Long examId, String studentEmail) {
        Exam exam = examRepository.findById(examId)
            .orElseThrow(() -> new EmsException(ErrorMessage.NO_SUCH_EXAM, Long.toString(examId)));

        if (!exam.getStudent().getEmail().equals(studentEmail)) {
            throw new EmsException(ErrorMessage.ACCESS_DENIED, studentEmail);
        }

        if (exam.getStatus() != Exam.ExamStatus.RELEASED) {
            throw new EmsException(ErrorMessage.EXAM_NOT_RELEASED, Long.toString(examId));
        }

        List<Correction> corrections = correctionRepository.findByQuestionExamId(examId);

        List<FragmentScoreDto> fragments = corrections.stream()
            .map(correction -> {
                Long questionId = correction.getQuestion().getId();
                String imageUrl = "/questions/" + questionId + "/image"; 
                
                return new FragmentScoreDto(
                    questionId,
                    correction.getQuestion().getQuestionNumber(),
                    imageUrl,
                    correction.getQuestion().getMaxScore(),
                    correction.getScore()
                );
            })
            .toList();

        boolean isReviewPeriodOpen = exam.getReleaseDate() != null && 
                                 exam.getReleaseDate().plusHours(48).isAfter(LocalDateTime.now());

        String pdfUrl = "/exams/" + exam.getId() + "/pdf";

        return new StudentExamDetailsDto(
            exam.getId(),
            exam.getSubject().getCode(), 
            pdfUrl,
            exam.getFinalScore(),
            isReviewPeriodOpen,
            fragments
        );
    }
}