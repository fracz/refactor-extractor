commit c2d773ef4c0916a44fd7936f7bbc22ec55102915
Author: Philipp Wollermann <philwo@google.com>
Date:   Fri Mar 24 12:35:20 2017 +0000

    sandbox: Improve the check whether the Linux sandbox is supported.

    Try to run /bin/true as a test of whether the Linux sandbox works,
    instead of just trying to create a bunch of namespaces as a proxy.

    This helps resolve issues on Linux distros where the earlier check
    worked, but then the sandbox ultimately failed due to other operations
    being unsupported.

    As an example, Debian Jessie and certain Docker versions seem to allow
    the creation of PID namespaces, but forbid mounting a new proc on top of
    /proc (see #1972). This resulted in Bazel thinking that sandboxing works
    fine, when it actually didn't. The improved check correctly catches this
    situation and disabled sandboxing.

    --
    PiperOrigin-RevId: 151116894
    MOS_MIGRATED_REVID=151116894