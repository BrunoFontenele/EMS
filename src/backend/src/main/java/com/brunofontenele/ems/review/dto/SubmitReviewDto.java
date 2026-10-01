package com.brunofontenele.ems.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SubmitReviewDto(
    @NotNull(message = "O ID da pergunta é obrigatório.")
    Long questionId,

    @NotBlank(message = "A justificação não pode estar vazia.")
    @Size(min = 10, max = 1000, message = "A justificação deve ter entre 10 e 1000 caracteres.")
    @Pattern(regexp = "^[^<>]*$", message = "Carateres inválidos. Evita usar os símbolos < ou >.")
    String justification
) {}