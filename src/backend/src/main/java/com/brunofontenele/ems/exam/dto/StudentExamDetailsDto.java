package com.brunofontenele.ems.exam.dto;

import java.util.List;

public record StudentExamDetailsDto(
    Long id,
    String subjectCode,
    String pdfUrl,           
    Integer finalScore,
    Boolean isReviewPeriodOpen,
    List<FragmentScoreDto> fragments
) {}