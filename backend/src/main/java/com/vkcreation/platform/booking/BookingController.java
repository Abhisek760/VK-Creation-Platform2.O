package com.vkcreation.platform.booking;

import com.vkcreation.platform.user.UserAccount;
import com.vkcreation.platform.common.PdfDocumentService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService service;
    private final PdfDocumentService pdf;
    public BookingController(BookingService service, PdfDocumentService pdf) { this.service=service; this.pdf=pdf; }

    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public BookingDtos.BookingResponse create(@AuthenticationPrincipal UserAccount user, @Valid @RequestBody BookingDtos.CreateRequest request) {
        return service.create(user, request);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CLIENT')")
    public List<BookingDtos.BookingResponse> mine(@AuthenticationPrincipal UserAccount user) { return service.myBookings(user); }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<BookingDtos.BookingResponse> all() { return service.all(); }

    @GetMapping(value = "/{id}/final-bill", produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<String> finalBill(@PathVariable String id, @AuthenticationPrincipal UserAccount user) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=vk-final-bill.html")
                .contentType(MediaType.TEXT_HTML)
                .body(service.finalBillHtml(id, user));
    }

    @GetMapping(value = "/{id}/final-bill.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<byte[]> finalBillPdf(@PathVariable String id, @AuthenticationPrincipal UserAccount user) {
        byte[] bytes = pdf.fromHtml(service.finalBillHtml(id, user));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=vk-final-bill.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }

    @PatchMapping("/{id}/advance")
    @PreAuthorize("hasRole('ADMIN')")
    public BookingDtos.BookingResponse setAmounts(@PathVariable String id, @Valid @RequestBody BookingDtos.AmountRequest request) {
        return service.setAmounts(id, request);
    }

    @PatchMapping("/{id}/installment")
    @PreAuthorize("hasRole('ADMIN')")
    public BookingDtos.BookingResponse setInstallment(@PathVariable String id, @Valid @RequestBody BookingDtos.InstallmentRequest request) {
        return service.setInstallment(id, request);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public BookingDtos.BookingResponse setStatus(@PathVariable String id, @Valid @RequestBody BookingDtos.StatusRequest request) {
        return service.setStatus(id, request.status());
    }
}
