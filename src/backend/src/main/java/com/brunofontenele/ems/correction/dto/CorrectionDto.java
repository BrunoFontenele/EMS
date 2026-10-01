package com.brunofontenele.ems.correction.dto;

import jakarta.validation.constraints.NotNull;
import com.brunofontenele.ems.correction.domain.Correction;

public record CorrectionDto(
		Long id,

        @NotNull(message = "O ID da pergunta é obrigatório")
        Long questionId,

        String professorEmail,

        Integer score,

        @NotNull(message = "A cotação máxima da pergunta é obrigatória")
        Integer maxScore
		) {

	public CorrectionDto(Correction correction) {
		this(
            correction.getId(),
            correction.getQuestion().getId(),
            correction.getProfessor().getEmail(),
            correction.getScore(),
            correction.getQuestion().getMaxScore()
        );
	}
}
