package com.brunofontenele.ems.subject.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.brunofontenele.ems.auditlog.service.AuditLogService;
import com.brunofontenele.ems.exceptions.EmsException;
import com.brunofontenele.ems.exceptions.ErrorMessage;
import com.brunofontenele.ems.notification.service.NotificationService;
import com.brunofontenele.ems.person.domain.Person;
import com.brunofontenele.ems.person.repository.PersonRepository;
import com.brunofontenele.ems.subject.domain.Subject;
import com.brunofontenele.ems.subject.dto.SubjectDto;
import com.brunofontenele.ems.subject.repository.SubjectRepository;

@Service
@Transactional
public class SubjectService {
	@Autowired
	private SubjectRepository subjectRepository;

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

	private Subject fetchSubjectOrThrow(long id) {
		return subjectRepository.findById(id)
				.orElseThrow(() -> new EmsException(ErrorMessage.NO_SUCH_SUBJECT, Long.toString(id)));
	}

	@Transactional(readOnly = true)
	public List<SubjectDto> getSubjects() {
		return subjectRepository.findAll().stream()
				.map(SubjectDto::new)
				.toList();
	}

	@Transactional(readOnly = true)
	public SubjectDto getSubject(long id) {
		return new SubjectDto(fetchSubjectOrThrow(id));
	}

	@Transactional
	public SubjectDto createSubject(SubjectDto subjectDto) {
		if (subjectRepository.existsByCode(subjectDto.code())) {
			throw new EmsException(ErrorMessage.SUBJECT_ALREADY_EXISTS, subjectDto.code());
		}

		Subject subject = new Subject(
				subjectDto.name(),
				subjectDto.code());

		Subject saved = subjectRepository.save(subject);

		auditLogService.logAction(
			getCurrentUserEmail(),
			String.format("Criou a disciplina '%s' (Código: %s)", saved.getName(), saved.getCode())
		);

		return new SubjectDto(saved);
	}

	@Transactional
	public SubjectDto updateSubject(long id, SubjectDto subjectDto) {
		Subject subject = fetchSubjectOrThrow(id);

		if (!subjectDto.active()){
			throw new EmsException(ErrorMessage.INACTIVE_SUBJECT, subject.getCode());
		}

		if (subjectDto.name() != null) 
    		subject.setName(subjectDto.name());
			
		if (subjectDto.code() != null && !subjectDto.code().isBlank()) {
        	if (!subject.getCode().equalsIgnoreCase(subjectDto.code())) {
            	if (subjectRepository.findByCode(subjectDto.code()).isPresent()) 
                	throw new EmsException(ErrorMessage.SUBJECT_ALREADY_EXISTS, subjectDto.code()); 
            	subject.setCode(subjectDto.code());
        	}
    	}

		Subject saved = subjectRepository.save(subject);

		auditLogService.logAction(
			getCurrentUserEmail(),
			String.format("Atualizou os dados da disciplina '%s' (Código: %s)", saved.getName(), saved.getCode())
		);

		return new SubjectDto(saved);
	}

	@Transactional
	public void toggleSubjectActive(long id) {
		Subject subject = fetchSubjectOrThrow(id);
		subject.setActive(!subject.isActive());
		subjectRepository.save(subject);

		auditLogService.logAction(
			getCurrentUserEmail(),
			String.format("%s a disciplina '%s' (Código: %s)", 
				subject.isActive() ? "Ativou" : "Desativou", 
				subject.getName(), 
				subject.getCode())
		);

		List<Person> teachers = personRepository.findBySubjectAndType(subject, Person.PersonType.TEACHER);
		for (Person teacher : teachers) {
			notificationService.sendNotification(
				teacher.getEmail(),
				String.format("O estado da disciplina %s foi alterado para %s.", 
					subject.getCode(), subject.isActive() ? "Ativo" : "Inativo")
			);
		}
	}
}