package com.vkcreation.platform.booking;

import com.vkcreation.platform.user.UserAccount;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService service;
    public BookingController(BookingService service) { this.service=service; }

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

    @PatchMapping("/{id}/advance")
    @PreAuthorize("hasRole('ADMIN')")
    public BookingDtos.BookingResponse setAmounts(@PathVariable String id, @Valid @RequestBody BookingDtos.AmountRequest request) {
        return service.setAmounts(id, request);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public BookingDtos.BookingResponse setStatus(@PathVariable String id, @Valid @RequestBody BookingDtos.StatusRequest request) {
        return service.setStatus(id, request.status());
    }
}
