commit cbc81f8c91affc7799f35bcc359a85db15aa479b
Author: Philipp Wollermann <philwo@google.com>
Date:   Sat Feb 27 06:31:58 2016 +0000

    Little refactorings on resource management stuff:
    - Remove unneeded abstract override AbstractAction#getResourceConsumption()
    - Remove comments justifying returning ResourceSet.ZERO from getResourceConsumption() - instead document it once on the base method.
    - Replace ResourceSet.create(0, 0, 0) with ResourceSet.ZERO.
    - Make sure no one ever returns null from getResourceConsumption() and then simplify the code in SkyframeActionExecutor to not check for it.

    --
    MOS_MIGRATED_REVID=115739250