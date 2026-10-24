package com.couturix.config;

import com.couturix.admin.Parametre;
import com.couturix.admin.ParametreRepository;
import com.couturix.atelier.Atelier;
import com.couturix.atelier.AtelierRepository;
import com.couturix.domain.enums.PlanCode;
import com.couturix.domain.enums.RoleCode;
import com.couturix.user.Role;
import com.couturix.user.RoleRepository;
import com.couturix.tailleur.Specialite;
import com.couturix.tailleur.SpecialiteCatalog;
import com.couturix.tailleur.SpecialiteRepository;
import com.couturix.user.User;
import com.couturix.user.UserRepository;
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
            RoleCode.ADMIN, "Administrateur",
            RoleCode.GERANT, "Gérant",
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
    private final AtelierRepository atelierRepository;
    private final PasswordEncoder passwordEncoder;
    private final CouturixProperties properties;
    private final SpecialiteRepository specialiteRepository;

    @Override
    @Transactional
    public void run(String... args) {
        ROLE_LABELS.forEach((code, libelle) -> {
            if (roleRepository.findByCode(code).isEmpty()) {
                Role role = new Role();
                role.setCode(code);
                role.setLibelle(libelle);
                roleRepository.save(role);
            }
        });
        parametreRepository.findById("stock.allowNegative").orElseGet(() -> {
            Parametre p = new Parametre();
            p.setCle("stock.allowNegative");
            p.setValeur("false");
            p.setDescription("Autoriser les sorties de stock négatives");
            return parametreRepository.save(p);
        });
        ensureSpecialites();
        if (userRepository.count() > 0) {
            return;
        }
        Atelier atelier = atelierRepository.findBySlug("atelier-principal").orElseGet(() -> {
            Atelier created = new Atelier();
            created.setNom(properties.bootstrap().adminNom().equals("Administrateur") ? "CouturiX" : "Atelier principal");
            created.setSlug("atelier-principal");
            created.setDescription("Atelier principal CouturiX");
            created.setPays("Congo");
            created.setDevise("XOF");
            created.setActif(true);
            return atelierRepository.save(created);
        });
        User admin = new User();
        admin.setEmail(properties.bootstrap().adminEmail());
        admin.setPasswordHash(passwordEncoder.encode(properties.bootstrap().adminPassword()));
        admin.setNom(properties.bootstrap().adminNom());
        admin.setPrenom(properties.bootstrap().adminPrenom());
        admin.setActif(true);
        admin.setMustChangePassword(true);
        admin.setEmailVerified(true);
        admin.setAtelier(atelier);
        admin.setPlanCode(PlanCode.PLATEFORME);
        roleRepository.findByCode(RoleCode.SUPER_ADMIN).ifPresent(role -> admin.setRoles(Set.of(role)));
        userRepository.save(admin);
        log.info("Compte SUPER_ADMIN initial créé : {}", admin.getEmail());
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
