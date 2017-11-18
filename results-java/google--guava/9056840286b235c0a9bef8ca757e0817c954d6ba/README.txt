commit 9056840286b235c0a9bef8ca757e0817c954d6ba
Author: Louis Wasserman <lowasser@google.com>
Date:   Fri Mar 8 13:10:57 2013 -0800

    Simplify and refactor the logic to ensure that Immutable(Bi)Map.Builder and friends create entries that can be reused by RegularImmutable(Bi)Map to avoid redundant copies.
    -------------
    Created by MOE: http://code.google.com/p/moe-java
    MOE_MIGRATED_REVID=43407355