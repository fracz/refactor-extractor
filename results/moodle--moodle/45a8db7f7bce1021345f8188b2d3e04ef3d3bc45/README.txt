commit 45a8db7f7bce1021345f8188b2d3e04ef3d3bc45
Author: Sam Hemelryk <sam@moodle.com>
Date:   Tue Dec 9 09:20:21 2014 +1300

    MDL-45699 cache: acceptance test improvements

    We now use the unit test cache config class when running
    acceptance tests so that the same defines are applicable there.