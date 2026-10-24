package com.vkcreation.platform.payment;

import com.vkcreation.platform.booking.*;
import com.vkcreation.platform.user.UserAccount;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {
    private final PaymentRepository payments;
    private final BookingRepository bookings;

    public PaymentService(PaymentRepository payments, BookingRepository bookings) {
        this.payments = payments;
        this.bookings = bookings;
    }

    @Transactional
    public PaymentDtos.PaymentResponse submit(String bookingId, UserAccount client, PaymentDtos.SubmitRequest request) {
        Booking booking = bookings.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found."));
        if (!booking.getClient().getId().equals(client.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found.");
        }
        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.REJECTED
                || booking.getStatus() == BookingStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Payments are not accepted for this booking status.");
        }
        if (booking.getCurrentDueAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No installment is currently due. Ask the admin to set the next installment.");
        }
        BigDecimal submitted = payments.sumAmountByBookingAndStatuses(bookingId, List.of(PaymentStatus.SUBMITTED));
        if (submitted.compareTo(BigDecimal.ZERO) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A payment submission is already awaiting admin review.");
        }
        BigDecimal verified = payments.sumAmountByBookingAndStatuses(bookingId, List.of(PaymentStatus.VERIFIED));
        BigDecimal remaining = booking.getTotalAmount().subtract(verified);
        if (request.amount().compareTo(booking.getCurrentDueAmount()) != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Payment must exactly match the current installment of " + booking.getCurrentDueAmount() + ".");
        }
        if (request.amount().compareTo(remaining) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment cannot exceed the remaining total balance of " + remaining + ".");
        }
        Payment payment = payments.save(new Payment(UUID.randomUUID().toString(), booking, request.amount(),
                request.method().trim(), request.providerReference(), request.proofUrl()));
        if (booking.getStatus() == BookingStatus.APPROVED) {
            booking.setStatus(BookingStatus.AWAITING_PAYMENT);
        }
        return PaymentDtos.PaymentResponse.from(payment);
    }

    @Transactional(readOnly = true)
    public String receiptHtml(String paymentId, UserAccount client) {
        Payment payment = payments.findById(paymentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found."));
        if (!payment.getBooking().getClient().getId().equals(client.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found.");
        }
        if (payment.getStatus() != PaymentStatus.VERIFIED || payment.getReceiptNumber() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A receipt is available only after admin verifies the payment.");
        }
        Booking booking = payment.getBooking();
        return "<!doctype html><html><head><meta charset=\"utf-8\"><title>Payment Receipt</title>" +
                "<style>body{font:16px Arial,sans-serif;max-width:720px;margin:40px auto;padding:24px;color:#222}h1{margin-bottom:4px}.muted{color:#666}.line{border-top:1px solid #ddd;margin:20px 0}.amount{font-size:24px;font-weight:bold}</style></head><body>" +
                "<h1>VK Creation</h1><p class=\"muted\">Payment receipt</p><div class=\"line\"></div>" +
                "<p><b>Receipt no:</b> " + html(payment.getReceiptNumber()) + "</p>" +
                "<p><b>Booking:</b> " + html(booking.getBookingCode()) + "</p>" +
                "<p><b>Client:</b> " + html(booking.getClientName()) + "</p>" +
                "<p><b>Event:</b> " + html(booking.getEventType()) + "</p>" +
                "<p><b>Payment method:</b> " + html(payment.getMethod()) + "</p>" +
                "<p><b>Transaction reference:</b> " + html(payment.getProviderReference() == null ? "—" : payment.getProviderReference()) + "</p>" +
                "<p><b>Verified at:</b> " + html(String.valueOf(payment.getVerifiedAt())) + "</p>" +
                "<div class=\"line\"></div><p class=\"amount\">Amount received: ₹" + payment.getAmount().toPlainString() + "</p>" +
                "<p class=\"muted\">This receipt represents a payment verified by VK Creation.</p><script>window.print()</script></body></html>";
    }

    private String html(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    @Transactional(readOnly = true)
    public List<PaymentDtos.PaymentResponse> mine(UserAccount client) {
        return payments.findByBooking_Client_IdOrderByCreatedAtDesc(client.getId())
                .stream().map(PaymentDtos.PaymentResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentDtos.PaymentResponse> all() {
        return payments.findAll().stream().map(PaymentDtos.PaymentResponse::from).toList();
    }

    @Transactional
    public PaymentDtos.PaymentResponse verify(String id, UserAccount admin, boolean approved) {
        Payment payment = payments.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found."));
        if (payment.getStatus() != PaymentStatus.SUBMITTED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Payment has already been reviewed.");
        }
        Booking booking = payment.getBooking();
        if (approved) {
            BigDecimal alreadyVerified = payments.sumAmountByBookingAndStatuses(booking.getId(), List.of(PaymentStatus.VERIFIED));
            BigDecimal totalAfterPayment = alreadyVerified.add(payment.getAmount());
            if (totalAfterPayment.compareTo(booking.getTotalAmount()) > 0) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Verifying this payment would exceed the booking total.");
            }
            payment.verify(admin);
            booking.setCurrentDueAmount(BigDecimal.ZERO);
            if (totalAfterPayment.compareTo(booking.getTotalAmount()) >= 0) {
                booking.setStatus(BookingStatus.COMPLETED);
            } else if (totalAfterPayment.compareTo(booking.getAdvanceAmount()) >= 0) {
                booking.setStatus(BookingStatus.CONFIRMED);
            } else {
                booking.setStatus(BookingStatus.AWAITING_PAYMENT);
            }
        } else {
            payment.reject(admin);
            if (booking.getStatus() == BookingStatus.AWAITING_PAYMENT) {
                booking.setStatus(BookingStatus.APPROVED);
            }
        }
        payments.save(payment);
        return PaymentDtos.PaymentResponse.from(payment);
    }
}
