commit 889ec7379b4c36bb3044492e7a6872089045c1df
Author: Irina Iancu <elenairina@google.com>
Date:   Mon Jan 23 14:55:47 2017 +0000

    Adding JavaSourceJarsProvider to Java Provider.

    Also refactoring JavaProvider to use the Builder pattern, given that it is going to encapsulate a fair number of other providers.

    --
    PiperOrigin-RevId: 145280532
    MOS_MIGRATED_REVID=145280532