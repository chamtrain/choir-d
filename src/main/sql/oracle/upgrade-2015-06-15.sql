-- Added this index because patient surveys often lookup study by site and token
create index patient_study_site_token_idx on patient_study (survey_site_id, token);

-- Correct an error in the previous release (use noorder for most sequences to
-- avoid creating contention in RAC cluster)
alter sequence service_audit_seq noorder;
