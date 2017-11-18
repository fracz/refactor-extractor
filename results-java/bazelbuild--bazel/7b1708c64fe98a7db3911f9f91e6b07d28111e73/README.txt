commit 7b1708c64fe98a7db3911f9f91e6b07d28111e73
Author: Laurent Le Brun <laurentlb@google.com>
Date:   Thu Oct 13 10:05:12 2016 +0000

    Refactoring for LoadStatement

    Use StringLiteral instead of String+Location. This improves consistency.

    getRawImports() now returns StringLiterals, which makes possible to show
    location in error messages.

    --
    MOS_MIGRATED_REVID=136019586