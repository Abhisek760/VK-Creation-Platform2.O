package com.vkcreation.platform.booking;

import com.vkcreation.platform.payment.PaymentRepository;
import com.vkcreation.platform.payment.PaymentStatus;
import com.vkcreation.platform.user.UserAccount;
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
    private final PaymentRepository payments;

    public BookingService(BookingRepository bookings, PaymentRepository payments) {
        this.bookings = bookings;
        this.payments = payments;
    }

    @Transactional
    public BookingDtos.BookingResponse create(UserAccount client, BookingDtos.CreateRequest request) {
        String code = "VK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Booking booking = new Booking(UUID.randomUUID().toString(), code, client, request.eventType().trim(),
                request.eventDate(), request.eventLocation(), request.packageName(), request.notes());
        return response(bookings.save(booking));
    }

    @Transactional(readOnly = true)
    public List<BookingDtos.BookingResponse> myBookings(UserAccount client) {
        return bookings.findByClient_IdOrderByCreatedAtDesc(client.getId()).stream().map(this::response).toList();
    }

    @Transactional(readOnly = true)
    public List<BookingDtos.BookingResponse> all() {
        return bookings.findAll().stream().map(BookingDtos.BookingResponse::from).toList();
    }

    @Transactional
    public BookingDtos.BookingResponse setAmounts(String id, BookingDtos.AmountRequest request) {
        if (request.advanceAmount().compareTo(request.totalAmount()) > 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Advance cannot exceed total amount.");
        Booking booking = get(id);
        BigDecimal verified = payments.sumAmountByBookingAndStatuses(booking.getId(), List.of(PaymentStatus.VERIFIED));
        BigDecimal submitted = payments.sumAmountByBookingAndStatuses(booking.getId(), List.of(PaymentStatus.SUBMITTED));
        if (request.advanceAmount().compareTo(verified) < 0 || request.advanceAmount().compareTo(submitted.add(verified)) < 0)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Advance cannot be reduced below payments already submitted or verified.");
        booking.setAmounts(request.totalAmount(), request.advanceAmount());
        if (booking.getStatus() == BookingStatus.PENDING) booking.setStatus(BookingStatus.APPROVED);
        return response(bookings.save(booking));
    }

    @Transactional
    public BookingDtos.BookingResponse setInstallment(String id, BookingDtos.InstallmentRequest request) {
        Booking booking = get(id);
        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.REJECTED
                || booking.getStatus() == BookingStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot set an installment for this booking status.");
        }
        BigDecimal verified = payments.sumAmountByBookingAndStatuses(booking.getId(), List.of(PaymentStatus.VERIFIED));
        BigDecimal submitted = payments.sumAmountByBookingAndStatuses(booking.getId(), List.of(PaymentStatus.SUBMITTED));
        if (submitted.compareTo(BigDecimal.ZERO) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Review the pending payment submission before setting the next installment.");
        }
        BigDecimal remaining = booking.getTotalAmount().subtract(verified);
        if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The booking is already fully paid.");
        }
        if (request.amount().compareTo(remaining) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Installment cannot exceed the remaining total balance of " + remaining + ".");
        }
        booking.setCurrentDueAmount(request.amount());
        return BookingDtos.BookingResponse.from(bookings.save(booking));
    }

    @Transactional
    public BookingDtos.BookingResponse setStatus(String id, BookingStatus status) {
        Booking booking = get(id);
        if (status == BookingStatus.CONFIRMED) {
            if (booking.getAdvanceAmount().compareTo(BigDecimal.ZERO) <= 0)
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Set an advance amount before confirming this booking.");
            BigDecimal verified = payments.sumAmountByBookingAndStatuses(booking.getId(), List.of(PaymentStatus.VERIFIED));
            if (verified.compareTo(booking.getAdvanceAmount()) < 0)
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Booking can only be confirmed after the full advance payment is verified.");
        }
        booking.setStatus(status);
        return BookingDtos.BookingResponse.from(bookings.save(booking));
    }

    private BookingDtos.BookingResponse response(Booking booking) {
        BigDecimal verified = payments.sumAmountByBookingAndStatuses(booking.getId(), List.of(PaymentStatus.VERIFIED));
        return BookingDtos.BookingResponse.from(booking, verified);
    }

    private Booking get(String id) {
        return bookings.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found."));
    }
}
