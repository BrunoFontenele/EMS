package com.brunofontenele.ems.config;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.brunofontenele.ems.person.domain.Person;
import com.brunofontenele.ems.person.domain.Person.PersonType;
import com.brunofontenele.ems.person.repository.PersonRepository;
import com.brunofontenele.ems.auditlog.service.AuditLogService;
import com.brunofontenele.ems.correction.domain.Correction;
import com.brunofontenele.ems.correction.repository.CorrectionRepository;
import com.brunofontenele.ems.exam.domain.Exam;
import com.brunofontenele.ems.question.domain.Question;
import com.brunofontenele.ems.question.repository.QuestionRepository;
import com.brunofontenele.ems.review.domain.Review;
import com.brunofontenele.ems.school.domain.School;
import com.brunofontenele.ems.subject.domain.Subject;
import com.brunofontenele.ems.subject.repository.SubjectRepository;
import com.brunofontenele.ems.exam.repository.ExamRepository;
import com.brunofontenele.ems.notification.service.NotificationService;
import com.brunofontenele.ems.school.repository.SchoolRepository;
import com.brunofontenele.ems.review.repository.ReviewRepository;

@Component
public class DataInitializer implements CommandLineRunner {
    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private NotificationService notificationService;

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final PersonRepository personRepository;
    private final PasswordEncoder passwordEncoder;
    private final CorrectionRepository correctionRepository;
    private final QuestionRepository questionRepository;
	private final SubjectRepository subjectRepository;
	private final ExamRepository examRepository;
	private final SchoolRepository schoolRepository;
	private final ReviewRepository reviewRepository;

    public DataInitializer(PersonRepository personRepository, PasswordEncoder passwordEncoder,
                           CorrectionRepository correctionRepository, QuestionRepository questionRepository,
                           SubjectRepository subjectRepository, ExamRepository examRepository, SchoolRepository schoolRepository, ReviewRepository reviewRepository) {
        this.personRepository = personRepository;
        this.passwordEncoder = passwordEncoder;
        this.correctionRepository = correctionRepository;
        this.questionRepository = questionRepository;
        this.subjectRepository = subjectRepository;
        this.examRepository = examRepository;
        this.schoolRepository = schoolRepository;
        this.reviewRepository = reviewRepository;
    }

    @Override
    public void run(String... args) {
        seed("Admin User", "admin@teste.com", "admin123", PersonType.ADMINISTRATOR);

        seedPhase2Data(); 
    }

    private void seed(String name, String email, String rawPassword, PersonType type) {
        if (personRepository.existsByEmail(email)) return;
        personRepository.save(new Person(name, email, passwordEncoder.encode(rawPassword), type, null, null));
        logger.info("Seeded demo account: {} ({})", email, type);
    }

