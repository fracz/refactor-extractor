commit b94b277cbef2fa3be2f55904e2cb899a8ad5b92b
Author: Nadya Zabrodina <Nadya.Zabrodina@jetbrains.com>
Date:   Wed Sep 7 19:00:40 2016 +0300

    [shelf]: refactoring

    * method rearrangement for getShelvedChangeLists;
    * change parameters order;
    * wrap getting shelved changes into notNullize collection and reuse it;
    * add an ability to unshelve already unshelved without restoring;