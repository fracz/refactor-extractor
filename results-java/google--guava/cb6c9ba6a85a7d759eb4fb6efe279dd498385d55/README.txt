commit cb6c9ba6a85a7d759eb4fb6efe279dd498385d55
Author: Chris Povirk <cpovirk@google.com>
Date:   Tue May 13 15:02:43 2014 -0400

    Reduce differences between "when is the next permit available?" and "request a permit":
    - Standardize the method name format.
    - Standardize on returning the next available time (rather than the wait time that one of the two currently returns).
    - Standardize on allowing the return value to indicate a time in the past (rather than requiring it to be a present/future time as one of the two currently does).

    Also, improve tests and documentation around all this, particularly the "in the past" part.
    -------------
    Created by MOE: http://code.google.com/p/moe-java
    MOE_MIGRATED_REVID=66891133