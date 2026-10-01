package com.brunofontenele.ems.school;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.lang.NonNull;

import com.brunofontenele.ems.school.dto.SchoolDto;
import com.brunofontenele.ems.school.service.SchoolService;

@RestController
public class SchoolController {
	@Autowired
	private SchoolService schoolService;

	@GetMapping("/schools")
	@PreAuthorize("hasAuthority('SCHOOL_READ')")
	public List<SchoolDto> getSchools() {
		return schoolService.getSchools();
	}

	@GetMapping("/schools/{id}")
	@PreAuthorize("hasAuthority('SCHOOL_READ')")
	public SchoolDto getSchool(@PathVariable long id) {
		return schoolService.getSchool(id);
	}

	@PostMapping("/schools")
	@PreAuthorize("hasAuthority('SCHOOL_CREATE')")
	public SchoolDto createSchool(@NonNull @Valid @RequestBody SchoolDto schoolDto) {
		return schoolService.createSchool(schoolDto);
	}

	@PutMapping("/schools/{id}")
	@PreAuthorize("hasAuthority('SCHOOL_UPDATE')")
	public SchoolDto updateSchool(@PathVariable long id, @NonNull @Valid @RequestBody SchoolDto schoolDto) {
		return schoolService.updateSchool(id, schoolDto);
	}

	@PutMapping("/schools/{id}/toggle")
	@PreAuthorize("hasAuthority('SCHOOL_UPDATE')")
	public void toggleSchoolActive(@PathVariable long id) {
		schoolService.toggleSchoolActive(id);
	}

	/*
	@DeleteMapping("/schools/{id}")
	@PreAuthorize("hasAuthority('SCHOOL_DELETE')")
	public void deleteSchool(@PathVariable long id) {
		schoolService.deleteSchool(id);
	}
		*/
}
