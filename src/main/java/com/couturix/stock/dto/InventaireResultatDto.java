package com.couturix.stock.dto;

import java.util.List;

public record InventaireResultatDto(String reference, int lignesCompte, int ajustements, List<MouvementDto> mouvements) {}
