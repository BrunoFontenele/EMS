package com.brunofontenele.ems.exam.dto;

import com.brunofontenele.ems.exam.domain.Exam;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ExamDto(
        Long id,
        
        @NotBlank(message = "O caminho do ficheiro é obrigatório")
        String filePath,

        @NotBlank(message = "O email do estudante é obrigatório")
        String studentEmail, 

        @NotBlank(message = "O código da escola é obrigatório")
        String schoolCode,

        @NotBlank(message = "O código da disciplina é obrigatório")
        String subjectCode,

        Integer finalScore,

        @NotNull(message = "O pedido de visualização é obrigatório")
        boolean viewRequested,

        LocalDateTime releaseDate,

        @NotBlank(message = "O estado do exame é obrigatório")
        String status
        ) {

    public ExamDto(Exam exam) {
        this(
            exam.getId(), 
            exam.getFilePath(), 
            exam.getStudent().getEmail(), 
            exam.getSchool().getCode(), 
            exam.getSubject().getCode(),
            exam.getFinalScore(),
            exam.isViewRequested(),
            exam.getReleaseDate(),
            exam.getStatus().name() 
        );
    }
}