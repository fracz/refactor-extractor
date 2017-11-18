commit ba9f2143030d1b781724f04adaac254af3326930
Author: nmittler <nathanmittler@google.com>
Date:   Mon Mar 30 11:34:57 2015 -0700

    Removing unnecessary use of Math.ceil in HTTP/2 priority algorithm.

    Motivation:

    We're currently using Math.ceil which isn't necessary. We should exchange for a lighter weight operation.

    Modifications:

    Changing the logic to just ensure that we allocate at least one byte to the child rather than always performing a ceil.

    Result:

    Slight performance improvement in the priority algorithm.