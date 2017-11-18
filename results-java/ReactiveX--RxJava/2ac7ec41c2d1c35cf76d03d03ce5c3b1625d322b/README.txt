commit 2ac7ec41c2d1c35cf76d03d03ce5c3b1625d322b
Author: Ben Christensen <benjchristensen@netflix.com>
Date:   Thu Jan 16 12:42:41 2014 -0800

    Re-implemented Take Operator with Bind

    - also simplified implementation to not worry about thread-safety as per Rx contract
    - performance improvement from 4,033,468 ops/sec -> 6,731,287 ops/sec