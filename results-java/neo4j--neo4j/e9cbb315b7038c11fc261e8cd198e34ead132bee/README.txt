commit e9cbb315b7038c11fc261e8cd198e34ead132bee
Author: Alistair Jones <alistair.jones@gmail.com>
Date:   Wed Apr 3 11:42:33 2013 +0100

    New (currently unimplemented) methods on BatchInserter for schema indexes.

    o Introduced concept of deferred schema indexes so that multiple indexes
      can be populated with a single scan.
    o Also improved JavaDoc for related classes, especially ResourceIterable.