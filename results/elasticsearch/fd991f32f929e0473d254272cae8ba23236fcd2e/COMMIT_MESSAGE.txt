commit fd991f32f929e0473d254272cae8ba23236fcd2e
Author: Lee Hinman <lee@writequit.org>
Date:   Tue Feb 21 15:54:55 2017 -0700

    Refactor TransportShardBulkAction and add unit tests

    This refactors the `TransportShardBulkAction` to split it appart and make it
    unit-testable, and then it also adds unit tests that use these methods.

    In particular, this makes `executeBulkItemRequest` shorter and more readable