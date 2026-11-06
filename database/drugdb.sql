-- ============================================================
-- Drug Information Analysis and Side-Effect Search System
-- Database script for MySQL
-- Run this whole file once in MySQL Workbench.
-- Sample data is for learning only, not medical advice.
-- ============================================================

CREATE DATABASE IF NOT EXISTS drugdb
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE drugdb;

-- Remove old tables first (so you can run this file again safely)
DROP TABLE IF EXISTS usage_steps;
DROP TABLE IF EXISTS age_rules;
DROP TABLE IF EXISTS medicine_side_effects;
DROP TABLE IF EXISTS medicine_ingredients;
DROP TABLE IF EXISTS side_effects;
DROP TABLE IF EXISTS ingredients;
DROP TABLE IF EXISTS medicines;

-- ------------------------------------------------------------
-- 1. Medicines (brand names like Dolo 650)
-- ------------------------------------------------------------
CREATE TABLE medicines (
  id          INT AUTO_INCREMENT PRIMARY KEY,
  brand_name  VARCHAR(100) NOT NULL UNIQUE,
  used_for    VARCHAR(200) NOT NULL
);

-- ------------------------------------------------------------
-- 2. Ingredients (chemical names like Paracetamol)
-- ------------------------------------------------------------
CREATE TABLE ingredients (
  id    INT AUTO_INCREMENT PRIMARY KEY,
  name  VARCHAR(100) NOT NULL UNIQUE
);

-- Which ingredients are inside which medicine (many-to-many)
CREATE TABLE medicine_ingredients (
  medicine_id    INT NOT NULL,
  ingredient_id  INT NOT NULL,
  strength       VARCHAR(30) NOT NULL,
  PRIMARY KEY (medicine_id, ingredient_id),
  FOREIGN KEY (medicine_id)   REFERENCES medicines(id)   ON DELETE CASCADE,
  FOREIGN KEY (ingredient_id) REFERENCES ingredients(id) ON DELETE CASCADE
);

-- ------------------------------------------------------------
-- 3. Side effects
-- ------------------------------------------------------------
CREATE TABLE side_effects (
  id        INT AUTO_INCREMENT PRIMARY KEY,
  name      VARCHAR(100) NOT NULL UNIQUE,
  severity  ENUM('mild','moderate','severe') NOT NULL
);

-- Which side effects belong to which medicine (many-to-many)
CREATE TABLE medicine_side_effects (
  medicine_id    INT NOT NULL,
  side_effect_id INT NOT NULL,
  PRIMARY KEY (medicine_id, side_effect_id),
  FOREIGN KEY (medicine_id)    REFERENCES medicines(id)    ON DELETE CASCADE,
  FOREIGN KEY (side_effect_id) REFERENCES side_effects(id) ON DELETE CASCADE
);

-- ------------------------------------------------------------
-- 4. Age rules (safety and amount for child, adult, elderly)
--    child = under 18, adult = 18 to 59, elderly = 60 and above
-- ------------------------------------------------------------
CREATE TABLE age_rules (
  id           INT AUTO_INCREMENT PRIMARY KEY,
  medicine_id  INT NOT NULL,
  age_group    ENUM('child','adult','elderly') NOT NULL,
  status       ENUM('safe','careful','avoid') NOT NULL,
  safety_note  VARCHAR(300) NOT NULL,
  dose_amount  VARCHAR(200) NOT NULL,
  daily_limit  VARCHAR(200) NOT NULL,
  UNIQUE (medicine_id, age_group),
  FOREIGN KEY (medicine_id) REFERENCES medicines(id) ON DELETE CASCADE
);

-- ------------------------------------------------------------
-- 5. How to use (step by step)
-- ------------------------------------------------------------
CREATE TABLE usage_steps (
  id           INT AUTO_INCREMENT PRIMARY KEY,
  medicine_id  INT NOT NULL,
  step_no      INT NOT NULL,
  step_text    VARCHAR(300) NOT NULL,
  FOREIGN KEY (medicine_id) REFERENCES medicines(id) ON DELETE CASCADE
);

