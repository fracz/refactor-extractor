commit 568b1161f9bdce183ac0755cc1e2a7bbdd9b5af5
Author: Mikhail Golubev <mikhail.golubev@jetbrains.com>
Date:   Tue May 31 21:52:24 2016 +0300

    Fix PIEAE in PyExtractMethodTest

    They were caused by the fact that we first replace selected fragment
    with method call and then use invalided elements of the same fragment
    to find its duplicates.
    I split ExtractMethodHelper#processDuplicates() into two methods:
    collectDuplicates() that finds duplicates before the substitution is
    performed and replaceDuplicatesWithPrompt() that handles user
    notification and replacing found occurrences afterwards.
    The same way this refactoring is implemented for Java sources
    (see ExtractMethodHandler.invokeOnElements()).