@column-formats
prompt 
prompt 
prompt 
prompt 
prompt ============= A
prompt ============= Remove any redundant indexes

set sqlprompt 'SQL> '

set pagesize 5000
set linesize 350
set tab off

prompt == The database layer created indexes for foreign keys if no exact match existed.
prompt ==   This resulted in redundant indexes. 
prompt ==   An index A is redundant if there is an index B on the same table with the
prompt ==      same columns plus more.  So A(col2,col4) is redundant with B(col2,col4,col5)
prompt ==   Redundant indexes should be removed
prompt
prompt == This lists all indexes that are redundant, and thus should be dropped.

SELECT dropstmt FROM (
SELECT concat('DROP INDEX  ',concat(x.index_name,';')) dropstmt
  FROM
(SELECT i.table_name, i.index_name, i.uniqueness,
       listagg(c.column_name, ', ') within group (order by c.column_position) index_columns
  FROM user_indexes i, user_ind_columns c
 WHERE i.table_name=c.table_name
   AND i.index_name=c.index_name
 GROUP by i.table_name, i.index_name, i.uniqueness) x JOIN
(SELECT i.table_name, i.index_name,
       listagg(c.column_name, ', ') within group (order by c.column_position) index_columns
  FROM user_indexes i, user_ind_columns c
 WHERE i.table_name=c.table_name
   AND i.index_name=c.index_name
 GROUP by i.table_name, i.index_name, i.uniqueness) y
       ON x.table_name=y.table_name 
       AND length(x.index_columns) < length(y.index_columns)
       AND 1 = instr(y.index_columns, concat(x.index_columns,','))
ORDER BY x.table_name, x.index_columns
) group by dropstmt;
-- The "group by" gets rid of redundant statements (occurs if A is a dup of both B and C)

prompt  If there are rows above, copy and paste them to drop redundant indexes
prompt
prompt == To inspect all the indexes, with duplicates marked, see:    @ list-all-ixs
prompt == You can run this again afterwards to verify they are gone:  @ A-remove-redundant-ixs
prompt == You can identify unindexed foreign keys with:               @ list-fks-noix
prompt == And then after that, you can list the indexs to see none are redundant:  @ list-all-ixs
prompt
prompt == When you are finished, type "quit" to leave sqlplus.
prompt == If you are running "script" to capture the output, then at the shell prompt, type:  exit
prompt
