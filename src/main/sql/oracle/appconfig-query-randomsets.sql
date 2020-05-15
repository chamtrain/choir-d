-- see all randomsets

set linesize 150
set pagesize 1000
column APP_CONFIG_ID  format 99999
column CONFIG_TYPE    format a20
column CONFIG_NAME    format a35
column CONFIG_VALUE   format a50
column ENABLED	            format a1
column SURVEY_SITE_ID format 999

SELECT * FROM app_config WHERE config_type='randomset' order by survey_site_id, config_name;
