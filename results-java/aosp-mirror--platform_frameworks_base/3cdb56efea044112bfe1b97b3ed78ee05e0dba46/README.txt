commit 3cdb56efea044112bfe1b97b3ed78ee05e0dba46
Author: Dianne Hackborn <hackbod@google.com>
Date:   Wed Nov 11 12:45:44 2015 -0800

    Some debugging improvements.

    - Fix dumping of package manager intent filters so the option
      to print the filter detail works again.
    - Extend dump resolvers to allow you to specify the specific
      types of resolvers you'd like to dump.
    - Add new package manager commands for querying activities,
      services, receivers.
    - Move the code for parsing a command line into an intent to
      the framework, so it can be used by the new package manager
      commands and later elsewhere.

    Change-Id: I56ea2bb8c3dd0e5198ee333be8f41ad9dcdb626f