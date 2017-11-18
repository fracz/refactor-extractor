commit 68d42102d736e091ad9953f8a825490aaf95e048
Author: Mattias Persson <mattias@neotechnology.com>
Date:   Sat Feb 27 22:33:16 2016 +0100

    Disables relative references in high limit format

    due to it being broken, as shown by consistency checker.
    The reason why the problems with them weren't exposed in RecordFormatTest
    was that the generated records always had id 0, which was neutral
    with regards to relative references. Testing has now been improved
    in this commit to expose these problems.

    Properly implementing relative references with the current assumptions
    around -1 proved to be complicated in a number of ways, which is
    the reason why relative references are now disabled instead of fixed
    in this commit.

    Relative references can of course be enabled at a later point, but for
    now it's valuable to have a functioning format in place.