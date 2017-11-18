commit bfb6dac929ff0040b5abc30c44fffc8ce4fb2cf4
Author: Coda Hale <coda.hale@gmail.com>
Date:   Mon Jan 2 20:30:49 2012 -0800

    Don't lock on ConcurrentHashMap.

    It's rude, according to FindBugs. Here, of course, we *do* want to use a lock, but it's an easy refactor to reduce false positives via static analysis.