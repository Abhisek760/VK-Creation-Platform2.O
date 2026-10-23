package com.vkcreation.platform.user;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
@Service
public class UserAccountDetailsService implements UserDetailsService {
 private final UserAccountRepository users;
 public UserAccountDetailsService(UserAccountRepository users){this.users=users;}
 @Override public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
  return users.findByEmailIgnoreCase(email).orElseThrow(()->new UsernameNotFoundException("Invalid email or password"));
 }
}