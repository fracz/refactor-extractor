commit e2663f62b0fbb8b9ce2e706b821a135e0bc7e885
Author: Dhruv Manek <dmanek@gmail.com>
Date:   Fri Nov 4 17:13:56 2011 -0700

    feat(ng:style): compatibility + perf improvements

    - better compatibility with 3rd party code - we clober 3rd party
      style only if it direcrtly collides with 3rd party styles
    - better perf since it doesn't execute stuff on every digest
    - lots of tests