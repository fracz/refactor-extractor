commit c66d07f06ad369c561a1a192d6df2fc493dd304c
Author: tbreisacher <tbreisacher@google.com>
Date:   Mon Sep 22 18:46:41 2014 -0700

    Fix in object pattern parsing to allow anything that is valid as the left side of an assignment, on the right side of a colon in an object pattern.

    This is a roll-forward, but with some refactoring so that speculative parsing will fail faster, which prevents parsing of large object literals from taking a ridiculously long time.
    -------------
    Created by MOE: http://code.google.com/p/moe-java
    MOE_MIGRATED_REVID=76126495