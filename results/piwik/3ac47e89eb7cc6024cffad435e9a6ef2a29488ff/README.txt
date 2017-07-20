commit 3ac47e89eb7cc6024cffad435e9a6ef2a29488ff
Author: Timo Besenreuther <timo.besenreuther@gmail.com>
Date:   Wed Mar 20 17:31:11 2013 +0100

    visitor log improvements

     * use a limit for the number of actions that are loaded for every visit. without a limit, the web server might crash (e.g. when there are 100K+ page views in one visit after a log import)
     * regular pagination in visitor log. (a) performance improvement: use indexes in the select query. (b) bug fix: the old logic was incorrect. in one setup, there are 10K+ visits but the visitor log only showed one full page and one page with one visit.
     * removed reference to trunk from tests read me