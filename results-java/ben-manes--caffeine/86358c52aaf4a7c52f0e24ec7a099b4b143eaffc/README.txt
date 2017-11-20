commit 86358c52aaf4a7c52f0e24ec7a099b4b143eaffc
Author: Ben Manes <ben.manes@gmail.com>
Date:   Fri Dec 30 19:02:27 2016 -0800

    Minor optimizations to simulated annealing

    This shows similar or improved hit rates on all the traces.
    - Changed the equation, as its different depending on the article.
    - A comparison paper suggested using Gaussian, which is beneficial
    - Restart when a 3pt+ loss is observed (not blind difference)
    - Shrink the pivot when changing directions