package com.vkcreation.platform.booking;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public final class BookingDtos {
    private BookingDtos() {}
    public record CreateRequest(@NotBlank @Size(max=80) String eventType,
                               @NotNull @FutureOrPresent LocalDate eventDate,
                               @Size(max=500) String eventLocation,
                               @Size(max=160) String packageName,
                               @Size(max=4000) String notes) {}
    public record AmountRequest(@NotNull @DecimalMin("0.00") BigDecimal totalAmount,
                                @NotNull @DecimalMin("0.00") BigDecimal advanceAmount) {}
    public record InstallmentRequest(@NotNull @DecimalMin("0.01") BigDecimal amount) {}
    public record StatusRequest(@NotNull BookingStatus status) {}
    public record BookingResponse(String id, String bookingCode, String clientName, String clientEmail,
                                  String clientPhone, String eventType, LocalDate eventDate, String eventLocation,
                                  String packageName, BigDecimal totalAmount, BigDecimal advanceAmount, BigDecimal currentDueAmount,
                                  BigDecimal remainingBalance, BookingStatus status, String notes) {
        static BookingResponse from(Booking b) { return from(b, b.getAdvanceAmount()); }
        static BookingResponse from(Booking b, BigDecimal paidAmount) {
            return new BookingResponse(b.getId(), b.getBookingCode(), b.getClientName(), b.getClientEmail(),
                    b.getClientPhone(), b.getEventType(), b.getEventDate(), b.getEventLocation(), b.getPackageName(),
                    b.getTotalAmount(), b.getAdvanceAmount(), b.getCurrentDueAmount(), b.getTotalAmount().subtract(paidAmount),
                    b.getStatus(), b.getNotes());
        }
    }
}
