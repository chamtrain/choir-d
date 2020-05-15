-- PostGres script to UPDATE an existing config property in the app_config table
--   This script inserts the app_config_change_history row
--     then updates both it and the old row with the value.
--   If there is no row, the insert fails and the updates both update zero rows.
--     since the 2 inserts in here will fail and the updates will update zero rows

-- You can make a copy of appconfig-sample-add-row.sql, then edit its parameters
\echo
\echo '== Adding a row to appconfig, using variables for site, cName, cValue and for yourUserId'
\echo

-- the main differences between this and appconfig-add-row.sql
--   1- it doesn't do the insert into app_config
--   2- in the change_history, it uses change_value of M(odified) instead of A(dd)

\echo ' == creating the app_config_change_history row'
INSERT INTO app_config_change_history (revision_number, user_principal_id, changed_at_time, app_config_id, 
                                       change_type, survey_site_id, config_name, config_type, config_value) 
  VALUES (nextval('app_config_change_sequence'), :yourUserId, date_trunc('milliseconds',localtimestamp),
          (SELECT app_config_id FROM app_config 
            WHERE config_name=:'cName' AND survey_site_id=:site AND config_type=:'ctype'),
          'M', :site, :'cName', :'ctype', :'cValue');

-- this will fail if insert into app_config_change_history failed (and no changed value is null)

\echo ' == updating the app_config_change_history row'
UPDATE app_config SET config_value = :'cValue'
 WHERE app_config_id = (
  SELECT app_config_id FROM app_config_change_history
   WHERE config_name=:'cName' AND survey_site_id=:site AND config_type=:'ctype' 
     AND revision_number=(SELECT max(revision_number) FROM app_config_change_history));

-- end
