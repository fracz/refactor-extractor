commit bbad298bf6ac335c9c32939eaf50bbffd5023360
Author: Jesse Vincent <jesse@bestpractical.com>
Date:   Tue Jul 30 16:36:47 2013 -0400

    Convert OpenMode from an Enum to static ints for perf improvement.
    (Based on profiling of long folder list opens)

    This should be backported to 4.4