commit 945a42de9c73a15f27d603ebef8b8a90a4c0ced8
Author: Michajlo Matijkiw <michajlo@google.com>
Date:   Mon Oct 12 16:24:01 2015 +0000

    Refactor cycle related skyvalues to use shared empty value

    Previously the cycle values (were supposed to) share a common abstract
    base class, however usage was inconsistent. Instead refactor to eliminate
    the specialized value classes, remove the abstract class, and share a
    common empty placeholder value, which should be useful for future/other
    current empty values.

    --
    MOS_MIGRATED_REVID=105217399