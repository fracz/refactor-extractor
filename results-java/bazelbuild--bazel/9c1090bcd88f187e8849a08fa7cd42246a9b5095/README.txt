commit 9c1090bcd88f187e8849a08fa7cd42246a9b5095
Author: Googler <noreply@google.com>
Date:   Sat Oct 8 07:49:02 2016 +0000

    Fall back to compiling header-only libraries with --compile_one_dependency.

    In toolchains that support parse_headers, this is the desired action. For
    other toolchains, this change makes blaze try to build a header-only library
    which creates the error that the header only library is not a supported target
    kind. Seems like either error message is not useful/actionable.

    In theory, it would be better to actually analyze whether a parse_headers is
    activated, but that requires refactoring a lot of code that is currently C++
    specific.

    --
    MOS_MIGRATED_REVID=135554761