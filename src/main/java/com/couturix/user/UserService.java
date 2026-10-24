package com.couturix.user;
import com.couturix.abonnement.AbonnementService;
import com.couturix.atelier.Atelier;
import com.couturix.atelier.AtelierRepository;
import com.couturix.common.api.PageResponse;
import com.couturix.common.audit.AuditService;
import com.couturix.common.exception.ApiException;
import com.couturix.common.tenant.TenantContext;
import com.couturix.domain.enums.PlanCode;
import com.couturix.domain.enums.RoleCode;
import com.couturix.security.SecurityUtils;
import com.couturix.user.dto.UserDto;
import com.couturix.user.dto.UserRequest;
import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AtelierRepository atelierRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final AbonnementService abonnementService;

    @Transactional(readOnly = true)
    public PageResponse<UserDto> list(String role, String q, Long atelierId, Pageable pageable) {
        Specification<User> spec = scopeSpec(atelierId);
        if (q != null && !q.isBlank()) {
            String like = "%" + q.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.like(cb.lower(root.get("nom")), like));
                predicates.add(cb.like(cb.lower(root.get("prenom")), like));
                predicates.add(cb.like(cb.lower(root.get("email")), like));
                return cb.or(predicates.toArray(Predicate[]::new));
            });
        }
        if (role != null && !role.isBlank()) {
            RoleCode code;
            try {
                code = RoleCode.valueOf(role.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw ApiException.badRequest("Rôle inconnu");
            }
            spec = spec.and((root, query, cb) -> {
                query.distinct(true);
                return cb.equal(root.join("roles").get("code"), code);
            });
        }
        return PageResponse.of(userRepository.findAll(spec, pageable).map(this::toDto));
    }

    @Transactional(readOnly = true)
    public UserDto get(Long id) { return toDto(require(id)); }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT')")
    public UserDto create(UserRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) throw ApiException.conflict("Email déjà utilisé");
        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password() == null || request.password().isBlank() ? "ChangeMe123!" : request.password()));
        user.setNom(request.nom());
        user.setPrenom(request.prenom());
        user.setTelephone(request.telephone());
        user.setActif(request.actif());
        user.setMustChangePassword(true);
        user.setRoles(resolveRoles(request.roles()));
        user.setPlanStartedAt(Instant.now());
        if (SecurityUtils.hasRole("SUPER_ADMIN") && request.plan() != null && !request.plan().isBlank()) {
            boolean ciblePlateforme = user.roleCodes().contains(RoleCode.SUPER_ADMIN)
                    || user.roleCodes().contains(RoleCode.ADMIN);
            user.setPlanCode(ciblePlateforme ? PlanCode.PLATEFORME : abonnementService.parse(request.plan()));
        }
        assignAtelier(user, request.atelierId());
        if (!SecurityUtils.isPlatformTeam() && user.getAtelier() == null) {
            throw ApiException.forbidden("Rattachez l’utilisateur à votre atelier");
        }
        User saved = userRepository.save(user);
        auditService.log(null, "CREATE_USER", "USER", saved.getId(), saved.getEmail());
        return toDto(saved);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT')")
    public UserDto update(Long id, UserRequest request) {
        User user = require(id);
        user.setNom(request.nom());
        user.setPrenom(request.prenom());
        user.setTelephone(request.telephone());
        user.setActif(request.actif());
        if (request.password() != null && !request.password().isBlank()) user.setPasswordHash(passwordEncoder.encode(request.password()));
        if (request.roles() != null) user.setRoles(resolveRoles(request.roles()));
        if (SecurityUtils.isPlatformTeam() && request.atelierId() != null) {
            assignAtelier(user, request.atelierId());
        }
        if (request.plan() != null && !request.plan().isBlank() && SecurityUtils.hasRole("SUPER_ADMIN")) {
            boolean ciblePlateforme = user.roleCodes().contains(RoleCode.SUPER_ADMIN)
                    || (request.roles() != null && request.roles().contains("SUPER_ADMIN"));
            PlanCode plan = ciblePlateforme ? PlanCode.PLATEFORME : abonnementService.parse(request.plan());
            if (user.getPlanCode() != plan) {
                user.setPlanCode(plan);
                user.setPlanStartedAt(Instant.now());
            }
        }
        return toDto(userRepository.save(user));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT')")
    public UserDto toggle(Long id, boolean actif) {
        User user = require(id);
        user.setActif(actif);
        return toDto(userRepository.save(user));
    }

    private Set<Role> resolveRoles(Set<String> codes) {
        Set<Role> roles = new HashSet<>();
        if (codes == null) return roles;
        boolean superAdmin = SecurityUtils.hasRole("SUPER_ADMIN");
        boolean platform = SecurityUtils.isPlatformTeam();
        for (String c : codes) {
            RoleCode code;
            try {
                code = RoleCode.valueOf(c);
            } catch (IllegalArgumentException ex) {
                throw ApiException.badRequest("Rôle inconnu: " + c);
            }
            if (code == RoleCode.SUPER_ADMIN && !superAdmin) {
                throw ApiException.forbidden("Ce rôle est réservé à l’équipe CouturiX");
            }
            if (code == RoleCode.ADMIN && !platform) {
                throw ApiException.forbidden("Ce rôle est réservé à l’équipe CouturiX");
            }
            roles.add(roleRepository.findByCode(code).orElseThrow(() -> ApiException.badRequest("Rôle inconnu: " + c)));
        }
        return roles;
    }

    private User require(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> ApiException.notFound("Utilisateur introuvable"));
        assertVisible(user);
        return user;
    }

    private void assignAtelier(User user, Long atelierId) {
        if (SecurityUtils.isPlatformTeam() && atelierId != null) {
            Atelier atelier = atelierRepository.findById(atelierId)
                    .orElseThrow(() -> ApiException.notFound("Atelier introuvable"));
            user.setAtelier(atelier);
            return;
        }
        TenantContext.atelier().ifPresent(user::setAtelier);
    }

    private void assertVisible(User user) {
        if (SecurityUtils.isPlatformTeam()) {
            return;
        }
        Long atelierId = user.getAtelier() == null ? null : user.getAtelier().getId();
        List<Long> scope = TenantContext.current().map(TenantContext.Snapshot::scopeIds).orElse(List.of());
        if (atelierId == null || !scope.contains(atelierId)) {
            throw ApiException.notFound("Utilisateur introuvable");
        }
    }

    private Specification<User> scopeSpec(Long atelierId) {
        if (SecurityUtils.isPlatformTeam()) {
            if (atelierId == null) {
                return (root, query, cb) -> cb.conjunction();
            }
            return (root, query, cb) -> cb.equal(root.get("atelier").get("id"), atelierId);
        }
        List<Long> ids = TenantContext.current().map(TenantContext.Snapshot::scopeIds).orElse(List.of());
        if (ids.isEmpty()) {
            return (root, query, cb) -> cb.disjunction();
        }
        return (root, query, cb) -> root.get("atelier").get("id").in(ids);
    }

    public UserDto toDto(User u) {
        return new UserDto(u.getId(), u.getEmail(), u.getNom(), u.getPrenom(), u.getTelephone(), u.getPhotoUrl(), u.isActif(),
                u.roleCodes().stream().map(Enum::name).collect(Collectors.toSet()),
                u.getAtelier() == null ? null : u.getAtelier().getId(),
                u.getAtelier() == null ? null : u.getAtelier().getNom(),
                (u.getPlanCode() == null ? PlanCode.GRATUIT : u.getPlanCode()).name());
    }
}
