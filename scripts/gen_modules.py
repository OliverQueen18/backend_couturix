# -*- coding: utf-8 -*-
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1] / "src" / "main" / "java" / "com" / "couturix"

def w(rel, content):
    p = ROOT / rel
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_text(content.strip() + "\n", encoding="utf-8")

w("user/dto/UserDto.java", """
package com.couturix.user.dto;
import java.util.Set;
public record UserDto(Long id, String email, String nom, String prenom, String telephone, String photoUrl, boolean actif, Set<String> roles) {}
""")

w("user/dto/UserRequest.java", """
package com.couturix.user.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Set;
public record UserRequest(@Email @NotBlank String email, String password, @NotBlank String nom, @NotBlank String prenom, String telephone, Set<String> roles, boolean actif) {}
""")

w("user/UserService.java", """
package com.couturix.user;
import com.couturix.common.api.PageResponse;
import com.couturix.common.audit.AuditService;
import com.couturix.common.exception.ApiException;
import com.couturix.domain.enums.RoleCode;
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

    @Transactional(readOnly = true)
    public PageResponse<UserDto> list(Pageable pageable) {
        return PageResponse.of(userRepository.findAll(pageable).map(this::toDto));
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
                u.roleCodes().stream().map(Enum::name).collect(Collectors.toSet()));
    }
}
""")

w("user/UserController.java", """
package com.couturix.user;
import com.couturix.common.api.PageResponse;
import com.couturix.user.dto.UserDto;
import com.couturix.user.dto.UserRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Utilisateurs")
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
public class UserController {
    private final UserService userService;
    @GetMapping public PageResponse<UserDto> list(Pageable pageable) { return userService.list(pageable); }
    @GetMapping("/{id}") public UserDto get(@PathVariable Long id) { return userService.get(id); }
    @PostMapping public UserDto create(@Valid @RequestBody UserRequest request) { return userService.create(request); }
    @PutMapping("/{id}") public UserDto update(@PathVariable Long id, @Valid @RequestBody UserRequest request) { return userService.update(id, request); }
    @PatchMapping("/{id}/actif") public UserDto toggle(@PathVariable Long id, @RequestParam boolean actif) { return userService.toggle(id, actif); }
}
""")

print("users ok")
print("ROOT", ROOT)
