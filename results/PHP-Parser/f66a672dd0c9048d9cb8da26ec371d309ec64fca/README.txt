commit f66a672dd0c9048d9cb8da26ec371d309ec64fca
Author: nikic <nikita.ppv@googlemail.com>
Date:   Sun Apr 29 22:57:46 2012 +0200

    Start refactoring parser skeleton

    The yacc parser skeleton with all those odd $yy short names is quite
    non-obvious. This commits starts to refactor it a bit, to use more
    obvious names and logic.