package com.couturix.user;
import com.couturix.abonnement.AbonnementService;
import com.couturix.common.api.PageResponse;
import com.couturix.common.audit.AuditService;
import com.couturix.common.exception.ApiException;
import com.couturix.common.tenant.TenantContext;
import com.couturix.domain.enums.PlanCode;
import com.couturix.domain.enums.RoleCode;
import com.couturix.security.SecurityUtils;
import com.couturix.user.dto.UserDto;
import com.couturix.user.dto.UserRequest;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final AbonnementService abonnementService;

    @Transactional(readOnly = true)
    public PageResponse<UserDto> list(String role, Pageable pageable) {
        if (role == null || role.isBlank()) {
            return PageResponse.of(userRepository.findAll(pageable).map(this::toDto));
        }
        RoleCode code;
        try {
            code = RoleCode.valueOf(role.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw ApiException.badRequest("Rôle inconnu");
        }
        return PageResponse.of(userRepository.findAll((root, query, cb) -> {
            query.distinct(true);
            return cb.equal(root.join("roles").get("code"), code);
        }, pageable).map(this::toDto));
    }

    @Transactional(readOnly = true)
    public UserDto get(Long id) { return toDto(require(id)); }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
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
        TenantContext.atelier().ifPresent(user::setAtelier);
        User saved = userRepository.save(user);
        auditService.log(null, "CREATE_USER", "USER", saved.getId(), saved.getEmail());
        return toDto(saved);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public UserDto update(Long id, UserRequest request) {
        User user = require(id);
        user.setNom(request.nom());
        user.setPrenom(request.prenom());
        user.setTelephone(request.telephone());
        user.setActif(request.actif());
        if (request.password() != null && !request.password().isBlank()) user.setPasswordHash(passwordEncoder.encode(request.password()));
        if (request.roles() != null) user.setRoles(resolveRoles(request.roles()));
        if (request.plan() != null && !request.plan().isBlank() && SecurityUtils.hasRole("SUPER_ADMIN")) {
            boolean ciblePlateforme = user.roleCodes().contains(RoleCode.SUPER_ADMIN)
                    || (request.roles() != null && request.roles().contains("SUPER_ADMIN"));
            user.setPlanCode(ciblePlateforme ? PlanCode.PLATEFORME : abonnementService.parse(request.plan()));
        }
        return toDto(userRepository.save(user));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public UserDto toggle(Long id, boolean actif) {
        User user = require(id);
        user.setActif(actif);
        return toDto(userRepository.save(user));
    }

    private Set<Role> resolveRoles(Set<String> codes) {
        Set<Role> roles = new HashSet<>();
        if (codes == null) return roles;
        for (String c : codes) {
            roles.add(roleRepository.findByCode(RoleCode.valueOf(c)).orElseThrow(() -> ApiException.badRequest("Rôle inconnu: " + c)));
        }
        return roles;
    }

    private User require(Long id) { return userRepository.findById(id).orElseThrow(() -> ApiException.notFound("Utilisateur introuvable")); }

    public UserDto toDto(User u) {
        return new UserDto(u.getId(), u.getEmail(), u.getNom(), u.getPrenom(), u.getTelephone(), u.getPhotoUrl(), u.isActif(),
                u.roleCodes().stream().map(Enum::name).collect(Collectors.toSet()),
                u.getAtelier() == null ? null : u.getAtelier().getId(),
                u.getAtelier() == null ? null : u.getAtelier().getNom(),
                (u.getPlanCode() == null ? PlanCode.GRATUIT : u.getPlanCode()).name());
    }
}
