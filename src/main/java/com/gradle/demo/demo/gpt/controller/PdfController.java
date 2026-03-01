package com.gradle.demo.demo.gpt.controller;


import com.gradle.demo.demo.gpt.model.SectionRequest;
import com.gradle.demo.demo.gpt.service.PdfGenerationService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/pdf")
public class PdfController {

    private final PdfGenerationService pdfService;

    public PdfController(PdfGenerationService pdfService) {
        this.pdfService = pdfService;
    }

    @PostMapping("/generate")
    public ResponseEntity<byte[]> generatePdf(@RequestBody List<SectionRequest> sections) {

        try {

            byte[] pdf = pdfService.generatePdf(sections);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=kyc.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}