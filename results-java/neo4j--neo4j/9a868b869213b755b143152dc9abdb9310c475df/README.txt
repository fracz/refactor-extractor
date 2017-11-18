commit 9a868b869213b755b143152dc9abdb9310c475df
Author: Mattias Persson <mattias@neotechnology.com>
Date:   Fri May 22 15:37:22 2015 +0200

    Fixes legacy index tx state issue where deleted entities might be visible

    when querying. Problem was when querying w/ start/end node. As it was the
    tx state didn't have the start/end node... something that a refactoring
    between 2.1 and 2.2 introduced.