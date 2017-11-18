commit 72cd972e99580b1fd2b33889af12e77b86940b98
Author: Lari Hotari <lari.hotari@gradle.com>
Date:   Thu Oct 13 15:45:01 2016 +0300

    Get rid of optimization for instanceof checks

    - newer JVMs are smarter and we prefer readability over the tiny bit of
      performance that we could possibly gain by just checking for class
      equality

    +review REVIEW-6294