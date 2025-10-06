package com.cabybara.prolearningplatform.model;

import com.cabybara.prolearningplatform.enums.UserEducation;
import com.cabybara.prolearningplatform.enums.UserHearAppFrom;
import com.cabybara.prolearningplatform.enums.UserLanguage;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "\"user\"")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "roles")
public class User extends AbstractEntity implements UserDetails {
    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false)
    private String password;

    @Column(columnDefinition = "user_language")
    @Enumerated(EnumType.STRING)
    @ColumnDefault("'vi'")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private UserLanguage language;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'High School'")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private UserEducation education;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'Google'")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private UserHearAppFrom hearAppFrom;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "user")
    private Set<Authority> roles;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        try {
            return this.roles.stream().map(role -> new SimpleGrantedAuthority(role.getAuthority().name())).collect(Collectors.toSet());
        } catch (Exception e) {
            e.printStackTrace();
            throw new UnsupportedOperationException("Unimplemented method 'getAuthorities'");
        }
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.email;
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
