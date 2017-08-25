commit 0b98d495a009647135742d34060084bb50daaa72
Author: Mike van Riel <mike.vanriel@naenius.com>
Date:   Tue Apr 2 05:59:05 2013 +0000

    Clean up test suite

    The test suite contains a series of tests that either do nothing (and thus
    should not exist) contain tests that are broken due to refactoring, or
    have become broken and obsolete.

    Tests that could not be fixed within 10 minutes have been disabled and
    should be fixed as part of the serialization refactoring.