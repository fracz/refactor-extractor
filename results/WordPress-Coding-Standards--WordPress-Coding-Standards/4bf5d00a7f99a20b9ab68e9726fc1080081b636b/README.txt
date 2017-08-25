commit 4bf5d00a7f99a20b9ab68e9726fc1080081b636b
Author: JDGrimes <jdg@codesymphony.co>
Date:   Sat Apr 4 11:49:54 2015 -0400

    Improve performance of the ArrayAssignmentRestictions sniff

    If no groups of keys to look for are registered, it will remove itself
    from listening to the rest of the file. Previously it would keep
    getting called for each token in the file it sniffs for, even though
    there were no checks to be performed. It did bail early, but now it
    will only get called once per file with no groups registered.

    We also remove the sniff from the VIP ruleset. This further improves
    performance by keeping the sniff from being run at all. This may seem
    like a strange thing to do, but this sniff actually does nothing by
    itself. It is extended by some other sniffs, and should probably have
    been abstract (but changing it now would not be backward compatible).

    Even though the sniff is no longer part of the default configuration,
    the performance improvements to the sniff itself are still useful, for
    people who may have (unnecessarily) included it in custom
    configurations.

    The possibility of custom configurations specifying this sniff is
    another reason not to make it abstract. Perhaps we should consider
    moving it’s code to the base `WordPress_Sniff` and deprecating it in
    future.