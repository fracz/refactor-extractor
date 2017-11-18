commit f61d7778f8251e330f6d5f789a87fb9e43c59cd6
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Fri Oct 17 19:23:33 2014 +0200

    Added basic performance test for classloaders caching

    - Needed to give generous max time regression because current master is slower that previous versions.
    - Tweaked the machinery so that it is possible to test with long running process. It can be still improved.
    - Started forking the test runs. It's simpler this way.