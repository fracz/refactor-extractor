commit c10120ac12f66a4e011173ede9c359a3dc38fcbd
Author: Lukacs Berki <lberki@google.com>
Date:   Tue Dec 6 15:56:14 2016 +0000

    Fix private visibility for aliased targets.

    Also a drive-by improvement on some related error messages.

    RELNOTES[INC]: Only targets with public visibility can be bound to something in //external: .

    --
    PiperOrigin-RevId: 141178039
    MOS_MIGRATED_REVID=141178039