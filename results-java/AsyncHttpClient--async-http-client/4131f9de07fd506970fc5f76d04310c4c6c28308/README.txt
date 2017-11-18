commit 4131f9de07fd506970fc5f76d04310c4c6c28308
Author: jfarcand <jfarcand@apache.org>
Date:   Wed Oct 20 20:39:51 2010 -0400

    Performance improvement

       + Allow nested call from an AsyncHandler
       + Better detect a connection that get remotly closed but still in the connection pool
       + Expose some NETTY performance trick
       + Make sure we always cancel Future that gets under unexpected state