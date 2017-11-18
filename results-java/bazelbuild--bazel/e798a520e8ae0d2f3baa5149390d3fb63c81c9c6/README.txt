commit e798a520e8ae0d2f3baa5149390d3fb63c81c9c6
Author: Brian Silverman <bsilver16384@gmail.com>
Date:   Mon Feb 15 16:26:16 2016 +0000

    Reduce the number of stat calls when setting up a sandbox

    This improved performance for a (somewhat artificial) test which runs
    100 genrules each with 3000 inputs by 25% on my laptop (2x
    hyperthreaded cores, SSD, ext4). Test code at
    <https://gist.github.com/bsilver8192/10527a862ce16bb7f79a>.

    --
    Change-Id: I7a7aaccdfbe2925c7e962c0192924ef1cf80b33a
    Reviewed-on: https://bazel-review.git.corp.google.com/#/c/2840/1..2
    MOS_MIGRATED_REVID=114694334