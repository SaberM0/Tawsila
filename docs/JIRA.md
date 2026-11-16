# Projet JIRA : Plateforme de Covoiturage Multi-Étapes

**Clé du Projet** : `COV`  
**Méthodologie** : Scrum / Agile  
**Durée du Sprint** : 4 jours (2 sprints de 2 jours)  
**Dates** : 16/11/2026 - 20/11/2026  
**Équipe** : Mouad (Lead Developer)  

---

## 1. Structure des Epics

| Clé Epic | Titre de l'Epic | Description | Statut |
| :--- | :--- | :--- | :--- |
| **COV-EPIC-01** | Architecture & Socle Technique | Initialisation du projet Spring Boot 3, Maven, PostgreSQL, Docker, OpenAPI et gestion centralisée des erreurs | **DONE** |
| **COV-EPIC-02** | Gestion des Profils (Conducteurs & Passagers) | Création, mise à jour, recherche et consultation des comptes conducteurs et passagers | **DONE** |
| **COV-EPIC-03** | Gestion du Parc de Véhicules | Gestion des véhicules, contrôle d'unicité d'immatriculation et règle de suppression protégée | **DONE** |
| **COV-EPIC-04** | Publication & Cycle de Vie des Trajets | Publication avec parcours multi-étapes ordonné, contrôle chronologique et clôture/annulation | **DONE** |
| **COV-EPIC-05** | Moteur de Recherche Multi-Arrêts | Recherche JPQL optimisée entre deux étapes avec calcul de segment et pagination | **DONE** |
| **COV-EPIC-06** | Réservation & Tarification Dynamique | Calcul du prix de segment, contrôle de délai 24h, mise à jour dynamique des statuts | **DONE** |
| **COV-EPIC-07** | Qualité, Tests Unitaires & Documentation | Tests unitaires JUnit 5, validation des 11 scénarios du brief, collection Postman et diagrammes UML | **DONE** |

---

## 2. Backlog Détaillé des User Stories & Tâches

### Sprint 1 (16/11/2026 - 18/11/2026) : Socle, Profils, Véhicules & Trajets

#### COV-01 : Initialisation de l'architecture logicielle
* **Type** : Task  
* **Epic** : COV-EPIC-01  
* **Estimation** : 3 SP  
* **Statut** : **DONE**  
* **Description** : Mise en place du projet Spring Boot 3.3.4 avec Java 17, Maven, configuration JPA/Hibernate, gestion des profils `dev` (H2) et `postgres`.

#### COV-02 : Gestion centralisée des exceptions RFC 7807
* **Type** : Story  
* **Epic** : COV-EPIC-01  
* **Estimation** : 3 SP  
* **Statut** : **DONE**  
* **Critères d'acceptation** :
  - Handler `@RestControllerAdvice` générant des réponses JSON standardisées `ProblemDetail`.
  - Codes HTTP retournés : 400 (validation), 403 (action interdite), 404 (non trouvé), 409 (règle métier violée), 500 (erreur interne).

#### COV-03 : Gestion des Conducteurs
* **Type** : User Story  
* **Epic** : COV-EPIC-02  
* **Estimation** : 5 SP  
* **Statut** : **DONE**  
* **En tant que** conducteur, **je veux** créer et modifier mon compte **afin de** proposer des trajets.  
* **Critères d'acceptation** :
  - *Given* un email ou numéro de permis déjà enregistré, *When* un conducteur tente de s'inscrire, *Then* le système rejette avec un code HTTP 409.
  - Recherche paginée par nom, prénom ou email.

#### COV-04 : Gestion des Passagers
* **Type** : User Story  
* **Epic** : COV-EPIC-02  
* **Estimation** : 3 SP  
* **Statut** : **DONE**  
* **En tant que** passager, **je veux** créer mon profil et consulter mon historique **afin de** réserver des trajets.  
* **Critères d'acceptation** :
  - Unicité de l'email obligatoire.
  - Recherche par email et consultation de ses réservations.

#### COV-05 : Gestion des Véhicules d'un Conducteur
* **Type** : User Story  
* **Epic** : COV-EPIC-03  
* **Estimation** : 5 SP  
* **Statut** : **DONE**  
* **En tant que** conducteur, **je veux** enregistrer mes véhicules **afin de** les affecter à mes trajets.  
* **Critères d'acceptation** :
  - Capacité passagers strictement comprise entre 1 et 8 places.
  - Immatriculation unique.
  - Suppression interdite si le véhicule est associé à un ou plusieurs trajets (409 Conflict).
  - Un conducteur ne peut pas modifier ou supprimer le véhicule d'un autre conducteur (403 Forbidden).

#### COV-06 : Publication de Trajets Multi-Étapes
* **Type** : User Story  
* **Epic** : COV-EPIC-04  
* **Estimation** : 8 SP  
* **Statut** : **DONE**  
* **En tant que** conducteur, **je veux** publier un trajet avec plusieurs points d'arrêt ordonnés **afin de** proposer des places.  
* **Critères d'acceptation** :
  - Le conducteur ne peut publier qu'avec un de SES propres véhicules (403).
  - Le nombre de places proposées est compris entre 1 et la capacité maximale du véhicule (Scénario 7 : 5 places dans un véhicule de 4 -> 400).
  - Minimum 2 points d'arrêt (ordre 0 pour le départ).
  - Date de départ dans le futur et heures de passage strictement croissantes.
  - Prix au départ égal à 0 DH, puis strictement croissant.
  - Chaque ville n'apparaît qu'une seule fois sur le parcours.

---

### Sprint 2 (18/11/2026 - 20/11/2026) : Recherche, Réservations, Clôture & Évaluation

