@column-formats
prompt 
prompt 
prompt 
prompt 
prompt ============= 8
prompt ============= Fix indexes on 3 more tables:
prompt ==            NOTIFICATION
prompt ==            PATIENT_ATTRIBUTE_HISTORY
prompt ==            PATIENT_RESULT_TYPE


prompt ======== NOTIFICATION

prompt == Add index NOT_PATIENT_SITE_SURDT_IDX on patient, site, survey_dt
create index NOT_PATIENT_SITE_SURDT_IDX on NOTIFICATION (patient_id, survey_site_id, survey_dt);

prompt == Add index NOTIFICATION_SITE_SURDT_IDX on:   site, survey_dt
create index NOTIFICATION_SITE_SURDT_IDX on NOTIFICATION (survey_site_id, survey_dt);


prompt
prompt ======== PATIENT_ATTRIBUTE_HISTORY

prompt == add index PAT_ATT_HIS_SITE_PAT_NAME_IDX on:  survey_site_id, patient_id, data_name
create index PAT_ATT_HIS_SITE_PAT_NAME_IDX on PATIENT_ATTRIBUTE_HISTORY (survey_site_id, patient_id, data_name);

prompt == drop unnec and poorly named index PATIENT_ATT_HIS_PAT_ID_IDX on patient_id, data_name
drop index PATIENT_ATT_HIS_PAT_ID_IDX;


prompt
prompt ======== PATIENT_RESULT_TYPE

prompt == add a better index PATIENT_RES_TYP_SITE_NAME_IDX:  UNIQUE on site, result_name
create UNIQUE index PATIENT_RES_TYP_SITE_NAME_IDX on PATIENT_RESULT_TYPE (survey_site_id, result_name);


prompt == You can check these tables fks and indexes with each of:  @not-list  @pah-list  @prt-list
set sqlprompt '(next is @9-fix-indexes) SQL> '

prompt
prompt == and the 2nd-to-last: @ 9-fix-indexes
prompt
