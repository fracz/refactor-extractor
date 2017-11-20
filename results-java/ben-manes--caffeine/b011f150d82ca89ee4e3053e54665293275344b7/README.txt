commit b011f150d82ca89ee4e3053e54665293275344b7
Author: Ben Manes <ben.manes@gmail.com>
Date:   Sat Apr 2 13:11:21 2016 -0700

    Improve the chance for escape analysis to avoid alloc on weak key write

    For a get(key) the lookup key is not allocated because the JIT detects
    the limited scope and that it never escapes the thread. For a write it
    often was not due to the retry loop and long method. By making the
    lookup key a simpler value object the JIT is more often able to guess
    correctly. By inlining it into the retry loop it is even more likely,
    resulting in a 10M+ ops/s improvement.