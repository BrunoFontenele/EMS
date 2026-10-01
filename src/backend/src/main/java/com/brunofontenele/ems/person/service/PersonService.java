package com.brunofontenele.ems.person.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.brunofontenele.ems.auditlog.service.AuditLogService;
import com.brunofontenele.ems.exceptions.EmsException;
import com.brunofontenele.ems.exceptions.ErrorMessage;
import com.brunofontenele.ems.notification.service.NotificationService;
import com.brunofontenele.ems.person.domain.Person;
import com.brunofontenele.ems.person.dto.PersonDto;
import com.brunofontenele.ems.person.repository.PersonRepository;
import com.brunofontenele.ems.school.domain.School;
import com.brunofontenele.ems.school.repository.SchoolRepository;
import com.brunofontenele.ems.subject.domain.Subject;
import com.brunofontenele.ems.subject.repository.SubjectRepository;

@Service
@Transactional
public class PersonService {
	@Autowired
	private PersonRepository personRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private SchoolRepository schoolRepository;

	@Autowired
	private SubjectRepository subjectRepository;

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

	private Person fetchPersonOrThrow(long id) {
		return personRepository.findById(id)
				.orElseThrow(() -> new EmsException(ErrorMessage.NO_SUCH_PERSON, Long.toString(id)));
	}

	@Transactional(readOnly = true)
	public List<PersonDto> getPeople() {
		return personRepository.findAll().stream()
				.map(PersonDto::new)
				.toList();
	}

	@Transactional(readOnly = true)
	public PersonDto getPerson(long id) {
		return new PersonDto(fetchPersonOrThrow(id));
	}

	@Transactional
    public PersonDto createPerson(PersonDto personDto) {
        if (personDto.password() == null || personDto.password().length() < 8) {
            throw new EmsException(ErrorMessage.PERSON_PASSWORD_NOT_VALID); 
        }

        switch (personDto.type()) {
            case SCHOOL_STAFF:
                if (personDto.schoolCode() == null || personDto.schoolCode().isBlank()) {
                    throw new EmsException(ErrorMessage.NO_SUCH_SCHOOL);
                }
                break;

            case STUDENT:
                if (personDto.schoolCode() == null || personDto.schoolCode().isBlank()) {
                    throw new EmsException(ErrorMessage.NO_SUCH_SCHOOL);
                }
                break;

            case TEACHER:
                if (personDto.schoolCode() == null || personDto.schoolCode().isBlank()) {
                    throw new EmsException(ErrorMessage.NO_SUCH_SCHOOL);
                }
                if (personDto.subjectCode() == null || personDto.subjectCode().isBlank()) {
                    throw new EmsException(ErrorMessage.NO_SUCH_SUBJECT);
                }
                break;

            case ADMINISTRATOR:
                break;
        }

        School school = null;
        if (personDto.schoolCode() != null && !personDto.schoolCode().isBlank()) {
            school = schoolRepository.findByCode(personDto.schoolCode())
                    .orElseThrow(() -> new EmsException(ErrorMessage.NO_SUCH_SCHOOL, personDto.schoolCode()));
            if (!school.isActive()) {
                throw new EmsException(ErrorMessage.INACTIVE_SCHOOL, personDto.schoolCode());
            }
        }

        Subject subject = null;
        if (personDto.type() == Person.PersonType.TEACHER && personDto.subjectCode() != null && !personDto.subjectCode().isBlank()) {
            subject = subjectRepository.findByCode(personDto.subjectCode())
                    .orElseThrow(() -> new EmsException(ErrorMessage.NO_SUCH_SUBJECT, personDto.subjectCode()));
            if (!subject.isActive()) {
                throw new EmsException(ErrorMessage.INACTIVE_SUBJECT, personDto.subjectCode());
            }
        }

        Person person = new Person(
                personDto.name(),
                personDto.email(),
                passwordEncoder.encode(personDto.password()),
                personDto.type(),
                school, 
                subject
        );  

        Person saved = personRepository.save(person);

        auditLogService.logAction(
            getCurrentUserEmail(),
            String.format("Criou o utilizador '%s' (%s) com perfil %s", saved.getName(), saved.getEmail(), saved.getType())
        );

        notificationService.sendNotification(
            saved.getEmail(),
            String.format("Bem-vindo ao EMS! A tua conta foi criada com o perfil de %s.", saved.getType())
        );

        return new PersonDto(saved);
    }

	@Transactional
	public PersonDto updatePerson(long id, PersonDto personDto) {
		Person person = fetchPersonOrThrow(id);

		if (!personDto.active()){
			throw new EmsException(ErrorMessage.INACTIVE_PERSON, personDto.email());
		}

		if (personDto.name() != null) 
    		person.setName(personDto.name());
			
		if (personDto.email() != null && !personDto.email().isBlank()) {
        	if (!person.getEmail().equalsIgnoreCase(personDto.email())) {
            	if (personRepository.findByEmail(personDto.email()).isPresent()) 
                	throw new EmsException(ErrorMessage.EMAIL_ALREADY_EXISTS); 
            	person.setEmail(personDto.email());
        	}
    	}

		if (personDto.type() != null) 
    		person.setType(personDto.type());

		if (personDto.password() != null && !personDto.password().isBlank()) 
            person.setPassword(passwordEncoder.encode(personDto.password()));

		if (personDto.schoolCode() != null) {
            School school = schoolRepository.findByCode(personDto.schoolCode())
                    .orElseThrow(() -> new EmsException(ErrorMessage.NO_SUCH_SCHOOL));

			if (!school.isActive()) {
				throw new EmsException(ErrorMessage.INACTIVE_SCHOOL, school.getCode());
			}

            person.setSchool(school);
        } else person.setSchool(null);

		if (personDto.subjectCode() != null) {
            Subject subject = subjectRepository.findByCode(personDto.subjectCode())
                    .orElseThrow(() -> new EmsException(ErrorMessage.NO_SUCH_SUBJECT));

			if (!subject.isActive()) {
				throw new EmsException(ErrorMessage.INACTIVE_SUBJECT, subject.getCode());
			}

            person.setSubject(subject);
        } else person.setSubject(null);

		Person saved = personRepository.save(person);

		auditLogService.logAction(
            getCurrentUserEmail(),
            String.format("Atualizou os dados do utilizador '%s' (ID: %d)", saved.getEmail(), saved.getId())
        );

		notificationService.sendNotification(
            saved.getEmail(),
            "Os dados do teu perfil foram atualizados por um administrador."
        );

		return new PersonDto(saved);
	}

	@Transactional
	public void togglePersonActive(long id) {
    	Person person = personRepository.findById(id).orElseThrow();
		person.setActive(!person.isActive());
		personRepository.save(person);

		auditLogService.logAction(
            getCurrentUserEmail(),
            String.format("%s o utilizador '%s' (ID: %d)", 
                person.isActive() ? "Ativou" : "Desativou", 
                person.getEmail(), 
                person.getId())
        );

		notificationService.sendNotification(
            person.getEmail(),
            String.format("A tua conta foi %s por um administrador.", person.isActive() ? "reativada" : "suspensa")
        );
	}
}