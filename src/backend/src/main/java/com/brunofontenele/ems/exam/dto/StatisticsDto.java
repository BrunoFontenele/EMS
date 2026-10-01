package com.brunofontenele.ems.exam.dto;

import java.util.List;

public record StatisticsDto(
    String schoolName,
    String schoolCode,
    String subjectCode,
    String subjectName,
    long totalStudents,
    Double averageScore,
    Integer highestScore,
    Integer lowestScore,
    long approvedCount,
    long failedCount,
    Double approvalRate,
    List<StudentsResultsDto> studentResults
) {}