package com.vkcreation.platform.booking;

import com.vkcreation.platform.user.UserAccount;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name="bookings")
public class Booking {
    @Id @Column(length=36, nullable=false) private String id;
    @Column(name="booking_code", nullable=false, unique=true, length=32) private String bookingCode;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="client_id", nullable=false) private UserAccount client;
    @Column(name="client_name", nullable=false, length=160) private String clientName;
    @Column(name="client_email", nullable=false, length=254) private String clientEmail;
    @Column(name="client_phone", nullable=false, length=32) private String clientPhone;
    @Column(name="event_type", nullable=false, length=80) private String eventType;
    @Column(name="event_date", nullable=false) private LocalDate eventDate;
    @Column(name="event_location", length=500) private String eventLocation;
    @Column(name="package_name", length=160) private String packageName;
    @Column(name="total_amount", nullable=false, precision=12, scale=2) private BigDecimal totalAmount=BigDecimal.ZERO;
    @Column(name="advance_amount", nullable=false, precision=12, scale=2) private BigDecimal advanceAmount=BigDecimal.ZERO;
    @Column(name="current_due_amount", nullable=false, precision=12, scale=2) private BigDecimal currentDueAmount=BigDecimal.ZERO;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=32) private BookingStatus status=BookingStatus.PENDING;
    @Column(columnDefinition="TEXT") private String notes;
    @Column(name="created_at", insertable=false, updatable=false) private LocalDateTime createdAt;
    @Column(name="updated_at", insertable=false, updatable=false) private LocalDateTime updatedAt;

    protected Booking() {}
    public Booking(String id, String code, UserAccount client, String eventType, LocalDate eventDate, String location, String packageName, String notes) {
        this.id=id; this.bookingCode=code; this.client=client; this.clientName=client.getFullName(); this.clientEmail=client.getEmail();
        this.clientPhone=client.getPhone()==null ? "" : client.getPhone(); this.eventType=eventType; this.eventDate=eventDate;
        this.eventLocation=location; this.packageName=packageName; this.notes=notes;
    }
    public String getId(){return id;} public String getBookingCode(){return bookingCode;} public UserAccount getClient(){return client;}
    public String getClientName(){return clientName;} public String getClientEmail(){return clientEmail;} public String getClientPhone(){return clientPhone;}
    public String getEventType(){return eventType;} public LocalDate getEventDate(){return eventDate;} public String getEventLocation(){return eventLocation;}
    public String getPackageName(){return packageName;} public BigDecimal getTotalAmount(){return totalAmount;} public BigDecimal getAdvanceAmount(){return advanceAmount;} public BigDecimal getCurrentDueAmount(){return currentDueAmount;}
    public BookingStatus getStatus(){return status;} public String getNotes(){return notes;}
    public void setAmounts(BigDecimal total, BigDecimal advance){if(this.totalAmount.compareTo(BigDecimal.ZERO)==0 && this.currentDueAmount.compareTo(BigDecimal.ZERO)==0){this.currentDueAmount=advance;} this.totalAmount=total; this.advanceAmount=advance;}
    public void setCurrentDueAmount(BigDecimal amount){this.currentDueAmount=amount;}
    public void setStatus(BookingStatus status){this.status=status;}
}
