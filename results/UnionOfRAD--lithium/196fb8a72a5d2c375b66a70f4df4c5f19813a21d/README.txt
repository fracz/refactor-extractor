commit 196fb8a72a5d2c375b66a70f4df4c5f19813a21d
Author: Nate Abele <nate.abele@gmail.com>
Date:   Mon Oct 25 21:47:08 2010 -0400

    Implementing data value casting at object-write time, refactoring results fetching in `RecordSet` and `DocumentSet` to use `Result` objects. Implementing `ArrayAccess` in `Document`.