commit bfd1d3384b41ac9e563afe5c95afa010d8c19f4a
Author: olaola <olaola@google.com>
Date:   Mon Jun 19 16:55:24 2017 -0400

    Adding support for SHA256 for remote execution. Switch remote execution to use
    the currently defined hash function for blobs. Some refactoring. Adding an option to set the hash function in the remote worker, defaulting to the current behavior (unfortunately it is a build option, have not found a clean way to specify it at runtime).

    BUG=62622420
    TESTED=remote worker
    RELNOTES: none
    PiperOrigin-RevId: 159473116