commit e3a76374209d17f3efde20a10f2e69b0c2a0d612
Author: lozal2244 <lozal2244@gmail.com>
Date:   Thu Dec 3 00:23:43 2015 +0100

    CO: improvement in SpecificPrice::getSpecificPrice

    This modification allows to give priority to the specificPrices with a specific id_cart.

    With this modification for example if a product is affected by two discounts, and one has the id_cart 0 and the orher the id_cart 3556, the second one will have more priority because it's more focused in this specific cart

    (cherry picked from commit 4312b61)