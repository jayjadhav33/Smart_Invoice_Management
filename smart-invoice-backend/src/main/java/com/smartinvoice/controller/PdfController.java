package com.smartinvoice.controller;

import com.itextpdf.text.DocumentException;
import com.smartinvoice.service.PdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PdfController {

    private final PdfService pdfService;

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(
            @PathVariable Long id)
            throws DocumentException {

        byte[] pdf = pdfService.generateInvoicePdf(id);

        HttpHeaders headers = new HttpHeaders();

        // Tell browser this is a PDF download
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData(
                "attachment",
                "invoice-" + id + ".pdf");
        headers.setCacheControl("must-revalidate, " +
                "post-check=0, pre-check=0");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }
}