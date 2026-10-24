package com.vkcreation.platform.payment;

import com.vkcreation.platform.booking.*;
import com.vkcreation.platform.user.UserAccount;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import java.math.BigDecimal;

@Service
public class PaymentService {
 private static final List<PaymentStatus> RESERVED_STATUSES = List.of(PaymentStatus.SUBMITTED, PaymentStatus.VERIFIED);
 private final PaymentRepository payments;
 private final BookingRepository bookings;
 public PaymentService(PaymentRepository payments, BookingRepository bookings) { this.payments=payments; this.bookings=bookings; }

 @Transactional
 public PaymentDtos.PaymentResponse submit(String bookingId, UserAccount client, PaymentDtos.SubmitRequest request) {
  Booking booking = bookings.findById(bookingId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found."));
  if (!booking.getClient().getId().equals(client.getId())) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found.");
  if (booking.getAdvanceAmount().compareTo(BigDecimal.ZERO) <= 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Admin has not set an advance amount.");
  if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.REJECTED || booking.getStatus() == BookingStatus.COMPLETED)
   throw new ResponseStatusException(HttpStatus.CONFLICT, "Payments are not accepted for this booking status.");
  BigDecimal reserved = payments.sumAmountByBookingAndStatuses(bookingId, RESERVED_STATUSES);
  BigDecimal remaining = booking.getAdvanceAmount().subtract(reserved);
  if (request.amount().compareTo(remaining) != 0)
   throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment must exactly match the remaining advance amount of " + remaining + ".");
  Payment payment = payments.save(new Payment(UUID.randomUUID().toString(), booking, request.amount(), request.method().trim(), request.providerReference(), request.proofUrl()));
  if (booking.getStatus() == BookingStatus.APPROVED) booking.setStatus(BookingStatus.AWAITING_PAYMENT);
  return PaymentDtos.PaymentResponse.from(payment);
 }

 @Transactional(readOnly=true)
 public List<PaymentDtos.PaymentResponse> mine(UserAccount client) {
  return payments.findByBooking_Client_IdOrderByCreatedAtDesc(client.getId()).stream().map(PaymentDtos.PaymentResponse::from).toList();
 }

 @Transactional(readOnly=true)
 public List<PaymentDtos.PaymentResponse> all() {
  return payments.findAll().stream().map(PaymentDtos.PaymentResponse::from).toList();
 }

 @Transactional
 public PaymentDtos.PaymentResponse verify(String id, UserAccount admin, boolean approved) {
  Payment payment = payments.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found."));
  if (payment.getStatus() != PaymentStatus.SUBMITTED) throw new ResponseStatusException(HttpStatus.CONFLICT, "Payment has already been reviewed.");
  Booking booking = payment.getBooking();
  if (approved) {
   BigDecimal alreadyVerified = payments.sumAmountByBookingAndStatuses(booking.getId(), List.of(PaymentStatus.VERIFIED));
   if (alreadyVerified.add(payment.getAmount()).compareTo(booking.getAdvanceAmount()) > 0)
    throw new ResponseStatusException(HttpStatus.CONFLICT, "Verifying this payment would exceed the booking advance amount.");
   payment.verify(admin);
   if (alreadyVerified.add(payment.getAmount()).compareTo(booking.getAdvanceAmount()) >= 0) booking.setStatus(BookingStatus.CONFIRMED);
   else booking.setStatus(BookingStatus.AWAITING_PAYMENT);
  } else {
   payment.reject(admin);
   if (booking.getStatus() == BookingStatus.AWAITING_PAYMENT) booking.setStatus(BookingStatus.APPROVED);
  }
  payments.save(payment);
  return PaymentDtos.PaymentResponse.from(payment);
 }
}