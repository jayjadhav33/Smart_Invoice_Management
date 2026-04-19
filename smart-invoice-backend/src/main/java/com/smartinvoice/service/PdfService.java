package com.smartinvoice.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.smartinvoice.entity.Invoice;
import com.smartinvoice.entity.InvoiceItem;
import com.smartinvoice.exception.ResourceNotFoundException;
import com.smartinvoice.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class PdfService {

    private final InvoiceRepository invoiceRepository;

    private static final BaseColor TEAL       = new BaseColor(13, 148, 136);
    private static final BaseColor TEAL_DARK  = new BaseColor(15, 118, 110);
    private static final BaseColor TEAL_LIGHT = new BaseColor(240, 253, 250);
    private static final BaseColor GRAY_50    = new BaseColor(249, 250, 251);
    private static final BaseColor GRAY_100   = new BaseColor(243, 244, 246);
    private static final BaseColor GRAY_200   = new BaseColor(229, 231, 235);
    private static final BaseColor GRAY_500   = new BaseColor(107, 114, 128);
    private static final BaseColor GRAY_700   = new BaseColor(55, 65, 81);
    private static final BaseColor GRAY_900   = new BaseColor(17, 24, 39);
    private static final BaseColor GREEN_BG   = new BaseColor(220, 252, 231);
    private static final BaseColor GREEN_TEXT = new BaseColor(21, 128, 61);
    private static final BaseColor AMBER_BG   = new BaseColor(254, 249, 195);
    private static final BaseColor AMBER_TEXT = new BaseColor(161, 98, 7);
    private static final BaseColor RED_BG     = new BaseColor(254, 226, 226);
    private static final BaseColor RED_TEXT   = new BaseColor(185, 28, 28);

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy");

    // Fonts
    private Font font(int size, int style, BaseColor color) {
        return new Font(Font.FontFamily.HELVETICA, size, style, color);
    }
    private Font regular(int size) { return font(size, Font.NORMAL, GRAY_700); }
    private Font bold(int size)    { return font(size, Font.BOLD,   GRAY_900); }
    private Font light(int size)   { return font(size, Font.NORMAL, GRAY_500); }
    private Font white(int size)   { return font(size, Font.BOLD,   BaseColor.WHITE); }
    private Font colored(int size, BaseColor c) { return font(size, Font.BOLD, c); }

    @Transactional(readOnly = true)
    public byte[] generateInvoicePdf(Long id)
            throws DocumentException {

        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Invoice not found: " + id));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 48, 48, 48, 48);
        PdfWriter writer = PdfWriter.getInstance(doc, out);
        doc.open();

        buildHeader(doc, invoice);
        doc.add(Chunk.NEWLINE);
        buildInfoSection(doc, invoice);
        doc.add(Chunk.NEWLINE);
        buildItemsTable(doc, invoice);
        buildTotals(doc, invoice);
        doc.add(Chunk.NEWLINE);
        if (invoice.getNotes() != null && !invoice.getNotes().isBlank())
            buildNotes(doc, invoice.getNotes());
        doc.add(Chunk.NEWLINE);
        buildFooter(doc);

        doc.close();
        return out.toByteArray();
    }

    // ── Header ─────────────────────────────────────
    private void buildHeader(Document doc, Invoice inv)
            throws DocumentException {

        PdfPTable t = new PdfPTable(2);
        t.setWidthPercentage(100);
        t.setWidths(new float[]{1f, 1f});

        // Left: branding
        PdfPCell left = new PdfPCell();
        left.setBackgroundColor(TEAL);
        left.setBorder(Rectangle.NO_BORDER);
        left.setPadding(24);

        Paragraph brand = new Paragraph("SmartInvoice",
                font(20, Font.BOLD, BaseColor.WHITE));
        brand.setSpacingAfter(4);
        left.addElement(brand);
        left.addElement(new Paragraph(
                "Invoice & Billing System",
                font(9, Font.NORMAL,
                        new BaseColor(167, 243, 208))));

        // Right: invoice info on dark bg
        PdfPCell right = new PdfPCell();
        right.setBackgroundColor(TEAL_DARK);
        right.setBorder(Rectangle.NO_BORDER);
        right.setPadding(24);
        right.setHorizontalAlignment(Element.ALIGN_RIGHT);

        Paragraph invLabel = new Paragraph("INVOICE",
                font(26, Font.BOLD, BaseColor.WHITE));
        invLabel.setAlignment(Element.ALIGN_RIGHT);
        right.addElement(invLabel);

        Paragraph invNum = new Paragraph(
                inv.getInvoiceNumber(),
                font(11, Font.NORMAL,
                        new BaseColor(167, 243, 208)));
        invNum.setAlignment(Element.ALIGN_RIGHT);
        invNum.setSpacingBefore(3);
        right.addElement(invNum);

        // Status pill
        String sText = inv.getStatus().name();
        BaseColor sBg = inv.getStatus() == Invoice.Status.PAID
                ? GREEN_BG
                : inv.getStatus() == Invoice.Status.OVERDUE
                ? RED_BG : AMBER_BG;
        BaseColor sTx = inv.getStatus() == Invoice.Status.PAID
                ? GREEN_TEXT
                : inv.getStatus() == Invoice.Status.OVERDUE
                ? RED_TEXT : AMBER_TEXT;

        PdfPTable pill = new PdfPTable(1);
        pill.setWidthPercentage(40);
        pill.setHorizontalAlignment(Element.ALIGN_RIGHT);
        PdfPCell pc = new PdfPCell(
                new Phrase("  " + sText + "  ",
                        font(9, Font.BOLD, sTx)));
        pc.setBackgroundColor(sBg);
        pc.setBorder(Rectangle.NO_BORDER);
        pc.setPadding(5);
        pc.setHorizontalAlignment(Element.ALIGN_CENTER);
        pill.addCell(pc);

        right.addElement(new Paragraph(" "));
        right.addElement(pill);

        t.addCell(left);
        t.addCell(right);
        doc.add(t);
    }

    // ── Info Section ────────────────────────────────
    private void buildInfoSection(Document doc, Invoice inv)
            throws DocumentException {

        PdfPTable t = new PdfPTable(2);
        t.setWidthPercentage(100);
        t.setWidths(new float[]{1f, 1f});

        // Bill To
        PdfPCell bill = new PdfPCell();
        bill.setBackgroundColor(TEAL_LIGHT);
        bill.setBorderColor(GRAY_200);
        bill.setPadding(18);

        bill.addElement(new Paragraph("BILL TO",
                font(8, Font.BOLD, TEAL)));
        bill.addElement(new Paragraph(" "));
        bill.addElement(new Paragraph(
                inv.getClient().getName(), bold(12)));
        if (inv.getClient().getEmail() != null)
            bill.addElement(new Paragraph(
                    inv.getClient().getEmail(), light(9)));
        if (inv.getClient().getPhone() != null)
            bill.addElement(new Paragraph(
                    inv.getClient().getPhone(), light(9)));
        if (inv.getClient().getAddress() != null)
            bill.addElement(new Paragraph(
                    inv.getClient().getAddress(), light(9)));
        if (inv.getClient().getGstNumber() != null) {
            bill.addElement(new Paragraph(" "));
            bill.addElement(new Paragraph(
                    "GST: " + inv.getClient().getGstNumber(),
                    font(9, Font.NORMAL, TEAL)));
        }

        // Invoice Details
        PdfPCell details = new PdfPCell();
        details.setBackgroundColor(GRAY_50);
        details.setBorderColor(GRAY_200);
        details.setPadding(18);

        details.addElement(new Paragraph(
                "INVOICE DETAILS",
                font(8, Font.BOLD, TEAL)));
        details.addElement(new Paragraph(" "));

        String[][] rows = {
            { "Invoice Number", inv.getInvoiceNumber() },
            { "Invoice Date", inv.getInvoiceDate() != null ? inv.getInvoiceDate().format(FMT) : "—" },
            { "Due Date", inv.getDueDate() != null ? inv.getDueDate().format(FMT) : "—" },
            { "Status", inv.getStatus().name() },
        };
        for (String[] row : rows) {
            details.addElement(new Paragraph(
                    row[0], light(8)));
            details.addElement(new Paragraph(
                    row[1], bold(10)));
            details.addElement(new Paragraph(" "));
        }

        t.addCell(bill);
        t.addCell(details);
        doc.add(t);
    }

    // ── Items Table ─────────────────────────────────
    private void buildItemsTable(Document doc, Invoice inv)
            throws DocumentException {

        PdfPTable t = new PdfPTable(5);
        t.setWidthPercentage(100);
        t.setWidths(new float[]{ 3.5f, 1f, 1.4f, 1.4f, 1.6f });
        t.setSpacingBefore(4);

        String[] heads = { "ITEM & DESCRIPTION", "QTY", "UNIT PRICE", "TAX", "AMOUNT" };
        int[] aligns = { Element.ALIGN_LEFT, Element.ALIGN_CENTER, Element.ALIGN_RIGHT, Element.ALIGN_RIGHT, Element.ALIGN_RIGHT };

        for (int i = 0; i < heads.length; i++) {
            PdfPCell c = new PdfPCell(new Phrase(heads[i], font(8, Font.BOLD, TEAL)));
            c.setBackgroundColor(TEAL_LIGHT);
            c.setBorderColor(GRAY_200);
            c.setPaddingTop(10); c.setPaddingBottom(10);
            c.setPaddingLeft(10); c.setPaddingRight(10);
            c.setHorizontalAlignment(aligns[i]);
            t.addCell(c);
        }

        boolean alt = false;
        for (InvoiceItem item : inv.getInvoiceItems()) {
            BaseColor rowBg = alt ? GRAY_50 : BaseColor.WHITE;
            alt = !alt;
            BigDecimal total = item.getSubtotal().add(item.getTaxAmount());

            addCell(t, item.getProduct().getName(), regular(10), rowBg, Element.ALIGN_LEFT);
            addCell(t, String.valueOf(item.getQuantity()), regular(10), rowBg, Element.ALIGN_CENTER);
            addCell(t, "Rs. " + item.getUnitPrice().toPlainString(), regular(10), rowBg, Element.ALIGN_RIGHT);
            addCell(t, "Rs. " + item.getTaxAmount().toPlainString(), light(10), rowBg, Element.ALIGN_RIGHT);
            addCell(t, "Rs. " + total.toPlainString(), bold(10), rowBg, Element.ALIGN_RIGHT);
        }

        doc.add(t);
    }

    private void addCell(PdfPTable t, String text, Font font, BaseColor bg, int align) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setBackgroundColor(bg);
        c.setBorderColor(GRAY_100);
        c.setPadding(10);
        c.setHorizontalAlignment(align);
        t.addCell(c);
    }

    // ── Totals ──────────────────────────────────────
    private void buildTotals(Document doc, Invoice inv)
            throws DocumentException {

        BigDecimal subtotal = inv.getInvoiceItems().stream()
                .map(InvoiceItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal taxTotal = inv.getInvoiceItems().stream()
                .map(InvoiceItem::getTaxAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        PdfPTable outer = new PdfPTable(2);
        outer.setWidthPercentage(100);
        outer.setWidths(new float[]{1f, 1f});
        outer.setSpacingBefore(2);

        PdfPCell empty = new PdfPCell();
        empty.setBorder(Rectangle.NO_BORDER);
        outer.addCell(empty);

        PdfPTable totals = new PdfPTable(2);
        totals.setWidthPercentage(100);

        addTotalRow(totals, "Subtotal",
                "Rs. " + subtotal.toPlainString(),
                light(10), regular(10),
                BaseColor.WHITE);
        addTotalRow(totals, "Total Tax",
                "Rs. " + taxTotal.toPlainString(),
                light(10), regular(10),
                GRAY_50);

        // Grand total
        PdfPCell gl = new PdfPCell(new Phrase("BALANCE DUE", white(11)));
        gl.setBackgroundColor(TEAL);
        gl.setBorder(Rectangle.NO_BORDER);
        gl.setPadding(12);
        totals.addCell(gl);

        PdfPCell gv = new PdfPCell(new Phrase(
                "Rs. " + inv.getTotalAmount().toPlainString(),
                white(13)));
        gv.setBackgroundColor(TEAL);
        gv.setBorder(Rectangle.NO_BORDER);
        gv.setPadding(12);
        gv.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totals.addCell(gv);

        PdfPCell totalsCell = new PdfPCell(totals);
        totalsCell.setBorder(Rectangle.NO_BORDER);
        outer.addCell(totalsCell);

        doc.add(outer);
    }

    private void addTotalRow(PdfPTable t, String label,
            String value, Font lf, Font vf, BaseColor bg) {
        PdfPCell l = new PdfPCell(new Phrase(label, lf));
        l.setBackgroundColor(bg); l.setPadding(10);
        l.setBorderColor(GRAY_100);
        t.addCell(l);

        PdfPCell v = new PdfPCell(new Phrase(value, vf));
        v.setBackgroundColor(bg); v.setPadding(10);
        v.setBorderColor(GRAY_100);
        v.setHorizontalAlignment(Element.ALIGN_RIGHT);
        t.addCell(v);
    }

    // ── Notes ───────────────────────────────────────
    private void buildNotes(Document doc, String notes)
            throws DocumentException {

        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(100);
        t.setSpacingBefore(4);

        PdfPCell c = new PdfPCell();
        c.setBackgroundColor(AMBER_BG);
        c.setBorderColor(new BaseColor(253, 230, 138));
        c.setPadding(14);

        c.addElement(new Paragraph("Notes",
                font(9, Font.BOLD, AMBER_TEXT)));
        c.addElement(new Paragraph(notes,
                font(10, Font.ITALIC, GRAY_700)));

        t.addCell(c);
        doc.add(t);
    }

    // ── Footer ──────────────────────────────────────
    private void buildFooter(Document doc)
            throws DocumentException {

        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(100);

        PdfPCell c = new PdfPCell();
        c.setBackgroundColor(GRAY_50);
        c.setBorderColor(GRAY_200);
        c.setPadding(16);

        Paragraph thanks = new Paragraph(
                "Thank you for your business!",
                font(11, Font.BOLD, GRAY_700));
        thanks.setAlignment(Element.ALIGN_CENTER);
        c.addElement(thanks);

        Paragraph sub = new Paragraph(
                "Generated by SmartInvoice  •  admin@smartinvoice.com",
                font(8, Font.NORMAL, GRAY_500));
        sub.setAlignment(Element.ALIGN_CENTER);
        sub.setSpacingBefore(4);
        c.addElement(sub);

        t.addCell(c);
        doc.add(t);
    }
}