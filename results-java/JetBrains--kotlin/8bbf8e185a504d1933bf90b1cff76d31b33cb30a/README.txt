commit 8bbf8e185a504d1933bf90b1cff76d31b33cb30a
Author: Alexey Sedunov <alexey.sedunov@jetbrains.com>
Date:   Mon Apr 24 18:24:07 2017 +0300

    Move: Filter out usages with visibility error before refactoring

    If declaration is already invisible before move,
    no visibility conflict should be reported

     #KT-17571 Fixed