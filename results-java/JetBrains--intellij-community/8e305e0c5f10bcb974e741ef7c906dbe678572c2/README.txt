commit 8e305e0c5f10bcb974e741ef7c906dbe678572c2
Author: Mikhail Golubev <mikhail.golubev@jetbrains.com>
Date:   Tue Oct 18 17:48:17 2016 +0300

    PY-17265 Cleanup related to transition to Java 8

    Not all results of refactorings like transforming anonymous classes
    into lambdas and replacing explicit type parameters with diamonds where
    applied during rebase on the updated master.