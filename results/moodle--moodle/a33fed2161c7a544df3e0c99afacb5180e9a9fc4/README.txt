commit a33fed2161c7a544df3e0c99afacb5180e9a9fc4
Author: Sam Hemelryk <sam@moodle.com>
Date:   Tue Dec 2 08:54:38 2014 +1300

    MDL-48374 behat: improved page load exceptions

    The following improvements have been made to the page load watching:
    * Improved the exceptions when a page load expectation fails.
    * Added an exception if start watching happens twice without a page load.
    * Improved the page load span and xpath to make it faster and less likely
      to interfer in the future.