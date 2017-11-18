commit eb136417465e13f0ba17c2ae8efdb9a45c22a315
Author: Ryan Lubke <ryan.lubke@oracle.com>
Date:   Fri Jun 22 11:38:40 2012 -0700

    Modularization of AHC.

    Some warts due to the refactoring:
      - duplicated api/test/resources to providers/grizzly/test/resources and providers/netty/test/resources.
        There's probably a better way to share resources between modules, but for an initial commit, this
        should be fine.

    Tip:
      - When checking the history of files, you may need to pass the --follow flag to the log command.