package com.covoiturage.domain.enums;

/**
 * StatutTrajet représente le statut d'un trajet :
 * - PUBLIE : ouvert à la réservation
 * - COMPLET : plus aucune place disponible
 * - TERMINE : le voyage a eu lieu
 * - ANNULE : annulé par le conducteur
 */
public enum StatutTrajet {
    PUBLIE,
    COMPLET,
    TERMINE,
    ANNULE
}
