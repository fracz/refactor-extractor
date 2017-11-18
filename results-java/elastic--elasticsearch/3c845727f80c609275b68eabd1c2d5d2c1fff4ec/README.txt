commit 3c845727f80c609275b68eabd1c2d5d2c1fff4ec
Author: Koen De Groote <kdg.private@gmail.com>
Date:   Wed Apr 26 04:15:00 2017 +0200

    Replace alternating regex with character classes

    This commit replaces two alternating regular expressions (that is,
    regular expressions that consist of the form a|b where a and b are
    characters) with the equivalent regular expression rewritten as a
    character class (that is, [ab]) The reason this is an improvement is
    because a|b involves backtracking while [ab] does not.

    Relates #24316