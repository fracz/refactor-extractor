commit bb95cb9f997afc34d54f3d5bcb247c44f368b62b
Author: Brian Muramatsu <btmura@google.com>
Date:   Wed Aug 29 10:43:21 2012 -0700

    Fix GPS settings change listener in LocManager

    Bug 7051185

    - Register a ContentObserver to track settings changes rather than
      opening up a Cursor with a ContentQueryMap.

    - Move updateProvidersLocked into init to assure that the
      ContentObserver does not miss any changes.

    - Move blacklist and fudger creation before loadProvidersLocked to
      improve code readability.

    Change-Id: I4d3e19fa33401c384bc2b00658d4336ea119e0e5