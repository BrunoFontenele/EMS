package com.brunofontenele.ems.review;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import com.brunofontenele.ems.review.dto.SubmitReviewDto;
import com.brunofontenele.ems.review.service.ReviewService;
import com.brunofontenele.ems.review.dto.ReviewDto;

@RestController
public class ReviewController {
    @Autowired
    private ReviewService reviewService;

    @GetMapping("/reviews")
    @PreAuthorize("hasAuthority('REVIEW_READ')")
    public List<ReviewDto> getReviews(Authentication authentication) {
        return reviewService.getReviews(authentication.getName());
    }

    @GetMapping("reviews/{id}")
    @PreAuthorize("hasAuthority('REVIEW_READ')")
    public ReviewDto getReview(@PathVariable long id, Authentication authentication) {
        return reviewService.getReview(id, authentication.getName());
    }

    @PostMapping("reviews/{examId}")
    @PreAuthorize("hasAuthority('REVIEW_CREATE')")
    public ReviewDto createReview(
            @PathVariable Long examId,
            @Valid @RequestBody SubmitReviewDto dto,
            Authentication authentication) {
        return reviewService.createReview(examId, authentication.getName(), dto);
    }

    @PutMapping("reviews/{id}")
    @PreAuthorize("hasAuthority('REVIEW_UPDATE')")
    public ReviewDto updateReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewDto dto,
            Authentication authentication) {
        return reviewService.updateReview(dto, authentication.getName());
    }

    @DeleteMapping("reviews/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('REVIEW_DELETE')")
    public void deleteReview(@PathVariable long id) {
        reviewService.deleteReview(id);
    }

    @PatchMapping("/reviews/{id}/grade")
    @PreAuthorize("hasAuthority('REVIEW_UPDATE')")
    public ReviewDto reviewResponse(
            @PathVariable long id,
            @RequestBody ReviewDto dto,
            Authentication authentication) {
        return reviewService.reviewResponse(id, authentication.getName(), dto);
    }
}
