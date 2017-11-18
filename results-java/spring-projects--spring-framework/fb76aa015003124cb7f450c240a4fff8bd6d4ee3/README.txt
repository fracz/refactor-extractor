commit fb76aa015003124cb7f450c240a4fff8bd6d4ee3
Author: Stephane Nicoll <snicoll@pivotal.io>
Date:   Fri Nov 3 13:25:23 2017 +0100

    Detected candidate inner classes

    This commit improves the indexer to also consider static inner classes
    on top of regular top level classes.

    Issue: SPR-16112