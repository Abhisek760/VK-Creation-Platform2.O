package com.vkcreation.platform.settings;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.UUID;
@RestController @RequestMapping("/api/settings")
public class StudioSettingsController {
 private static final String ID="vk-studio-settings";
 private final StudioSettingsRepository settings;
 public StudioSettingsController(StudioSettingsRepository settings){this.settings=settings;}
 public record SettingsRequest(@NotBlank @Size(max=160) String studioName,@Email @Size(max=254) String contactEmail,@Size(max=32) String contactPhone,@Size(max=160) String upiId,@Size(max=1200) String upiQrUrl,@Size(max=160) String bankName,@Size(max=160) String bankAccountName,@Size(max=80) String bankAccountNumber,@Size(max=20) String bankIfsc,@Size(max=2000) String paymentInstructions,@Size(max=8000) String legalTerms){}
 @GetMapping public StudioSettings get(){return settings.findById(ID).orElseGet(()->settings.save(new StudioSettings(ID)));}
 @PutMapping @PreAuthorize("hasRole('ADMIN')") public StudioSettings update(@Valid @RequestBody SettingsRequest r){if(r.upiQrUrl()!=null&&!r.upiQrUrl().isBlank())validateHttps(r.upiQrUrl());StudioSettings s=settings.findById(ID).orElseGet(()->new StudioSettings(ID));s.update(r.studioName().trim(),r.contactEmail(),r.contactPhone(),r.upiId(),r.upiQrUrl(),r.bankName(),r.bankAccountName(),r.bankAccountNumber(),r.bankIfsc(),r.paymentInstructions(),r.legalTerms());return settings.save(s);}
 private void validateHttps(String value){try{URI u=URI.create(value);if(!"https".equalsIgnoreCase(u.getScheme())||u.getHost()==null)throw new IllegalArgumentException();}catch(Exception e){throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST,"QR image URL must use HTTPS.");}}
}