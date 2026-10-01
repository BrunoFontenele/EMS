package com.brunofontenele.ems.exam.dto;

public record FragmentScoreDto(
    Long id,               
    Integer number,          
    String imageUrl,         
    Integer maxScore,
    Integer score
) {}