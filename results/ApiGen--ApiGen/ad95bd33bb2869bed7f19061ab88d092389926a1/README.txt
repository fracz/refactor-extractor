commit ad95bd33bb2869bed7f19061ab88d092389926a1
Author: Nelson Martell <nelson6e65@gmail.com>
Date:   Sun Jul 2 09:08:59 2017 -0400

    Improvements in binary autoloading (#921)

    * Move cli autoloading functionality to another file.

    Define some constants for root directory and runtime contexts.

    * Include 'bin' and 'boostrap' dirs for cs checks.

    'check-cs' and 'fix-cs' composer scripts will check for those dirs.

    * :fire: Move autoloader to 'bin/bootstrap.php'.

    * Improve error message.

    * :fire: Simplify constants on bootstrap script.

    :new: `APIGEN_IS_STANDALONE`.

    * :fire: Remove global constants from autoloader.

    * :memo: Minor improvements in 'bin/bootstrap.php'.