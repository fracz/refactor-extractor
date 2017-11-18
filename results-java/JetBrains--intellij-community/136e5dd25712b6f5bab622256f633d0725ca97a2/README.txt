commit 136e5dd25712b6f5bab622256f633d0725ca97a2
Author: Denis.Zhdanov <Denis.Zhdanov@jetbrains.com>
Date:   Fri Mar 30 14:25:33 2012 +0400

    IDEA-83278 "Wrapping and Braces -> Keep when reformatting -> Multiple expressions in one line" prevents refactorings/intentions from producing correctly formatted code

    Make it possible to use 'multiple expressions on the same line' from tests as well