commit bd441caaf6fb268b2756bc51304efa038bf58376
Author: Daniel Nishi <dhnishi@google.com>
Date:   Mon Apr 10 11:21:26 2017 -0700

    Use the StorageStatsManager in FileCollector.

    This should vastly improve the speed of the FileCollector.

    Change-Id: I7a878a0622bbd6c758fb1d36125414d8b025e709
    Fixes: 35807386
    Test: Existing tests continue to pass.