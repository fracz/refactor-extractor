commit b7e1ce07756aaca829828c2053eca0d66dd4d440
Author: Adam Lesinski <adamlesinski@google.com>
Date:   Mon Apr 11 20:03:01 2016 -0700

    Optimize ResTable::getLocales() to improve bindApplication performance

    Change from linear searching for uniqueness to binary search.

    Bug:27198799
    Change-Id: I1ccb6e93cc213810848f07d631d9d8de7c719803