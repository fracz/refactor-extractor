commit e9ac47b390a4324561f412bbf326b0e7664d0ecc
Author: nono <aschor@users.noreply.github.com>
Date:   Sat Dec 10 19:38:55 2016 +0100

    [caldav] fix #4820 recurring events not scheduled (#4855)

    * Attempt to fix #4820 recurring events not scheduled

    Added a lot of trace and debug lines to be able to understand what was
    going on

    revert .classpath modifications
    cosmetics + small perf improvement
    remove lambda expression
    (jenkins alergic)

    * remove unrelated trace logs relative to categories

    to prevent merge conflicts.
    They will be added in "filtercategoryany" PR.

    * remove commented code

    but kept explanation as of why I removed the code
    "oldEventIds.remove(filename);" (the javadocs tells that remove only
    removes first occurence in a list)