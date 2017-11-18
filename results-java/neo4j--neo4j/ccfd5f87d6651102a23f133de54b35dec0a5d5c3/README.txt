commit ccfd5f87d6651102a23f133de54b35dec0a5d5c3
Author: Martin Furmanski <martin.furmanski@neotechnology.com>
Date:   Tue Apr 25 12:09:30 2017 +0200

    fix long recovery leading to oom in in-flight map

    A long recovery during startup could lead to the in-flight map
    being filled up with enough entries to exhaust memory. This was
    an unnoticed side-effect of a recent refactoring which was
    previously protected against by holding the core state monitor
    during the entire recovery step.