commit e7f0b58cf86db62e311f5635a4608777bc36933e
Author: Adrian Cole <adriancole@users.noreply.github.com>
Date:   Tue Jul 11 09:27:42 2017 +0800

    Refactors codec to reduce redundant list creation (#1650)

    This includes cleanups and performance improvements when reading spans.
    Notably, this stops recreating lists by re-using Span.Builder

    To test this, we use the same client-span json from zipkin-reporter.