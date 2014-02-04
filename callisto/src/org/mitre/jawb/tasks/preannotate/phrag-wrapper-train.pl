#!/afs/rcf/lang/bin/perl;

#use File::Basename;

$perl = "/afs/rcf/lang/bin/perl";

$punctok = "/afs/rcf/project/read-comp/bin/punctoker /afs/rcf/project/read-comp/specs/splitpunct.spec";
$senttag = "$perl /afs/rcf/user/wmorgan/l/tallal/callisto/src/org/mitre/jawb/tasks/preannotate/senttag.pl";
$phrag = "/afs/rcf/project/tallal/wmorgan/phrag-1.5.3/bin/phrag";
$mklex = "/afs/rcf/project/tallal/wmorgan/phrag-1.5.3/bin/mklex";

$filename = shift or die "first argument must be the new filename";
$traindir = shift or die "second argument must training file directory";
$specfile = shift or die "third argument must be spec file";

$args = join(" ", @ARGV);

if(qx(grep "<lex>" $filename)) {
  print STDERR "no lexing+senttagging needed for $filename\n";
}
else {
  print STDERR "lexing+senttagging needed for $filename\n";
  run(qq(cat $filename | $punctok | $senttag - > $filename.tmp));
  run(qq(mv $filename.tmp $filename));

  if(qx(grep -i "<doc>" $filename)) {
    print STDERR "no <doc> wrapping needed for $filename\n";
  }
  else {
    print STDERR "<doc> wrapping needed for $filename\n";
    run(qq((echo "<doc>"; cat $filename; echo "</doc>") > $filename.tmp));
    run(qq(mv $filename.tmp $filename));
  }
}

#$newname = $traindir . "/" . basename($filename);
#run(qq(mv $filename $newname));
run(qq(cat $traindir/*.sgml | $mklex -tf 1 > $traindir/lexicon.txt));
run(qq((echo "<top>";cat $traindir/*.sgml;echo "</top>") > $traindir/corpus.xml));
run(qq($phrag $specfile hmm.train\\? t hmm.lexicon.list $traindir/lexicon.txt hmm.train.input $traindir/corpus.xml $args));

exit 0;

sub run {
  my $c = shift;

  print STDERR "running [$c]\n";
  return qx($c);
}
