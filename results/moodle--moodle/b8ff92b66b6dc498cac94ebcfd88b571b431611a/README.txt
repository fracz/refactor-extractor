commit b8ff92b66b6dc498cac94ebcfd88b571b431611a
Author: skodak <skodak>
Date:   Mon Jun 18 13:43:40 2007 +0000

    MDL-9137 various grading improvements
    1/ initial support for migration of old grade_items and categories (not tested)
    2/ rewritten grade update and calculation logic
    3/ initial support for calculation formulas
    4/ minor API refactoring and cleanup
    5/ various small bugfixes
    6/ fixed use of grademax with scales
    7/ fixed some unit tests

    TODO:
    * implement proper locking of grades - needs discussion
    * force recalculation of all formulas after adding/removing/changing of grade items
    * better delete flag support
    * support for NULLs n backup - Eloy already proposed a solution
    * support for NULLs in set_field()
    * speedup
    * more unit tests nd functional tests