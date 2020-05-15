-- Sample Oracle script to add a new app_config row to the database for a randomset.
-- If there is already a row, use appconfig-sample-change-randomset.sql
-- You can't use appconfig-add-row.sql because the value is too long- sqlplus limits variables to 250 characters.
-- To see all randomsets, use:  appconfig-select-randomsets.sql

-- Note yourUserId is your numeric user_principal.user_principal_id, not your user name
--   if needed, use:  select * from user_principal where username='admin';

-- First, define these 3 values, and then replace the json text in quotes below with your json in single quotes.
-- (In Oracle sqlplus, variables can be up to 250 chars, only.  A statement can be up to 2500 chars.)
define site=1
define cName=DDTreatmentSet
define yourUserId=1

INSERT INTO app_config (app_config_id, survey_site_id, config_type, config_name, enabled) 
  VALUES (app_config_sequence.nextval, &&site, 'randomset', '&&cName', 'Y');

-- this fails unless app_config . config_value=NULL, so if previous fails, this will, too
INSERT INTO app_config_change_history (revision_number, user_principal_id, changed_at_time,change_type, app_config_id, survey_site_id, config_name, config_type) 
  VALUES (app_config_change_sequence.nextval, &&yourUserId, systimestamp(3), 'A',
       (SELECT app_config_id from app_config where config_type='randomset' AND config_name='&&cName' AND survey_site_id=&&site AND config_value IS NULL),
       &&site, '&&cName', 'randomset');

-- these also fail unless config_value=NULL
update app_config set config_value = 
'{"name":"DDTreatmentSet","alg":"KSort","title":"DD","type":"TreatmentSet","desc":"Duloxetine/Desipramine for Pain","user":"Dr. Salmasi","pop":1000,"state":"Enrolling","groups":[{"name":"Duloxetine","desc":"DRUG:  Duloxetine (Cymbalta) 20mg\n*also available in 30 and 60 mg tablets\n \nWEEK 1 20mg by mouth each morning\nWEEK 2 40mg by mouth each morning\nWEEK 3 60mg by mouth each morning","pct":2,"sub":false,"max":0,"closeOnMax":false},{"name":"Desipramine","desc":"DRUG:  Desipramine (Norpramin) 25mg\n*also available in 10, 50, 75, 100 and 150 mg tablets\n\nWEEK 1 25mg by mouth each morning\nWEEK 2 50mg by mouth each morning\nWEEK 3 75mg by mouth each morning\n","pct":2,"sub":false,"max":0,"closeOnMax":false}],"categories":[{"name":"Fibromyalgia","title":"Fibromyalgia","question":"Does the patient meet the criteria for Fibromyalgia?","values":[{"name":"Yes","title":"Has-fibromyalgia","answer":"Yes"},{"name":"No","title":"Fibromyalgia-free","answer":"No"}]}]}'
 WHERE config_name='&&cName' AND survey_site_id=&&site AND config_type='randomset' AND config_value IS NULL;

UPDATE app_config_change_history SET config_value = 
   (select config_value from app_config where config_type='randomset' AND config_name='&&cName' AND survey_site_id=&&site)
 WHERE config_name='&&cName' AND config_type='randomset' AND survey_site_id=&&site AND config_value IS NULL;

-- end
