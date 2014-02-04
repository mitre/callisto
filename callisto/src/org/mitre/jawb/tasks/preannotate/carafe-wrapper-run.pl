#!/afs/rcf/lang/bin/perl;

##
## This takes 4 arguments
##  1) A directory containing the file(s) to process
##  2) A regexp (Emacs style) that selects the files to process in that directory
##     This can just be a single filename string if there is just a single file to process
##       Use ".*sgml" to process all files with suffix .sgml in the directory
##  3) A trained model to use for processing
##  4) A lexicon directory (directory with text files in it)

$rootdir = "/afs/rcf/project/tallal/wellner";
$perl = "/afs/rcf/lang/bin/perl";
$mimetype = "sgml";

##$subent = "$perl /afs/rcf/user/wmorgan/l/tallal/callisto/subxmlent.pl";
$subent = "$perl /afs/rcf/project/tallal/wellner/callisto/src/org/mitre/jawb/tasks/preannotate/subxmlent.pl";
$punctok = "/afs/rcf/project/read-comp/bin/punctoker /afs/rcf/project/read-comp/specs/splitpunct.spec";
#$senttag = "$perl /afs/rcf/user/wmorgan/l/tallal/callisto/why-the-fuck-is-catalyst-STILL-ruining-my-fucking-life.pl";
$senttag = "$perl /afs/rcf/project/tallal/wellner/callisto/src/org/mitre/jawb/tasks/preannotate/senttag.pl";
$carafe = "/afs/rcf/project/rcii/bin/carafe_decode.opt";

$filedir = shift or die "first argument must be the test file directory (files to decode)";
$file = shift or die "second argument is the file in the directory to apply carafe to";
$modelname = shift or die "third argument must the model file to use";
$lexicondir = shift or die "fourth argument must be the directory containing lexicon files";
$outsuff = shift or die "fifth argument is the suffix for output files";

$args = join(" ", @ARGV);

if($mimetype =~ /sgml|xml/i) {
  $precommand = "cat $filedir/$file";
}
else {
  $precommand = "$subent $filedir/$file";
}

$precommand .= " | $punctok | $senttag - 0 > $filedir/$file.sent";


$mvcommand = "mv $filedir/$file.sent $filedir/$file";

$carafecommand = "$carafe -input-dir $filedir/ -filter $file -model $modelname -lexicon-dir $lexicondir -outsuffix $outsuff";

print STDERR "carafe-wrapper-run.pl: executing [$precommand]\n";

if (qx(grep "<lex>" $filedir/$file)) {
  print STDERR "no lexing+senttagging needed for $filedir/$file\n";
}
else {
  print STDERR "lexing+senttagging needed for $filedir/$file\n";  
  qx($precommand);
  qx($mvcommand);
}

qx((echo "<top>";cat $filedir/$file;echo "</top>") > $filedir/$file.tt);
qx(mv $filedir/$file.tt $filedir/$file);

print STDERR "carafe-wrapper-run.pl: executing [$mvcommand]\n";

print STDERR "carafe-wrapper-run.pl: executing [$carafecommand]\n";
qx($carafecommand);
