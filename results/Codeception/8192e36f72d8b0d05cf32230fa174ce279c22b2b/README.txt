commit 8192e36f72d8b0d05cf32230fa174ce279c22b2b
Author: Philipp Kretzschmar <philipp.kretzschmar@gmail.com>
Date:   Mon Jun 20 22:55:52 2016 +0200

    Diff shows only changed lines (#3242)

    Diff shows only changed lines

    The diff used to show both complete strings instead of only the changed lines,
    I've added a unit test for the case and refactored a bit inside the Console.php