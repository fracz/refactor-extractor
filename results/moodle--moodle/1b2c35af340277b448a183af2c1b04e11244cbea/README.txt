commit 1b2c35af340277b448a183af2c1b04e11244cbea
Author: Andrew Nicols <andrew@nicols.co.uk>
Date:   Tue Dec 2 08:57:38 2014 +0800

    MDL-48374 behat: improved page load detection

    Check that page load detection was correctly started before testing that a
    new page was loaded.

    Without this, it is possible to have mutliple subsequent cases of:
        And a new page should have loaded since I started watching

    Without first starting the page load detection.