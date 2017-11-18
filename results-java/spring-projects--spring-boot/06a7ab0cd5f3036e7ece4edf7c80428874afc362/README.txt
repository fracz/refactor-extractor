commit 06a7ab0cd5f3036e7ece4edf7c80428874afc362
Author: Phillip Webb <pwebb@pivotal.io>
Date:   Tue Dec 20 13:52:53 2016 -0800

    Polish ReservoirFactory support

    Polish Dropwizrd reservoir support including a refactor of
    `ReservoirFactory` to allow reservoirs to be created based on a
    metric name.

    See gh-5199
    See gh-7105