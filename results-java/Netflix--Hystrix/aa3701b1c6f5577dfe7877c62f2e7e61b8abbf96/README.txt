commit aa3701b1c6f5577dfe7877c62f2e7e61b8abbf96
Author: Ben Christensen <benjchristensen@netflix.com>
Date:   Mon May 13 12:30:37 2013 -0700

    Fix non-deterministic test

    - it is relying on scheduling of the collapser timer
    - this is an example so specifically should not be refactored to use artifical time