package com.couturix.config;

import com.couturix.abonnement.PlanOffreService;
import com.couturix.admin.Parametre;
import com.couturix.admin.ParametreRepository;
import com.couturix.domain.enums.PlanCode;
import com.couturix.domain.enums.RoleCode;
import com.couturix.user.Role;
import com.couturix.user.RoleRepository;
import com.couturix.tailleur.Specialite;
import com.couturix.tailleur.SpecialiteCatalog;
import com.couturix.tailleur.SpecialiteRepository;
import com.couturix.user.User;
import com.couturix.user.UserRepository;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private static final Map<RoleCode, String> ROLE_LABELS = Map.of(
            RoleCode.SUPER_ADMIN, "Super administrateur",
            RoleCode.ADMIN, "Administrateur CouturiX",
            RoleCode.GERANT, "Administrateur d’atelier",
            RoleCode.CAISSIER, "Caissier",
            RoleCode.RECEPTIONNISTE, "Réceptionniste",
            RoleCode.TAILLEUR, "Tailleur",
            RoleCode.MAGASINIER, "Magasinier",
            RoleCode.COMPTABLE, "Comptable",
            RoleCode.CLIENT, "Client"
    );

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ParametreRepository parametreRepository;
    private final PasswordEncoder passwordEncoder;
    private final CouturixProperties properties;
    private final SpecialiteRepository specialiteRepository;
    private final PlanOffreService planOffreService;

    @Override
    @Transactional
    public void run(String... args) {
        ROLE_LABELS.forEach((code, libelle) -> {
            roleRepository.findByCode(code).ifPresentOrElse(role -> {
                if (!libelle.equals(role.getLibelle())) {
                    role.setLibelle(libelle);
                    roleRepository.save(role);
                }
            }, () -> {
                Role role = new Role();
                role.setCode(code);
                role.setLibelle(libelle);
                roleRepository.save(role);
            });
        });
        parametreRepository.findById("stock.allowNegative").orElseGet(() -> {
            Parametre p = new Parametre();
            p.setCle("stock.allowNegative");
            p.setValeur("false");
            p.setDescription("Autoriser les sorties de stock négatives");
            return parametreRepository.save(p);
        });
        ensureSpecialites();
        planOffreService.ensureDefaults();
        ensureBootstrapAdmin();
    }

    private void ensureBootstrapAdmin() {
        String email = properties.bootstrap().adminEmail() == null ? "" : properties.bootstrap().adminEmail().trim();
        String password = properties.bootstrap().adminPassword();
        if (email.isBlank() || password == null || password.isBlank()) {
            log.warn("ADMIN_EMAIL / ADMIN_PASSWORD absents : compte bootstrap non synchronisé");
            return;
        }
        Role superAdmin = roleRepository.findByCode(RoleCode.SUPER_ADMIN)
                .orElseThrow(() -> new IllegalStateException("Rôle SUPER_ADMIN introuvable"));
        User byEmail = userRepository.findByEmailIgnoreCase(email).orElse(null);
        if (byEmail != null && !byEmail.roleCodes().contains(RoleCode.SUPER_ADMIN)) {
            log.error("ADMIN_EMAIL {} est déjà utilisé par un compte sans rôle SUPER_ADMIN", email);
            return;
        }
        User admin = byEmail != null ? byEmail : findExistingPlatformAdmin();
        boolean created = admin == null;
        if (created) {
            admin = new User();
            admin.setNom(properties.bootstrap().adminNom());
            admin.setPrenom(properties.bootstrap().adminPrenom());
        }
        admin.setEmail(email);
        if (admin.getPasswordHash() == null || !passwordEncoder.matches(password, admin.getPasswordHash())) {
            admin.setPasswordHash(passwordEncoder.encode(password));
        }
        admin.setActif(true);
        admin.setEmailVerified(true);
        admin.setMustChangePassword(false);
        admin.setAtelier(null);
        admin.setPlanCode(PlanCode.PLATEFORME);
        Set<Role> roles = new HashSet<>(admin.getRoles() == null ? Set.of() : admin.getRoles());
        roles.add(superAdmin);
        admin.setRoles(roles);
        userRepository.save(admin);
        log.info(created ? "Compte SUPER_ADMIN initial créé : {}" : "Compte SUPER_ADMIN bootstrap synchronisé : {}",
                admin.getEmail());
    }

    private User findExistingPlatformAdmin() {
        return userRepository.findDistinctByRoles_CodeOrderByIdAsc(RoleCode.SUPER_ADMIN).stream()
                .filter(user -> user.getPlanCode() == PlanCode.PLATEFORME || user.getAtelier() == null)
                .findFirst()
                .orElse(null);
    }

    private void ensureSpecialites() {
        Set<String> existing = specialiteRepository.findAll().stream()
                .map(Specialite::getLibelle)
                .map(libelle -> libelle.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
        for (String libelle : SpecialiteCatalog.LIBELLES) {
            if (existing.add(libelle.toLowerCase(Locale.ROOT))) {
                Specialite specialite = new Specialite();
                specialite.setLibelle(libelle);
                specialiteRepository.save(specialite);
            }
        }
    }
}
