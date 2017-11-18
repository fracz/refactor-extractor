commit 021de736237f6d408d61c1c4f884f98125cc081a
Author: Daisuke Miyakawa <dmiyakawa@google.com>
Date:   Fri Oct 9 12:45:17 2009 -0700

    Clean-up vCard code.

    Note that refactor is still on-going. Some changes done now may be
    reverted in the future.

    - Move reusable constants from VCardComposer to Constants.
    - Make ContactStruct appropriately refers to Constants.
    - Move PBAP-related code at the bottom of vCard composer
    - Remove some redundant code.

    Internal issue number: 2160039