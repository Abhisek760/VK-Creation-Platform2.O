package com.vkcreation.platform.payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface PaymentRepository extends JpaRepository<Payment,String> {
 List<Payment> findByBooking_Client_IdOrderByCreatedAtDesc(String clientId);
}