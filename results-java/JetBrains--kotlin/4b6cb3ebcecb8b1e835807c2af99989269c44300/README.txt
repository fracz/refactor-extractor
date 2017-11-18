commit 4b6cb3ebcecb8b1e835807c2af99989269c44300
Author: Mikhail Glukhikh <Mikhail.Glukhikh@jetbrains.com>
Date:   Wed Oct 21 12:06:21 2015 +0300

    A new kind of synthetic accessors for backing fields, if accessed inside lambda / object literal / local class #KT-9657 Fixed

    A set of tests provided
    Also #KT-4867 Fixed
    Also #KT-8750 Fixed
    Slight codegen refactoring