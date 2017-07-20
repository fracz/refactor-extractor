commit 0bcfc08b3f62d765f6f26ed2771b0e44242482ae
Author: Adam Silverstein <adamsilverstein@earthboundhosting.com>
Date:   Wed Feb 15 17:59:42 2017 +0000

    REST API: improve test fixture generation, normalizing data.

    Add a data normalization pass when generating data fixtures for the REST API endpoints. Ensures that the `wp-api-generated.js` fixture won't change between test runs. Set more default properties and use fixed values for any properties that can't be easily controlled (object IDs and derivatives like link). Generate the fixture file with JSON_PRETTY_PRINT so that future diffs are easier to follow.

    Props jnylen0, netweb.
    Fixes #39264.

    Built from https://develop.svn.wordpress.org/trunk@40061


    git-svn-id: http://core.svn.wordpress.org/trunk@39998 1a063a9b-81f0-0310-95a4-ce76da25c4cd