package com.vkcreation.platform.settings;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="studio_settings")
public class StudioSettings {
 @Id @Column(length=36,nullable=false) private String id;
 @Column(name="studio_name",nullable=false,length=160) private String studioName;
 @Column(name="contact_email",length=254) private String contactEmail;
 @Column(name="contact_phone",length=32) private String contactPhone;
 @Column(name="upi_id",length=160) private String upiId;
 @Column(name="upi_qr_url",length=1200) private String upiQrUrl;
 @Column(name="bank_name",length=160) private String bankName;
 @Column(name="bank_account_name",length=160) private String bankAccountName;
 @Column(name="bank_account_number",length=80) private String bankAccountNumber;
 @Column(name="bank_ifsc",length=20) private String bankIfsc;
 @Column(name="payment_instructions",length=2000) private String paymentInstructions;
 @Column(name="legal_terms",length=8000) private String legalTerms;
 @Column(name="updated_at",insertable=false,updatable=false) private LocalDateTime updatedAt;
 protected StudioSettings(){}
 public StudioSettings(String id){this.id=id;this.studioName="VK Creation";this.paymentInstructions="Use your booking code as the payment reference. Manual transfers count only after admin verification.";}
 public String getId(){return id;} public String getStudioName(){return studioName;} public String getContactEmail(){return contactEmail;} public String getContactPhone(){return contactPhone;} public String getUpiId(){return upiId;} public String getUpiQrUrl(){return upiQrUrl;} public String getBankName(){return bankName;} public String getBankAccountName(){return bankAccountName;} public String getBankAccountNumber(){return bankAccountNumber;} public String getBankIfsc(){return bankIfsc;} public String getPaymentInstructions(){return paymentInstructions;} public String getLegalTerms(){return legalTerms;} public LocalDateTime getUpdatedAt(){return updatedAt;}
 public void update(String studioName,String contactEmail,String contactPhone,String upiId,String upiQrUrl,String bankName,String bankAccountName,String bankAccountNumber,String bankIfsc,String paymentInstructions,String legalTerms){this.studioName=studioName;this.contactEmail=contactEmail;this.contactPhone=contactPhone;this.upiId=upiId;this.upiQrUrl=upiQrUrl;this.bankName=bankName;this.bankAccountName=bankAccountName;this.bankAccountNumber=bankAccountNumber;this.bankIfsc=bankIfsc;this.paymentInstructions=paymentInstructions;this.legalTerms=legalTerms;}
}