package com.covoiturage.service.impl;

import com.covoiturage.domain.entity.Conducteur;
import com.covoiturage.domain.entity.PointArret;
import com.covoiturage.domain.entity.Trajet;
import com.covoiturage.domain.entity.Vehicule;
import com.covoiturage.domain.enums.StatutTrajet;
import com.covoiturage.domain.repository.ConducteurRepository;
import com.covoiturage.domain.repository.ReservationRepository;
import com.covoiturage.domain.repository.TrajetRepository;
import com.covoiturage.domain.repository.VehiculeRepository;
import com.covoiturage.dto.request.PointArretRequest;
import com.covoiturage.dto.request.TrajetCreateRequest;
import com.covoiturage.dto.response.ReservationResponse;
import com.covoiturage.dto.response.TrajetDetailResponse;
import com.covoiturage.dto.response.TrajetSearchResponse;
import com.covoiturage.dto.response.TrajetSearchResult;
import com.covoiturage.dto.response.TrajetSummaryResponse;
import com.covoiturage.exception.BusinessValidationException;
import com.covoiturage.exception.ForbiddenOperationException;
import com.covoiturage.exception.ResourceNotFoundException;
import com.covoiturage.mapper.ConducteurMapper;
import com.covoiturage.mapper.PointArretMapper;
import com.covoiturage.mapper.ReservationMapper;
import com.covoiturage.mapper.TrajetMapper;
import com.covoiturage.mapper.VehiculeMapper;
import com.covoiturage.service.TrajetService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrajetServiceImpl implements TrajetService {

    private static final Logger log = LoggerFactory.getLogger(TrajetServiceImpl.class);

    private final TrajetRepository trajetRepository;
    private final ConducteurRepository conducteurRepository;
    private final VehiculeRepository vehiculeRepository;
    private final ReservationRepository reservationRepository;
    private final TrajetMapper trajetMapper;
    private final PointArretMapper pointArretMapper;
    private final ReservationMapper reservationMapper;
    private final ConducteurMapper conducteurMapper;
    private final VehiculeMapper vehiculeMapper;

    @Override
    @Transactional
    public TrajetDetailResponse publishTrajet(TrajetCreateRequest request, Long requestingDriverId) {
        if (requestingDriverId != null && !requestingDriverId.equals(request.conducteurId())) {
            log.warn("Action interdite : conducteur {} a tenté de publier pour le compte du conducteur {}",
                    requestingDriverId, request.conducteurId());
            throw new ForbiddenOperationException("Vous ne pouvez publier un trajet que pour votre propre compte");
        }

        Conducteur conducteur = conducteurRepository.findById(request.conducteurId())
                .orElseThrow(() -> {
                    log.warn("Conducteur introuvable lors de la publication du trajet : id={}", request.conducteurId());
                    return new ResourceNotFoundException("Conducteur non trouvé avec l'identifiant : " + request.conducteurId());
                });

        Vehicule vehicule = vehiculeRepository.findById(request.vehiculeId())
                .orElseThrow(() -> {
                    log.warn("Véhicule introuvable lors de la publication du trajet : id={}", request.vehiculeId());
                    return new ResourceNotFoundException("Véhicule non trouvé avec l'identifiant : " + request.vehiculeId());
                });

        // Règle 8 : Le conducteur publie uniquement avec l'un de ses propres véhicules (403)
        if (!vehicule.getConducteur().getId().equals(conducteur.getId())) {
            log.warn("Refus de publication : le véhicule {} n'appartient pas au conducteur {}", vehicule.getId(), conducteur.getId());
            throw new ForbiddenOperationException("Le conducteur ne peut publier qu'avec un de ses propres véhicules");
        }

        // Règle 7 : Places proposées comprises entre 1 et la capacité du véhicule (400)
        if (request.placesProposees() < 1 || request.placesProposees() > vehicule.getCapacite()) {
            log.warn("Refus de publication : {} places proposées pour une capacité de {}", request.placesProposees(), vehicule.getCapacite());
            throw new BusinessValidationException(vehicule.getCapacite() + " places au maximum");
        }

        // Validation du parcours et des points d'arrêt
        List<PointArretRequest> pointsRequest = request.pointsArret();
        if (pointsRequest == null || pointsRequest.size() < 2) {
            log.warn("Refus de publication : parcours incomplet (< 2 points)");
            throw new BusinessValidationException("Un trajet doit comporter au moins deux points d'arrêt (départ et arrivée)");
        }

        // Tri des points par ordre
        List<PointArretRequest> sortedPoints = pointsRequest.stream()
                .sorted(Comparator.comparingInt(PointArretRequest::ordre))
                .toList();

        PointArretRequest premierPoint = sortedPoints.get(0);
        PointArretRequest dernierPoint = sortedPoints.get(sortedPoints.size() - 1);

        if (premierPoint.ordre() != 0) {
            log.warn("Refus de publication : le premier point d'arrêt doit avoir l'ordre 0");
            throw new BusinessValidationException("Le point de départ doit avoir l'ordre 0");
        }

        // Le départ est dans le futur
        if (!premierPoint.heurePassage().isAfter(LocalDateTime.now())) {
            log.warn("Refus de publication : date de départ dans le passé ou présent ({})", premierPoint.heurePassage());
            throw new BusinessValidationException("L'heure de départ du trajet doit être dans le futur");
        }

        // Le prix au départ vaut 0
        if (premierPoint.prixDepuisDepart().setScale(2, RoundingMode.HALF_UP).compareTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)) != 0) {
            log.warn("Refus de publication : prix du premier point d'arrêt non nul ({})", premierPoint.prixDepuisDepart());
            throw new BusinessValidationException("Le prix depuis le départ au premier point doit être égal à 0 DH");
        }

        // Vérification unicité des villes, chronologie stricte des heures et croissance stricte des prix
        Set<String> villesUniques = new HashSet<>();
        LocalDateTime previousTime = null;
        BigDecimal previousPrice = null;
        int expectedOrder = 0;

        for (PointArretRequest p : sortedPoints) {
            if (p.ordre() != expectedOrder) {
                log.warn("Refus de publication : discontinuité des ordres des points d'arrêt (attendu: {}, reçu: {})", expectedOrder, p.ordre());
                throw new BusinessValidationException("Les ordres des points d'arrêt doivent être consécutifs (0, 1, 2...)");
            }
            expectedOrder++;

            String villeNormalisee = p.ville().trim().toLowerCase();
            if (!villesUniques.add(villeNormalisee)) {
                log.warn("Refus de publication : ville présente plusieurs fois ({})", p.ville());
                throw new BusinessValidationException("Une ville n'apparaît qu'une seule fois sur le parcours : " + p.ville());
            }

            if (previousTime != null) {
                if (!p.heurePassage().isAfter(previousTime)) {
                    log.warn("Refus de publication : heures de passage non croissantes (précédent: {}, actuel: {})", previousTime, p.heurePassage());
                    throw new BusinessValidationException("Les heures de passage doivent être strictement croissantes");
                }
            }
            previousTime = p.heurePassage();

            BigDecimal currentPrice = p.prixDepuisDepart().setScale(2, RoundingMode.HALF_UP);
            if (previousPrice != null) {
                if (currentPrice.compareTo(previousPrice) <= 0) {
                    log.warn("Refus de publication : prix non strictement croissant (précédent: {}, actuel: {})", previousPrice, currentPrice);
                    throw new BusinessValidationException("Le prix depuis le départ doit être strictement croissant");
                }
            }
            previousPrice = currentPrice;
        }

        // Construction du trajet
        Trajet trajet = Trajet.builder()
                .conducteur(conducteur)
                .vehicule(vehicule)
                .villeDepart(premierPoint.ville().trim())
                .villeArrivee(dernierPoint.ville().trim())
                .dateDepart(premierPoint.heurePassage())
                .placesProposees(request.placesProposees())
                .statut(StatutTrajet.PUBLIE)
                .build();

        for (PointArretRequest p : sortedPoints) {
            PointArret pointArret = pointArretMapper.toEntity(p);
            pointArret.setVille(p.ville().trim());
            pointArret.setPrixDepuisDepart(p.prixDepuisDepart().setScale(2, RoundingMode.HALF_UP));
            trajet.addPointArret(pointArret);
        }

        Trajet saved = trajetRepository.save(trajet);
        log.info("Trajet publié avec succès : id={}, depart={}, arrivee={}, places={}, conducteurId={}",
                saved.getId(), saved.getVilleDepart(), saved.getVilleArrivee(), saved.getPlacesProposees(), conducteur.getId());

        int placesRestantes = calculatePlacesRestantes(saved);
        return trajetMapper.toDetailResponse(saved, placesRestantes);
    }

    @Override
    public TrajetDetailResponse getTrajetById(Long id) {
        Trajet trajet = trajetRepository.findByIdWithDetails(id)
                .orElseThrow(() -> {
                    log.warn("Trajet introuvable avec l'id : {}", id);
                    return new ResourceNotFoundException("Trajet non trouvé avec l'identifiant : " + id);
                });

        int placesRestantes = calculatePlacesRestantes(trajet);
        return trajetMapper.toDetailResponse(trajet, placesRestantes);
    }

    @Override
    public List<TrajetSummaryResponse> getTrajetsByConducteur(Long conducteurId) {
        if (!conducteurRepository.existsById(conducteurId)) {
            log.warn("Conducteur introuvable pour consultation des trajets : id={}", conducteurId);
            throw new ResourceNotFoundException("Conducteur non trouvé avec l'identifiant : " + conducteurId);
        }

        return trajetRepository.findByConducteurIdWithDetails(conducteurId).stream()
                .map(t -> trajetMapper.toSummaryResponse(t, calculatePlacesRestantes(t)))
                .toList();
    }

    @Override
    public int calculatePlacesRestantes(Trajet trajet) {
        int reservedSeats = reservationRepository.countConfirmedSeatsByTrajetId(trajet.getId());
        int restantes = trajet.getPlacesProposees() - reservedSeats;
        return Math.max(0, restantes);
    }

    @Override
    @Transactional
    public void checkAndUpdateTrajetStatus(Trajet trajet) {
        if (trajet.getStatut() == StatutTrajet.TERMINE || trajet.getStatut() == StatutTrajet.ANNULE) {
            return;
        }

        int placesRestantes = calculatePlacesRestantes(trajet);
        if (placesRestantes == 0 && trajet.getStatut() == StatutTrajet.PUBLIE) {
            trajet.setStatut(StatutTrajet.COMPLET);
            trajetRepository.save(trajet);
            log.info("Trajet {} devenu COMPLET (0 places restantes)", trajet.getId());
        } else if (placesRestantes > 0 && trajet.getStatut() == StatutTrajet.COMPLET) {
            trajet.setStatut(StatutTrajet.PUBLIE);
            trajetRepository.save(trajet);
            log.info("Trajet {} redevenu PUBLIE ({} places restantes)", trajet.getId(), placesRestantes);
        }
    }

    @Override
    public List<ReservationResponse> getReservationsByTrajet(Long trajetId, Long requestingDriverId) {
        Trajet trajet = trajetRepository.findByIdWithDetails(trajetId)
                .orElseThrow(() -> new ResourceNotFoundException("Trajet non trouvé avec l'identifiant : " + trajetId));

        if (requestingDriverId != null && !trajet.getConducteur().getId().equals(requestingDriverId)) {
            log.warn("Action interdite : conducteur {} a tenté de consulter les réservations du trajet {} du conducteur {}",
                    requestingDriverId, trajetId, trajet.getConducteur().getId());
            throw new ForbiddenOperationException("Vous ne pouvez consulter que les réservations de vos propres trajets");
        }

        return reservationRepository.findByTrajetIdWithDetails(trajetId).stream()
                .map(reservationMapper::toResponse)
                .toList();
    }

    @Override
    public Page<TrajetSearchResponse> searchTrajets(
            String villeDepart,
            String villeArrivee,
            LocalDate dateDepart,
            Integer placesDemandees,
            BigDecimal maxPrix,
            Pageable pageable) {

        if (villeDepart == null || villeDepart.trim().isEmpty()) {
            throw new BusinessValidationException("La ville de départ est obligatoire pour la recherche");
        }
        if (villeArrivee == null || villeArrivee.trim().isEmpty()) {
            throw new BusinessValidationException("La ville d'arrivée est obligatoire pour la recherche");
        }
        if (dateDepart == null) {
            throw new BusinessValidationException("La date de départ est obligatoire pour la recherche");
        }

        int requiredSeats = (placesDemandees != null && placesDemandees > 0) ? placesDemandees : 1;
        LocalDateTime startOfDay = dateDepart.atStartOfDay();
        LocalDateTime endOfDay = dateDepart.atTime(LocalTime.MAX);

        log.info("Recherche de trajets : départ='{}', arrivée='{}', date={}, places={}, maxPrix={}",
                villeDepart, villeArrivee, dateDepart, requiredSeats, maxPrix);

        Page<TrajetSearchResult> results = trajetRepository.searchTrajets(
                villeDepart.trim(),
                villeArrivee.trim(),
                startOfDay,
                endOfDay,
                requiredSeats,
                maxPrix,
                pageable);

        log.info("Recherche terminée : {} trajet(s) trouvé(s)", results.getTotalElements());

        return results.map(res -> {
            Trajet t = res.trajet();
            int placesRestantes = Math.max(0, (int) (t.getPlacesProposees() - res.reservedSeats()));
            return new TrajetSearchResponse(
                    t.getId(),
                    res.pointDepart().getVille(),
                    res.pointArrivee().getVille(),
                    res.pointDepart().getHeurePassage(),
                    res.pointArrivee().getHeurePassage(),
                    res.prixSegment().setScale(2, RoundingMode.HALF_UP),
                    placesRestantes,
                    t.getStatut(),
                    conducteurMapper.toResponse(t.getConducteur()),
                    vehiculeMapper.toResponse(t.getVehicule()),
                    pointArretMapper.toResponseList(t.getPointsArret())
            );
        });
    }
}
