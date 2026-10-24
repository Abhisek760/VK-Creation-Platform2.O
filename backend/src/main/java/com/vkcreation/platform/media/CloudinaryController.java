package com.vkcreation.platform.media;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;

@RestController @RequestMapping("/api/media")
public class CloudinaryController {
 @Value("${app.cloudinary.cloud-name:}") private String cloudName;
 @Value("${app.cloudinary.api-key:}") private String apiKey;
 @Value("${app.cloudinary.api-secret:}") private String apiSecret;
 public record UploadSignature(String cloudName,String apiKey,long timestamp,String folder,String signature){}
 @PostMapping("/signature") @PreAuthorize("hasRole('ADMIN')")
 public UploadSignature signature(){if(cloudName.isBlank()||apiKey.isBlank()||apiSecret.isBlank())throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Cloudinary is not configured. Set CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY and CLOUDINARY_API_SECRET.");long ts=Instant.now().getEpochSecond();String folder="vk-creation/gallery";String params="folder="+folder+"&timestamp="+ts;try{MessageDigest digest=MessageDigest.getInstance("SHA-1");byte[] hash=digest.digest((params+apiSecret).getBytes(StandardCharsets.UTF_8));StringBuilder hex=new StringBuilder();for(byte b:hash)hex.append(String.format("%02x",b));return new UploadSignature(cloudName,apiKey,ts,folder,hex.toString());}catch(Exception e){throw new IllegalStateException("Could not sign Cloudinary upload.",e);}}
}