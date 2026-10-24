package com.vkcreation.platform.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment,String> {
 List<Payment> findByBooking_Client_IdOrderByCreatedAtDesc(String clientId);
 List<Payment> findByBooking_IdOrderByCreatedAtAsc(String bookingId);
 @Query("select coalesce(sum(p.amount), 0) from Payment p where p.booking.id = :bookingId and p.status in :statuses")
 BigDecimal sumAmountByBookingAndStatuses(@Param("bookingId") String bookingId, @Param("statuses") List<PaymentStatus> statuses);
}