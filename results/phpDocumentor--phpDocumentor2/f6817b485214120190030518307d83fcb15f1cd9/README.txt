commit f6817b485214120190030518307d83fcb15f1cd9
Author: Mike van Riel <mike.vanriel@naenius.com>
Date:   Sun May 5 10:42:12 2013 +0200

    Fix #759; @internal tag should prevent parsing of element

    Elements that are tagged with @internal should only be processed if the
    project is configured to process internal visibility (default is off).
    This behaviour was broken during the beta refactoring, or didn't function
    properly to begin with.

    With this commit the functionality is re-introduced and moved a point where
    it can be cached.

    An edge case was introduced here; if the visibility changes than the cache
    would contain invalid data because it won't recognize that the files were
    changed. Issue #808 and #809 are created to combat this edge case and
    prevent future edge cases with Project settings.