commit 251219128913d8c8429d6f879d11208fed062dc7
Author: Dominic Luechinger <doldev@snowgarden.ch>
Date:   Tue Nov 8 00:10:14 2011 +0100

    Minor improvement: Cleaned up code that was introduced as bugfix to ZF-7289

    The code is quite unreadable. This version replaces the double check of
    ambiguity. The check and early return is already done by $this->_authenticateValidateResultSet($resultIdentities).

    At the point of the check, loop over all identities (most time 1, due default of $_ambiguityIdentity = false) and
    check if identity is valid. No need to check other identities.
    If no valid identity was found, the latest result (failure) is returned.

    Additionally:
    - Fixed minor bug in test case. Check of $result2 was not done right. Copy/paste error.