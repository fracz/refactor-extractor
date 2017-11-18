commit 141b9967094c32ca36a2ce87ef991610d7ffa317
Author: Alexander Udalov <Alexander.Udalov@jetbrains.com>
Date:   Mon Apr 15 19:04:11 2013 +0400

    Minor refactoring in ExpressionCodegen

    Inline useless method, factor out resolveToCallableMethod logic from
    resolveToCallable