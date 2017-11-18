commit cd45e79ab272055de8165914bfc07f481268f1a2
Author: Mikhail Golubev <mikhail.golubev@jetbrains.com>
Date:   Fri Oct 21 20:39:54 2016 +0300

    PY-17265 Placement of inserted function is consistent with Move refactoring

    Namely, if a function is being moved to another file it will be
    inserted at its end unless there is a top-level usage of it. In this
    case the generated function will be inserted right before the first
    such usage so as not to produce unresolved references. If function
    stays in the same file, it will be inserted after its original parent
    statement i.e. another function or a class, again if there is no
    conflicting usages.