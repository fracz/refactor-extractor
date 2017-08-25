commit 03285ba7c7090f4881797bb74c14aaf74b48c47e
Author: Steve Clay <steve@mrclay.org>
Date:   Thu Feb 2 23:48:56 2017 -0500

    fix(security): random byte generation improved on some systems

    On some Windows systems, `microtime()` is of insufficient precision and
    can yield the same value on multiple calls. In our algorithm this results
    in a divide-by-zero error and a `$rounds` count of `0`, so a little extra
    entropy is lost. There are numerous other entropy sources tapped, so this
    isn't a serious flaw, but here we avoid the error and just fix `$rounds`
    to a reasonable value to pick up a little more entropy.

    This issue should affect few systems, as it's a fallback mechanism for
    several better random sources.

    This also adds PHP7's `random_bytes` as the first source.

    Fixes #10750