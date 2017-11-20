commit 4ec042446ad76ae68120137d3bb9515901d6a11d
Author: Ben Manes <ben.manes@gmail.com>
Date:   Thu May 7 16:27:24 2015 -0700

    Extract trace format logic from event object

    The data object shouldn't include the logic for reading and writing itself into
    a particular file format. That was done for simplicity while hashing out other
    details and should have been refactored from the API before release. This is a
    cosmetic fix and the deprecated methods will be removed in version 2.0.0.