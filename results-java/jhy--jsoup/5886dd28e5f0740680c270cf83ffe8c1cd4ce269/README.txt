commit 5886dd28e5f0740680c270cf83ffe8c1cd4ce269
Author: Jonathan Hedley <jonathan@hedley.net>
Date:   Sat Apr 2 14:41:41 2016 -0700

    Perf improvement during parsing.

    Don't binary search for chars in short lists, just scan.