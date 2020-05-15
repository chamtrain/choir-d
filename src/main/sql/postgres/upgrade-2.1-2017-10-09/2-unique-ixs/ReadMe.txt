================================================================
upgrade-2.1-2017-09-12/2-unique-ixs

SUMMARY 

This set of scripts make three indexes UNIQUE which were intended to be unique.

They also clean up possible duplicates that would prevent making such an index.
Your server should be off-line, or there's a tiny chance you'll clean up duplicates
and then it'll make another.

There's more to it, but the scripts will walk you through it.

Once the scripts are done, if the old server software tries to make a duplicate 
PATIENT_ATTRIBUTE, it'll get a UNIQUE-CONSTRAINT VIOLATION and no harm will be done.
The 2.1 server won't try to make duplicates.

The software was always capable of randomly making duplicate tokens.  Now, it senses the 
duplicate when the UNIQUE-CONSTRAINT VIOLATION occurs and tries again with a new one.
Plus it was making some random tokens unnecessarily in the token table- these are cleaned up.

It might be faster to create an index if there's a similar one available.
so instead of deleting index on A,B,C and re-creating it as unique, we
  1) Create a temporary index on A,B,C,D
  2) drop the non-unique one on A,B,C
  3) then create the new unique one on A,B,C
  4) drop the temporary one


INSTRUCTIONS

When you run these, look over the output for errors.
There shouldn't be any, of course, and these are tested...

I added lots of comments, just in case you need them.
They have no "quit" statement at the end- they leave sqlplus running (except the last one)
Plus, at the end of each script, it prints out the name of the next script to run

You can review the purpose of each script below, or start, with the command:

  % psq 
  vagrant=> \i 1-pa-chk-unique.sql


================ Patient Attributes ================

@1-pa-chk-unique.sql
  This just tells you that the index:  PATIENT_ATT_PAT_DAT_UQ
  is NOT a unique index, which is the problem we're fixing.
  It also tells you if there are any duplicates to fix.

@2-pa-analyze-make-tmp.sql
  This analyzes your duplicates and makes a temporary table

@3-pa-del-younger-dups.sql
  This deletes the least-recently-used duplicates.

@4-pa-fix-unique-index.sql
  This creates the new index and drops the old.
  
@5-pa-attr-tmp-cleanup.sql
  This just deletes the temporary table.

================ SURVEY Registration table ================

This should have no problems with duplicates.
So we can just add the new unique index and delete the old.

6a-chk-sr-site-tok-ix.sql
  Verifies that this index needs to be fixed.
  Also looks for duplicates- there shouldn't be any, might me

6b-sr-chk-site_tok_dups.sql
  This checks for duplicates

6c-sr-mk-tmp-tables.sql
  Makes temporary tables to clean up the duplicates

6d-sr-fix-site-token-dups.sql
  Changes duplicate tokens to be unique ones
  Drops the temporary tables

6e-sr-fix-site-token-ix.sql
  Now that the duplicates are gone, the SURVEY_REGISTRATION index on Site+Token can be unique

7-fix-act-tokens.sql
  Renames the misleading, and possibly duplicate, tokens in ACTIVITY
  This also adds a new constraint, that either Token or Assessment_Reg_ID should not be set

@8-sr-chk-pat-tok.sql
  Verifies that this index needs to be fixed.

@9-sr-fix-pat-tok.sql
  This makes the SURVEY_REGISTRATION tables index on SURVEY_SITE_ID, PATIENT, TOKEN unique

================

mk-tmp-ixs.sql - utility to create a table tmp_ixs containing all the indexes on all the tables.
   To see the indexes for one, eg app_config:   SELECT * from tmp_isx where table_name='app_config';

tokens-utils.sql  - shows you some relationships between tokens in various tables

That's it...
###
