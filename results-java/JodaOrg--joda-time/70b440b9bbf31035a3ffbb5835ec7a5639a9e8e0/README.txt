commit 70b440b9bbf31035a3ffbb5835ec7a5639a9e8e0
Author: Stephen Colebourne <scolebourne@joda.org>
Date:   Mon Dec 1 14:19:55 2014 +0000

    Fix name provider

    Name provider relied on tzdb short names matching JDK data names
    This has ceased to be true due to recent changes in tzdb
    Add improved matching based on summer/winter
    Data returned still depends on the JDK data, not the tzdb
    Fixes #175