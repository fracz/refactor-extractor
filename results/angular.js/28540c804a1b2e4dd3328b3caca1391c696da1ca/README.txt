commit 28540c804a1b2e4dd3328b3caca1391c696da1ca
Author: Igor Minar <igor@angularjs.org>
Date:   Tue Jul 29 10:46:00 2014 -0700

    perf(Scope): remove the need for the extra watch in $watchGroup

    Instead of using a counter and an extra watch, just schedule the reaction function via $evalAsync.

    This gives us the same/similar ordering and coalecsing of updates as counter without the extra
    overhead. Also the code is easier to read.

    Since interpolation uses watchGroup, this change additionally improves performance of interpolation.

    In large table benchmark digest cost went down by 15-20% for interpolation.

    Closes #8396