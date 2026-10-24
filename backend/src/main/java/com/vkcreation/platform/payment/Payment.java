package com.vkcreation.platform.payment;
import com.vkcreation.platform.booking.Booking;
import com.vkcreation.platform.user.UserAccount;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity @Table(name="payments")
public class Payment {
 @Id @Column(length=36,nullable=false) private String id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="booking_id",nullable=false) private Booking booking;
 @Column(nullable=false,precision=12,scale=2) private BigDecimal amount;
 @Column(nullable=false,length=32) private String method;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=24) private PaymentStatus status=PaymentStatus.SUBMITTED;
 @Column(name="provider_reference",length=160) private String providerReference;
 @Column(name="receipt_number",length=40,unique=true) private String receiptNumber;
 @Column(name="proof_url",length=1000) private String proofUrl;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="verified_by") private UserAccount verifiedBy;
 @Column(name="verified_at") private LocalDateTime verifiedAt;
 @Column(name="created_at",insertable=false,updatable=false) private LocalDateTime createdAt;
 protected Payment(){}
 public Payment(String id,Booking booking,BigDecimal amount,String method,String reference,String proof){this.id=id;this.booking=booking;this.amount=amount;this.method=method;this.providerReference=reference;this.proofUrl=proof;}
 public String getId(){return id;} public Booking getBooking(){return booking;} public BigDecimal getAmount(){return amount;} public String getMethod(){return method;} public String getReceiptNumber(){return receiptNumber;}
 public PaymentStatus getStatus(){return status;} public String getProviderReference(){return providerReference;} public String getProofUrl(){return proofUrl;}
 public UserAccount getVerifiedBy(){return verifiedBy;} public LocalDateTime getVerifiedAt(){return verifiedAt;}
 public void verify(UserAccount admin){status=PaymentStatus.VERIFIED;verifiedBy=admin;verifiedAt=LocalDateTime.now();receiptNumber="VK-RCPT-"+java.time.Year.now().getValue()+"-"+id.substring(0,8).toUpperCase();}
 public void reject(UserAccount admin){status=PaymentStatus.REJECTED;verifiedBy=admin;verifiedAt=LocalDateTime.now();}
}