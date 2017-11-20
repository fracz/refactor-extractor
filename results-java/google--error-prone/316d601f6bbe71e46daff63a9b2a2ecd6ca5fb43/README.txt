commit 316d601f6bbe71e46daff63a9b2a2ecd6ca5fb43
Author: eaftan <eaftan@google.com>
Date:   Thu Oct 22 16:30:12 2015 -0700

    CollectionIncompatibleType now checks Collection#containsAll,
    Collection#removeAll, and Collection#retainAll.

    This required refactoring how we match on method invocations and extract
    the interesting parts of them.  I also moved this check into its own
    package, as there was getting to be a lot of code.

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=106102787