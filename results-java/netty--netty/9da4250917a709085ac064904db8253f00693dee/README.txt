commit 9da4250917a709085ac064904db8253f00693dee
Author: Trustin Lee <t@motd.kr>
Date:   Fri Nov 21 11:07:24 2014 +0900

    Backport the IntObjectHashMap changes in f23f3b9617b01095416334060ca8379316946e5c

    Motivation:

    The mentioned commit contains a bug fix and an improvement in
    IntObjectHashMap that requires backporting.

    Modifications:

    Update IntObjectMap, IntObjectHashMap, and IntObjectHashMapTest

    Result:

    Easier to backport HTTP/2 and other changes in master in the future