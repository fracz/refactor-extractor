commit 4dac97741dff0d38212a0908cda8cd45a6227da5
Author: blickly <blickly@google.com>
Date:   Wed Dec 3 14:38:11 2014 -0800

    [NEW TYPE INFERENCE] Warn about !T when T is a type variable

    Since this is not supported, warn explicitly rather than silently ignoring
    the annotation.
    This helps to improve the situation around issues #209 and #723
    -------------
    Created by MOE: http://code.google.com/p/moe-java
    MOE_MIGRATED_REVID=81282745