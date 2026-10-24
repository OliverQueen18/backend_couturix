package com.couturix.abonnement;

import com.couturix.atelier.Atelier;
import com.couturix.atelier.AtelierRepository;
import com.couturix.atelier.TenantService;
import com.couturix.common.exception.ApiException;
import com.couturix.common.tenant.TenantContext;
import com.couturix.domain.enums.PlanCode;
import com.couturix.domain.enums.RoleCode;
import com.couturix.notification.SmsGatewayService;
import com.couturix.security.SecurityUtils;
import com.couturix.user.User;
import com.couturix.user.UserRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AbonnementService {

    private final UserRepository userRepository;
    private final AtelierRepository atelierRepository;
    private final TenantService tenantService;
    private final SmsGatewayService smsGatewayService;
    private final PlanOffreService planOffreService;

    @Transactional(readOnly = true)
    public AbonnementDto courant() {
        boolean bypass = TenantContext.current().map(TenantContext.Snapshot::bypass).orElse(false);
        if (bypass || SecurityUtils.hasRole("SUPER_ADMIN")) {
            int used = TenantContext.current().map(snapshot -> snapshot.scopeIds().size()).orElse(0);
            return toDto(planOffreService.resolve(PlanCode.PLATEFORME, Instant.now()), used);
        }
        User user = userRepository.findById(SecurityUtils.currentUserId())
                .orElseThrow(() -> ApiException.unauthorized("Non authentifié"));
        Atelier racine = racine();
        int used = racine == null ? 0 : tenantService.collectScope(racine.getId()).size();
        return toDto(maison(user, racine), used);
    }

    @Transactional
    public AbonnementDto assigner(Long userId, String planCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("Utilisateur introuvable"));
        PlanCode plan = parse(planCode);
        user.setPlanCode(plan);
        user.setPlanStartedAt(Instant.now());
        userRepository.save(user);
        int used = user.getAtelier() == null ? 0 : 1;
        return toDto(planOffreService.resolve(plan, user.getPlanStartedAt()), used);
    }

    public void assertSms() {
        if (!courant().sms()) {
            throw ApiException.forbidden(
                    "Les SMS sont inclus à partir de l’offre Standard, ou pendant la période d’essai. Passez à une offre supérieure pour en envoyer.");
        }
    }

    public boolean smsAutorise() {
        try {
            return courant().sms();
        } catch (Exception ex) {
            return false;
        }
    }

    public void assertPeutCreer() {
        if (TenantContext.current().map(TenantContext.Snapshot::bypass).orElse(false)
                || SecurityUtils.hasRole("SUPER_ADMIN")) {
            return;
        }
        AbonnementDto courant = courant();
        if (!courant.peutCreer()) {
            String limite = courant.illimite() ? "illimité" : String.valueOf(courant.ateliersMax());
            throw ApiException.forbidden(
                    "Votre abonnement " + courant.libelle() + " autorise " + limite
                            + " atelier(s). Passez à une offre supérieure pour en créer un autre.");
        }
    }

    public PlanCode parse(String raw) {
        try {
            PlanCode plan = PlanCode.from(raw);
            if (!plan.offreAtelier()) {
                throw new IllegalArgumentException("not an atelier plan");
            }
            return plan;
        } catch (IllegalArgumentException ex) {
            throw ApiException.badRequest("Abonnement inconnu");
        }
    }

    private List<User> atelierHolders(Long atelierId) {
        Map<Long, User> holders = new LinkedHashMap<>();
        for (RoleCode role : List.of(RoleCode.GERANT, RoleCode.ADMIN)) {
            for (User user : userRepository.findAdminsByAtelierId(atelierId, role)) {
                holders.putIfAbsent(user.getId(), user);
            }
        }
        return new ArrayList<>(holders.values());
    }

    private Atelier racine() {
        Long id = TenantContext.current().map(TenantContext.Snapshot::atelierId).orElse(null);
        if (id == null) {
            return null;
        }
        Atelier cursor = atelierRepository.findById(id).orElse(null);
        while (cursor != null && cursor.getParent() != null) {
            cursor = cursor.getParent();
        }
        return cursor;
    }

    private EffectivePlan maison(User courant, Atelier racine) {
        PlanCode plan = courant.getPlanCode() == null ? PlanCode.GRATUIT : courant.getPlanCode();
        Instant started = courant.getPlanStartedAt();
        if (racine != null) {
            for (User admin : atelierHolders(racine.getId())) {
                PlanCode candidat = admin.getPlanCode() == null ? PlanCode.GRATUIT : admin.getPlanCode();
                if (rang(candidat) > rang(plan)) {
                    plan = candidat;
                    started = admin.getPlanStartedAt();
                } else if (rang(candidat) == rang(plan) && admin.getPlanStartedAt() != null
                        && (started == null || admin.getPlanStartedAt().isBefore(started))) {
                    started = admin.getPlanStartedAt();
                }
            }
        }
        return planOffreService.resolve(plan, started);
    }

    private int rang(PlanCode plan) {
        return switch (plan) {
            case GRATUIT -> 0;
            case STANDARD -> 1;
            case PREMIUM -> 2;
            case GOLD -> 3;
            case PLATEFORME -> 4;
        };
    }

    private AbonnementDto toDto(EffectivePlan plan, int used) {
        return new AbonnementDto(
                plan.code(),
                plan.libelle(),
                used,
                plan.maxAteliers(),
                plan.illimite(),
                plan.peutCreer(used),
                plan.sms(),
                smsGatewayService.isReady(),
                plan.essai(),
                plan.essaiJours(),
                plan.essaiJoursRestants(),
                plan.essaiFin());
    }
}