-- ============================================================
-- SAMPLE DATA
-- ============================================================

INSERT INTO medicines (id, brand_name, used_for) VALUES
(1, 'Dolo 650',   'Fever and body pain'),
(2, 'Crocin',     'Fever and mild pain'),
(3, 'Combiflam',  'Pain, swelling and fever'),
(4, 'Brufen 400', 'Pain and swelling'),
(5, 'Disprin',    'Pain, fever and blood thinning'),
(6, 'Cetzine',    'Allergy, sneezing and cold'),
(7, 'Omez',       'Acidity and heartburn');

INSERT INTO ingredients (id, name) VALUES
(1, 'Paracetamol'),
(2, 'Ibuprofen'),
(3, 'Aspirin'),
(4, 'Cetirizine'),
(5, 'Omeprazole');

INSERT INTO medicine_ingredients (medicine_id, ingredient_id, strength) VALUES
(1, 1, '650 mg'),
(2, 1, '500 mg'),
(3, 2, '400 mg'),
(3, 1, '325 mg'),
(4, 2, '400 mg'),
(5, 3, '350 mg'),
(6, 4, '10 mg'),
(7, 5, '20 mg');

INSERT INTO side_effects (id, name, severity) VALUES
(1,  'Nausea',                              'mild'),
(2,  'Rash',                                'mild'),
(3,  'Liver damage if too much is taken',   'severe'),
(4,  'Headache',                            'mild'),
(5,  'Stomach pain',                        'moderate'),
(6,  'Dizziness',                           'mild'),
(7,  'Bleeding',                            'severe'),
(8,  'Drowsiness',                          'mild'),
(9,  'Dry mouth',                           'mild'),
(10, 'Diarrhea',                            'moderate');

INSERT INTO medicine_side_effects (medicine_id, side_effect_id) VALUES
(1, 1), (1, 2), (1, 3),
(2, 1), (2, 2), (2, 3),
(3, 1), (3, 4), (3, 5), (3, 6), (3, 3),
(4, 4), (4, 1), (4, 6), (4, 5),
(5, 1), (5, 5), (5, 7),
(6, 8), (6, 9), (6, 4),
(7, 4), (7, 1), (7, 10);

INSERT INTO age_rules (medicine_id, age_group, status, safety_note, dose_amount, daily_limit) VALUES
-- Dolo 650
(1, 'child',   'careful', 'This is an adult-strength tablet. A child needs a dose by weight.', 'Ask a doctor or pharmacist', 'Not decided here'),
(1, 'adult',   'safe',    'Safe when taken as directed.', '1 tablet at a time', 'Up to 4 tablets in 24 hours'),
(1, 'elderly', 'safe',    'Safe when taken as directed. Watch the liver.', '1 tablet at a time', 'Up to 4 tablets in 24 hours. Doctor may lower it'),
-- Crocin
(2, 'child',   'careful', 'A child needs a dose by weight, often as syrup.', 'Ask a doctor or pharmacist', 'Not decided here'),
(2, 'adult',   'safe',    'Safe when taken as directed.', '1 tablet at a time', 'Up to 4 tablets in 24 hours'),
(2, 'elderly', 'safe',    'Safe when taken as directed. Watch the liver.', '1 tablet at a time', 'Up to 4 tablets in 24 hours. Doctor may lower it'),
-- Combiflam
(3, 'child',   'avoid',   'Has two medicines in one tablet. Not for young children unless a doctor says so.', 'Ask a doctor first', 'Not decided here'),
(3, 'adult',   'safe',    'Safe when taken as directed, after food.', '1 tablet at a time', 'Up to 3 tablets in 24 hours'),
(3, 'elderly', 'careful', 'Higher risk of stomach and kidney problems.', '1 tablet at a time, only if the doctor agrees', 'Doctor decides the daily limit'),
-- Brufen 400
(4, 'child',   'careful', 'Only with a doctor''s advice. Dose is by weight.', 'Ask a doctor or pharmacist', 'Not decided here'),
(4, 'adult',   'safe',    'Safe when taken as directed, after food.', '1 tablet at a time', 'Up to 3 tablets in 24 hours'),
(4, 'elderly', 'careful', 'Higher risk of stomach and kidney problems.', '1 tablet at a time, only if the doctor agrees', 'Doctor decides the daily limit'),
-- Disprin
(5, 'child',   'avoid',   'Not for children and teenagers under 16.', 'Do not give', 'Do not give'),
(5, 'adult',   'safe',    'Safe when taken as directed, after food.', '1 tablet at a time', 'Up to 3 tablets in 24 hours'),
(5, 'elderly', 'careful', 'Higher risk of bleeding.', 'Only if the doctor agrees', 'Doctor decides the daily limit'),
-- Cetzine
(6, 'child',   'careful', 'Needs a lower dose.', 'Ask a doctor or pharmacist', 'Not decided here'),
(6, 'adult',   'safe',    'Safe when taken as directed.', '1 tablet once a day', '1 tablet in 24 hours'),
(6, 'elderly', 'careful', 'May cause extra sleepiness and a risk of falling.', '1 tablet once a day, or less if the doctor says', '1 tablet in 24 hours'),
-- Omez
(7, 'child',   'careful', 'Only if a doctor prescribes it.', 'Ask a doctor', 'Not decided here'),
(7, 'adult',   'safe',    'Safe for short use as directed.', '1 capsule once a day', '1 capsule in 24 hours'),
(7, 'elderly', 'careful', 'Long use may weaken bones. Doctor should review.', '1 capsule once a day', '1 capsule in 24 hours');

