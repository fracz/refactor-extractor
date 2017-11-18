commit aa6cd26c8f921e3b5e31cba6c077f5612efb1bac
Author: emcmanus <emcmanus@google.com>
Date:   Fri Apr 3 11:46:44 2015 -0700

    Initial support for getters in @AutoValue.Builder interfaces. This version requires the builder getter to have exactly the same signature as the getter for the corresponding @AutoValue property.

    This is the first of a set of improvements detailed at []
    -------------
    Created by MOE: http://code.google.com/p/moe-java
    MOE_MIGRATED_REVID=90268015