package com.couturix.commande;

import com.couturix.common.exception.ApiException;
import com.couturix.domain.enums.CommandeStatut;
import java.util.Map;
import java.util.Set;

public final class CommandeWorkflow {

    private static final Map<CommandeStatut, Set<CommandeStatut>> ALLOWED = Map.of(
            CommandeStatut.BROUILLON, Set.of(CommandeStatut.CONFIRMEE, CommandeStatut.ANNULEE),
            CommandeStatut.CONFIRMEE, Set.of(CommandeStatut.EN_PREPARATION, CommandeStatut.ANNULEE),
            CommandeStatut.EN_PREPARATION, Set.of(CommandeStatut.EN_COUPE),
            CommandeStatut.EN_COUPE, Set.of(CommandeStatut.EN_COUTURE),
            CommandeStatut.EN_COUTURE, Set.of(CommandeStatut.FINITION),
            CommandeStatut.FINITION, Set.of(CommandeStatut.CONTROLE),
            CommandeStatut.CONTROLE, Set.of(CommandeStatut.PRETE),
            CommandeStatut.PRETE, Set.of(CommandeStatut.LIVREE),
            CommandeStatut.LIVREE, Set.of(),
            CommandeStatut.ANNULEE, Set.of()
    );

    private CommandeWorkflow() {}

    public static void assertTransition(CommandeStatut from, CommandeStatut to) {
        if (from == to) {
            throw ApiException.badRequest("La commande est déjà au statut " + to);
        }
        Set<CommandeStatut> next = ALLOWED.getOrDefault(from, Set.of());
        if (!next.contains(to)) {
            throw ApiException.badRequest("Transition illégale: " + from + " → " + to);
        }
    }

    public static boolean isAllowed(CommandeStatut from, CommandeStatut to) {
        return ALLOWED.getOrDefault(from, Set.of()).contains(to);
    }
}
