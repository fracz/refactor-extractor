commit 7d6e5a2d0151a288264837ff592e31ea02542c58
Author: Vojta Jina <vojta.jina@gmail.com>
Date:   Mon Dec 23 17:54:05 2013 -0800

    chore: update Karma and SauceLabs launcher

    This should improve stability as it contains capture timeout (if a browser does not capture in a given timeout it gets killed) and retry (if a browser fails to start, Karma will try n times before failing).