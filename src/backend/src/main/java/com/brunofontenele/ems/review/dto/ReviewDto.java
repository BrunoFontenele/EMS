package com.brunofontenele.ems.review.dto;

import com.brunofontenele.ems.review.domain.Review;
import java.time.LocalDateTime;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReviewDto(
    Long id,

    @NotNull(message = "O ID da questão é obrigatório") 
    Long questionId,

    @NotBlank(message = "A justificação do estudante é obrigatória")
    String studentJustification,

    Integer oldScore,

    @Min(value = 0, message = "A nota mínima é 0")
    Integer newScore,

    LocalDateTime createdAt,

    Review.ReviewStatus reviewStatus,
    
    String assignedTeacherEmail
) {
    public ReviewDto(Review review) {
        this(
            review.getId(),
            review.getQuestion() != null ? review.getQuestion().getId() : null,
            review.getStudentJustification(),
            review.getOldScore(),
            review.getNewScore(),
            review.getCreatedAt(),
            review.getStatus(),
            review.getAssignedTeacher() != null ? review.getAssignedTeacher().getEmail() : null
        );
    }
}