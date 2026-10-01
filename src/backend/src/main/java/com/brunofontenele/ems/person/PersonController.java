package com.brunofontenele.ems.person;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.brunofontenele.ems.person.dto.PersonDto;
import com.brunofontenele.ems.person.service.PersonService;

@RestController
public class PersonController {
	@Autowired
	private PersonService personService;

	@GetMapping("/people")
	@PreAuthorize("hasAuthority('PERSON_READ')")
	public List<PersonDto> getPeople() {
		return personService.getPeople();
	}

	@GetMapping("/people/{id}")
	@PreAuthorize("hasAuthority('PERSON_READ')")
	public PersonDto getPerson(@PathVariable long id) {
		return personService.getPerson(id);
	}

	@PostMapping("/people")
	@PreAuthorize("hasAuthority('PERSON_CREATE')")
	public PersonDto createPerson(@Valid @RequestBody PersonDto personDto) {
		return personService.createPerson(personDto);
	}

	@PutMapping("/people/{id}")
	@PreAuthorize("hasAuthority('PERSON_UPDATE')")
	public PersonDto updatePerson(@PathVariable long id,@Valid @RequestBody PersonDto personDto) {
		return personService.updatePerson(id, personDto);
	}

	@PutMapping("/people/{id}/toggle")
	@PreAuthorize("hasAuthority('PERSON_UPDATE')")
	public void togglePersonActive(@PathVariable long id) {
		personService.togglePersonActive(id);
	}

	/* 
	@DeleteMapping("/people/{id}")
	@PreAuthorize("hasAuthority('PERSON_DELETE')")
	public void deletePerson(@PathVariable long id) {
		personService.deletePerson(id);
	}
		*/
}
