#!/bin/sh
#
# ant create.db.scripts  creates 2 files to populate a new database:
#      build/release/choir-<version>-<data|table>-<dbType>.sql
# In doing so, it deletes your schema.
#
# To test those scripts, first delete your schema, e.g. with:  ant nuke.database
# Then:  cd build/release
# Then run this unix/shell script to apply these new scripts, using the database
# information in your ../../../build.properties file

# For oracle, it does:
#   sqlplus user/password@irt-db-09:1521/irtdev @choir-2.0.0-tables-oracle.sql > tables.log 2>&1

# For postgres, they do:
#   psql -b -q  --log-file=tables.log --file=choir..sql -U vagrant_registry_dev vagrant

# This makes it easier to run those commands for us at Stanford

# This lives in src/main/scripts
# and is copied into build/release/ (IF it doesn't already exist)

fileprefix=choir-2.0.0

findBuildPropsFile() { # sets bp to it, or leaves bp as unfound ../../build.properties
    bp="build.properties"
    f="../$bp"
    dir=`pwd`
    dir=`dirname $dir`
    while [ "$dir" != '/'  -a  ! -f $f ] ; do
	f="../$f"
    done
    if [ -f "$f" ] ; then
	bp=$f
    else
	bp=../../$bp
    fi
}

findBuildPropsFile

echo
usage() {
    if [ $# -gt 0 ] ; then
	echo "  ERROR: $*"
	echo
    fi
    echo "  USAGE: apply-script [ -x | -n ] t|d|td|(s <script.sql>)"
    echo "    Applies one or two $fileprefix scripts to the database specified in $bp"
    echo "    The purpose of this script is to make it easy to test scripts for a release."
    echo "    t   - apply just the tables script"
    echo "    d   - apply just the data script"
    echo "    td  - apply both the tables and data scripts"
    echo "    s f - run the script specified as the next arg"
    echo "    -x  - turns on shell debugging output"
    echo "    -n  - test this script, do everything but don't submit the actual SQL script"
    echo "    -l  - don't send sql script output to the log file: tables.log or data.log"
    echo "  For oracle:   If sqlplus isn't on the path, env var SQLPLUS should contain the path to it"
    echo "  For postgres: If psql    isn't on the path, env var PSQL    should contain the path to it"
    echo
    echo "  Note: If you already have a database initialized, remove the schema by,"
    echo "    in the registry directory, first running:  ant nuke.database"
    echo "  Note: This was tested only with a local postgres and a remote oracle."
    echo
    exit 1
}

if [ "x$1" = "x-x" ] ; then
    shift
    set -x
fi


# ==== Read in script arguments
doit=1      #  -n means don't actually do it, just run the rest of the script
dotables=0  #  t  turns this on
dodata=0    #  d turns this on
dolog=1     #
doscr=''
while [ $# -gt 0 ] ; do
    case $1 in
	-n) doit=0 ;;
	-l) dolog=0 ;;
	t)  dotables=1 ;;
	d)  dodata=1   ;;
	td)  dotables=1; dodata=1 ;;
	dt)  dotables=1; dodata=1 ;;
	s)   doscr=1; script=$2; shift ;;
	-h) usage ;;
	--help) usage;;
	*)  usage "Unknown arg: $1"
    esac
    shift
done
if [ $doscr -eq 1 ] ; then
    if [ "x$script" = 'x' ] ; then
	usage "There must be an sql script file after the 's' arg"
    elif [ ! -f "$script" ] ; then
	usage "The script file argument wasn't found: $script"
    fi
elif [ $dotables -eq 0 -a $dodata -eq 0 ] ; then
    usage "You must specify d, t, or:  s scriptFile"
fi


# ==== Ensure this is running inside the release directory
dir=`/bin/pwd`
dir=`basename $dir`
if [ "$dir" != 'release' -a "$dotables$dodata" != '00' ] ; then
   usage "To use the d or t args, you must run this script in your dev environment's  build/release directory"
fi


mkSqlScriptName() {  #  tables|data  -- db must be set
    case $1 in
	tables)  ;;
	data)    ;;
	*)  usage "mkScriptName- expected data|tables, not: $1"
    esac
    if [ "x$db" = "x" ] ; then
	usage "mkScriptName- database (db) isn't yet set."
    fi
    sqlScriptName=$fileprefix-$1-$db.sql
    if [ ! -f ./$sqlScriptName ] ; then
	usage "File doesn't exist: $sqlScriptName"
    fi
}
    

