commit 52455102107bfba52347c18e2f02ae148f127958
Author: Janak Ramakrishnan <janakr@google.com>
Date:   Mon Aug 3 17:05:05 2015 +0000

    Make some improvements to GraphConcurrencyTest -- versions are sensible, and we should now fail the test if there's an exception thrown in a worker thread that would cause a deadlock due to countdown latches not being mutated as expected.

    I don't know why the Mac Bazel tests are internally failing to build. Any ideas? I was very cargo-culty with the testutil library because I have no idea what's going on there with the duplicate packages.

    --
    MOS_MIGRATED_REVID=99733410