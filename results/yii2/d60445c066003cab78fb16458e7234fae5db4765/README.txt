commit d60445c066003cab78fb16458e7234fae5db4765
Author: Carsten Brandt <mail@cebe.cc>
Date:   Sat Jan 10 19:37:16 2015 +0100

    Add DateValidator::$timestampAttributeFormat

    includes refactoring of the formatting method to fix some major timezone issues.

    fixes #5053
    close #6820