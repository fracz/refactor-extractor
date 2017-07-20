commit 40a9c88f60cca96657b1b40a3a1b2d3d6c1f8e91
Author: Vincent Dauce <eXorus@users.noreply.github.com>
Date:   Wed Oct 12 02:05:03 2016 +0200

    Improve the stability of WebDriver when Selenium server is gone (#3534)

    * Improve debugWebDriverLogs

    * Improve _saveScreenshot

    * Improve _savePageSource

    * Improve cleanWebDriver

    * Improve _initialize

    * Improve _saveScreenshot when webDriver is null

    * Fix Inline control structures are not allowed

    * Update WebDriver.php

    * Added spaces around concatenation operator

    * Skip test if WebDriver failed to initialize

    * Update _saveScreenshot

    * Update WebDriver.php

    * Improve initializeSession

    initialize and initializeSession are almost the same so I merged them like this initializeSession are also improved

    * Update WebDriver.php

    * Update Metadata.php

    * Update IgnoreIfMetadataBlocked.php

    * Update Unit.php

    * Update WebDriver.php

    * Update WebDriver.php

    * Remove metadata

    * Reset changes

    * Reset changes

    * Reset changes

    * Catch errors in _before for not to crash and continue test execution

    Implement  #3598