commit f3ef94d35edb7bc46013711a74343062bcca0878
Author: nmittler <nmittler@gmail.com>
Date:   Mon Oct 20 12:38:08 2014 -0700

    Slight performance improvement to IntObjectHashMap.hashIndex()

    Motivation:

    Using a needless local copy of keys.length.

    Modifications:

    Using keys.length explicitly everywhere.

    Result:

    Slight performance improvement of hashIndex.