package com.smartinvoice.repository;

import com.smartinvoice.entity.Invoice;
import com.smartinvoice.entity.Invoice.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByClientId(Long clientId);
    List<Invoice> findByStatus(Status status);
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    long countByStatus(Status status);
}