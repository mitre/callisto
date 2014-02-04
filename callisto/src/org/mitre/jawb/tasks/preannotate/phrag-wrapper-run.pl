#!/afs/rcf/lang/bin/perl;

$perl = "/afs/rcf/lang/bin/perl";

$subent = "$perl /afs/rcf/user/wmorgan/l/tallal/callisto/src/org/mitre/jawb/tasks/preannotate/subxmlent.pl";
$punctok = "/afs/rcf/project/read-comp/bin/punctoker /afs/rcf/project/read-comp/specs/splitpunct.spec";
$senttag = "$perl /afs/rcf/user/wmorgan/l/tallal/callisto/src/org/mitre/jawb/tasks/preannotate/senttag.pl";
$phrag = "/afs/rcf/project/tallal/wmorgan/phrag-1.5.3/bin/phrag";

$input = shift or die "need input fn as first arg";
$output = shift or die "need out fn as second arg";
$mimetype = shift or die "need mimetype as third arg";
$specfile = shift or die "need specfile fn as fourth arg";

$args = join(" ", @ARGV);

if($mimetype =~ /sgml|xml/i) {
  $command = "cat $input | $punctok | $senttag - 1";
}
else {
  $command = "$subent $input | $punctok | $senttag - 0";
}

$command .= " | $phrag $specfile $args hmm.tag.input - hmm.tag.output $output";

print STDERR "phrag-wrapper-run.pl: executing [$command]\n";

qx($command);
