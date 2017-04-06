commit 91bde2e6b28758ba966db60ff61f6a3a31a0ddec
Author: Arjen Poutsma <apoutsma@pivotal.io>
Date:   Tue Sep 13 20:13:36 2016 +0200

    Refactor Router to RoutingFunctions

    This commit refactors the Router into a RoutingFunctions class, by:

      - Renaming the class :)
      - Moving all Configuration logic into a separate, top-level
      Configuration class with mutable builder.