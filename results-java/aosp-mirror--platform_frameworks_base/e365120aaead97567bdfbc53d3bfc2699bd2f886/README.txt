commit e365120aaead97567bdfbc53d3bfc2699bd2f886
Author: Eino-Ville Talvala <etalvala@google.com>
Date:   Fri Jun 19 17:29:14 2015 -0700

    Camera2: Fix StreamConfigurationMap#isOutputSupportedFor for depth

    Need to check the right configuration list if the format is a
    depth format.

    Also refactor code slightly to use SurfaceUtils when possible.

    Bug: 21902551
    Change-Id: Icca2e81d8144bede46ad9f117d5e010ed409887c