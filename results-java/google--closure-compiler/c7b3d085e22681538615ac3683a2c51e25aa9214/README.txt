commit c7b3d085e22681538615ac3683a2c51e25aa9214
Author: Ben Lickly <blickly@google.com>
Date:   Thu Mar 6 16:20:03 2014 -0800

    Handle && and || more precisely.

    In method SemanticReverseAbstractInterpreter#caseAndOrNotShortCircuiting, we must specialize the scope for the left type before analyzing the right side. If we specialize the left at the end of the method, we may miss improvements that happened while analyzing the right.

    (The other changes are for style, not for functionality.)
    -------------
    Created by MOE: http://code.google.com/p/moe-java
    MOE_MIGRATED_REVID=62686629