commit a0eccb7441dfc7a3bcb6ef2e384cd1f63b9b03cb
Author: Greg Estren <gregce@google.com>
Date:   Mon Feb 1 22:46:10 2016 +0000

    Trim the BuildOptions input of ConfigurationFragment.key to only those
    options actually needed by the fragment. This protects against, e.g.,
    unnecessarily duplicating CppConfiguration instances when only Java flags
    change.

    This is a recommit of ca1b21ac6d8a58041db822725b42de151b163dee which was
    rolled back because it broke LIPO.

    This change is particularly important for dynamic configurations, which may
    mix and match fragments arbitrarily throughout a build. This not only has
    performance implications, but also correctness implications: code that
    expects two configured targets to have the same fragment (value) shouldn't
    break just because the second CT's configuration is a trimmed version of the
    first's.

    The original change breaks FDO/LIPO because CppConfiguration can't be
    shared across configurations. That's because it mutates state when
    prepareHook() is called, and each configuration calls prepareHook. We
    should ultimately solve this by refactoring the FDO/LIPO implementation
    but don't want to block dynamic configuration progress on that. So this
    change only enables trimming for dynamic configurations.

    --
    MOS_MIGRATED_REVID=113570250