commit 700b5e66684b769a371eac645e80411238b4a98c
Author: Pieter Kokx <pieter@kokx.nl>
Date:   Mon Jul 2 00:15:02 2012 +0200

    Did some refactoring on the service manager.

    - We now use the has() method to check on peering service managers
      first.
    - Closures are no longer allowed as abstract factories.