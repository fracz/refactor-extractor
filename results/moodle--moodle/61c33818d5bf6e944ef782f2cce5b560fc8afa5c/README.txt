commit 61c33818d5bf6e944ef782f2cce5b560fc8afa5c
Author: skodak <skodak>
Date:   Sat Jun 23 16:33:17 2007 +0000

    MDL-10231 merge grade_calculations and grade_items table + calculation improvements
    MDL-10233 fixed grade_*::fetch() - does not modify $this anymore, we can now use it from normal methods to fetch other objects of the same class.