commit df7cc48622a17907b7f84d0a8e3dc2470d1a8460
Author: hlopko <hlopko@google.com>
Date:   Mon Aug 7 15:54:03 2017 +0200

    Only add coverage flags when gcno file is expected

    Change https://github.com/bazelbuild/bazel/commit/63dabb6cfd55febc14e221ec51b18120558bc23c refactored the coverage feature, but wrongly started
    add coverage flags when the gcno file was not expected/read by bazel. This
    can be harmful, since the size of inputs increases unnecessarily.

    RELNOTES: None.
    PiperOrigin-RevId: 164455431