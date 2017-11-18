commit 0ab589113877249aaa4a74d29d135920a3534e08
Author: Ben Lickly <blickly@google.com>
Date:   Wed May 15 16:06:41 2013 -0700

    Further improve performance of MinimizeConditions.

    Keep track of which trees are changed/unchanged when building the
    minimized condition trees, and use this information instead of
    calling the expensive Node.isEquivalentTo() method to determine
    if there are changes to the AST.
    -------------
    Created by MOE: http://code.google.com/p/moe-java
    MOE_MIGRATED_REVID=46692215