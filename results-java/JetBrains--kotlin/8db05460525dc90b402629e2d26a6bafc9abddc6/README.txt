commit 8db05460525dc90b402629e2d26a6bafc9abddc6
Author: Alexander.Podkhalyuzin <Alexander.Podkhalyuzin@jetbrains.com>
Date:   Mon May 21 18:29:01 2012 +0400

    Do not check function names in scope to suggest name for introduce variable refactoring (local values can overload function names from upper scopes).