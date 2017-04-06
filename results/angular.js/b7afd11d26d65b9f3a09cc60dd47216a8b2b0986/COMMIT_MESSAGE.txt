commit b7afd11d26d65b9f3a09cc60dd47216a8b2b0986
Author: Caitlin Potter <caitpotter88@gmail.com>
Date:   Fri Nov 7 16:13:31 2014 -0500

    refactor($parse): don't use bind-once interceptor for non-bind-once expressions

    Side-effects:
      - Logic for allOrNothing watches now lives in $interpolate rather than $parse

    Credit to @jbedard for idea to remove $watch interceptors craziness from $interpolate. Even though
    it technically didn't actually work, it was worth a shot, and helped clean things up a bit. Go team!

    Closes #9958
    Closes #9961