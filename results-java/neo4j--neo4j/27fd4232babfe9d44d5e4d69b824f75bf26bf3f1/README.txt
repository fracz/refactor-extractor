commit 27fd4232babfe9d44d5e4d69b824f75bf26bf3f1
Author: Julian Simpson <simpsonjulian@gmail.com>
Date:   Mon Apr 29 11:30:43 2013 +1200

    Remove unused Timer

    The present GuardingRequestFilter sets a Timer that logs 2 messages, and then does nothing.  This commit removes the unused code, in order to improve the user experience for Cloud (the only place I know of that uses the GuardingRequestFilter).