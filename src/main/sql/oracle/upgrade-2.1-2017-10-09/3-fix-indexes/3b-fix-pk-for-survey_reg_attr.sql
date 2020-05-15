@column-formats

-- turn off echoing before/after variable substitution
SET VERIFY OFF
define TABLE = 'SURVEY_REG_ATTR'
define PK = 'SURVEY_REG_ATTR_PK'

prompt
prompt
prompt
prompt
prompt ============= 3b.
prompt ============= Fixing the Survey_Reg_Attr Pk to be 2 columns

-- define a variable as the value of a column
column sra_pkname new_value sra_pkname
column sra_pkname format a25
SELECT (constraint_name) sra_pkname 
FROM user_constraints WHERE table_name='&TABLE' AND constraint_type='P';

prompt == Dropping old the primary key, named...

ALTER TABLE &table DROP CONSTRAINT &sra_pkname;

prompt == Creating the new one, named &PK on (SURVEY_REG_ID, DATA_NAME);

ALTER TABLE &table ADD CONSTRAINT &PK PRIMARY KEY (SURVEY_REG_ID, DATA_NAME);

column TABLE_NAME format a16
column COLUMN_NAME format a18
prompt == The primary key now should have 2 columns, SURVEY_REG_ID, DATA_NAME

SELECT '   ', i.table_name, i.index_name primary_key_name, col.column_name
  FROM user_indexes i, user_ind_columns col, user_constraints con
 WHERE i.table_name='&TABLE' AND col.table_name='&TABLE' AND con.table_name='&TABLE'
   AND i.index_name=col.index_name AND i.index_name=con.index_name AND con.constraint_type = 'P'
 ORDER by i.index_name, col.column_position, col.column_name;

prompt ==
prompt == If it matches now, proceed to:  @ 4a-chk-user-principal-name-uq
prompt
prompt == If for ALTER TABLE command, you get:  ORA-00942: table or view does not exist
prompt ==   then you do not have this table- no harm done.  Proceed to:  @ 4a-chk-user-principal-name-uq
prompt
