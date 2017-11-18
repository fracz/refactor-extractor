commit 7d7788fe12f3e6ecbd372d2b36887d09d0c5fa1c
Author: Mikhail Golubev <mikhail.golubev@jetbrains.com>
Date:   Wed Jun 1 17:27:02 2016 +0300

    Fine-grained write actions in "Extract Method" refactoring for Python

    This way it's safer to launch time-consuming dependent tasks
    (e.g. searching for code duplicates) synchonously since under
    WriteAction they might cause deadlock.