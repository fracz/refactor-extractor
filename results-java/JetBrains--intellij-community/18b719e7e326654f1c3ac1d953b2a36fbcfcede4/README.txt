commit 18b719e7e326654f1c3ac1d953b2a36fbcfcede4
Author: Aleksey Pivovarov <AMPivovarov@gmail.com>
Date:   Tue Jun 16 14:29:47 2015 +0300

    merge: use default DialogWrapper bottom actiond

    * pass bottom actions individually (list is unnecessary here)
    * avoid duplication, simplify

    TODO: bottom actions logic should be properly refactored, when non-modal merge will be implemented