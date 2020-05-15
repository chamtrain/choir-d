-- Oracle script to add a new config property to the app_config table
--   This script inserts the row, then adds a new row to app_config_change_history
--     then updates both new rows with the value.
--   If there is already a row, use appconfig-change-row.sql
--     since the 2 inserts in here will fail and the updates will update zero rows
-- NOTE:  The value must be 250 characters or less (so won't work for randomset and prob not emailtemplate)

-- It's best to copy appconfig-sample-add-row.sql
-- and set its parameters.
prompt
prompt == Adding a row to appconfig, using variables for site, cName, cValue and for yourUserId
prompt ==    Note the value must be at most 250 chars, see the randomset samples for up to 2500 chars
prompt

INSERT INTO app_config (app_config_id, survey_site_id, config_type, config_name, enabled) 
  VALUES (app_config_sequence.nextval, &&site, '&&ctype', '&&cName', 'Y');

INSERT INTO app_config_change_history (revision_number, user_principal_id, changed_at_time, app_config_id, 
                                       change_type, survey_site_id, config_name, config_type) 
  VALUES (app_config_change_sequence.nextval, &&yourUserId, systimestamp(3),
          (SELECT app_config_id FROM app_config 
            WHERE config_name='&&cName' AND survey_site_id=&&site AND config_type='&&ctype' AND config_value IS NULL),
          'A', &&site, '&&cName', '&&ctype');

-- the next 2 will fail if insert into app_config_change_history failed (and no changed value is null)
UPDATE app_config SET config_value = '&&cValue'
 WHERE app_config_id = (
  SELECT app_config_id FROM app_config_change_history
   WHERE config_name='&&cName' AND survey_site_id=&&site AND config_type='&&ctype' AND config_value IS NULL);

UPDATE app_config_change_history SET config_value = '&&cValue'
 WHERE config_name='&&cName' AND survey_site_id=&&site AND config_type='&&ctype' AND config_value IS NULL;

-- end
