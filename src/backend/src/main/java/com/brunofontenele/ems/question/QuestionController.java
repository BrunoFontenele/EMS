package com.brunofontenele.ems.question;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import com.brunofontenele.ems.question.dto.QuestionDto;
import com.brunofontenele.ems.question.service.QuestionService;

@RestController
public class QuestionController {
	@Autowired
	private QuestionService questionService;

	@GetMapping("/questions")
	@PreAuthorize("hasAuthority('QUESTION_READ')")
	public List<QuestionDto> getQuestions(Authentication authentication) {
		return questionService.getQuestions(authentication.getName());
	}

    @GetMapping("/questions/{id}")
	@PreAuthorize("hasAuthority('QUESTION_READ')")
	public QuestionDto getQuestion(@PathVariable long id, Authentication authentication) {
		return questionService.getQuestion(id, authentication.getName());
	}

    @GetMapping(value = "/questions/{questionId}/image", produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE})
    @PreAuthorize("hasAuthority('QUESTION_READ')")
    public ResponseEntity<byte[]> getQuestionImage(@PathVariable Long questionId, Authentication authentication) {
        byte[] imageBytes = questionService.getQuestionImage(questionId, authentication.getName());
        return ResponseEntity.ok().body(imageBytes);
    }
    
    @PostMapping(value = "/questions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('QUESTION_CREATE')")
    public QuestionDto createQuestion(
        @RequestPart("question") QuestionDto questionDto,
        @RequestPart("file") MultipartFile file
    ) {
        return questionService.createQuestion(questionDto, file);
    }
    

	@PutMapping(value = "/questions/{id}")
	@PreAuthorize("hasAuthority('QUESTION_UPDATE')")
	public QuestionDto updateQuestion(
        @PathVariable Long id,
        @RequestBody QuestionDto questionDto
    ) {
		return questionService.updateQuestion(questionDto);
	}

	@DeleteMapping("/questions/{id}")
	@PreAuthorize("hasAuthority('QUESTION_DELETE')")
	public void deleteQuestion(@PathVariable long id) {
		questionService.deleteQuestion(id);
	}
}
