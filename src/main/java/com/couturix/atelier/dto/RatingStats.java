package com.couturix.atelier.dto;

public record RatingStats(double moyenne, long total) {
    public static RatingStats empty() {
        return new RatingStats(0, 0);
    }
}
