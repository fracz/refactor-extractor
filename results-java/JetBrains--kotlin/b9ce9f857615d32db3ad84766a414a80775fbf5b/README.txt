commit b9ce9f857615d32db3ad84766a414a80775fbf5b
Author: Pavel V. Talanov <talanov.pavel@gmail.com>
Date:   Wed Dec 2 14:42:32 2015 +0300

    Script, IDE: tweak extract based refactorings to behave better in scripts

    Fix not being able to invoke introduce variable for top level script expression
    Prohibit introduce parameter and introduce property for scripts on top level
    Basic test for introduce function (produces red code atm)