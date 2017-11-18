commit 7de289f355f40f82b65f9c138df695f575191ef9
Author: Lorenzo Colitti <lorenzo@google.com>
Date:   Wed Nov 25 12:00:52 2015 +0900

    Temporarily add a requestNetwork flavour that takes a legacy type

    This method is public @hide to support progressive refactoring of
    tethering away from startUsingNetworkFeature to requestNetwork,
    without getting in the way of the CONNECTIVITY_ACTION cleanup in
    b/22513439 .

    Bug: 9580643
    Bug: 22513439
    Change-Id: I9053ec746cc8f415a2d5849f044667eeb14e1b19