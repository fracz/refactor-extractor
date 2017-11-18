commit f27a1cb3c1a9675e9ddb27d0e7ad0993b123eba9
Author: Adam Murdoch <adam@gradle.com>
Date:   Tue Oct 27 08:56:44 2015 +1100

    Fixed linking of test suite binaries into the top level `binaries` container.

    There is currently quite a lot of logic that is duplicated between the base component plugins and test suite plugins. This change does not improve this situation, except to add some test coverage, so that we can later remove the duplication without breaking things.