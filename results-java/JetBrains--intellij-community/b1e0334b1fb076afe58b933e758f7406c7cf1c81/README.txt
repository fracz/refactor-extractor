commit b1e0334b1fb076afe58b933e758f7406c7cf1c81
Author: Dmitry Batrak <Dmitry.Batrak@jetbrains.com>
Date:   Wed Sep 28 13:29:59 2016 +0300

    Created utility method to add dependant disposables to editor, refactored previously existed similar code and fixes for EA-89074, EA-89083 to use it (following IDEA-CR-14087)