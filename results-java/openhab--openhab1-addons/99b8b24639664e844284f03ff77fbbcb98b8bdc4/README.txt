commit 99b8b24639664e844284f03ff77fbbcb98b8bdc4
Author: Jürgen Richtsfeld <juergen.richtsfeld@gmail.com>
Date:   Mon Apr 6 21:24:23 2015 +0200

    Make RFXComBindingProvider a AutoUpdateBindingProvider so the item states are only updated if a command was successfully sent.
    Further did some minor refactorings to get some methods smaller.
    Do some synchronization fixes.