/*
 * Sample query for getting appointments out of Clarity (the EPIC data warehouse).
 *
 * This is the same as the other one, but adds a couple columns for language preference.
 */
WITH ENC_COUNT AS (
    SELECT COUNT(*) AS ENC_ROW_COUNT
    FROM PAT_ENC PE
    WHERE PE.CONTACT_DATE >= TRUNC(SYSDATE)-30
    AND PE.APPT_TIME IS NOT NULL
  ), ENC_DTL AS (
    SELECT PAT.PAT_ID,
    PAT.PAT_FIRST_NAME,
    PAT.PAT_LAST_NAME,
    PAT.PAT_MRN_ID AS MRN,
    PAT.ADD_LINE_1,
    PAT.ADD_LINE_2,
    PAT.CITY,
    PAT.ZIP,
    PAT.HOME_PHONE,
    PAT.WORK_PHONE,
    PAT.EMAIL_ADDRESS,
    PAT.LANGUAGE_C,
    PAT.ETHNIC_GROUP_C,
    PAT.SEX_C,
    TO_CHAR(PAT.BIRTH_DATE,'MM/DD/YYYY') AS DOB,
    TO_CHAR(PE.APPT_TIME,'MM/DD/YYYY') AS APPOINTMENT_DATE,
    TO_CHAR(PE.APPT_TIME,'HH24:MI') AS APPOINTMENT_TIME,
    PE.PAT_ENC_CSN_ID,
    PE.APPT_PRC_ID,
    PE.VISIT_PROV_ID,
    PE.ENC_TYPE_C,
    PE.APPT_STATUS_C,
    PE.APPT_CANCEL_DATE,
    PE.APPT_SERIAL_NO,
    PE.APPT_BLOCK_C,
    PE.CANCEL_REASON_CMT ,
    CD_PAT_ENC.DEPARTMENT_NAME AS PAT_ENC_DEPARTMENT,
    CD_PAT_ENC.DEPARTMENT_ID AS PAT_ENC_DEPARTMENT_ID
    FROM PAT_ENC PE
    INNER JOIN PATIENT PAT ON PE.PAT_ID = PAT.PAT_ID
    INNER JOIN CLARITY_DEP CD_PAT_ENC ON CD_PAT_ENC.DEPARTMENT_ID = PE.DEPARTMENT_ID
    WHERE PE.CONTACT_DATE >= TRUNC(SYSDATE)-30
    AND PE.APPT_TIME IS NOT NULL
    AND ROWNUM <= (SELECT ENC_ROW_COUNT+1 FROM ENC_COUNT)
  )
SELECT DISTINCT
    PE.PAT_ID,
    PE.PAT_FIRST_NAME,
    PE.PAT_LAST_NAME,
    PE.MRN,
    PE.ADD_LINE_1,
    PE.ADD_LINE_2,
    PE.CITY,
    PE.ZIP,
    PE.HOME_PHONE,
    PE.WORK_PHONE,
    PE.EMAIL_ADDRESS,
    PE.DOB,
    PE.APPOINTMENT_DATE,
    PE.APPOINTMENT_TIME,
    LG.NAME AS LANGUAGE,
    LG.LANGUAGE_C,
    CP.PRC_ABBR AS VISIT_TYPE,
    CP.PRC_ID AS VISIT_TYPE_ID,
    PE.PAT_ENC_CSN_ID,
    ENC_PROV.PROV_NAME AS ENC_PROV_NAME,
    ENC_PROV.PROV_ID,
    PE.PAT_ENC_DEPARTMENT,
    PE.PAT_ENC_DEPARTMENT_ID,
    S.NAME AS SEX,
    ZEG.NAME AS ETHNICITY,
    ZPR.NAME AS PATIENT_RACE,
    PE.ENC_TYPE_C,
    PE.APPT_STATUS_C,
    PE.APPT_CANCEL_DATE,
    PE.APPT_SERIAL_NO,
    PE.APPT_BLOCK_C,
    PE.CANCEL_REASON_CMT
FROM ENC_DTL PE
  LEFT OUTER JOIN ZC_SEX S ON S.RCPT_MEM_SEX_C = PE.SEX_C
  LEFT OUTER JOIN PATIENT_RACE PR ON PR.PAT_ID = PE.PAT_ID
  LEFT OUTER JOIN ZC_PATIENT_RACE ZPR ON ZPR.PATIENT_RACE_C = PR.PATIENT_RACE_C AND ZPR.NAME IS NOT NULL AND PR.LINE=1
  LEFT OUTER JOIN ZC_LANGUAGE LG ON LG.LANGUAGE_C = PE.LANGUAGE_C
  LEFT OUTER JOIN ZC_ETHNIC_GROUP ZEG ON ZEG.ETHNIC_GROUP_C = PE.ETHNIC_GROUP_C
  LEFT OUTER JOIN CLARITY_PRC CP ON CP.PRC_ID = PE.APPT_PRC_ID
  LEFT OUTER JOIN CLARITY_SER ENC_PROV ON ENC_PROV.PROV_ID = PE.VISIT_PROV_ID
  LEFT OUTER JOIN ZC_APPT_STATUS ZAS ON ZAS.APPT_STATUS_C = PE.APPT_STATUS_C;
  LEFT OUTER JOIN CLARITY_SER CS_REF_BY ON CS_REF_BY.PROV_ID = PEREF.REFERRING_PROV_ID
  LEFT OUTER JOIN CLARITY_SER_ADDR CE ON CE.PROV_ID = CS_REF_BY.PROV_ID AND CE.LINE = 1
  LEFT OUTER JOIN CLARITY_SER CS_PCP ON CS_PCP.PROV_ID = PEREF.PCP_PROV_ID
  LEFT OUTER JOIN CLARITY_PRC CP ON CP.PRC_ID = PEREF.APPT_PRC_ID
  LEFT OUTER JOIN CLARITY_SER ENC_PROV ON ENC_PROV.PROV_ID = PEREF.VISIT_PROV_ID
  LEFT OUTER JOIN ZC_APPT_STATUS ZAS ON ZAS.APPT_STATUS_C = PEREF.APPT_STATUS_C
  LEFT OUTER JOIN ZC_STATE ZSTATE ON CE.STATE_C = ZSTATE.STATE_C
  LEFT OUTER JOIN ZC_STATE ZS ON ZS.STATE_C = PAT.STATE_C
  INNER JOIN CLARITY_DEP CD_PAT_ENC ON CD_PAT_ENC.DEPARTMENT_ID = PEREF.DEPARTMENT_ID
WHERE PEREF.APPT_TIME >= TRUNC(SYSDATE)-30
;
