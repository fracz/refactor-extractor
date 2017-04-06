commit 833b94454878621e36d05c9bf72d86a46c9e5cd2
Merge: f946712 e271b17
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Tue Sep 18 19:25:25 2012 +0200

    merged branch mazen/optional-optimization (PR #5340)

    Commits
    -------

    e271b17 Remove the string optimization since it causes no real performance gain but increases generation time of the dumped PHP Container

    Discussion
    ----------

    PhpDumper and large strings

    When the PhpDumper is dealing with longer strings, the regular expression performed to optimize this can be quite a performance hog.
    In our case sometimes the dumper takes more then 30 seconds if leaving this enabled. Disabling it will bring it back to sub-second speed.

    This patch makes the optimization optional by passing in an additional container option.

    ---------------------------------------------------------------------------

    by fabpot at 2012-08-25T16:57:29Z

    I don't like adding yet another option for something that should "just works". It would be better to find a better way to optimise the strings for all cases.

    ---------------------------------------------------------------------------

    by mazen at 2012-08-25T17:22:07Z

    I never really tested how much of a runtime difference it incurs when either using the "optimized" version or the non optimized version, so:

    Having an example at hand which generates stable results yields (in a non-debug environment with a booted container using either of the optimization methods):

    Without optimized strings:

    ```
    Time taken for tests:   14.865 seconds
    Complete requests:      1000
    Failed requests:        0
    Write errors:           0
    Total transferred:      217000 bytes
    HTML transferred:       8000 bytes
    Requests per second:    67.27 [#/sec] (mean)
    Time per request:       14.865 [ms] (mean)
    Time per request:       14.865 [ms] (mean, across all concurrent requests)
    Transfer rate:          14.26 [Kbytes/sec] received

    Connection Times (ms)
                  min  mean[+/-sd] median   max
    Connect:        0    0   0.0      0       0
    Processing:    14   15  19.7     14     632
    Waiting:       14   15  19.7     14     632
    Total:         14   15  19.7     14     632

    Percentage of the requests served within a certain time (ms)
      50%     14
      66%     14
      75%     14
      80%     14
      90%     14
      95%     14
      98%     15
      99%     23
     100%    632 (longest request)
    ```

    With Optimized Strings enabled

    ```
    Time taken for tests:   14.077 seconds
    Complete requests:      1000
    Failed requests:        0
    Write errors:           0
    Total transferred:      217000 bytes
    HTML transferred:       8000 bytes
    Requests per second:    71.04 [#/sec] (mean)
    Time per request:       14.077 [ms] (mean)
    Time per request:       14.077 [ms] (mean, across all concurrent requests)
    Transfer rate:          15.05 [Kbytes/sec] received

    Connection Times (ms)
                  min  mean[+/-sd] median   max
    Connect:        0    0   0.0      0       0
    Processing:    14   14   1.3     14      48
    Waiting:       14   14   1.3     14      48
    Total:         14   14   1.3     14      48

    Percentage of the requests served within a certain time (ms)
      50%     14
      66%     14
      75%     14
      80%     14
      90%     14
      95%     14
      98%     14
      99%     15
     100%     48 (longest request)
    ```

    So the response times differ by around 0.8ms

    Building the non-optimized container takes around 800ms
    Building the optimized container takes 43 seconds

    From my Point of View it would just be viable to remove the optimization (since it already incurred some issues fixed in 808088a3ca607bf62f6c70ef7d3a3066f0cfac98).

    I do not see a way how to improve the regexps (but by all means I'm no regular expression guru)

    ---------------------------------------------------------------------------

    by fabpot at 2012-08-30T07:12:55Z

    I'm also for removing these optimizations. What others think?

    ---------------------------------------------------------------------------

    by Baachi at 2012-08-30T07:54:53Z

    I'm +1 for removing this feature.
    The performance boost is to small.