commit 6db6c15d0662f6fc310a3f97eddddd3b9c432aec
Author: Jason Tedor <jason@tedor.me>
Date:   Sun Mar 20 17:47:39 2016 -0400

    Add tests of POSIX handling for installing plugins

    This commit refactors the unit tests for installing plugins to test
    against mock filesystems (as well as the native filesystem) for better
    test coverage. This commit also adds tests that cover the POSIX
    attributes handling when installing plugins (e.g., ensuring that the
    plugins directory has the right permissions, the bin directory has
    execute permissions, and the config directory has the same owner and
    group as its parent).