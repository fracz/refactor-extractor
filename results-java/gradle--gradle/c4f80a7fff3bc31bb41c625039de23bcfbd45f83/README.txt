commit c4f80a7fff3bc31bb41c625039de23bcfbd45f83
Author: Jendrik Johannes <jendrik@gradle.com>
Date:   Fri Oct 6 09:17:39 2017 +0200

    Improve binary compatibility check report

    The report now corresponds to what we had in the JDiff reports before:
    - It also shows changes to Incubating API
    - It excludes some more redundant change types for better readability

    The report generator now also reports (and fails) for missing @since
    tags. This is implemented by checking the source of a class that
    includes new API elements suing the 'com.github.javaparser'.

    Only changes to non-incubating public API need to be accepted
    explicitly in accept-public-api-changes.json.