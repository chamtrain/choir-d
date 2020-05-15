-- Oracle script to UPDATE an existing config property in the app_config table
--   This script inserts the app_config_change_history row
--     then updates both it and the old row with the value.
--   If there is no row, the insert fails and the updates both update zero rows.
--     since the 2 inserts in here will fail and the updates will update zero rows
-- NOTE:  The value must be 250 characters or less (so won't work for randomset and prob not emailtemplate)

-- You can make a copy of appconfig-sample-add-row.sql, then edit its parameters
prompt
prompt == Adding a row to appconfig, using variables for site, cName, cValue and for yourUserId
prompt ==    Note the value must be at most 250 chars, see the randomset samples for up to 2500 chars
prompt

-- the main differences between this and appconfig-add-row.sql
--   1- it doesn't do the insert into app_config
--   2- in the change_history, it uses change_value of M(odified) instead of A(dd)

INSERT INTO app_config_change_history (revision_number, user_principal_id, changed_at_time, app_config_id, 
                                       change_type, survey_site_id, config_name, config_type) 
  VALUES (app_config_change_sequence.nextval, &&yourUserId, systimestamp(3),
          (SELECT app_config_id FROM app_config 
            WHERE config_name='&&cName' AND survey_site_id=&&site AND config_type='&&ctype'),
          'M', &&site, '&&cName', '&&ctype');

-- the next 2 will fail if insert into app_config_change_history failed (and no changed value is null)
UPDATE app_config SET config_value = '&&cValue'
 WHERE app_config_id = (
  SELECT app_config_id FROM app_config_change_history
   WHERE config_name='&&cName' AND survey_site_id=&&site AND config_type='&&ctype' AND config_value IS NULL);

UPDATE app_config_change_history SET config_value = '&&cValue'
 WHERE config_name='&&cName' AND survey_site_id=&&site AND config_type='&&ctype' AND config_value IS NULL;

-- end
