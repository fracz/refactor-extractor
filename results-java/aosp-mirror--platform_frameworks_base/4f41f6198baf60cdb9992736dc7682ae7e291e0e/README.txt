commit 4f41f6198baf60cdb9992736dc7682ae7e291e0e
Author: Neil Fuller <nfuller@google.com>
Date:   Mon May 9 16:55:36 2016 +0100

    Add support for ICU data pinning in the Zygote

    Upstream ICU caches use SoftReferences. On Android this means
    that useful cached data initialized in the Zygote are "lost" when
    the Zygote GCs and cannot be shared with apps. This change makes use
    of an Android patch to ICU to ensure References created during
    Zygote initialization are "strong". i.e. they are never collected.
    This prevents them being GCd and ensures they can be shared between
    applications.

    After switching ICU to use strong references, this change
    also creates DecimalFormatSymbols objects for common ULocales
    (ROOT, US and the user's default, if different). DecimalFormatSymbols
    makes use of an ICU Reference cache and this alone has been shown to
    improve the construction time of java.text.DecimalFormat by 1-1.5
    milliseconds on a Seed device. This saving applies the first time one
    is created in each app for each locale, and again if SoftReferences
    have been cleared.

    The cost to the heap size of the Zygote has been measured at ~107k.
    This value will change as more caches are switched to use the new
    CacheValue class.

    Formatting is typically performed on the UI thread and the intention
    of this change is to reduce app start up time and jank in apps like
    the Dialer which do a lot of formatting when scrolling lists. The
    change may also enable more virtual memory page-sharing between
    apps, though this is not the specific goal.

    Bug: 28326526
    (cherry picked from commit 41c9dc3b6938c5674c88ef4bc27b3d95f56efebe)

    Change-Id: I48e4d57ecbb207b9a5e17b6caf5e7b282e4a40e3