@column-formats
prompt 
prompt 
prompt 
prompt 
prompt ============= 9
prompt ============= Fix indexes on the last 3 tables:
prompt ==            PATIENT_RESULT
prompt ==            APP_CONFIG


prompt
prompt ======== PATIENT_RESULT

prompt == add a better index PAT_RES_SITE_ASMTREG_IDX on site, assessment_reg_id, patient_res_typ_id
create index PAT_RES_SITE_ASMTREG_TYPE_IDX on PATIENT_RESULT (survey_site_id, assessment_reg_id, patient_res_typ_id);


prompt
prompt ======== APP_CONFIG - add 2 indexes for queries

prompt == add index APP_CONFIG_SITE_ENA_TYPE_IX on:  site, enabled, config_type
create index APP_CONFIG_SITE_ENA_TYPE_IX on APP_CONFIG (survey_site_id, enabled, config_type);

prompt == add unique index APP_CONFIG_SITE_TYPE_NAME_UQ on:  survey_site_id", "config_type", "config_name
create index APP_CONFIG_SITE_TYPE_NAME_UQ on APP_CONFIG (survey_site_id, config_type, config_name);


prompt == You can check these tables with:  @pr-list  @ac-list
prompt
prompt == Just one more: @ A-remove-redundant-ixs
prompt
set sqlprompt '(next: @A-remove-redundant-fk-ixs) SQL> '