#### COV-07 : Recherche Optimisée de Trajets entre Deux Villes
* **Type** : User Story  
* **Epic** : COV-EPIC-05  
* **Estimation** : 8 SP  
* **Statut** : **DONE**  
* **En tant que** passager, **je veux** rechercher des trajets entre deux points d'arrêt et une date **afin de** trouver un itinéraire adéquat.  
* **Critères d'acceptation** :
  - Requête JPQL unique joignant deux points d'arrêt du même trajet.
  - Respect strict du sens du trajet : ordre montée < ordre descente (Scénario 2 : Settat -> Casablanca ne renvoie pas le trajet de Hamid).
  - Calcul dynamique du prix du segment recherché (`prixDescente - prixMontee`).
  - Filtrage sur le prix maximal appliqué au segment.
  - Pagination et tri par heure de départ.

#### COV-08 : Réservation de Places sur un Segment
* **Type** : User Story  
* **Epic** : COV-EPIC-06  
* **Estimation** : 8 SP  
* **Statut** : **DONE**  
* **En tant que** passager, **je veux** réserver une ou plusieurs places entre ma ville de montée et ma ville de descente.  
* **Critères d'acceptation** :
  - Scénario 1 : Salma réserve 1 place Settat -> Marrakech -> 201 ; montant 50 DH ; 2 places restantes.
  - Scénario 3 : Karim réserve 2 places Casablanca -> Marrakech -> 201 ; montant 160 DH ; trajet devient COMPLET.
  - Scénario 4 : Imane réserve sur un trajet COMPLET -> 409 Conflict.
  - Scénario 6 : Montée à Marrakech et descente à Settat -> 400 Bad Request ("montée après la descente").
  - Matrice des rôles : un conducteur ne peut pas réserver de places (403).

#### COV-09 : Annulation de Réservation avec Règle des 24h
* **Type** : User Story  
* **Epic** : COV-EPIC-06  
* **Estimation** : 5 SP  
* **Statut** : **DONE**  
* **En tant que** passager, **je veux** annuler ma réservation si mes plans changent.  
* **Critères d'acceptation** :
  - Scénario 5 : Karim annule 3 jours avant -> 204 No Content ; le trajet redevient PUBLIE.
  - Scénario 10 : Salma annule 2 heures avant le départ -> 409 Conflict ("délai de 24 h dépassé").

#### COV-10 : Cycle de Vie du Trajet (Clôture & Annulation en Masse)
* **Type** : User Story  
* **Epic** : COV-EPIC-04  
* **Estimation** : 5 SP  
* **Statut** : **DONE**  
* **En tant que** conducteur propriétaire, **je veux** terminer ou annuler mon trajet.  
* **Critères d'acceptation** :
  - Scénario 9 : Terminer le trajet avant son heure de départ -> 409 Conflict.
  - Terminer après l'heure de départ -> statut TERMINE.
  - Scénario 11 : Annuler un trajet avec réservations -> 204 No Content ; le trajet et toutes les réservations CONFIRMEE passent à ANNULEE dans la même transaction (@Transactional).

#### COV-11 : Infrastructure Docker & Jeu de Données
* **Type** : Task  
* **Epic** : COV-EPIC-07  
* **Estimation** : 3 SP  
* **Statut** : **DONE**  
* **Description** : Docker Compose avec PostgreSQL 16 et pgAdmin 4. Initialisation automatique de minimum 5 enregistrements dans chaque table (Conducteurs, Passagers, Véhicules, Trajets, Points d'arrêt, Réservations).

#### COV-12 : Tests Automatisés & Validation des 11 Scénarios
* **Type** : Task  
* **Epic** : COV-EPIC-07  
* **Estimation** : 5 SP  
* **Statut** : **DONE**  
* **Description** : Couverture complète par tests unitaires JUnit 5, Mockito et AssertJ validant les cas nominaux et règles refusées.

---

## 3. Matrice de Traçabilité des Scénarios de Validation

| # Scénario | Intitulé Scénario | Ticket JIRA | Résultat Attendu | Statut du Test |
| :---: | :--- | :---: | :--- | :---: |
| **1** | Salma réserve 1 place Settat → Marrakech | `COV-08` | 201 ; montant 50 DH ; 2 places restantes | **PASS** |
| **2** | Karim recherche Settat → Casablanca | `COV-07` | 200 ; le trajet de Hamid n'apparaît pas | **PASS** |
| **3** | Karim réserve 2 places Casablanca → Marrakech | `COV-08` | 201 ; montant 160 DH ; trajet COMPLET | **PASS** |
| **4** | Imane réserve 1 place sur le trajet complet | `COV-08` | 409 | **PASS** |
| **5** | Karim annule sa réservation 3 jours avant | `COV-09` | 204 ; trajet de nouveau PUBLIE | **PASS** |
| **6** | Imane réserve avec la montée à Marrakech et la descente à Settat | `COV-08` | 400 ; montée après la descente | **PASS** |
| **7** | Hamid propose 5 places dans un véhicule d'une capacité de 4 | `COV-06` | 400 ; 4 places au maximum | **PASS** |
| **8** | Hamid publie avec le véhicule d'un autre conducteur | `COV-06` | 403 | **PASS** |
| **9** | Hamid termine le trajet avant 17:00 le jour du départ | `COV-10` | 409 | **PASS** |
| **10** | Salma annule 2 heures avant le départ | `COV-09` | 409 ; délai de 24 h dépassé | **PASS** |
| **11** | Hamid annule un trajet qui a deux réservations | `COV-10` | 204 ; trajet et réservations annulés ensemble | **PASS** |
