commit 2961012887b2f79f93de8f39e3d01b6f64810723
Author: Ben Lickly <blickly@google.com>
Date:   Mon Jun 23 16:03:00 2014 -0700

    [NEW TYPE INFERENCE] Create subclasses of JSType to reduce mem bloat.

    The change is invisible outside JSType.
    This CL only refactors JSType to have a single subclass; future CLs will add the specialized subclasses.

    - Keep all method code in JSType
    - Change all field accesses to getter methods
    - Move the fields to the subclass
    -------------
    Created by MOE: http://code.google.com/p/moe-java
    MOE_MIGRATED_REVID=69744593