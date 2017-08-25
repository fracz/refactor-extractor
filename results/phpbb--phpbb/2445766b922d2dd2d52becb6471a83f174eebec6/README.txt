commit 2445766b922d2dd2d52becb6471a83f174eebec6
Author: Joseph Warner <hardolaf@hardolaf.com>
Date:   Wed Jun 19 14:57:11 2013 -0400

    [feature/auth-refactor] Refactor acp_board for new auth interface

    Partially refactors acp_board for the new authentication interface.
    Leaves some questionable if statements in the file.
    Modifies the interface to correctly impletment the acp() method.
    Modifies each provider to comply with the above mentioned interface
    modification.

    PHPBB3-9734