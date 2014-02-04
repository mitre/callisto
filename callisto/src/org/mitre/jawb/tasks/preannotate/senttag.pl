#!/afs/rcf/lang/bin/perl -w

$fn = shift;
$use_text_field = (shift || 0);
unshift @ARGV, $fn;

$inside_s = 0;
$inside_text = 0;

while(<>) {
  die "document already sentence tagged!" if m,</?s>,;

  $inside_text = 1 if m,<text>,i;

  if(m,</text>,i) {
    $inside_text = 0;
    if($use_text_field && $inside_s) {
      s,(</text>),</s>$1,i;
      $inside_s = 0;
    }
  }

  if(!$inside_s && /<lex>/i && (!$use_text_field || $inside_text)) {
    s,((<[^/].*?>)*<lex>),<s>$1,;
    $inside_s = 1;
  }

  if($inside_s) {
    s,(<lex.*?>[\.?!]+</lex>)(\s*)(?!\s*)$,$1</s>$2<s>,gi;

    if(m,(<lex.*?>[\.?!]+</lex>)(\s*)$,i) {
      s,(<lex.*?>[\.?!]+</lex>)(\s*)$,$1</s>$2,gi;
      $inside_s = 0;
    }
  }
  print;
}

print "</s>" if $inside_s;
