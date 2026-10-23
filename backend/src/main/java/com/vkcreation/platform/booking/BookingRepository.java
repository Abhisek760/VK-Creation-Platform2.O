package com.vkcreation.platform.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking,String> {
    List<Booking> findByClient_IdOrderByCreatedAtDesc(String clientId);
}
