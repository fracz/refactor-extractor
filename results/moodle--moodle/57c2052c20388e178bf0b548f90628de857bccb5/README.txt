commit 57c2052c20388e178bf0b548f90628de857bccb5
Author: David Mudrák <david@moodle.com>
Date:   Thu Sep 5 00:01:42 2013 +0200

    MDL-29378 Code cleanup in the Survey module

    While working on the issue, I spotted these two places that were worth of
    fixing. The first one is a trivial reminiscence of a previous refactoring,
    after which both branches of the if() statement became equal.

    The second one is actually a typo as in theory it could generate unexpected
    input fields with the name like qPP1. Luckily this never happened due to the
    way how survey questions are hardcoded (there are no questions with the type 2
    that would require two answers to their subquestions).