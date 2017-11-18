commit 0f4928f1f73407485d6d94beda1dba1a2360ebbf
Author: Svet Ganov <svetoslavganov@google.com>
Date:   Thu Feb 2 20:02:51 2017 -0800

    Refactoring of auto fill - lifecycle, auth, improvements

    1. Move management of the remote fill service in a dedicated
       class that abstracts away the async and ephemeral nature
       of the binding.

    2. Update auth to move fingerprint out of the platform and
       allow response and dataset auth.

    3. Cleaned up the fill and save callback classes.

    4. The UI is now shared among all sessions and cleaned up.

    5. Reshuffled the remote callbacks to have cleaner separation.

    6. Cleaned up and tightened the reponse and dataset classes.

    7. Added API to support communicationn with intent based auth.

    Test: CTS + manually

    bug:31001899

    Change-Id: Idc924a01d1aea82807e0397ff7293d2b8470d4d6