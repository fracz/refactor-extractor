commit ede0059219008c732a79bcd754a30b1eac497c96
Author: Eric Hwang <ehwang@fb.com>
Date:   Tue May 19 17:31:46 2015 -0700

    Enforce/clarify the creation of PreferredProperties

    Rewrite PreferredProperties to enforce that callers can only create valid forms of PreferredProperties. Also refactor the logic around PreferredProperties so that they are consolidated in the class itself instead of spread around the different callsites.