commit b7797917ab63aa3983e30d01b28752f92a3198d3
Author: Trustin Lee <t@motd.kr>
Date:   Wed Apr 3 16:15:33 2013 +0900

    Deprecate Bootstrap.shutdown() and use EventLoopGroup.shutdown() wherever possible

    There are still some tests that use Bootstrap.shutdown() though.  They need non-trivial refactoring, which will come soon.