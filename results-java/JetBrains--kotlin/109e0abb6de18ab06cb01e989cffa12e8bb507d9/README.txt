commit 109e0abb6de18ab06cb01e989cffa12e8bb507d9
Author: Mikhail Glukhikh <Mikhail.Glukhikh@jetbrains.com>
Date:   Thu Apr 16 20:05:46 2015 +0300

    Review fixes for mutable variable smart casting (mostly refactoring).

    DataFlowValue.isPredictable() introduced instead of isStableIdentifier().
    Parenthesized assignments are treated correctly.