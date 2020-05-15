-- PostGres script
-- assumes table is set with \set table the_table_name
--      or psql ... --set=table=patient_study -f list-table-pk.sql

\echo
SELECT kc.table_name, tc.constraint_name pk_name,
       string_agg(CAST(kc.column_name as text), CAST(', ' as text)
                  order by kc.position_in_unique_constraint) index_columns
  FROM information_schema.table_constraints tc JOIN information_schema.key_column_usage kc  
    ON kc.constraint_name = tc.constraint_name
 WHERE tc.constraint_type = 'PRIMARY KEY' 
       AND kc.table_name = LOWER(:'table') AND tc.table_name = LOWER(:'table')
 GROUP BY kc.table_name, tc.constraint_name, tc.constraint_type;

