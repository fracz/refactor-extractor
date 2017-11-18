commit c7e9231a868c90bc062ccdbcd82f1387f6cedda6
Author: Ryan Ernst <ryan@iernst.net>
Date:   Wed Apr 19 09:09:34 2017 -0700

    Plugins: Remove leniency for missing plugins dir (#24173)

    This leniency was left in after plugin installer refactoring for 2.0
    because some tests still relied on it. However, the need for this
    leniency no longer exists.