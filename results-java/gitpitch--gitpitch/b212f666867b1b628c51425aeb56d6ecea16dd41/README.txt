commit b212f666867b1b628c51425aeb56d6ecea16dd41
Author: David Russell <david@gitpitch.com>
Date:   Thu Sep 29 17:04:27 2016 +0700

    Reduced cache interval for print and offline bundles on feature branches.

    Cache interval for print and offline bundles remains at 20 minutes for master branch. New shorter 20 second cache interval introduced for feature branches to improve developer experience.