setDBandProg() {
    grep registry.database $bp | grep -v system | grep -v '#' > .tmp.props

    isora=`grep oracle .tmp.props | wc -l`
    ispg=`grep postgres .tmp.props | wc -l`
    if [ $isora -gt 0 ] ; then
	db=oracle
	if [ "x$SQLPLUS" != x ] ; then
	    PROG=$SQLPLUS
	    if [ ! -x "PROG" ] ; then
		usage "SQLPLUS is defined, but isn't executable: $PROG"
	    fi
	else
	    PROG=`which sqlplus`
	    if [ ! -x "$PROG" ] ; then
		usage "found sqlplus, but it isn't executable: $PROG"
	    fi
	fi
	echo "  -- database: $db,  program=$PROG"
    elif [ $ispg -gt 0 ] ; then
	db=postgres
	if [ "x$PSQL" != x ] ; then
	    PROG=$SQLPLUS
	    if [ ! -x "$PROG" ] ; then
		usage "PSQL is defined, but isn't executable: $PROG"
	    fi
	else
	    PROG=`which psql`
	    if [ ! -x "$PROG" ] ; then
		usage "found sqlplus, but it isn't executable: $PROG"
	    fi
	fi
	echo "  -- database: $db,  program=$PROG"
    else
	echo "================= These values were found in $bp"
	cat .tmp.props
	echo "  = = = = = = = ="
	usage "No database type was identified in the data found in $bp"
    fi
}


get123() {  # for rearranging args
    num=$#
    first=$1
    second=$2
    third=$3
}


getPropValue() {  # prop
    second=`grep $1 .tmp.props | cut -f2 -d=`
    if [ "x$second" = 'x' ] ; then
	usage "Got no value for property $1 from .tmp.props"
    fi
    echo $second
}


mkOracleUrl() {  # unpacks the url parameter for the sqlplus command line
    wholeurl=`getPropValue registry.database.url`
    # echo "  -- wholeurl=$wholeurl"
    url=`echo $wholeurl | cut -f2 -d'@'`
    # echo "  -- url=$url"
    if [ "x$url" = 'x' ] ; then
	usage "Could not get url from registry.database.url=stuff@url: $wholeurl"
    fi
    get123 `echo $url | tr ':' ' '`
    if [ $num -eq 3 ] ; then
	url=$first:$second/$third
    fi
}


applyScript() {  # tables|data
    DO="  DOING:"
    if [ $doit -eq 0 ] ; then
	DO="  NOT DOING:"
    fi
    echo "   If the script appears to hang, type control-d"
    case $db in
	oracle)
	    mkOracleUrl
	    echo "$DO $PROG $user/$pass@$url @$sqlScriptName > $1.log 2>&1"
	    if [ $doit -eq 1 ] ; then
                      $PROG $user/$pass@$url @$sqlScriptName > $1.log 2>&1
		echo " ---- done "
		grep -A 1 ERROR $1.log
		echo "  Wrote: $1.log"

		n=`grep 'OID generation failed' $1.log | wc -l`
		if [ $n -gt 0 ] ; then
		    echo
		    echo "  If sqlplus won't connect to an oracle database, it might be because"
		    echo "    it doesn't realize your hostname belongs to the localhost"
		    echo "    To remedy this, append a space and your hostname ("`hostname`") to"
		    echo "    the 127.0.0.1 line in /etc/hosts"
		    echo "  If you run vpn software, such as PulseSecure, it may reset your /etc/hosts"
		    echo "    file every time you connect, so you'd need to fix it again."
		    echo
		fi
	    fi
	    ;;
	postgres)
	    out=''
	    msg=''
	    if [ $dolog -eq 1 ] ; then
		out=" --log-file=$1.log"
		msg="Wrote: $1.log"
	    fi
	    echo "$DO $PROG -b -q $out --file=$sqlScriptName -U $user $pass"
	    if [ $doit -eq 1 ] ; then
                      $PROG -b -q $out --file=$sqlScriptName -U $user $pass
		echo "  $msg"
	    fi
	    ;;
    esac
}


# ==== run
setDBandProg

user=`getPropValue registry.database.user`
pass=`getPropValue registry.database.password`

if [ $dotables -eq 1 ] ; then
    echo;echo
    echo " ================ Creating the schema"
    mkSqlScriptName tables
    applyScript
fi
if [ $dodata -eq 1 ] ; then
    echo;echo
    echo " ================ Initializing the data"
    mkSqlScriptName data
    applyScript
fi
if [ $doscr -eq 1 ] ; then
    echo;echo
    echo " ================ running your script"
    sqlScriptName=$script
    applyScript
fi


rm -f .tmp.props
echo
echo "  apply-tables is done"
echo
###
