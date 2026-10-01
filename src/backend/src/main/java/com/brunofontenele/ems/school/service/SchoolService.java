package com.brunofontenele.ems.school.service;

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
import com.brunofontenele.ems.school.domain.School;
import com.brunofontenele.ems.school.dto.SchoolDto;
import com.brunofontenele.ems.school.repository.SchoolRepository;

@Service
@Transactional
public class SchoolService {
	@Autowired
	private SchoolRepository schoolRepository;

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

	private School fetchSchoolOrThrow(long id) {
		return schoolRepository.findById(id)
				.orElseThrow(() -> new EmsException(ErrorMessage.NO_SUCH_SCHOOL, Long.toString(id)));
	}

	@Transactional(readOnly = true)
	public List<SchoolDto> getSchools() {
		return schoolRepository.findAll().stream()
				.map(SchoolDto::new)
				.toList();
	}

	@Transactional(readOnly = true)
	public SchoolDto getSchool(long id) {
		return new SchoolDto(fetchSchoolOrThrow(id));
	}

	@Transactional
	public SchoolDto createSchool(SchoolDto schoolDto) {
		if (schoolRepository.existsByCode(schoolDto.code())) {
			throw new EmsException(ErrorMessage.SCHOOL_ALREADY_EXISTS, schoolDto.code());
		}

		School school = new School(
				schoolDto.name(),
				schoolDto.code(),
				schoolDto.region());
		
		School saved = schoolRepository.save(school);

		auditLogService.logAction(
			getCurrentUserEmail(),
			String.format("Criou a escola '%s' (Código: %s, Região: %s)", saved.getName(), saved.getCode(), saved.getRegion())
		);

		return new SchoolDto(saved);
	}

	@Transactional
	public SchoolDto updateSchool(long id, SchoolDto schoolDto) {
		School school = fetchSchoolOrThrow(id);

		if (!schoolDto.active()){
			throw new EmsException(ErrorMessage.INACTIVE_SCHOOL, school.getCode());
		}

		if (schoolDto.name() != null) 
    		school.setName(schoolDto.name());
			
		if (schoolDto.code() != null && !schoolDto.code().isBlank()) {
        	if (!school.getCode().equalsIgnoreCase(schoolDto.code())) {
            	if (schoolRepository.findByCode(schoolDto.code()).isPresent()) 
                	throw new EmsException(ErrorMessage.SCHOOL_ALREADY_EXISTS, schoolDto.code()); 
            	school.setCode(schoolDto.code());
        	}
    	}

		if (schoolDto.region() != null) 
    		school.setRegion(schoolDto.region());

		School saved = schoolRepository.save(school);

		auditLogService.logAction(
			getCurrentUserEmail(),
			String.format("Atualizou os dados da escola '%s' (Código: %s)", saved.getName(), saved.getCode())
		);

		return new SchoolDto(saved);
	}

	@Transactional
	public void toggleSchoolActive(long id) {
    	School school = schoolRepository.findById(id).orElseThrow();
		school.setActive(!school.isActive());
		schoolRepository.save(school);

		auditLogService.logAction(
			getCurrentUserEmail(),
			String.format("%s a escola '%s' (Código: %s)", 
				school.isActive() ? "Ativou" : "Desativou", 
				school.getName(), 
				school.getCode())
		);

		List<Person> staffMembers = personRepository.findBySchool(school);
		for (Person staff : staffMembers) {
			notificationService.sendNotification(
				staff.getEmail(),
				String.format("O estado operacional da tua escola (%s) foi alterado para %s.", 
					school.getName(), school.isActive() ? "Ativo" : "Inativo")
			);
		}
	}
}