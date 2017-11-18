commit 51e9cf3d0ad0375930f3d776e1c61cbfebcebe5f
Author: Adam Murdoch <adam@gradle.com>
Date:   Fri Apr 8 09:43:39 2016 +1000

    Changed contract for `GradleExecutor.withDeprecationChecksDisabled()` so that each call to `withDeprecationChecksDisabled()` allows the presence of one deprecation message in the build output.

    Improved implementation to skip over stack traces associated with deprecation warnings, and removed implicit disabling of stack trace checks. Also improved accuracy of stack trace check.