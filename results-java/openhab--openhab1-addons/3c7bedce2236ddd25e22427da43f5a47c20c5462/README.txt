commit 3c7bedce2236ddd25e22427da43f5a47c20c5462
Author: Lars Küchenmeister <lkuech@gmail.com>
Date:   Wed Sep 2 20:57:36 2015 +0200

    RRD4J: Handling of Dimmer and Rollershutter items during restore of values improved (Fix for #3090)

    make sure Items that need PercentTypes instead of DecimalTypes are
    restored the right way.