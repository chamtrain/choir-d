-- PostGres script to select all config_values like the string it prompts for.

\prompt '  String to match: ' value
\set val   '\'%' :value '%\''

\echo
\echo '== Finds all rows like %value%, where you:  \set value string'
\echo '==   Either when the script prompts for it, or use in sqlplus:  define value=... '
\echo '==   There may be multiple rows due to multiple sites or if more names match'
\echo '==   value = ':val

SELECT * FROM app_config WHERE config_name LIKE :val  order by survey_site_id, config_name;

-- end
