commit 85470414c132dfeb574908f58e66447b65369c4c
Author: Hannes Papenberg <info@joomlager.de>
Date:   Tue Dec 16 16:51:03 2014 +0100

    Implementing JComponentRouterAdvanced class. Fixes #5444

    Call-time pass-by-reference has been removed

    Removing JError, using Exception instead

    protecting $name and renaming register() to registerView()

    Adding removeRule, getRules and renamed $id to $key in register method

    Making method names consistent

    Implementing JComponentRouterViewconfiguration for configuration of views in JComponentRouterAdvanced

    Codestyle, smaller improvements, unittests for all component router classes except for JComponentRouterAdvanced

    Removing ability to have one view with different names and implementing unittests for JComponentRouterAdvanced

    Adding get<View>Slug() and get<View>Key() methods to JComponentRouterAdvanced

    Updating unittest

    Small fixes

    Adding back in platform check

    Adding back in platform check

    Adding back in platform check

    Adding back in platform check

    Implementing feedback so far

    Adding "covers" notation for unittests