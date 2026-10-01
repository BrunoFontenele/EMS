package com.brunofontenele.ems.exam.dto;

public record StudentsResultsDto(
    Long examId,
    String studentName,
    String studentEmail,
    Integer finalScore
) {}