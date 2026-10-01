package com.brunofontenele.ems.correction;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import com.brunofontenele.ems.correction.dto.CorrectionDto;
import com.brunofontenele.ems.correction.service.CorrectionService;

import org.springframework.security.core.Authentication;

@RestController
public class CorrectionController {
	@Autowired
	private CorrectionService correctionService;

	@GetMapping("/corrections")
	@PreAuthorize("hasAuthority('CORRECTION_READ')")
	public List<CorrectionDto> getCorrections(Authentication authentication) {
		return correctionService.getCorrections(authentication.getName());
	}

    @GetMapping("/corrections/{userId}")
    @PreAuthorize("hasAuthority('CORRECTION_READ')")
    public CorrectionDto getCorrectionsByProfessor(@PathVariable Long userId, Authentication authentication) {
        return correctionService.getCorrection(userId, authentication.getName());
    }

	@PostMapping("/corrections")
	@PreAuthorize("hasAuthority('CORRECTION_CREATE')")
	public CorrectionDto createCorrection(@RequestBody CorrectionDto correctionDto) {
		return correctionService.createCorrection(correctionDto);
	}

	@PutMapping("/corrections/{id}")
	@PreAuthorize("hasAuthority('CORRECTION_UPDATE')")
	public CorrectionDto updateCorrection(
		@PathVariable long id, 
		@RequestBody CorrectionDto correctionDto, 
		Authentication authentication) {
		return correctionService.updateCorrection(id, correctionDto, authentication.getName());
	}

	@DeleteMapping("/corrections/{id}")
	@PreAuthorize("hasAuthority('CORRECTION_DELETE')")
	public void deleteCorrection(@PathVariable long id) {
		correctionService.deleteCorrection(id);
	}

    @PatchMapping("corrections/{id}/score")
    @PreAuthorize("hasAuthority('CORRECTION_UPDATE')") 
    public ResponseEntity<CorrectionDto> submitScore(
            @PathVariable Long id,
            @Valid @RequestBody CorrectionDto dto,
            Authentication authentication) {
        CorrectionDto updatedCorrection = correctionService.submitScore(id, dto.score(), authentication.getName());
        
        return ResponseEntity.ok(updatedCorrection);
    }
}
