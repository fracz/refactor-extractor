commit b6f14644b43ea8a6fbe732098f60b99db79ae971
Author: Yaroslav Lepenkin <yaroslav.lepenkin@jetbrains.com>
Date:   Fri May 20 16:14:23 2016 +0300

    Recent tests collapses tests executed from the same run configuration.
    - provided simple versioning of TestStateStorage.Record
    - run configuration name hash is saved into TestStateStorage.Record
    - lots of refactorings