package com.brunofontenele.ems.exam.dto;

import java.time.LocalDateTime;

public record StudentExamSummaryDto(
    Long id,
    String subjectCode,
    LocalDateTime releaseDate,
    Integer finalScore
) {}