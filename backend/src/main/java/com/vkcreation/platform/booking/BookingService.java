package com.vkcreation.platform.booking;

import com.vkcreation.platform.user.UserAccount;
import com.vkcreation.platform.user.UserRole;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class BookingService {
    private final BookingRepository bookings;
    public BookingService(BookingRepository bookings) { this.bookings = bookings; }

    @Transactional
    public BookingDtos.BookingResponse create(UserAccount client, BookingDtos.CreateRequest request) {
        String code = "VK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Booking booking = new Booking(UUID.randomUUID().toString(), code, client, request.eventType().trim(),
                request.eventDate(), request.eventLocation(), request.packageName(), request.notes());
        return BookingDtos.BookingResponse.from(bookings.save(booking));
    }

    @Transactional(readOnly = true)
    public List<BookingDtos.BookingResponse> myBookings(UserAccount client) {
        return bookings.findByClient_IdOrderByCreatedAtDesc(client.getId()).stream().map(BookingDtos.BookingResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<BookingDtos.BookingResponse> all() {
        return bookings.findAll().stream().map(BookingDtos.BookingResponse::from).toList();
    }

    @Transactional
    public BookingDtos.BookingResponse setAmounts(String id, BookingDtos.AmountRequest request) {
        if (request.advanceAmount().compareTo(request.totalAmount()) > 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Advance cannot exceed total amount.");
        Booking b = get(id);
        b.setAmounts(request.totalAmount(), request.advanceAmount());
        if (b.getStatus() == BookingStatus.PENDING) b.setStatus(BookingStatus.APPROVED);
        return BookingDtos.BookingResponse.from(bookings.save(b));
    }

    @Transactional
    public BookingDtos.BookingResponse setStatus(String id, BookingStatus status) {
        Booking b = get(id);
        b.setStatus(status);
        return BookingDtos.BookingResponse.from(bookings.save(b));
    }

    private Booking get(String id) {
        return bookings.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found."));
    }
}
