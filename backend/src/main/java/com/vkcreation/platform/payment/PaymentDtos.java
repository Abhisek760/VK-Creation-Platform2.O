package com.vkcreation.platform.payment;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public final class PaymentDtos {
 private PaymentDtos(){}
 public record SubmitRequest(@NotNull @DecimalMin("0.01") BigDecimal amount,@NotBlank @Size(max=32) String method,@Size(max=160) String providerReference,@Size(max=1000) String proofUrl){}
 public record VerifyRequest(@NotNull Boolean approved){}
 public record PaymentResponse(String id,String bookingId,BigDecimal amount,String method,PaymentStatus status,String providerReference,String proofUrl,LocalDateTime verifiedAt){
  static PaymentResponse from(Payment p){return new PaymentResponse(p.getId(),p.getBooking().getId(),p.getAmount(),p.getMethod(),p.getStatus(),p.getProviderReference(),p.getProofUrl(),p.getVerifiedAt());}
 }
}