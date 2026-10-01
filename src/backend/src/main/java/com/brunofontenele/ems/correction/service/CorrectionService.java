package com.brunofontenele.ems.correction.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.brunofontenele.ems.auditlog.service.AuditLogService;
import com.brunofontenele.ems.notification.service.NotificationService;
import com.brunofontenele.ems.exceptions.EmsException;
import com.brunofontenele.ems.exceptions.ErrorMessage;
import com.brunofontenele.ems.person.domain.Person;
import com.brunofontenele.ems.person.repository.PersonRepository;
import com.brunofontenele.ems.question.domain.Question;
import com.brunofontenele.ems.question.repository.QuestionRepository;
import com.brunofontenele.ems.correction.domain.Correction;
import com.brunofontenele.ems.correction.dto.CorrectionDto;
import com.brunofontenele.ems.correction.repository.CorrectionRepository;
import com.brunofontenele.ems.exam.domain.Exam;
import com.brunofontenele.ems.exam.repository.ExamRepository;

@Service
@Transactional
public class CorrectionService {
    @Autowired
    private PersonRepository personRepository;

	@Autowired
	private QuestionRepository questionRepository;

	@Autowired
	private CorrectionRepository correctionRepository;

    @Autowired
    private ExamRepository examRepository;

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

	private Correction fetchCorrectionOrThrow(long id) {
		return correctionRepository.findById(id)
				.orElseThrow(() -> new EmsException(ErrorMessage.NO_SUCH_CORRECTION, Long.toString(id)));
	}

    private void verifyCorrectionAccess(Person user, Correction correction) {
        if (user.getType() == Person.PersonType.ADMINISTRATOR) {
            return; 
        } 
        else if (user.getType() == Person.PersonType.TEACHER) { 
            if (correction.getProfessor() == null || !correction.getProfessor().getId().equals(user.getId())) {
                throw new EmsException(ErrorMessage.ACCESS_DENIED, user.getEmail());
            }
        } 
        else {
            throw new EmsException(ErrorMessage.ACCESS_DENIED, user.getEmail());
        }
    }

    @Transactional(readOnly = true)
    public List<CorrectionDto> getCorrections(String userEmail) {
        Person user = personRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EmsException(ErrorMessage.NO_SUCH_PERSON, userEmail));

