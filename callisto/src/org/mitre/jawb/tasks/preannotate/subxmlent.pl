#!/afs/rcf/lang/bin/perl -w

while(<>) {
    s/&/&amp;/g;
    s/</&lt;/g;
    s/>/&gt;/g;
    print;
}
