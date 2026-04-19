package com.smartinvoice.service;

import com.smartinvoice.dto.InvoiceDTO;
import com.smartinvoice.dto.InvoiceItemDTO;
import com.smartinvoice.entity.*;
import com.smartinvoice.exception.ResourceNotFoundException;
import com.smartinvoice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;
    private final InvoiceItemRepository invoiceItemRepository;

    // Get all invoices
    @Transactional(readOnly = true)
    public List<InvoiceDTO> getAllInvoices() {
        return invoiceRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Get invoice by ID
    @Transactional(readOnly = true)
    public InvoiceDTO getInvoiceById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Invoice not found with id: " + id));
        return mapToDTO(invoice);
    }

    // Get invoices by client
    @Transactional(readOnly = true)
    public List<InvoiceDTO> getInvoicesByClient(Long clientId) {
        return invoiceRepository.findByClientId(clientId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Get invoices by status
    @Transactional(readOnly = true)
    public List<InvoiceDTO> getInvoicesByStatus(String status) {
        try {
            Invoice.Status invoiceStatus = Invoice.Status.valueOf(
                    status.toUpperCase().trim());
            return invoiceRepository.findByStatus(invoiceStatus)
                    .stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid status value: '" + status +
                    "'. Valid values are: PAID, UNPAID, OVERDUE");
        }
    }

    // Create invoice
    public InvoiceDTO createInvoice(InvoiceDTO dto) {
        Client client = clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Client not found with id: " + dto.getClientId()));

        Invoice invoice = new Invoice();
        invoice.setClient(client);
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setDueDate(dto.getDueDate());
        invoice.setStatus(Invoice.Status.UNPAID);
        invoice.setNotes(dto.getNotes());
        invoice.setInvoiceNumber(generateInvoiceNumber());

        // Process invoice items and calculate total
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<InvoiceItem> items = new ArrayList<>();

        if (dto.getInvoiceItems() != null) {
            for (com.smartinvoice.dto.InvoiceItemDTO itemDTO
                    : dto.getInvoiceItems()) {

                Product product = productRepository
                        .findById(itemDTO.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Product not found with id: "
                                + itemDTO.getProductId()));

                InvoiceItem item = new InvoiceItem();
                item.setInvoice(invoice);
                item.setProduct(product);
                item.setQuantity(itemDTO.getQuantity());
                item.setUnitPrice(product.getPrice());

                // Calculate subtotal
                BigDecimal subtotal = product.getPrice()
                        .multiply(BigDecimal.valueOf(
                                itemDTO.getQuantity()));

                // Calculate tax
                BigDecimal taxAmount = BigDecimal.ZERO;
                if (product.getTaxPercentage() != null) {
                    taxAmount = subtotal
                            .multiply(product.getTaxPercentage())
                            .divide(BigDecimal.valueOf(100));
                }

                item.setSubtotal(subtotal);
                item.setTaxAmount(taxAmount);
                items.add(item);

                totalAmount = totalAmount
                        .add(subtotal).add(taxAmount);
            }
        }

        invoice.setInvoiceItems(items);
        invoice.setTotalAmount(totalAmount);
        Invoice saved = invoiceRepository.save(invoice);
        return mapToDTO(saved);
    }

    // Update invoice status
    public InvoiceDTO updateInvoiceStatus(Long id, String status) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Invoice not found with id: " + id));
        invoice.setStatus(
                Invoice.Status.valueOf(status.toUpperCase()));
        Invoice updated = invoiceRepository.save(invoice);
        return mapToDTO(updated);
    }

    // Delete invoice
    public void deleteInvoice(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Invoice not found with id: " + id));
        invoiceRepository.delete(invoice);
    }

    // Dashboard summary
    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalInvoices", invoiceRepository.count());
        summary.put("paidInvoices",
                invoiceRepository.countByStatus(Invoice.Status.PAID));
        summary.put("unpaidInvoices",
                invoiceRepository.countByStatus(Invoice.Status.UNPAID));
        summary.put("overdueInvoices",
                invoiceRepository.countByStatus(Invoice.Status.OVERDUE));
        summary.put("totalClients", clientRepository.count());
        summary.put("totalProducts", productRepository.count());
        return summary;
    }

    // Generate invoice number
    private String generateInvoiceNumber() {
        long count = invoiceRepository.count() + 1;
        return String.format("INV-%d-%04d",
                LocalDate.now().getYear(), count);
    }

    // ---- Mapper Methods ----

    private InvoiceDTO mapToDTO(Invoice invoice) {
        InvoiceDTO dto = new InvoiceDTO();
        dto.setId(invoice.getId());
        dto.setInvoiceNumber(invoice.getInvoiceNumber());
        dto.setClientId(invoice.getClient().getId());
        dto.setClientName(invoice.getClient().getName());
        dto.setInvoiceDate(invoice.getInvoiceDate());
        dto.setDueDate(invoice.getDueDate());
        dto.setStatus(invoice.getStatus().name());
        dto.setTotalAmount(invoice.getTotalAmount());
        dto.setNotes(invoice.getNotes());

        if (invoice.getInvoiceItems() != null) {
            dto.setInvoiceItems(invoice.getInvoiceItems()
                    .stream()
                    .map(this::mapItemToDTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    private InvoiceItemDTO mapItemToDTO(InvoiceItem item) {
        InvoiceItemDTO dto = new InvoiceItemDTO();
        dto.setId(item.getId());
        dto.setProductId(item.getProduct().getId());
        dto.setProductName(item.getProduct().getName());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setTaxAmount(item.getTaxAmount());
        dto.setSubtotal(item.getSubtotal());
        return dto;
    }
}