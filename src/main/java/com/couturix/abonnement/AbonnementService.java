package com.couturix.abonnement;

import com.couturix.atelier.Atelier;
import com.couturix.atelier.AtelierRepository;
import com.couturix.atelier.TenantService;
import com.couturix.common.exception.ApiException;
import com.couturix.common.tenant.TenantContext;
import com.couturix.domain.enums.PlanCode;
import com.couturix.notification.SmsGatewayService;
import com.couturix.security.SecurityUtils;
import com.couturix.user.User;
import com.couturix.user.UserRepository;
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

    @Transactional(readOnly = true)
    public AbonnementDto courant() {
        boolean bypass = TenantContext.current().map(TenantContext.Snapshot::bypass).orElse(false);
        if (bypass || SecurityUtils.hasRole("SUPER_ADMIN")) {
            int used = TenantContext.current().map(snapshot -> snapshot.scopeIds().size()).orElse(0);
            return toDto(PlanCode.PLATEFORME, used);
        }
        User user = userRepository.findById(SecurityUtils.currentUserId())
                .orElseThrow(() -> ApiException.unauthorized("Non authentifié"));
        Atelier racine = racine();
        int used = racine == null ? 0 : tenantService.collectScope(racine.getId()).size();
        return toDto(planDeLaMaison(user, racine), used);
    }

    @Transactional
    public AbonnementDto assigner(Long userId, String planCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("Utilisateur introuvable"));
        PlanCode plan = parse(planCode);
        user.setPlanCode(plan);
        userRepository.save(user);
        int used = user.getAtelier() == null ? 0 : 1;
        return toDto(plan, used);
    }

    public void assertSms() {
        if (!courant().sms()) {
            throw ApiException.forbidden(
                    "Les SMS sont inclus à partir de l’offre Standard. Passez à une offre supérieure pour en envoyer.");
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

    private PlanCode planDeLaMaison(User courant, Atelier racine) {
        PlanCode plan = null;
        if (racine != null) {
            for (String code : userRepository.findAdminPlanCodes(racine.getId())) {
                PlanCode candidat = PlanCode.from(code);
                if (plan == null || rang(candidat) > rang(plan)) {
                    plan = candidat;
                }
            }
        }
        if (plan != null) {
            return plan;
        }
        return courant.getPlanCode() == null ? PlanCode.GRATUIT : courant.getPlanCode();
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

    private AbonnementDto toDto(PlanCode plan, int used) {
        return new AbonnementDto(
                plan.name(),
                plan.getLibelle(),
                used,
                plan.getMaxAteliers(),
                plan.illimite(),
                plan.peutCreer(used),
                plan.autoriseSms(),
                smsGatewayService.isReady());
    }
}
