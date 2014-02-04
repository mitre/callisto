#!/afs/rcf/lang/bin/perl;

##
## This takes 3 arguments
##  1) A file name (full path) where the learned model will be saved
##  2) A file name (full path) that contains the tagset of interest - i.e.
##     what tags it should pay attention to
##  3) A directory name (full path) that contains lexicons to be used in training

$rootdir = "/afs/rcf/project/tallal/wellner";

$perl = "/afs/rcf/lang/bin/perl";

$subent = "cat";#"$perl /afs/rcf/user/wmorgan/l/tallal/callisto/subxmlent.pl";
$punctok = "/afs/rcf/project/read-comp/bin/punctoker /afs/rcf/project/read-comp/specs/splitpunct.spec";
$senttag = "$perl /afs/rcf/project/tallal/wellner/callisto/src/org/mitre/jawb/tasks/preannotate/senttag.pl";
$carafe = "/afs/rcf/project/rcii/bin/carafe_train.opt";

$traindir = shift or die "first arg is the directory containin the training files";
$modelname = shift or die "second argument must the model file to write";
$tagsetname = shift or die "third argument must be the tagset file to use";
$lexicondir = shift or die "fourth argument must be the directory containing lexicon files";

$args = join(" ", @ARGV);


run(qq((cat $traindir/*.sgml) > $traindir/corpus.xml.pre));


if(qx(grep "<lex>" $traindir/corpus.xml.pre)) {
  print STDERR "no lexing+senttagging needed for $filename\n";
}
else {
  print STDERR "lexing+senttagging needed for $filename\n";
  run(qq($subent $traindir/corpus.xml.pre | $punctok | $senttag - 0 > $traindir/corpus.xml.pre.tmp));
  run(qq(mv $traindir/corpus.xml.pre.tmp $traindir/corpus.xml.pre));
}

run(qq((echo "<top>";cat $traindir/corpus.xml.pre;echo "</top>") > $traindir/corpus.xml));
run(qq((rm $traindir/corpus.xml.pre)));

run(qq($carafe -input-dir $traindir/ -filter "corpus.xml" -model $modelname -tagset $tagsetname -lexicon-dir $lexicondir));

exit 0;

sub run {
  my $c = shift;

  print STDERR "running [$c]\n";
  return qx($c);
}
