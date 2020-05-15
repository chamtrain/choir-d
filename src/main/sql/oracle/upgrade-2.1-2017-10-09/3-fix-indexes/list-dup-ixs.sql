set pagesize 5000
set linesize 350
set tab off

column status        format a10
column table_name    format a30
column fk_name       format a30
column fk_columns    format a30
column index_name    format a30
column index_columns format a55
column uniq          format a8
column dup           format a3

prompt == This is a list of all indexes that are redundant, and should be dropped.
prompt == See also:  @ list-all-ixs

SELECT dropstmt "These drop redundant indexes" FROM (
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
) group by dropstmt;  -- get rid of redundant statements (occurs if A is a dup of both B and C)

prompt  If there are rows above, copy and paste them to drop redundant indexes
prompt

