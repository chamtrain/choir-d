-- PostGres script

\echo '== This is a list of all indexes that are redundant, and should be dropped.'
\echo '== To list all indexes for all tables, use:  \\i list-ixs.sql'

SELECT dropstmt "Drop these redundant indexes" FROM (
WITH tmp_ixs AS 
  (SELECT schema_name, table_name, index_name, uniq, 
          substring(columns from 2 for char_length(columns)-2) as index_columns
     FROM
    (SELECT n.nspname  as schema_name,
           t.relname  as table_name,
           c.relname  as index_name,
           CASE WHEN pg_get_indexdef(indexrelid) like 'CREATE UNIQUE %' THEN 'UNIQUE' ELSE 'non-uq' END as uniq,
           substring(pg_get_indexdef(indexrelid) from '\(.+\)') as columns
    FROM pg_catalog.pg_class c
        JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace
        JOIN pg_catalog.pg_index i ON i.indexrelid = c.oid
        JOIN pg_catalog.pg_class t ON i.indrelid   = t.oid
    WHERE c.relkind = 'i'
        AND n.nspname NOT IN ('pg_catalog', 'pg_toast')
        AND pg_catalog.pg_table_is_visible(c.oid)  ) x
  )

SELECT concat('DROP INDEX  ',concat(tx.index_name,';')) dropstmt
  FROM
(SELECT x.table_name, x.index_name
  FROM tmp_ixs x JOIN tmp_ixs y
       ON x.table_name=y.table_name 
  WHERE char_length(x.index_columns) < char_length(y.index_columns)
    AND 1 = position(concat(x.index_columns,',') in y.index_columns)
ORDER BY x.table_name, x.index_columns
) as tx ) as ty group by dropstmt;  -- get rid of redundant statements (occurs if A is a dup of both B and C)

\echo '== If there are rows above, copy and paste them to drop redundant indexes.'
\echo

