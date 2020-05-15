#!/bin/sh

# This is a shell script.  If you're on a PC, good luck...

echo
error() {
    echo "  ERROR: $*"
    echo
    exit 1
}

if [ "x$PSQL" = "x" ] ; then
    PSQL=/Applications/Postgres.app/Contents/Versions/latest/bin/psql
fi
if [ ! -f "$PSQL" -o ! -x "$PSQL" ] ; then
    err "  You must first set PSQL to point to your binary: PATH/psql"
fi


if [ $# -eq 0 -o "x$1" = "x-h" -o "x$1" = "x--help" ] ; then
    echo " USAGE:  prep-treatmentset [ 1 | 2 ]"
    echo
    echo " -- FIRST, ensure there are patients, run ImportFilesCreate"
    echo "    and make sure your instance of CHOIR picks up the CSV files"
    echo
    echo " -- step 1: rename a bunch of patients to be ready or not or have old treatment set"
    echo " --         create a shell script to remember the MRNs of their names"
    echo " --         add Completed activities for some of them"
    echo
    echo "-- BY HAND, in the UI, register all the patients and assign "
    echo
    echo "       to treatment sets:  two, five, eight, fift, sevenmo"
    echo " -- step 2: this pushes back their dates 2, 5, 8, 15, 29 weeks"
    echo
    error "Need step number, 1 or 2"
else
    case $1 in
	1) mode=1;;
	2) mode=2;;
	3) mode=3;;
	*) error "Bad arg: $1"
    esac
fi

# 15 of these
#  sevenmo=7813971-4
#  fift=3820387-3
#  eight=8888283-2
#  five=6203872-4
#  two=2137905-2
#  not1=4380928-4
#  not2=2444693-2
#  ready1=5206456-5
#  ready2=4459955-3
#  ready3=8659416-5
#  ready4=5341913-1
#  ready5=2163811-9
#  ready6=7363601-1
#  ready7=6654888-4
#  ready8=7511745-7


tok=1
nextToken() {
   tok=`expr $tok + 1`
}

addComplete() {  # MRN
   echo "INSERT INTO Activity (PATIENT_ID, ACTIVITY_DT, ACTIVITY_TYPE, DT_CREATED, USER_PRINCIPAL_ID, ACTIVITY_ID, SURVEY_SITE_ID, TOKEN)" >> $q
   echo "     VALUES ('$1', current_timestamp, 'Completed', current_timestamp, 1, nextval('activity_id_seq'), 1, $tok);" >> $q
}

setName() { #  MRN, Name=ready/not/two/five/eight
    echo "update patient set first_name='$2' where patient_id='$1';" >> $q
    nextToken
    case $2 in
    Not*)  ;;
	*) addComplete $1 ;;
    esac
}

echo
if [ $mode = 1 ] ; then
    q=.tmp.q1.sql
    cat > $q <<EOF
SELECT CASE WHEN rnum BETWEEN 8 AND 15 THEN concat('ready',rnum-7,'=',pid)
            WHEN rnum BETWEEN 6 AND 7  THEN concat('not',rnum-5,'=',pid)
            WHEN rnum=5  THEN concat('two=',pid)
            WHEN rnum=4  THEN concat('five=',pid)
            WHEN rnum= 3 THEN concat('eight=',pid)
            WHEN rnum= 2 THEN concat('fift=',pid)
            WHEN rnum= 1 THEN concat('sevenmo=',pid) ELSE concat('other',rnum,'=',pid) END
 FROM (
   SELECT a.patient_id pid, a.visit_dt, row_number() OVER (order by a.visit_dt DESC) AS rnum
     FROM patient p JOIN appt_registration a ON p.patient_id=a.patient_id
    WHERE a.survey_site_id=1 ORDER BY a.visit_dt DESC) t;
EOF
    "$PSQL" --quiet -U vagrant_registry_dev vagrant -f $q | grep = > .tmp.q1.dat
    rm -f $q
    source .tmp.q1.dat
    rm -rf $q
    
    setName $ready1 Ready1
    setName $ready2 Ready2
    setName $ready3 Ready3
    setName $ready4 Ready4
    setName $ready5 Ready5
    setName $ready6 Ready6
    setName $ready7 Ready7
    setName $ready8 Ready8
    setName $not1  Not1
    setName $not2  Not2
    setName $two  Two
    setName $five Five
    setName $eight Eight
    setName $fift Fifteen
    setName $sevenmo SevenMo

    "$PSQL" --quiet -U vagrant_registry_dev vagrant -f $q

    rm -rf .tmp/q1.*
    echo "Now register each and assign two, Five, Eight to groups"
    
elif [ $mode = 2 ] ; then
    source .tmp.q1.dat
    echo "update randomset_participant set dt_assigned = dt_assigned - interval '15 day' where patient_id='$two';" > .tmp.q2.sql
    echo "update randomset_participant set dt_assigned = dt_assigned - interval '35 day' where patient_id='$five';" >> .tmp.q2.sql
    echo "update randomset_participant set dt_assigned = dt_assigned - interval '60 day' where patient_id='$eight';" >> .tmp.q2.sql
    echo "update randomset_participant set dt_assigned = dt_assigned - interval '110 day' where patient_id='$fift';" >> .tmp.q2.sql
    echo "update randomset_participant set dt_assigned = dt_assigned - interval '215 day' where patient_id='$sevenmo';" >> .tmp.q2.sql
    "$PSQL" --quiet -U vagrant_registry_dev vagrant -f .tmp.q2.sql
    echo "  Pushed back participation dates so we can view assignment on different dates"
    echo "  see ./.tmp.q2.sql"
else
    echo "  No mode given, nothing done..."
fi
echo
