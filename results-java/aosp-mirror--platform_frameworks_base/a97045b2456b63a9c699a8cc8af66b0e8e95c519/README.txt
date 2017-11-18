commit a97045b2456b63a9c699a8cc8af66b0e8e95c519
Author: Todd Kennedy <toddke@google.com>
Date:   Fri Jun 10 16:42:00 2016 -0700

    New API w/ multiple prefixes

    Use the new API that contains multiple hash prefixes and a mask. Also
    do some small refactoring necessary to handle multiple prefixes and
    use a common implementation of the hash generation

    Change-Id: Ib52f767ea6aadc30c67c5bdee949e9f9c5f04e44