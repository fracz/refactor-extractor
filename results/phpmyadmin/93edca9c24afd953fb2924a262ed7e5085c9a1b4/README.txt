commit 93edca9c24afd953fb2924a262ed7e5085c9a1b4
Author: Michal Čihař <mcihar@suse.cz>
Date:   Tue Jun 21 14:49:04 2011 +0200

    Update Blowfish to latest version available from Horde.

    This basically just improves documentation, defines public/private
    methods and simplifies some code using new PHP features.

    The Horde itself is no longer using this, they switched to PEAR package
    Crypt_Blowfish, maybe we should do it as well?