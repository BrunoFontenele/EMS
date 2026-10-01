package com.brunofontenele.ems.question.dto;

import com.brunofontenele.ems.question.domain.Question;

public record QuestionDto(
		Long id,
		Integer questionNumber,
        Integer maxScore,
        String filePath,
        Long examId,
        String subjectCode
		) {

	public QuestionDto(Question question) {
		this(
            question.getId(), 
            question.getQuestionNumber(), 
            question.getMaxScore(), 
            question.getFilePath(),
            question.getExam().getId(),
            question.getSubject().getCode()
        );
	}
}
