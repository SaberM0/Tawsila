package com.covoiturage.domain.enums;

/**
 * StatutReservation représente le statut d'une réservation :
 * - CONFIRMEE : réservation validée, places occupées
 * - ANNULEE : réservation annulée par le passager ou avec le trajet
 */
public enum StatutReservation {
    CONFIRMEE,
    ANNULEE
}
