commit b752e7685891bc3d00fad51627161437e78ae6b5
Author: Eric Anderson <ejona@google.com>
Date:   Thu Feb 4 16:55:31 2016 -0800

    Automated readability/efficiency tweaks

    Although the changes were determined automatically, they were manually
    applied to the codebase.

    ClientCalls actually has a bug fix, since the suggestion to add
    interrupt() made it obvious that interrupted() was inappropriate.