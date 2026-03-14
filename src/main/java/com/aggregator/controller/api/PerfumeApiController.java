package com.aggregator.controller.api;

import com.aggregator.dto.PerfumeDto;
import com.aggregator.service.PerfumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/perfumes")
public class PerfumeApiController {

    private final PerfumeService perfumeService;

    @Autowired
    public PerfumeApiController(PerfumeService perfumeService) {
        this.perfumeService = perfumeService;
    }

    @GetMapping
    public ResponseEntity<List<PerfumeDto>> getAllPerfumes() {
        return ResponseEntity.ok(perfumeService.getAllPerfumes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PerfumeDto> getPerfumeById(@PathVariable Long id) {
        PerfumeDto dto = perfumeService.getPerfumeDetails(id);
        if (dto != null) {
            return ResponseEntity.ok(dto);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<PerfumeDto> createPerfume(@RequestBody PerfumeDto dto) {
        return ResponseEntity.ok(perfumeService.createPerfume(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePerfume(@PathVariable Long id) {
        perfumeService.deletePerfume(id);
        return ResponseEntity.ok().build();
    }
}
