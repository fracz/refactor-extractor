commit e0e5a4454ba0a3d0886f93080d02465071d5e5fe
Author: Nathan Harmata <nharmata@google.com>
Date:   Fri Apr 3 17:11:37 2015 +0000

    Some minor improvements to KeyedLocker:

    (i) Change the semantics of KeyedLocker.AutoUnlocker#close such that it can be called at most once per AutoUnlocker instance.
    (ii) Change KeyedLocker.AutoUnlocker#close to throw a IllegalUnlockException (RuntimeException) on error, rather than leave the behavior intentionally underspecified.
    (iii) explicitly mention in AutoLocker#lock that a thread can call lock(k) multiple times before unlocking. Combined with (i), this implies that KeyedLocker#lock implementations will want to return fresh AutoUnlocker instances.

    These semantics are bit nicer to use anyway, but I also want them because I will soon be introducing KeyedLocker#lockBatch, and it's much easier to specify that given the above.

    --
    MOS_MIGRATED_REVID=90259645