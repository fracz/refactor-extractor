commit a82346f440b3b87f4f1769ebaa7e8ede20c66a9e
Author: Ben Manes <ben.manes@gmail.com>
Date:   Tue May 12 02:16:51 2015 -0700

    Replace enum with int for drain status

    This has a minor improvement to memory usage and the read/write
    benchmark. It replaces a megamorphic method dispatch with an inlined
    switch statement. Overall should be about the same, but can't hurt.