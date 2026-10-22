ALTER TABLE modeles RENAME COLUMN temps_fabrication_minutes TO temps_fabrication_jours;

UPDATE modeles
SET temps_fabrication_jours = CASE
    WHEN temps_fabrication_jours IS NULL THEN NULL
    WHEN temps_fabrication_jours <= 0 THEN 0
    WHEN temps_fabrication_jours < 60 THEN temps_fabrication_jours
    ELSE GREATEST(1, CEIL(temps_fabrication_jours / 1440.0)::int)
END;
