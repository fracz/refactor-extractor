commit a2fbb8835d91e5a96d0a352ea68ad8f1e00fe873
Author: Tianyin Xu <tixu@cs.ucsd.edu>
Date:   Sun Aug 9 16:12:07 2015 -0700

    [TACHYON-590] 1st step: refactoring the current Shell command implementation (consolidate the sanity checks on the number of parameters and pass TachyonURI instead of strings) so that we can add the wildcard support later.