        if (user.getType() == Person.PersonType.ADMINISTRATOR) {
            return correctionRepository.findAll().stream()
                    .map(CorrectionDto::new)
                    .toList();
        } 
        else if (user.getType() == Person.PersonType.TEACHER) {
            return correctionRepository.findByProfessor(user).stream()
                    .map(CorrectionDto::new)
                    .toList();
        } 
        else {
            throw new EmsException(ErrorMessage.ACCESS_DENIED, userEmail);
        }
    }

    @Transactional(readOnly = true)
    public CorrectionDto getCorrection(long id, String userEmail) {
        Person user = personRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EmsException(ErrorMessage.NO_SUCH_PERSON, userEmail));

        Correction correction = fetchCorrectionOrThrow(id);

        verifyCorrectionAccess(user, correction);

        return new CorrectionDto(correction);
    }

	@Transactional
	public CorrectionDto createCorrection(CorrectionDto correctionDto) {
        if (correctionDto.professorEmail() == null || correctionDto.professorEmail().isBlank()) {
            throw new EmsException(ErrorMessage.NO_SUCH_PERSON, correctionDto.professorEmail());
        }
        if (correctionDto.questionId() == null || correctionDto.questionId() <= 0) {
            throw new EmsException(ErrorMessage.NO_SUCH_QUESTION, Long.toString(correctionDto.questionId()));
        }

        Person professor = personRepository.findByEmail(correctionDto.professorEmail())
                .orElseThrow(() -> new EmsException(ErrorMessage.NO_SUCH_PERSON, correctionDto.professorEmail()));

        Question question = questionRepository.findById(correctionDto.questionId())
                .orElseThrow(() -> new EmsException(ErrorMessage.NO_SUCH_QUESTION, Long.toString(correctionDto.questionId())));

        Correction correction = new Correction(
                question,
                professor,
                correctionDto.score()
        );
        Correction saved = correctionRepository.save(correction);

        auditLogService.logAction(
            getCurrentUserEmail(),
            String.format("Atribuiu a correção da Pergunta ID %d ao professor '%s'", question.getId(), professor.getEmail())
        );

        notificationService.sendNotification(
            professor.getEmail(),
            String.format("Foi-te atribuída a correção da questão %d da disciplina %s.", 
                question.getQuestionNumber(), question.getSubject().getCode())
        );

		return new CorrectionDto(saved);
	}

	@Transactional
	public CorrectionDto updateCorrection(long id, CorrectionDto correctionDto, String userEmail) { 
        if (correctionDto.id() == null) {
            throw new EmsException(ErrorMessage.NO_SUCH_CORRECTION, "null");
        }

        Person user = personRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EmsException(ErrorMessage.NO_SUCH_PERSON, userEmail));

        Correction correction = fetchCorrectionOrThrow(correctionDto.id());

        if (user.getType() != Person.PersonType.ADMINISTRATOR) {
            throw new EmsException(ErrorMessage.ACCESS_DENIED, userEmail);
        } 

        Integer score = correctionDto.score();
        int maxScore = correction.getQuestion().getMaxScore();

        if (correctionDto.score() != null) {
                if (correctionDto.questionId() != null && !correctionDto.questionId().equals(correction.getQuestion().getId())) {
                    Question newQuestion = questionRepository.findById(correctionDto.questionId())
                            .orElseThrow(() -> new EmsException(ErrorMessage.NO_SUCH_QUESTION, Long.toString(correctionDto.questionId())));
                    
                    correction.setQuestion(newQuestion);
            }
        }
        
        // Alteração de Professor Atribuído 
        if (correctionDto.professorEmail() != null && 
            (correction.getProfessor() == null || !correctionDto.professorEmail().equals(correction.getProfessor().getEmail()))) {

            Person newProfessor = personRepository.findByEmail(correctionDto.professorEmail())
                    .orElseThrow(() -> new EmsException(ErrorMessage.NO_SUCH_PERSON, correctionDto.professorEmail()));
            
            correction.setProfessor(newProfessor);

            notificationService.sendNotification(
                newProfessor.getEmail(),
                String.format("Foi-te reatribuída a correção da questão %d da disciplina %s.", 
                    correction.getQuestion().getQuestionNumber(), correction.getQuestion().getSubject().getCode())
            );
        }

        if (correctionDto.score() != null) {
            if (score < 0 || score > maxScore) {
                throw new EmsException(ErrorMessage.SCORE_EXCEEDS_MAX, String.valueOf(score), String.valueOf(maxScore));
            }
            correction.setScore(score);
        }

        Correction saved = correctionRepository.save(correction);

        auditLogService.logAction(
            userEmail,
            String.format("Administrador atualizou a correção ID %d (Pergunta ID %d)", saved.getId(), saved.getQuestion().getId())
        );

        return new CorrectionDto(saved);
    }

	@Transactional
	public void deleteCorrection(long id) {
		Correction correction = fetchCorrectionOrThrow(id);
		correctionRepository.deleteById(id);

        auditLogService.logAction(
            getCurrentUserEmail(),
            String.format("Eliminou o registo de correção ID %d", id)
        );
	}

    @Transactional
    public CorrectionDto submitScore(Long id, Integer newScore, String professorEmail) { 
        Correction correction = fetchCorrectionOrThrow(id);

        Person loggedInUser = personRepository.findByEmail(professorEmail)
                .orElseThrow(() -> new EmsException(ErrorMessage.NO_SUCH_PERSON, professorEmail));
                
        verifyCorrectionAccess(loggedInUser, correction);

        if (newScore > correction.getQuestion().getMaxScore()) {
            throw new EmsException(ErrorMessage.SCORE_EXCEEDS_MAX, newScore.toString(), correction.getQuestion().getMaxScore().toString());
        }

        correction.setScore(newScore);
        correction = correctionRepository.save(correction);

        auditLogService.logAction(
            professorEmail,
            String.format("O Professor avaliou o fragmento da Pergunta ID %d com a nota %d/%d", 
                correction.getQuestion().getId(), newScore, correction.getQuestion().getMaxScore())
        );

        Exam exam = correction.getQuestion().getExam();

        boolean pendingCorrections = correctionRepository.existsByQuestionExamIdAndScoreIsNull(exam.getId());

        if (!pendingCorrections) {
            List<Correction> allExamCorrections = correctionRepository.findByQuestionExamId(exam.getId());

            int totalScore = allExamCorrections.stream()
                                               .mapToInt(Correction::getScore)
                                               .sum();

            exam.setFinalScore(totalScore);
            exam.setStatus(Exam.ExamStatus.CLOSED);
            examRepository.save(exam);

            auditLogService.logAction(
                "SYSTEM",
                String.format("Todas as correções do Exame ID %d foram concluídas. Nota final calculada: %d", exam.getId(), totalScore)
            );

            notificationService.sendNotification(
                exam.getStudent().getEmail(),
                String.format("A correção da tua prova de %s foi concluída (todas as questões foram avaliadas).", 
                    exam.getSubject().getCode())
            );
        }

        return new CorrectionDto(correction);
    }
}