#!/bin/sh

# To use this, make changes in _work function
# and then run this script

_setcp() {
cp=""
for i in `ls ${_dircontainingjars}/*.jar`
do
  cp="$cp;$i"
done
cp="${_servletapijar};$cp"
# cp="c:/bea/81sp3/weblogic81/server/lib/weblogic.jar;$cp"
# cp="c:/website_instances/project/deploy/wiki/WEB-INF/lib/oxy-wiki-classes.jar;$cp"

CLASSPATH="$cp"
export CLASSPATH
}

_convert() {
rm -rf $_oxywikioutdir
java \
  net.ugorji.oxygen.wiki.tool.JspwikiMigrate \
  -df \
  -m $_matchedpages \
  -nm $_notmatchedpages \
  -s $_jspwikipagedir \
  -d $_oxywikioutdir \
  -a $_jspwikiattachdir \
  
}

_test() {
java \
  net.ugorji.oxygen.wiki.test.WikiTest \
  -checkdir $_oxywikioutdir 

}

_work() {
_aa="C:/tmp/jspwiki-conversions/diablo-usability"
#_jspwikipagedir="$_aa/jspwiki"
_jspwikipagedir="Z:/internal/councils/usability/diablo/JSPWiki"
_jspwikiattachdir="$_jspwikipagedir"
_oxywikioutdir="$_aa/oxywiki"
_matchedpages=".*?\\.txt"
_notmatchedpages="ABCDEFGHIJKLMNOPQRST|About\\.txt"
_dircontainingjars="c:/website_instances/project/build/wiki/webapp/WEB-INF/lib"
_servletapijar="c:/website_instances/project/deploy/3rdparty/servlet-api.jar"

_setcp
_convert
_test
cd $_oxywikioutdir

}

_work









































# _matchedpages="Diablo.*RePlan\\.txt|DiabloStatus.*txt|Diablo.Security.*txt|Diablo.JMS.*txt"
# _matchedpages=".*?(LeftMenu|Main|WebSphere|WAS|IBM|CompetitiveAnalysis|DeveloperWorks).*?\\.txt"

#  -t "DiabloStatus" "Status" \
#  -t "Diablo\\." "" \
#  -t "RePlan" "2004" \
#  -t "Diablo" "Roadmap/Replan/" \
#  -t \\. /

#   -d C:/weblogic/internal/docs/wiki/wl-pages/qa-tools \
#   -m "Coconut.*txt|CodeCoverage.*txt|Diablo.ABL2.*txt|Diablo.CodeCoverage.*txt|Diablo.QATools.*txt|Diablo.coconut.*txt|Diablo.framework.*txt|DomainGen.*txt|ManualReUpload.*txt|Sapphire.*txt|Manoj.*txt|Vlad.*txt|Gouri.*txt" \
#   -t "Diablo\\.QATools\\." "" \
#   -t "Diablo\\.QATools\\." "" \
#   -d C:/tmp/bb2 \