INSERT INTO usage_steps (medicine_id, step_no, step_text) VALUES
(1, 1, 'Swallow with water'),
(1, 2, 'Keep a gap of 4 to 6 hours between tablets'),
(1, 3, 'Do not take other medicines that also contain paracetamol'),
(2, 1, 'Swallow with water'),
(2, 2, 'Keep a gap of 4 to 6 hours between tablets'),
(2, 3, 'Do not take other medicines that also contain paracetamol'),
(3, 1, 'Take after food, never on an empty stomach'),
(3, 2, 'Swallow with a full glass of water'),
(3, 3, 'Do not take other pain tablets with it'),
(4, 1, 'Take after food'),
(4, 2, 'Swallow with a full glass of water'),
(4, 3, 'Stop and see a doctor if you get stomach pain'),
(5, 1, 'Dissolve the tablet in a glass of water'),
(5, 2, 'Take after food'),
(5, 3, 'Do not use if you bleed easily'),
(6, 1, 'Take once a day, best at night'),
(6, 2, 'Swallow with water'),
(6, 3, 'Do not drive if you feel sleepy'),
(7, 1, 'Take once a day, before breakfast'),
(7, 2, 'Swallow the capsule whole with water'),
(7, 3, 'Do not use for a long time without a doctor');

-- ============================================================
-- TEST QUERIES (select one and run it to check your data)
-- ============================================================

-- A) Search by chemical name: which medicines contain Paracetamol?
-- SELECT m.brand_name, mi.strength
-- FROM medicines m
-- JOIN medicine_ingredients mi ON mi.medicine_id = m.id
-- JOIN ingredients i ON i.id = mi.ingredient_id
-- WHERE i.name LIKE '%Paracetamol%';

-- B) Safety of Combiflam for a child
-- SELECT m.brand_name, a.status, a.safety_note, a.dose_amount, a.daily_limit
-- FROM medicines m
-- JOIN age_rules a ON a.medicine_id = m.id
-- WHERE m.brand_name LIKE '%Combiflam%' AND a.age_group = 'child';

-- C) Report: most common side effects
-- SELECT s.name, COUNT(*) AS medicines_count
-- FROM side_effects s
-- JOIN medicine_side_effects ms ON ms.side_effect_id = s.id
-- GROUP BY s.name
-- ORDER BY medicines_count DESC;
