commit 75a622ef06412c2c2a6e84231c3855e260ed7666
Author: Lóránt Pintér <lorant@gradle.com>
Date:   Thu Sep 17 00:41:41 2015 +0200

    Introduced `ProjectionsDefined` state

    Also refactored tests in `DefaultModelRegistryTest` to automatically do
    the right thing instead of enumerating cases manually where possible.

    +review REVIEW-5620