commit 24cd70058d081b93061734aec29e05f9e37abb3a
Author: lucastetreault <lucastetreault@gmail.com>
Date:   Fri Sep 11 22:11:57 2015 -0600

    refactor(*): use `isDefined` and `isUndefined` consistently

    Fix any place that compares with `undefined` to use `isUndefined` and `isDefined` instead.

    Closes #4365
    Closes #12831