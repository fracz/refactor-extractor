commit a9ba0e3736d3cd7011b937c43bc152430c57b784
Author: Valentina Kiryushkina <valentina.kiryushkina@jetbrains.com>
Date:   Tue Jun 13 13:52:41 2017 +0300

    PY-14218 Minor refactoring Make methods private as they now aren't directly called because of toolbar refactoring. Now we use action toolbar so actionPerformed() is called instead of these methods