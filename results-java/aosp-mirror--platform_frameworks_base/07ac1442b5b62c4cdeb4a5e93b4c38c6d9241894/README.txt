commit 07ac1442b5b62c4cdeb4a5e93b4c38c6d9241894
Author: Narayan Kamath <narayan@google.com>
Date:   Fri Feb 10 15:08:07 2017 +0000

    PackageParser: String interning fixes.

    Stop interning string metadata values as well as class names as it's
    unlikely there will be much duplication among these.

    Also make sure we intern the same set of strings when parsing packages
    from their cache entries as we do when parsing them from the package
    itself.

    This change also improves error reporting for the unit-test and fixes
    a failure that was introduced by a previous change (the addition of
    static libraries).

    Test: PackageParserTest
    Bug: 34726698

    Change-Id: Ia0d0342b91b3294bd5569756255918d1dc886e05