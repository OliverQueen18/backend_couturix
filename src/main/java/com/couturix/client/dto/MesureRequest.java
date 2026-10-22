package com.couturix.client.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MesureRequest(
        BigDecimal tourCou,
        BigDecimal tourPoitrine,
        BigDecimal tourTaille,
        BigDecimal tourHanches,
        BigDecimal tourBras,
        BigDecimal tourPoignet,
        BigDecimal epaule,
        BigDecimal longueurDos,
        BigDecimal longueurDevant,
        BigDecimal longueurManche,
        BigDecimal tourCuisse,
        BigDecimal tourGenou,
        BigDecimal basPantalon,
        BigDecimal longueurPantalon,
        BigDecimal hauteur,
        String notes,
        LocalDate datePrise
) {}
