commit 2ed80032f9a0b12355335584dcd118f8fd254f2f
Author: Nick Telford <nick.telford@gmail.com>
Date:   Tue Feb 4 16:40:09 2014 +0000

    Add AbstractServerFactory#shutdownGracePeriod

    We seemed to lose this option in the refactoring of 0.7. Jetty has
    changed the way that shutdown is handled to better support multiple
    lifecycles, but this still makes sense.