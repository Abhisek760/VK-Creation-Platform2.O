package com.vkcreation.platform.payment;

import com.vkcreation.platform.booking.BookingRepository;
import com.vkcreation.platform.user.UserAccount;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@RestController
@RequestMapping("/api/payments/phonepe")
public class PhonePeController {
 private final PaymentService paymentService;
 private final PaymentRepository payments;
 private final BookingRepository bookings;
 private final RestClient http=RestClient.create();
 @Value("${app.phonepe.enabled:false}") private boolean enabled;
 @Value("${app.phonepe.base-url:https://api-preprod.phonepe.com/apis/pg-sandbox}") private String baseUrl;
 @Value("${app.phonepe.client-id:}") private String clientId;
 @Value("${app.phonepe.client-secret:}") private String clientSecret;
 @Value("${app.phonepe.client-version:1}") private String clientVersion;
 @Value("${app.phonepe.redirect-url:http://localhost:3000/portal}") private String redirectUrl;
 public PhonePeController(PaymentService paymentService,PaymentRepository payments,BookingRepository bookings){this.paymentService=paymentService;this.payments=payments;this.bookings=bookings;}
 public record StartResponse(String orderId,String redirectUrl,String status,String message){}
 @PostMapping("/start/{bookingId}") @PreAuthorize("hasRole('CLIENT')")
 public StartResponse start(@PathVariable String bookingId,@AuthenticationPrincipal UserAccount client){
  requireConfigured();
  var booking=bookings.findByIdForUpdate(bookingId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Booking not found."));
  if(!booking.getClient().getId().equals(client.getId()))throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Booking not found.");
  if(booking.getCurrentDueAmount()==null||booking.getCurrentDueAmount().compareTo(BigDecimal.ZERO)<=0)throw new ResponseStatusException(HttpStatus.CONFLICT,"No installment is currently due.");
  String orderId="VK"+UUID.randomUUID().toString().replace("-","").substring(0,24).toUpperCase(Locale.ROOT);
  long amount=booking.getCurrentDueAmount().multiply(new BigDecimal("100")).setScale(0,RoundingMode.UNNECESSARY).longValueExact();
  Map<String,Object> body=new LinkedHashMap<>();body.put("merchantOrderId",orderId);body.put("amount",amount);body.put("expireAfter",1200);
  Map<String,Object> flow=new LinkedHashMap<>();flow.put("type","PG_CHECKOUT");flow.put("message","VK Creation installment payment");flow.put("merchantUrls",Map.of("redirectUrl",redirectUrl));body.put("paymentFlow",flow);
  String token;
  try {
   token=accessToken();
  } catch (Exception e) {
   throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"Could not authenticate with PhonePe. No payment order was reserved; check merchant credentials and try again.");
  }
  paymentService.createGatewayPayment(bookingId,client,orderId);
  try{
   Map response=http.post().uri(baseUrl+"/checkout/v2/pay").header("Authorization","O-Bearer "+token).contentType(MediaType.APPLICATION_JSON).body(body).retrieve().body(Map.class);
   if(response==null)throw new IllegalStateException("Empty response from PhonePe.");
   Object dataObj=response.get("data");Map data=dataObj instanceof Map?(Map)dataObj:response;
   Object url=data.get("redirectUrl");
   if(!(url instanceof String)||((String)url).isBlank())throw new IllegalStateException("PhonePe did not return a checkout URL.");
   return new StartResponse(orderId,(String)url,"PENDING","Continue to PhonePe to complete your installment payment.");
  }catch(ResponseStatusException e){throw e;}catch(Exception e){throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"Could not start PhonePe checkout. If the provider may have received the request, check payment status before retrying.");}
 }
 @GetMapping("/status/{orderId}") @PreAuthorize("hasRole('CLIENT')")
 public Map<String,Object> status(@PathVariable String orderId,@AuthenticationPrincipal UserAccount client){
  requireConfigured();
  Payment payment=payments.findFirstByMethodAndProviderReference("PHONEPE",orderId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Payment order not found."));
  if(!payment.getBooking().getClient().getId().equals(client.getId()))throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Payment order not found.");
  try{
   Map response=http.get().uri(baseUrl+"/checkout/v2/order/"+orderId+"/status?details=true").header("Authorization","O-Bearer "+accessToken()).retrieve().body(Map.class);
   if(response==null)throw new IllegalStateException("Empty response from PhonePe.");
   Map data=response.get("data") instanceof Map?(Map)response.get("data"):response;
   String state=String.valueOf(data.getOrDefault("state","PENDING"));
   if("COMPLETED".equalsIgnoreCase(state)){
    long paise=((Number)data.getOrDefault("amount",0)).longValue();
    String txn=orderId;
    Object details=data.get("paymentDetails");
    if(details instanceof List list&&!list.isEmpty()&&list.get(0) instanceof Map first)txn=String.valueOf(first.getOrDefault("transactionId",orderId));
    paymentService.confirmGatewayPayment(orderId,txn,BigDecimal.valueOf(paise,2));
   } else if ("FAILED".equalsIgnoreCase(state) || "EXPIRED".equalsIgnoreCase(state) || "CANCELLED".equalsIgnoreCase(state)) {
    paymentService.failGatewayPayment(orderId);
   }
   return Map.of("orderId",orderId,"gatewayState",state,"paymentStatus",payments.findFirstByMethodAndProviderReference("PHONEPE",orderId).map(p->p.getStatus().name()).orElse("UNKNOWN"),"message","Only a server-verified COMPLETED gateway status is counted as paid.");
  }catch(ResponseStatusException e){throw e;}catch(Exception e){throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"Could not verify PhonePe status. Try again shortly.");}
 }
 private void requireConfigured(){if(!enabled||clientId.isBlank()||clientSecret.isBlank())throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"PhonePe checkout is not enabled/configured. Set PHONEPE_ENABLED=true and gateway credentials after merchant onboarding.");}
 private String accessToken(){
  MultiValueMap<String,String> form=new LinkedMultiValueMap<>();form.add("client_id",clientId);form.add("client_version",clientVersion);form.add("client_secret",clientSecret);form.add("grant_type","client_credentials");
  Map response=http.post().uri(baseUrl+"/v1/oauth/token").contentType(MediaType.APPLICATION_FORM_URLENCODED).body(form).retrieve().body(Map.class);
  Object token=response==null?null:response.get("access_token");if(!(token instanceof String)||((String)token).isBlank())throw new IllegalStateException("PhonePe token was not returned.");return (String)token;
 }
}
