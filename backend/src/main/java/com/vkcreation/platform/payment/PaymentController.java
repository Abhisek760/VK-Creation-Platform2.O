package com.vkcreation.platform.payment;
import com.vkcreation.platform.user.UserAccount;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api")
public class PaymentController {
 private final PaymentService service;
 public PaymentController(PaymentService service){this.service=service;}
 @PostMapping("/bookings/{bookingId}/payments") @PreAuthorize("hasRole('CLIENT')")
 public PaymentDtos.PaymentResponse submit(@PathVariable String bookingId,@AuthenticationPrincipal UserAccount user,@Valid @RequestBody PaymentDtos.SubmitRequest request){return service.submit(bookingId,user,request);}
 @GetMapping("/payments/my") @PreAuthorize("hasRole('CLIENT')")
 public List<PaymentDtos.PaymentResponse> mine(@AuthenticationPrincipal UserAccount user){return service.mine(user);}
 @GetMapping("/payments") @PreAuthorize("hasRole('ADMIN')")
 public List<PaymentDtos.PaymentResponse> all(){return service.all();}
 @PatchMapping("/payments/{id}/verify") @PreAuthorize("hasRole('ADMIN')")
 public PaymentDtos.PaymentResponse verify(@PathVariable String id,@AuthenticationPrincipal UserAccount admin,@Valid @RequestBody PaymentDtos.VerifyRequest request){return service.verify(id,admin,request.approved());}
}