commit 0acd006477cf4c741fbea5384bcc30dda99ba210
Author: Liam Miller-Cushon <cushon@google.com>
Date:   Thu Jan 21 00:45:32 2016 +0000

    Refactor DependencyModule#computeStrictClasspath

    so it can operate on lists of classpath entries, instead of pre-joined
    classpath strings.

    Also improve error for missing .jdeps inputs.

    --
    MOS_MIGRATED_REVID=112634009