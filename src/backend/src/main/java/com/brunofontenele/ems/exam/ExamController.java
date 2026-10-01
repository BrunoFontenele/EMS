package com.brunofontenele.ems.exam;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.security.core.Authentication;

import com.brunofontenele.ems.exam.dto.ExamDto;
import com.brunofontenele.ems.exam.dto.StudentExamDetailsDto;
import com.brunofontenele.ems.exam.dto.StatisticsDto;
import com.brunofontenele.ems.exam.service.ExamService;


@RestController
public class ExamController {
	@Autowired
	private ExamService examService;

	@GetMapping("/exams")
	@PreAuthorize("hasAuthority('EXAM_READ')")
	public List<ExamDto> getExams(Authentication authentication) {
        return examService.getExams(authentication.getName());
	}

    @GetMapping("/exams/{id}")
	@PreAuthorize("hasAuthority('EXAM_READ')")
	public ExamDto getExam(@PathVariable long id, Authentication authentication) {
		return examService.getExam(id, authentication.getName());
	}

    @GetMapping("exams/{id}/application") 
    @PreAuthorize("hasAuthority('EXAM_READ')")
    public ResponseEntity<byte[]> getExamPdf(@PathVariable long id, Authentication authentication) {
        byte[] pdfBytes = examService.getExamPdf(id, authentication.getName());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @PostMapping(value = "/exams", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('EXAM_CREATE')")
    public ExamDto createExam(
        @RequestPart("exam") ExamDto examDto,
        @RequestPart("file") MultipartFile file
    ) {
        return examService.createExam(examDto, file);
    }

	@PutMapping(value = "/exams/{id}")
	@PreAuthorize("hasAuthority('EXAM_UPDATE')")
	public ExamDto updateExam(
        @PathVariable Long id,
        @RequestBody ExamDto examDto
    ) {
		return examService.updateExam(examDto);
	}

    /* 
	@DeleteMapping("/exams/{id}")
	@PreAuthorize("hasAuthority('EXAM_DELETE')")
	public void deleteExam(@PathVariable long id) {
		examService.deleteExam(id);
	}
        */

    @PutMapping(value = "/exams/{id}/pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('EXAM_UPDATE')")
    public ExamDto updateExamPdf(@PathVariable long id, @RequestParam("file") MultipartFile file) {
        return examService.updateExamPdf(id, file);
    }

    @PostMapping("/exams/distribute")
    @PreAuthorize("hasAuthority('QUESTION_CREATE')")
    public void distributeExams(@RequestParam String subjectCode) {
        examService.distributeExams(subjectCode);
    }


    // STATISTICS

    @GetMapping("/exams/statistics")
    @PreAuthorize("hasAuthority('EXAM_READ')")
    public StatisticsDto getSchoolStatistics(
            @RequestParam String subjectCode,
            Authentication authentication) {
        return examService.getSchoolStatisticsBySubject(subjectCode, authentication.getName());
    }

    // STUDENT VIEW

    @PatchMapping("exams/{examId}/request-view")
    @PreAuthorize("hasAuthority('EXAM_READ')") 
    public ResponseEntity<Void> requestView(
            @PathVariable Long examId, 
            Authentication authentication) {
        
        String studentEmail = authentication.getName();
        examService.requestExamView(examId, studentEmail);
        
        return ResponseEntity.ok().build();
    }

    @GetMapping("exams/{examId}/student-view")
    @PreAuthorize("hasAuthority('EXAM_READ')")
    public ResponseEntity<StudentExamDetailsDto> getStudentExamDetails(
            @PathVariable Long examId,
            Authentication authentication) {
        String studentEmail = authentication.getName();
        return ResponseEntity.ok(examService.getStudentExamDetails(examId, studentEmail));  
    }

    // EXAM RELEASE

    @PatchMapping("/subjects/{subjectCode}/releaseAll")
    @PreAuthorize("hasAuthority('EXAM_READ')")
    public ResponseEntity<Void> bulkRelease(
            @PathVariable String subjectCode, 
            Authentication authentication) {
        
        String staffEmail = authentication.getName();
        examService.bulkReleaseExams(subjectCode, staffEmail);
        
        return ResponseEntity.ok().build();
    }

    @PatchMapping("exams/{examId}/releaseExam")
    @PreAuthorize("hasAuthority('EXAM_READ')")
    public ResponseEntity<Void> releaseExam(
            @PathVariable Long examId,
            Authentication authentication) {
        
        String staffEmail = authentication.getName();
        examService.releaseExam(examId, staffEmail);
        
        return ResponseEntity.ok().build();
    }

}
