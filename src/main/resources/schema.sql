-- Model relacional de la pràctica (Patient, Patient_disease, Disease).

CREATE TABLE IF NOT EXISTS patient (
    id_patient   SERIAL PRIMARY KEY,
    given_name   VARCHAR(100) NOT NULL,
    family_name  VARCHAR(150) NOT NULL,
    nif          VARCHAR(20)  NOT NULL,
    birth_date   DATE,
    gender       VARCHAR(5),
    address      VARCHAR(200),
    city         VARCHAR(100),
    postal_code  VARCHAR(10),
    country      VARCHAR(5),
    telephone    VARCHAR(30),
    email        VARCHAR(150)
);

CREATE TABLE IF NOT EXISTS disease (
    snomed_code VARCHAR(20) PRIMARY KEY,
    name        VARCHAR(150) NOT NULL
);

CREATE TABLE IF NOT EXISTS patient_disease (
    id_patient  INTEGER NOT NULL REFERENCES patient(id_patient) ON DELETE CASCADE,
    snomed_code VARCHAR(20) NOT NULL REFERENCES disease(snomed_code) ON DELETE CASCADE,
    status      VARCHAR(50),
    treatment   TEXT,
    PRIMARY KEY (id_patient, snomed_code)
);