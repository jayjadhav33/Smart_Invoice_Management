package com.smartinvoice.controller;

import com.smartinvoice.dto.InvoiceDTO;
import com.smartinvoice.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class InvoiceController {

    private final InvoiceService invoiceService;

    // GET all invoices
    @GetMapping
    public ResponseEntity<List<InvoiceDTO>> getAllInvoices() {
        return ResponseEntity.ok(invoiceService.getAllInvoices());
    }

    // GET invoice by ID
    @GetMapping("/{id}")
    public ResponseEntity<InvoiceDTO> getInvoiceById(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.getInvoiceById(id));
    }

    // GET invoices by client
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<InvoiceDTO>> getByClient(
            @PathVariable Long clientId) {
        return ResponseEntity.ok(
                invoiceService.getInvoicesByClient(clientId));
    }

    // GET invoices by status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<InvoiceDTO>> getByStatus(
            @PathVariable String status) {
        return ResponseEntity.ok(
                invoiceService.getInvoicesByStatus(status));
    }

    // GET dashboard summary
    @GetMapping("/dashboard/summary")
    public ResponseEntity<Map<String, Object>> getDashboardSummary() {
        return ResponseEntity.ok(invoiceService.getDashboardSummary());
    }

    // POST create invoice
    @PostMapping
    public ResponseEntity<InvoiceDTO> createInvoice(
            @Valid @RequestBody InvoiceDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(invoiceService.createInvoice(dto));
    }

    // PATCH update invoice status
    @PatchMapping("/{id}/status")
    public ResponseEntity<InvoiceDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(
                invoiceService.updateInvoiceStatus(id, status));
    }

    // DELETE invoice
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteInvoice(@PathVariable Long id) {
        invoiceService.deleteInvoice(id);
        return ResponseEntity.ok("Invoice deleted successfully");
    }
}