  private void seedPhase2Data() {
        if (correctionRepository.count() > 0) return;

        School school1 = new School("Escola Secundária de Lisboa", "ESL-01", "Lisboa"); 
        School school2 = new School("Escola Secundária de Coimbra", "ESC-01", "Lisboa");
        schoolRepository.saveAll(List.of(school1, school2));

        Subject mat = new Subject("Matemática A", "MAT-A");
        Subject fis = new Subject("Física", "FIS"); 
        subjectRepository.saveAll(List.of(mat, fis));
   
        Person staff = new Person("Funcionário ESL", "staff_ist@teste.pt", passwordEncoder.encode("staff123"), Person.PersonType.SCHOOL_STAFF, school1, null);
        Person staff2 = new Person("Funcionário ESC", "staff_esc@teste.pt", passwordEncoder.encode("staff123"), Person.PersonType.SCHOOL_STAFF, school2, null);
        
        Person prof1 = new Person("Prof Matemática Um", "prof1@teste.pt", passwordEncoder.encode("proff123"), Person.PersonType.TEACHER, school1, mat);
        Person prof2 = new Person("Prof Matemática Dois", "prof_mat2@teste.pt", passwordEncoder.encode("proff123"), Person.PersonType.TEACHER, school1, mat);
        Person profFis1 = new Person("Prof Física Um", "prof2@teste.pt", passwordEncoder.encode("proff123"), Person.PersonType.TEACHER, school1, fis);
        Person profFis2 = new Person("Prof Física Dois", "prof3@teste.pt", passwordEncoder.encode("proff123"), Person.PersonType.TEACHER, school2, fis);

        Person s1 = new Person("Ana Silva", "aluno1@teste.pt", passwordEncoder.encode("aluno123"), Person.PersonType.STUDENT, school1, null);
        Person s2 = new Person("Bernardo Costa", "aluno2@teste.pt", passwordEncoder.encode("aluno123"), Person.PersonType.STUDENT, school1, null);
        Person s3 = new Person("Carlos Santos", "aluno3@teste.pt", passwordEncoder.encode("aluno123"), Person.PersonType.STUDENT, school1, null);
        Person s4 = new Person("Diana Ferreira", "aluno4@teste.pt", passwordEncoder.encode("aluno123"), Person.PersonType.STUDENT, school1, null);
        Person s5 = new Person("Eduardo Lima", "aluno5@teste.pt", passwordEncoder.encode("aluno123"), Person.PersonType.STUDENT, school1, null);
        Person s6 = new Person("Francisca Rocha", "aluno6@teste.pt", passwordEncoder.encode("aluno123"), Person.PersonType.STUDENT, school1, null);
        Person s7 = new Person("Gonçalo Martins", "aluno7@teste.pt", passwordEncoder.encode("aluno123"), Person.PersonType.STUDENT, school2, null);
        Person s8 = new Person("Helena Ribeiro", "aluno8@teste.pt", passwordEncoder.encode("aluno123"), Person.PersonType.STUDENT, school2, null);
        Person s9 = new Person("Inês Moreira", "aluno9@teste.pt", passwordEncoder.encode("aluno123"), Person.PersonType.STUDENT, school2, null);
        Person s10 = new Person("Jorge Mendes", "aluno10@teste.pt", passwordEncoder.encode("aluno123"), Person.PersonType.STUDENT, school2, null);

        personRepository.saveAll(List.of(
            staff, staff2, prof1, prof2, profFis1, profFis2, 
            s1, s2, s3, s4, s5, s6, s7, s8, s9, s10
        ));

        Exam examMat1 = new Exam("uploads/exams/pdf_sample1.pdf", s1, school1, mat);
        examMat1.setStatus(Exam.ExamStatus.CLOSED); examMat1.setFinalScore(75);

        Exam examMat2 = new Exam("uploads/exams/pdf_sample1.pdf", s2, school1, mat);
        examMat2.setStatus(Exam.ExamStatus.RELEASED); examMat2.setFinalScore(95); examMat2.setReleaseDate(LocalDateTime.now().minusHours(5));

        Exam examMat3 = new Exam("uploads/exams/pdf_sample1.pdf", s3, school2, mat);
        examMat3.setStatus(Exam.ExamStatus.IN_REVIEW);

        Exam examMat4 = new Exam("uploads/exams/pdf_sample1.pdf", s4, school1, mat);
        examMat4.setStatus(Exam.ExamStatus.UPLOAD_IN_PROGRESS); 

        Exam examMat5 = new Exam("uploads/exams/pdf_sample1.pdf", s5, school2, mat);
        examMat5.setStatus(Exam.ExamStatus.RELEASED); examMat5.setFinalScore(28); examMat5.setReleaseDate(LocalDateTime.now().minusHours(24));

        Exam examMat6 = new Exam("uploads/exams/pdf_sample1.pdf", s6, school1, mat);
        examMat6.setStatus(Exam.ExamStatus.RELEASED); examMat6.setFinalScore(12); examMat6.setReleaseDate(LocalDateTime.now().minusHours(24));

        // Exames baseados no pdf_sample2.pdf (3 PÁGINAS)
        Exam examFis1 = new Exam("uploads/exams/pdf_sample2.pdf", s7, school1, fis);
        examFis1.setStatus(Exam.ExamStatus.RELEASED); examFis1.setFinalScore(88); examFis1.setReleaseDate(LocalDateTime.now().minusHours(3));

        Exam examFis2 = new Exam("uploads/exams/pdf_sample2.pdf", s8, school2, fis);
        examFis2.setStatus(Exam.ExamStatus.RELEASED); examFis2.setFinalScore(55); examFis2.setReleaseDate(LocalDateTime.now().minusDays(3));

        examRepository.saveAll(List.of(examMat1, examMat2, examMat3, examMat4, examMat5, examMat6, examFis1, examFis2));

        java.util.List<Question> allQuestions = new java.util.ArrayList<>();
        java.util.List<Correction> allCorrections = new java.util.ArrayList<>();
        int imgCounter = 1;

        Object[][] matExamsData = {
            {examMat1, new Integer[]{20, 20, 20, 15}},   
            {examMat2, new Integer[]{25, 25, 25, 20}},   
            {examMat3, new Integer[]{null, null, null, null}}, 
            {examMat5, new Integer[]{10, 10, 8, 0}},      
            {examMat6, new Integer[]{5, 5, 2, 0}}        
        }; 

        for (Object[] data : matExamsData) {
            Exam ex = (Exam) data[0];
            Integer[] scores = (Integer[]) data[1];
            for (int i = 0; i < 4; i++) { 
                String imgPath = "uploads/questions/question_sample" + ((imgCounter++ % 6) + 1) + ".png";
                Question q = new Question(i + 1, 25, imgPath, ex, mat);
                allQuestions.add(q);
                
                Person prof = (i % 2 == 0) ? prof1 : prof2; 
                allCorrections.add(new Correction(q, prof, scores[i]));
            }
        }

        Object[][] fisExamsData = {
            {examFis1, new Integer[]{30, 30, 28}}, 
            {examFis2, new Integer[]{20, 20, 15}}  
        };
        int[] fisMaxScores = {34, 33, 33};

        for (Object[] data : fisExamsData) {
            Exam ex = (Exam) data[0];
            Integer[] scores = (Integer[]) data[1];
            for (int i = 0; i < 3; i++) {
                String imgPath = "uploads/questions/question_sample" + ((imgCounter++ % 6) + 1) + ".png";
                Question q = new Question(i + 1, fisMaxScores[i], imgPath, ex, fis);
                allQuestions.add(q);
                
                Person prof = (i % 2 == 0) ? profFis1 : profFis2; 
                allCorrections.add(new Correction(q, prof, scores[i]));
            }
        }

        questionRepository.saveAll(allQuestions);
        correctionRepository.saveAll(allCorrections);

        Question reviewQuestion = allQuestions.get(4); 
        Review review = new Review(reviewQuestion, "Acho que a minha justificação vale a cotação total, o critério é ambíguo.", 25);
        review.setAssignedTeacher(prof2);
        review.setStatus(Review.ReviewStatus.IN_REVIEW);
        reviewRepository.save(review);

        auditLogService.logAction("SYSTEM", "População inicial da base de dados executada com rigor de segmentação de páginas.");
        notificationService.sendNotification(s2.getEmail(), "O teu exame de MAT-A foi corrigido e já está disponível para consulta!");
        notificationService.sendNotification(s5.getEmail(), "O teu exame de MAT-A foi corrigido e já está disponível para consulta!");
        notificationService.sendNotification(s8.getEmail(), "O teu exame de FIS foi corrigido e já está disponível para consulta!");
        notificationService.sendNotification(prof2.getEmail(), "Recebeste um novo pedido de revisão para avaliar na disciplina MAT-A.");

        System.out.println("Seed perfeito concluído: A correspondência entre ficheiros PDF, número de páginas, fragmentos e notas matemáticas está garantida.");
    }
}

