commit 41ae784a6fdeb26696c3b7acfc2ddded6eae0ab0
Author: Jason Tedor <jason@tedor.me>
Date:   Tue Nov 22 18:39:14 2016 -0500

    Refactor handling of bind permissions

    This commit refactors the handling of bind permissions, which is in need
    of a little cleanup. For example, in its current state, the code for
    handling permissions for transport profiles is split across two
    methods. This commit refactors this code hopefully making it easier to
    work with in future changes. This change is mostly mechanical, no
    functionality is changed.

    Relates #21742