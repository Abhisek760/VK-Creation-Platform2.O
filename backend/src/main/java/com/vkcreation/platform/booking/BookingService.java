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
        return bookings.findAll().stream().map(this::response).toList();
    }

    @Transactional
    public BookingDtos.BookingResponse setAmounts(String id, BookingDtos.AmountRequest request) {
        if (request.advanceAmount().compareTo(request.totalAmount()) > 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Advance cannot exceed total amount.");
        Booking booking = bookings.findByIdForUpdate(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found."));
        BigDecimal verified = payments.sumAmountByBookingAndStatuses(booking.getId(), List.of(PaymentStatus.VERIFIED));
        BigDecimal submitted = payments.sumAmountByBookingAndStatuses(booking.getId(), List.of(PaymentStatus.SUBMITTED));
        if (request.advanceAmount().compareTo(verified) < 0 || request.advanceAmount().compareTo(submitted.add(verified)) < 0)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Advance cannot be reduced below payments already submitted or verified.");
        if (request.totalAmount().compareTo(verified) < 0 || request.totalAmount().compareTo(submitted.add(verified)) < 0)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Total amount cannot be reduced below payments already submitted or verified.");
        booking.setAmounts(request.totalAmount(), request.advanceAmount());
        if (booking.getStatus() == BookingStatus.PENDING) booking.setStatus(BookingStatus.APPROVED);
        return response(bookings.save(booking));
    }

    @Transactional
    public BookingDtos.BookingResponse setInstallment(String id, BookingDtos.InstallmentRequest request) {
        Booking booking = bookings.findByIdForUpdate(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found."));
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
        return response(bookings.save(booking));
    }

    @Transactional
    public BookingDtos.BookingResponse setStatus(String id, BookingStatus status) {
        Booking booking = bookings.findByIdForUpdate(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found."));
        if (status == BookingStatus.COMPLETED) {
            BigDecimal totalVerified = payments.sumAmountByBookingAndStatuses(booking.getId(), List.of(PaymentStatus.VERIFIED));
            if (totalVerified.compareTo(booking.getTotalAmount()) < 0)
                throw new ResponseStatusException(HttpStatus.CONFLICT, "A booking can only be marked completed after the full total is verified as paid.");
        }
        if (status == BookingStatus.CONFIRMED) {
            if (booking.getAdvanceAmount().compareTo(BigDecimal.ZERO) <= 0)
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Set an advance amount before confirming this booking.");
            BigDecimal verified = payments.sumAmountByBookingAndStatuses(booking.getId(), List.of(PaymentStatus.VERIFIED));
            if (verified.compareTo(booking.getAdvanceAmount()) < 0)
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Booking can only be confirmed after the full advance payment is verified.");
        }
        booking.setStatus(status);
        return response(bookings.save(booking));
    }

    @Transactional(readOnly = true)
    public String finalBillHtml(String id, UserAccount client) {
        Booking booking = get(id);
        if (!booking.getClient().getId().equals(client.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found.");
        }
        BigDecimal verified = payments.sumAmountByBookingAndStatuses(booking.getId(), List.of(PaymentStatus.VERIFIED));
        if (verified.compareTo(booking.getTotalAmount()) < 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Final bill is available only after the full booking amount is verified as paid.");
        }
        StringBuilder rows = new StringBuilder();
        payments.findByBooking_IdOrderByCreatedAtAsc(booking.getId()).stream()
                .filter(p -> p.getStatus() == PaymentStatus.VERIFIED)
                .forEach(p -> rows.append("<tr><td>").append(html(p.getReceiptNumber() == null ? "—" : p.getReceiptNumber()))
                        .append("</td><td>").append(html(p.getVerifiedAt() == null ? "—" : p.getVerifiedAt().toString()))
                        .append("</td><td>₹").append(p.getAmount().toPlainString()).append("</td></tr>"));
        return "<!doctype html><html><head><meta charset=\"utf-8\"><title>Final Bill</title><style>body{font:16px Arial,sans-serif;max-width:800px;margin:40px auto;padding:24px;color:#222}table{width:100%;border-collapse:collapse}td,th{padding:10px;border-bottom:1px solid #ddd;text-align:left}.amount{font-size:24px;font-weight:bold}</style></head><body>" +
                "<h1>VK Creation</h1><h2>Final Bill</h2><p><b>Booking:</b> " + html(booking.getBookingCode()) +
                "</p><p><b>Client:</b> " + html(booking.getClientName()) + "</p><p><b>Event:</b> " + html(booking.getEventType()) +
                "</p><p><b>Event date:</b> " + html(booking.getEventDate().toString()) + "</p><hr><p>Total booking amount: ₹" +
                booking.getTotalAmount().toPlainString() + "</p><p>Total verified payments: ₹" + verified.toPlainString() +
                "</p><p class=\"amount\">Balance due: ₹0.00</p><h3>Verified installment receipts</h3><table><thead><tr><th>Receipt</th><th>Verified at</th><th>Amount</th></tr></thead><tbody>" +
                rows + "</tbody></table><p>All payments listed above have been verified by VK Creation.</p><script>window.print()</script></body></html>";
    }

    private String html(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    private BookingDtos.BookingResponse response(Booking booking) {
        BigDecimal verified = payments.sumAmountByBookingAndStatuses(booking.getId(), List.of(PaymentStatus.VERIFIED));
        return BookingDtos.BookingResponse.from(booking, verified);
    }

    private Booking get(String id) {
        return bookings.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found."));
    }
}
