package com.covoiturage.service.impl;

import com.covoiturage.domain.entity.Passager;
import com.covoiturage.domain.entity.PointArret;
import com.covoiturage.domain.entity.Reservation;
import com.covoiturage.domain.entity.Trajet;
import com.covoiturage.domain.enums.StatutReservation;
import com.covoiturage.domain.enums.StatutTrajet;
import com.covoiturage.domain.repository.PassagerRepository;
import com.covoiturage.domain.repository.PointArretRepository;
import com.covoiturage.domain.repository.ReservationRepository;
import com.covoiturage.domain.repository.TrajetRepository;
import com.covoiturage.dto.request.ReservationCreateRequest;
import com.covoiturage.dto.response.ReservationResponse;
import com.covoiturage.exception.BusinessConflictException;
import com.covoiturage.exception.BusinessValidationException;
import com.covoiturage.exception.ForbiddenOperationException;
import com.covoiturage.exception.ResourceNotFoundException;
import com.covoiturage.mapper.ReservationMapper;
import com.covoiturage.service.ReservationService;
import com.covoiturage.service.TrajetService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationServiceImpl implements ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationServiceImpl.class);

    private final ReservationRepository reservationRepository;
    private final TrajetRepository trajetRepository;
    private final PassagerRepository passagerRepository;
    private final PointArretRepository pointArretRepository;
    private final TrajetService trajetService;
    private final ReservationMapper reservationMapper;

    @Override
    @Transactional
    public ReservationResponse bookSeats(ReservationCreateRequest request, Long requestingPassengerId) {
        if (requestingPassengerId != null && !requestingPassengerId.equals(request.passagerId())) {
            log.warn("Action interdite : l'utilisateur {} tente de réserver pour le compte du passager {}",
                    requestingPassengerId, request.passagerId());
            throw new ForbiddenOperationException("Vous ne pouvez effectuer une réservation que pour votre propre compte");
        }

        Passager passager = passagerRepository.findById(request.passagerId())
                .orElseThrow(() -> {
                    log.warn("Passager introuvable pour réservation : id={}", request.passagerId());
                    return new ResourceNotFoundException("Passager non trouvé avec l'identifiant : " + request.passagerId());
                });

        Trajet trajet = trajetRepository.findByIdWithDetails(request.trajetId())
                .orElseThrow(() -> {
                    log.warn("Trajet introuvable pour réservation : id={}", request.trajetId());
                    return new ResourceNotFoundException("Trajet non trouvé avec l'identifiant : " + request.trajetId());
                });

        // Règle 4 : Si Imane réserve pendant que le trajet est COMPLET -> 409
        if (trajet.getStatut() == StatutTrajet.COMPLET) {
            log.warn("Refus de réservation : le trajet {} est COMPLET", trajet.getId());
            throw new BusinessConflictException("Le trajet est complet, aucune place disponible");
        }

        if (trajet.getStatut() != StatutTrajet.PUBLIE) {
            log.warn("Refus de réservation : le trajet {} n'est pas ouvert à la réservation (statut: {})", trajet.getId(), trajet.getStatut());
            throw new BusinessConflictException("Le trajet n'est plus ouvert à la réservation");
        }

        // Vérification des points d'arrêt
        PointArret pointMontee = pointArretRepository.findById(request.pointMonteeId())
                .orElseThrow(() -> new ResourceNotFoundException("Point de montée non trouvé avec l'identifiant : " + request.pointMonteeId()));

        PointArret pointDescente = pointArretRepository.findById(request.pointDescenteId())
                .orElseThrow(() -> new ResourceNotFoundException("Point de descente non trouvé avec l'identifiant : " + request.pointDescenteId()));

        if (!pointMontee.getTrajet().getId().equals(trajet.getId()) || !pointDescente.getTrajet().getId().equals(trajet.getId())) {
            log.warn("Refus de réservation : les points d'arrêt n'appartiennent pas au trajet {}", trajet.getId());
            throw new BusinessValidationException("Les points de montée et de descente doivent appartenir au parcours du trajet");
        }

        // Règle 6 : La montée est avant la descente (Scénario 6: montée après la descente -> 400)
        if (pointMontee.getOrdre() >= pointDescente.getOrdre()) {
            log.warn("Refus de réservation : point de montée (ordre {}) après ou égal à descente (ordre {})",
                    pointMontee.getOrdre(), pointDescente.getOrdre());
            throw new BusinessValidationException("montée après la descente");
        }

        // Règle : Un passager n'a qu'une seule réservation CONFIRMEE par trajet
        if (reservationRepository.existsByTrajetIdAndPassagerIdAndStatut(trajet.getId(), passager.getId(), StatutReservation.CONFIRMEE)) {
            log.warn("Refus de réservation : le passager {} possède déjà une réservation confirmée sur le trajet {}",
                    passager.getId(), trajet.getId());
            throw new BusinessConflictException("Vous avez déjà une réservation confirmée sur ce trajet");
        }

        // Vérification des places restantes
        int placesRestantesActuelles = trajetService.calculatePlacesRestantes(trajet);
        if (request.nombrePlaces() > placesRestantesActuelles) {
            log.warn("Refus de réservation : {} places demandées mais seulement {} restantes sur le trajet {}",
                    request.nombrePlaces(), placesRestantesActuelles, trajet.getId());
            throw new BusinessConflictException("Nombre de places insuffisant (disponibles : " + placesRestantesActuelles + ")");
        }

        // Calcul automatique du montant du segment
        // Montant total = (prix du point de descente − prix du point de montée) × NombrePlaces
        BigDecimal prixUnitaire = pointDescente.getPrixDepuisDepart().subtract(pointMontee.getPrixDepuisDepart());
        BigDecimal montantTotal = prixUnitaire.multiply(BigDecimal.valueOf(request.nombrePlaces()))
                .setScale(2, RoundingMode.HALF_UP);

        Reservation reservation = Reservation.builder()
                .passager(passager)
                .trajet(trajet)
                .pointMontee(pointMontee)
                .pointDescente(pointDescente)
                .nombrePlaces(request.nombrePlaces())
                .montantTotal(montantTotal)
                .dateReservation(LocalDateTime.now())
                .statut(StatutReservation.CONFIRMEE)
                .build();

        Reservation saved = reservationRepository.save(reservation);

        // Mise à jour automatique du statut du trajet (Scénario 3 : devient COMPLET si 0 places restantes)
        int nouvellesPlacesRestantes = placesRestantesActuelles - request.nombrePlaces();
        if (nouvellesPlacesRestantes == 0) {
            trajet.setStatut(StatutTrajet.COMPLET);
            trajetRepository.save(trajet);
            log.info("Le trajet {} est devenu COMPLET suite à la réservation {}", trajet.getId(), saved.getId());
        }

        log.info("Réservation effectuée avec succès : id={}, passager={}, montant={}, placesRestantes={}",
                saved.getId(), passager.getEmail(), montantTotal, nouvellesPlacesRestantes);

        return reservationMapper.toResponse(saved, nouvellesPlacesRestantes);
    }

    @Override
    @Transactional
    public void cancelReservation(Long reservationId, Long requestingPassengerId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> {
                    log.warn("Réservation introuvable pour annulation : id={}", reservationId);
                    return new ResourceNotFoundException("Réservation non trouvée avec l'identifiant : " + reservationId);
                });

        if (requestingPassengerId != null && !reservation.getPassager().getId().equals(requestingPassengerId)) {
            log.warn("Action interdite : le passager {} tente d'annuler la réservation {} du passager {}",
                    requestingPassengerId, reservationId, reservation.getPassager().getId());
            throw new ForbiddenOperationException("Vous ne pouvez annuler que vos propres réservations");
        }

        if (reservation.getStatut() == StatutReservation.ANNULEE) {
            log.warn("Refus d'annulation : réservation {} déjà annulée", reservationId);
            throw new BusinessConflictException("Cette réservation est déjà annulée");
        }

        Trajet trajet = reservation.getTrajet();

        // Règle 10 : Un passager annule jusqu'à 24 heures avant le départ (Scénario 10: délai de 24 h dépassé -> 409)
        LocalDateTime limiteAnnulation = trajet.getDateDepart().minusHours(24);
        if (LocalDateTime.now().isAfter(limiteAnnulation)) {
            log.warn("Refus d'annulation : délai de 24h dépassé pour la réservation {}. Départ={}, Limite={}",
                    reservationId, trajet.getDateDepart(), limiteAnnulation);
            throw new BusinessConflictException("délai de 24 h dépassé");
        }

        reservation.setStatut(StatutReservation.ANNULEE);
        reservationRepository.save(reservation);

        // Mise à jour automatique : Si le trajet était COMPLET, il redevient PUBLIE car des places se libèrent
        if (trajet.getStatut() == StatutTrajet.COMPLET) {
            trajet.setStatut(StatutTrajet.PUBLIE);
            trajetRepository.save(trajet);
            log.info("Le trajet {} redevient PUBLIE suite à l'annulation de la réservation {}", trajet.getId(), reservationId);
        }

        log.info("Réservation {} annulée avec succès (Scénario nominal)", reservationId);
    }

    @Override
    public ReservationResponse getReservationById(Long reservationId, Long requestingPassengerId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation non trouvée avec l'identifiant : " + reservationId));

        if (requestingPassengerId != null && !reservation.getPassager().getId().equals(requestingPassengerId)) {
            log.warn("Action interdite : passager {} a tenté d'accéder à la réservation {} d'un autre passager",
                    requestingPassengerId, reservationId);
            throw new ForbiddenOperationException("Vous ne pouvez consulter que vos propres réservations");
        }

        int placesRestantes = trajetService.calculatePlacesRestantes(reservation.getTrajet());
        return reservationMapper.toResponse(reservation, placesRestantes);
    }

    @Override
    public List<ReservationResponse> getReservationsByPassager(Long passagerId, Long requestingPassengerId) {
        if (requestingPassengerId != null && !requestingPassengerId.equals(passagerId)) {
            log.warn("Action interdite : consultation des réservations du passager {} par {}", passagerId, requestingPassengerId);
            throw new ForbiddenOperationException("Vous ne pouvez consulter que vos propres réservations");
        }

        if (!passagerRepository.existsById(passagerId)) {
            throw new ResourceNotFoundException("Passager non trouvé avec l'identifiant : " + passagerId);
        }

        return reservationRepository.findByPassagerIdWithDetails(passagerId).stream()
                .map(r -> reservationMapper.toResponse(r, trajetService.calculatePlacesRestantes(r.getTrajet())))
                .toList();
    }
}
