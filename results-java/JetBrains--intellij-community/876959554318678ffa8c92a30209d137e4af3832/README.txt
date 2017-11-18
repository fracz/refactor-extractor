commit 876959554318678ffa8c92a30209d137e4af3832
Author: Mikhail Golubev <mikhail.golubev@jetbrains.com>
Date:   Fri Oct 20 21:31:35 2017 +0300

    PY-18216 Move and other refactorings don't run full-blown optimize imports

    but rather only delete unused imports so as not to cause unwanted
    changes in affected files.