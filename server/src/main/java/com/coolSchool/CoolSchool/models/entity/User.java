package com.coolSchool.CoolSchool.models.entity;
import com.coolSchool.CoolSchool.enums.Role;
import jakarta.persistence.*;

import java.util.Collection;
import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "_users")
public class User implements UserDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;
  @NotNull(message = "The name should not be null!")
  private String firstname;
  @NotNull(message = "The name should not be null!")
  private String lastname;
  @Email
  @NotNull(message = "The email should not be null!")
  private String email;
  @NotNull(message = "The password should not be null!")
  private String password;
  @NotNull(message = "The address should not be null!")
  private String address;
  @NotNull(message = "The username should not be null!")
  private String username;
  @NotNull()
  @Enumerated(EnumType.STRING)
  private Role role;

  @OneToMany(mappedBy = "user")
  private List<Token> tokens;
  @ManyToOne
  @JoinColumn(name = "file_id")
  private File profilePic;
  @Column(name = "is_deleted")
  private boolean deleted;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return role.getAuthorities();
  }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
