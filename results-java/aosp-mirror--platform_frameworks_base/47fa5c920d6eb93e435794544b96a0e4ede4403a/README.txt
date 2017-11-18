commit 47fa5c920d6eb93e435794544b96a0e4ede4403a
Author: Deepanshu Gupta <deepanshu@google.com>
Date:   Mon Jul 7 14:22:19 2014 -0700

    Optimize Blend composites. [DO NOT MERGE]

    Removed redundant array allocations to improve performance for various
    blending modes.

    Change-Id: Iaba1d6ff3ad03eebdc859c599b610cc593370438
    (cherry picked from commit 9ce074610413ce3a5dd0cef9295f0ae9061402b7)