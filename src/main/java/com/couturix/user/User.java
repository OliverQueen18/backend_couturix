package com.couturix.user;

import com.couturix.atelier.Atelier;
import com.couturix.common.domain.AuditableEntity;
import com.couturix.common.tenant.TenantAssigner;
import com.couturix.common.tenant.TenantOwned;
import com.couturix.domain.enums.PlanCode;
import com.couturix.domain.enums.RoleCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.Filter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users")
@Filter(name = "atelierFilter", condition = "atelier_id IN (:atelierIds)")
@EntityListeners({AuditingEntityListener.class, TenantAssigner.class})
public class User extends AuditableEntity implements TenantOwned {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 180)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(length = 40)
    private String telephone;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Column(nullable = false)
    private boolean actif = true;

    @Column(name = "must_change_password", nullable = false)
    private boolean mustChangePassword = false;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_code", nullable = false, length = 20)
    private PlanCode planCode = PlanCode.GRATUIT;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "atelier_id")
    private Atelier atelier;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    public Set<RoleCode> roleCodes() {
        return roles.stream().map(Role::getCode).collect(Collectors.toSet());
    }

    public String nomComplet() {
        return prenom + " " + nom;
    }
}
