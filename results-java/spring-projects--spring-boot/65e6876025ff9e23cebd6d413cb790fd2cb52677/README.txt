commit 65e6876025ff9e23cebd6d413cb790fd2cb52677
Author: Jon Schneider <jkschneider@gmail.com>
Date:   Fri Nov 3 10:49:37 2017 -0500

    Upgrade to Micrometer 1.0.0-rc.3

    Upgrade to Micrometer 1.0.0-rc.3 and refactor existing
    auto-configuration to align with updated APIs.

    Note that Spring MVC instrumentation has now changed from an interceptor
    to a Filter.

    See gh-10906