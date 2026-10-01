package com.brunofontenele.ems.exam.dto;

import java.util.List;

public record StudentExamViewDto(
    String pdfUrl,           
    Integer finalScore,
    List<FragmentScoreDto> fragments
) {}