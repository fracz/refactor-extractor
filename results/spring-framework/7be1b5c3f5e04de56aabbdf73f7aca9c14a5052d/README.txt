commit 7be1b5c3f5e04de56aabbdf73f7aca9c14a5052d
Author: Costin Leau <cleau@vmware.com>
Date:   Thu Jun 2 10:01:14 2011 +0000

    revised cache abstraction
    + remove generic signature on key generator (as the type is not used anywhere)
    + add a small improvement to CacheAspect to nicely handle the cases where the aspect is pulled in but not configured