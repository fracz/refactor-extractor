commit 5520581b5f043fb858b5b2044ff33ad8545a6d38
Author: Adam Lesinski <adamlesinski@google.com>
Date:   Mon Apr 11 20:03:01 2016 -0700

    Optimize ResTable::getLocales() to improve bindApplication performance

    Change from linear searching for uniqueness to binary search.

    Bug:27198799
    Change-Id: Ifa4672929df286c4693ab1f77716f08945941b0c