commit 316cb875836cb69e38309b62e9ad56b11a3f71b6
Author: Phillip Webb <pwebb@gopivotal.com>
Date:   Wed Apr 23 10:45:05 2014 +0100

    Create ApplicationPid and remove SystemUtils

    Create a new ApplicationPid class to remove the need for SystemUtils
    and refactor existing calls.