-- PostGres script to add a new config property to the app_config table
--   This script inserts the row, then adds a new row to app_config_change_history
--     then updates both new rows with the value.
--   If there is already a row, use appconfig-change-row.sql
--     since the 2 inserts in here will fail and the updates will update zero rows

-- It's best to copy appconfig-sample-add-row.sql
-- and set its parameters.
\echo
\echo '== Adding a row to appconfig, using variables for site, cName, cValue and for yourUserId'
\echo

\echo == 'inserting into app_config...'
INSERT INTO app_config (app_config_id, survey_site_id, config_type, config_name, enabled, config_value)
  VALUES (nextval('app_config_change_sequence'), :site, :'ctype', :'cName', 'Y', :'cValue');

\echo == 'inserting into app_config_change_history...'
INSERT INTO app_config_change_history (revision_number, user_principal_id, changed_at_time, app_config_id, 
                                       change_type, survey_site_id, config_name, config_type, config_value)
  VALUES (nextval('app_config_change_sequence'), :yourUserId, date_trunc('milliseconds',localtimestamp),
          (SELECT app_config_id FROM app_config 
            WHERE config_name=:'cName' AND survey_site_id=:site AND config_type=:'ctype'),
          'A', :site, :'cName', :'ctype', :'cValue');

-- end
