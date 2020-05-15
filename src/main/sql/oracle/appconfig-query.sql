-- Oracle script to select all config_values like the string it prompts for.

prompt
prompt '== Finds all rows like *name*, where you specify the name'
prompt '==   Either when the script prompts for it, or use in sqlplus:  define name=... '
prompt '==   There may be multiple rows due to multiple sites or if more names match'
prompt

set linesize 150
column config_type format a13
column config_name format a35
column config_value format a55
column SURVEY_SITE_ID format 9999
column app_config_id format 999999

SELECT * FROM app_config WHERE config_name LIKE '%&&name%' order by survey_site_id, config_name;

