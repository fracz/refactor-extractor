commit 9ce074610413ce3a5dd0cef9295f0ae9061402b7
Author: Deepanshu Gupta <deepanshu@google.com>
Date:   Mon Jul 7 14:22:19 2014 -0700

    Optimize Blend composites.

    Removed redundant array allocations to improve performance for various
    blending modes.

    Change-Id: Iaba1d6ff3ad03eebdc859c599b610cc593370438