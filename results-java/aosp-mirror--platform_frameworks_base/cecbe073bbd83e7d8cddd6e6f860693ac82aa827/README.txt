commit cecbe073bbd83e7d8cddd6e6f860693ac82aa827
Author: Romain Guy <romainguy@google.com>
Date:   Tue Jul 18 15:42:06 2017 -0700

    Put generated/compiled graphics code in getCodeCacheDir()

    A refactoring introduced a slight regression that caused compiled
    shaders to be cached in the wrong directory.

    Bug: 63813783
    Test: Manual
    Change-Id: I863e584ff1df4cda9242c64afd643a899023ee1c