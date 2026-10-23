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
 private final PaymentRepository payments; private final BookingRepository bookings;
 public PaymentService(PaymentRepository payments,BookingRepository bookings){this.payments=payments;this.bookings=bookings;}
 @Transactional public PaymentDtos.PaymentResponse submit(String bookingId,UserAccount client,PaymentDtos.SubmitRequest r){
  Booking b=bookings.findById(bookingId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Booking not found."));
  if(!b.getClient().getId().equals(client.getId())) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Booking not found.");
  if(b.getAdvanceAmount().compareTo(BigDecimal.ZERO)<=0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Admin has not set an advance amount.");
  if(r.amount().compareTo(b.getAdvanceAmount())>0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Payment cannot exceed the configured advance amount.");
  Payment p=payments.save(new Payment(UUID.randomUUID().toString(),b,r.amount(),r.method().trim(),r.providerReference(),r.proofUrl()));
  return PaymentDtos.PaymentResponse.from(p);
 }
 @Transactional(readOnly=true) public List<PaymentDtos.PaymentResponse> mine(UserAccount client){
  return payments.findByBooking_Client_IdOrderByCreatedAtDesc(client.getId()).stream().map(PaymentDtos.PaymentResponse::from).toList();
 }
 @Transactional(readOnly=true) public List<PaymentDtos.PaymentResponse> all(){
  return payments.findAll().stream().map(PaymentDtos.PaymentResponse::from).toList();
 }
 @Transactional public PaymentDtos.PaymentResponse verify(String id,UserAccount admin,boolean approved){
  Payment p=payments.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Payment not found."));
  if(p.getStatus()!=PaymentStatus.SUBMITTED) throw new ResponseStatusException(HttpStatus.CONFLICT,"Payment has already been reviewed.");
  if(approved){p.verify(admin);p.getBooking().setStatus(BookingStatus.CONFIRMED);}else p.reject(admin);
  payments.save(p);return PaymentDtos.PaymentResponse.from(p);
 }
}