commit f33a98c3cc9a6e19948946972902f623161b7bbc
Author: JDGrimes <jdg@codesymphony.co>
Date:   Fri Jun 12 18:13:56 2015 -0400

    Remove non-rules from VIP ruleset

    These rules are not really rules at all. Instead, they are parent
    classes extended by other sniffs. They don’t do anything on their own,
    and since the classes will be auto-loaded, we don’t need to include
    them in the ruleset.

    I’ve also updated each of these classes to skip over a file if there
    are no tests to run. This will improve performance when running the
    `WordPress` ruleset.

    The function restrictions class could possibly be made abstract in the
    future, if we wanted to do that. However, the variable restrictions
    sniff has its own tests, and so it can’t be abstract.

    Related: #345