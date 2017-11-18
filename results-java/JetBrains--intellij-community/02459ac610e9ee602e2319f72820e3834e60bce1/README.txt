commit 02459ac610e9ee602e2319f72820e3834e60bce1
Author: Ilya.Kazakevich <Ilya.Kazakevich@jetbrains.com>
Date:   Wed Jun 24 22:37:55 2015 +0300

    TypeEvalContextCache improved for  PY-16282 according to IDEA-CR-3330.

    It now uses ProjectService, so each project has its own cache, and cache is destroyed when project is closed.