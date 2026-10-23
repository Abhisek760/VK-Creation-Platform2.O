package com.vkcreation.platform.user;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
public class UserAccount implements UserDetails {
    @Id @Column(length = 36, nullable = false)
    private String id;
    @Column(name = "full_name", nullable = false, length = 160)
    private String fullName;
    @Column(nullable = false, unique = true, length = 254)
    private String email;
    @Column(length = 32)
    private String phone;
    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 24)
    private UserRole role = UserRole.CLIENT;
    @Column(nullable = false)
    private boolean enabled = true;
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    protected UserAccount() {}
    public UserAccount(String id, String fullName, String email, String phone, String passwordHash, UserRole role) {
        this.id=id; this.fullName=fullName; this.email=email; this.phone=phone; this.passwordHash=passwordHash; this.role=role;
    }
    public String getId(){ return id; }
    public String getFullName(){ return fullName; }
    public String getEmail(){ return email; }
    public String getPhone(){ return phone; }
    public UserRole getRole(){ return role; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities(){ return List.of(new SimpleGrantedAuthority("ROLE_"+role.name())); }
    @Override public String getPassword(){ return passwordHash; }
    @Override public String getUsername(){ return email; }
    @Override public boolean isEnabled(){ return enabled; }
    @Override public boolean isAccountNonExpired(){ return true; }
    @Override public boolean isAccountNonLocked(){ return true; }
    @Override public boolean isCredentialsNonExpired(){ return true; }
}
