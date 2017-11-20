commit 623e0c3fefd7ff0b3e479cdfea43d13c7d210638
Author: Jonathan Gerrish <jonathan@indiekid.rg>
Date:   Fri Dec 2 12:05:15 2016 -0800

    Performance + Memory improvements - Application's resource loader is no longer tied to the Android Runtime's resource loader so we don't need to cache per sdk. Also cache the compile time SDK resource loader as this is constant across all tests.