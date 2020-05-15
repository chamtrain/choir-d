-- PostGres script
-- List of all foreign keys and whether they're covered by indexes

\echo '=== (re)Creating a list of ALL the tables and their index columns'

WITH
 fks AS (
   WITH fks AS (SELECT r.conrelid tblid, r.conname fkname,
           trim(both '()' from substring(pg_catalog.pg_get_constraintdef(r.oid, true) from '\(.*?\)')) as fkcols
         FROM pg_catalog.pg_constraint r WHERE r.contype = 'f'),
     tables AS (SELECT c.oid AS tblid, c.relname AS tblname
                  FROM pg_catalog.pg_class c LEFT JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace
                 WHERE c.relkind='r' AND pg_catalog.pg_table_is_visible(c.oid) AND not c.relname like 'pg_%')
   SELECT tblname, fkname, fkcols
     FROM tables natural join fks
    ORDER BY tblname, fkcols),
  ixs AS (
    SELECT schema_name, table_name, index_name, uniq, 
            substring(columns from 2 for char_length(columns)-2) as ixcols
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
        AND pg_catalog.pg_table_is_visible(c.oid)
    ) x)
SELECT * FROM (
  SELECT tblname, fkname, fkcols
  FROM  fks
  WHERE NOT exists (SELECT * FROM ixs WHERE ixs.table_name=tblname AND position(fks.fkcols IN ixs.ixcols)=1)
  ORDER BY tblname, fkcols) q;
-- ORDER BY n.nspname, t.relname, c.relname);

-- we order by index_columns so you can easily see where 2 overlap