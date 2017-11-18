commit e50be62d4fded7b335c5b7c9c1b98693b2432244
Author: Brett Wooldridge <brett.wooldridge@gmail.com>
Date:   Mon Sep 21 18:27:13 2015 +0900

    More metrics refactor.  Isolate poolEntry from metrics package, reinstate no-op class to allow JIT to DCE the code.