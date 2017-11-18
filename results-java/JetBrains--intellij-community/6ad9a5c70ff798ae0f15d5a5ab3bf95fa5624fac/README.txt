commit 6ad9a5c70ff798ae0f15d5a5ab3bf95fa5624fac
Author: Roman Shevchenko <roman.shevchenko@jetbrains.com>
Date:   Sun Sep 4 21:53:53 2016 +0300

    [platform] refactorings in platform update code

    - plugin selection is performed before loading a platform patch
    - platform and plugin update preparation under common progress
    - update installation code extracted from UpdateChecker