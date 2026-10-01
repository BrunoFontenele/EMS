package com.brunofontenele.ems.subject;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import org.springframework.lang.NonNull;

import com.brunofontenele.ems.subject.dto.SubjectDto;
import com.brunofontenele.ems.subject.service.SubjectService;

@RestController
public class SubjectController {
	@Autowired
	private SubjectService subjectService;

	@GetMapping("/subjects")
	@PreAuthorize("hasAuthority('SUBJECT_READ')")
	public List<SubjectDto> getSubjects() {
		return subjectService.getSubjects();
	}

	@GetMapping("/subjects/{id}")
	@PreAuthorize("hasAuthority('SUBJECT_READ')")
	public SubjectDto getSubject(@PathVariable long id) {
		return subjectService.getSubject(id);
	}

	@PostMapping("/subjects")
	@PreAuthorize("hasAuthority('SUBJECT_CREATE')")
	public SubjectDto createSubject(@NonNull @Valid @RequestBody SubjectDto subjectDto) {
		return subjectService.createSubject(subjectDto);
	}

	@PutMapping("/subjects/{id}")
	@PreAuthorize("hasAuthority('SUBJECT_UPDATE')")
	public SubjectDto updateSubject(@PathVariable long id,@NonNull @Valid @RequestBody SubjectDto subjectDto) {
		return subjectService.updateSubject(id, subjectDto);
	}

	@PutMapping("/subjects/{id}/toggle")
	@PreAuthorize("hasAuthority('SUBJECT_UPDATE')")
	public void toggleSubjectActive(@PathVariable long id) {
		subjectService.toggleSubjectActive(id);
	}

	/*
	@DeleteMapping("/subjects/{id}")
	@PreAuthorize("hasAuthority('SUBJECT_DELETE')")
	public void deleteSubject(@PathVariable long id) {
		subjectService.deleteSubject(id);
	}
	*/
}
