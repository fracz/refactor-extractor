commit bdb9f00c756ef5640caaeadb0183c30f02b69e5f
Author: Mikhail Glukhikh <Mikhail.Glukhikh@jetbrains.com>
Date:   Mon Jun 19 16:00:30 2017 +0300

    Introduce "Useless call on collection type" inspection

    Related to KT-12165
    Supported functions: filterNotNull, filterIsInstance,
    mapNotNull, mapNotNullTo, mapIndexedNotNull, mapIndexedNotNullTo

    Also, "Useless cal on not-null" improved a bit