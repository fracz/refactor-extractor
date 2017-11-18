commit eaa5ca879eb5567052debacb4ac151e62207349a
Author: Brett Wooldridge <brett.wooldridge@gmail.com>
Date:   Mon Sep 21 18:30:57 2015 +0900

    More metrics refactor.  Isolate poolEntry from metrics package, reinstate no-op class to allow JIT to DCE the code.