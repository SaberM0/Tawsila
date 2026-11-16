-- Initialisation de la base de données Covoiturage Multi-Étapes
-- Minimum 5 enregistrements dans chacune des tables

CREATE TABLE IF NOT EXISTS conducteurs (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    telephone VARCHAR(30) NOT NULL,
    numero_permis VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS passagers (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    telephone VARCHAR(30) NOT NULL
);

CREATE TABLE IF NOT EXISTS vehicules (
    id BIGSERIAL PRIMARY KEY,
    marque VARCHAR(100) NOT NULL,
    modele VARCHAR(100) NOT NULL,
    immatriculation VARCHAR(50) NOT NULL UNIQUE,
    couleur VARCHAR(50) NOT NULL,
    capacite INT NOT NULL CHECK (capacite >= 1 AND capacite <= 8),
    conducteur_id BIGINT NOT NULL REFERENCES conducteurs(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS trajets (
    id BIGSERIAL PRIMARY KEY,
    conducteur_id BIGINT NOT NULL REFERENCES conducteurs(id) ON DELETE CASCADE,
    vehicule_id BIGINT NOT NULL REFERENCES vehicules(id) ON DELETE RESTRICT,
    ville_depart VARCHAR(100) NOT NULL,
    ville_arrivee VARCHAR(100) NOT NULL,
    date_depart TIMESTAMP NOT NULL,
    places_proposees INT NOT NULL CHECK (places_proposees >= 1),
    statut VARCHAR(30) NOT NULL DEFAULT 'PUBLIE'
);

CREATE TABLE IF NOT EXISTS points_arret (
    id BIGSERIAL PRIMARY KEY,
    trajet_id BIGINT NOT NULL REFERENCES trajets(id) ON DELETE CASCADE,
    ville VARCHAR(100) NOT NULL,
    ordre INT NOT NULL CHECK (ordre >= 0),
    heure_passage TIMESTAMP NOT NULL,
    prix_depuis_depart NUMERIC(10, 2) NOT NULL CHECK (prix_depuis_depart >= 0)
);

CREATE TABLE IF NOT EXISTS reservations (
    id BIGSERIAL PRIMARY KEY,
    passager_id BIGINT NOT NULL REFERENCES passagers(id) ON DELETE CASCADE,
    trajet_id BIGINT NOT NULL REFERENCES trajets(id) ON DELETE CASCADE,
    point_montee_id BIGINT NOT NULL REFERENCES points_arret(id) ON DELETE RESTRICT,
    point_descente_id BIGINT NOT NULL REFERENCES points_arret(id) ON DELETE RESTRICT,
    nombre_places INT NOT NULL CHECK (nombre_places >= 1),
    montant_total NUMERIC(10, 2) NOT NULL CHECK (montant_total >= 0),
    date_reservation TIMESTAMP NOT NULL,
    statut VARCHAR(30) NOT NULL DEFAULT 'CONFIRMEE'
);

-- Insertion des 5 conducteurs
INSERT INTO conducteurs (nom, prenom, email, telephone, numero_permis) VALUES
('El Amrani', 'Hamid', 'hamid.elamrani@covoiturage.ma', '0661123456', 'PERMIS-1001-A'),
('Benjelloun', 'Youssef', 'youssef.benjelloun@covoiturage.ma', '0662234567', 'PERMIS-1002-B'),
('Idrissi', 'Fatima Zahra', 'fatima.idrissi@covoiturage.ma', '0663345678', 'PERMIS-1003-C'),
('Tazi', 'Omar', 'omar.tazi@covoiturage.ma', '0664456789', 'PERMIS-1004-D'),
('Bennani', 'Sara', 'sara.bennani@covoiturage.ma', '0665567890', 'PERMIS-1005-E');

-- Insertion des 5 passagers
INSERT INTO passagers (nom, prenom, email, telephone) VALUES
('Alami', 'Karim', 'karim.alami@covoiturage.ma', '0671112233'),
('Chraibi', 'Salma', 'salma.chraibi@covoiturage.ma', '0672223344'),
('Mansouri', 'Imane', 'imane.mansouri@covoiturage.ma', '0673334455'),
('Alaoui', 'Mehdi', 'mehdi.alaoui@covoiturage.ma', '0674445566'),
('Bouzidi', 'Noura', 'noura.bouzidi@covoiturage.ma', '0675556677');

-- Insertion des 5+ véhicules
INSERT INTO vehicules (marque, modele, immatriculation, couleur, capacite, conducteur_id) VALUES
('Dacia', 'Logan', '12345-A-1', 'Gris Platine', 4, 1),
('Renault', 'Clio 5', '67890-B-6', 'Blanc Glacier', 4, 2),
('Peugeot', '208', '11223-D-26', 'Bleu Vertigo', 4, 3),
('Volkswagen', 'Golf 8', '44556-E-1', 'Noir Intense', 4, 4),
('Toyota', 'Yaris', '77889-A-6', 'Rouge Allure', 4, 5),
('Dacia', 'Duster', '99001-A-1', 'Orange Atacama', 4, 1);

-- Insertion des 5 trajets (dans le futur)
INSERT INTO trajets (conducteur_id, vehicule_id, ville_depart, ville_arrivee, date_depart, places_proposees, statut) VALUES
(1, 1, 'Rabat', 'Marrakech', CURRENT_TIMESTAMP + INTERVAL '3 days', 3, 'PUBLIE'),
(2, 2, 'Casablanca', 'Tanger', CURRENT_TIMESTAMP + INTERVAL '4 days', 3, 'PUBLIE'),
(3, 3, 'Fes', 'Agadir', CURRENT_TIMESTAMP + INTERVAL '5 days', 3, 'PUBLIE'),
(4, 4, 'Marrakech', 'Oujda', CURRENT_TIMESTAMP + INTERVAL '6 days', 4, 'PUBLIE'),
(5, 5, 'Tanger', 'Casablanca', CURRENT_TIMESTAMP + INTERVAL '7 days', 3, 'PUBLIE');

-- Insertion des points d'arrêt ordonnés
-- Trajet 1 : Hamid (Rabat -> Casablanca -> Settat -> Marrakech)
INSERT INTO points_arret (trajet_id, ville, ordre, heure_passage, prix_depuis_depart) VALUES
(1, 'Rabat', 0, CURRENT_TIMESTAMP + INTERVAL '3 days', 0.00),
(1, 'Casablanca', 1, CURRENT_TIMESTAMP + INTERVAL '3 days' + INTERVAL '1 hour 15 minutes', 40.00),
(1, 'Settat', 2, CURRENT_TIMESTAMP + INTERVAL '3 days' + INTERVAL '2 hours 15 minutes', 70.00),
(1, 'Marrakech', 3, CURRENT_TIMESTAMP + INTERVAL '3 days' + INTERVAL '4 hours 30 minutes', 120.00);

-- Trajet 2 : Youssef (Casablanca -> Tanger)
INSERT INTO points_arret (trajet_id, ville, ordre, heure_passage, prix_depuis_depart) VALUES
(2, 'Casablanca', 0, CURRENT_TIMESTAMP + INTERVAL '4 days', 0.00),
(2, 'Rabat', 1, CURRENT_TIMESTAMP + INTERVAL '4 days' + INTERVAL '1 hour', 40.00),
(2, 'Kenitra', 2, CURRENT_TIMESTAMP + INTERVAL '4 days' + INTERVAL '1 hour 45 minutes', 60.00),
(2, 'Asilah', 3, CURRENT_TIMESTAMP + INTERVAL '4 days' + INTERVAL '3 hours 30 minutes', 110.00),
(2, 'Tanger', 4, CURRENT_TIMESTAMP + INTERVAL '4 days' + INTERVAL '4 hours 15 minutes', 140.00);

-- Trajet 3 : Fatima (Fes -> Agadir)
INSERT INTO points_arret (trajet_id, ville, ordre, heure_passage, prix_depuis_depart) VALUES
(3, 'Fes', 0, CURRENT_TIMESTAMP + INTERVAL '5 days', 0.00),
(3, 'Meknes', 1, CURRENT_TIMESTAMP + INTERVAL '5 days' + INTERVAL '45 minutes', 25.00),
(3, 'Rabat', 2, CURRENT_TIMESTAMP + INTERVAL '5 days' + INTERVAL '2 hours 30 minutes', 70.00),
(3, 'Casablanca', 3, CURRENT_TIMESTAMP + INTERVAL '5 days' + INTERVAL '3 hours 45 minutes', 100.00),
(3, 'Agadir', 4, CURRENT_TIMESTAMP + INTERVAL '5 days' + INTERVAL '8 hours', 200.00);

-- Trajet 4 : Omar (Marrakech -> Oujda)
INSERT INTO points_arret (trajet_id, ville, ordre, heure_passage, prix_depuis_depart) VALUES
(4, 'Marrakech', 0, CURRENT_TIMESTAMP + INTERVAL '6 days', 0.00),
(4, 'Casablanca', 1, CURRENT_TIMESTAMP + INTERVAL '6 days' + INTERVAL '2 hours 45 minutes', 90.00),
(4, 'Rabat', 2, CURRENT_TIMESTAMP + INTERVAL '6 days' + INTERVAL '4 hours', 130.00),
(4, 'Fes', 3, CURRENT_TIMESTAMP + INTERVAL '6 days' + INTERVAL '6 hours 30 minutes', 180.00),
(4, 'Oujda', 4, CURRENT_TIMESTAMP + INTERVAL '6 days' + INTERVAL '10 hours', 250.00);

-- Trajet 5 : Sara (Tanger -> Casablanca)
INSERT INTO points_arret (trajet_id, ville, ordre, heure_passage, prix_depuis_depart) VALUES
(5, 'Tanger', 0, CURRENT_TIMESTAMP + INTERVAL '7 days', 0.00),
(5, 'Larache', 1, CURRENT_TIMESTAMP + INTERVAL '7 days' + INTERVAL '1 hour', 35.00),
(5, 'Kenitra', 2, CURRENT_TIMESTAMP + INTERVAL '7 days' + INTERVAL '2 hours 30 minutes', 75.00),
(5, 'Rabat', 3, CURRENT_TIMESTAMP + INTERVAL '7 days' + INTERVAL '3 hours 15 minutes', 100.00),
(5, 'Casablanca', 4, CURRENT_TIMESTAMP + INTERVAL '7 days' + INTERVAL '4 hours 30 minutes', 135.00);

-- Insertion de 5 réservations réparties
INSERT INTO reservations (passager_id, trajet_id, point_montee_id, point_descente_id, nombre_places, montant_total, date_reservation, statut) VALUES
(2, 1, 3, 4, 1, 50.00, CURRENT_TIMESTAMP, 'CONFIRMEE'), -- Salma : Settat -> Marrakech (120 - 70 = 50)
(4, 2, 5, 7, 1, 60.00, CURRENT_TIMESTAMP, 'CONFIRMEE'), -- Mehdi : Casablanca -> Kenitra (60 - 0 = 60)
(5, 3, 10, 12, 2, 140.00, CURRENT_TIMESTAMP, 'CONFIRMEE'), -- Noura : Fes -> Rabat ((70 - 0)*2 = 140)
(1, 5, 19, 22, 1, 100.00, CURRENT_TIMESTAMP, 'CONFIRMEE'), -- Karim : Tanger -> Rabat (100 - 0 = 100)
(3, 4, 14, 15, 1, 90.00, CURRENT_TIMESTAMP, 'CONFIRMEE'); -- Imane : Marrakech -> Casablanca (90 - 0 = 90)
