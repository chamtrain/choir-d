-- Sample PostGres script to update an existing app_config row with changed randomset data.
--
-- Note: you should not change strata or values (unless you delete its old rows from 
--   randomset and randomset_stratum, and fix the stratum names of existing randomset_participant rows,
--   or remove their participation (delete them first from the randomset_participant_hist table.)
-- To see all randomsets, use:  appconfig-select-randomsets.sql
-- 
-- If there is no app_config row with the site/name randomset, the updates will change 0 rows and the insert will fail.

-- Note yourUserId is your numeric user_principal.user_principal_id, not your user name
--   if needed, use:  select * from user_principal where username='admin';

-- First, define these 3 values, and then replace the json text in quotes below with your json in single quotes.
\set site       1
\set cName      DDTreatmentSet
\set yourUserId 1
\set cValue     '{"name":"DDTreatmentSet","alg":"KSort","title":"DD","type":"TreatmentSet","desc":"Duloxetine/Desipramine for Pain","user":"Dr. Salmasi","pop":1000,"state":"Enrolling","groups":[{"name":"Duloxetine","desc":"DRUG:  Duloxetine (Cymbalta) 20mg\n*also available in 30 and 60 mg tablets\n \nWEEK 1 20mg by mouth each morning\nWEEK 2 40mg by mouth each morning\nWEEK 3 60mg by mouth each morning","pct":2,"sub":false,"max":0,"closeOnMax":false},{"name":"Desipramine","desc":"DRUG:  Desipramine (Norpramin) 25mg\n*also available in 10, 50, 75, 100 and 150 mg tablets\n\nWEEK 1 25mg by mouth each morning\nWEEK 2 50mg by mouth each morning\nWEEK 3 75mg by mouth each morning\n","pct":2,"sub":false,"max":0,"closeOnMax":false}],"categories":[{"name":"Fibromyalgia","title":"Fibromyalgia","question":"Does the patient meet the criteria for Fibromyalgia?","values":[{"name":"Yes","title":"Has-fibromyalgia","answer":"Yes"},{"name":"No","title":"Fibromyalgia-free","answer":"No"}]}]}'

\set ctype      randomset

\i appconfig-change-row.sql

-- end
