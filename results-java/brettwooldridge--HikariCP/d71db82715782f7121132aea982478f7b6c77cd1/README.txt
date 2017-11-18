commit d71db82715782f7121132aea982478f7b6c77cd1
Author: Brett Wooldridge <brett.wooldridge@gmail.com>
Date:   Tue Nov 18 23:56:33 2014 +0900

    Fix #198 improve shutdown handling with respect to asynchronous close() calls that might be occurring.