commit aa616f31fe7c0c8e3657bb9a5889ec5e56ee5232
Author: Philip Milne <pmilne@google.com>
Date:   Fri May 27 18:38:01 2011 -0700

    Response to code review for GridLayout:

    . Fixed spelling.
    . Added comments on internal methods.
    . Adopted the suggested internal name changes to improve clarity.
    . Added UNDEFINED constant to public API to avoid making reference to Integer.MAX_VALUE in docs.
    . Added final everywhere, then removed it.
    . Make the Interval class package private so that it can be put somewhere more general later.
    . Tidy code, removing maximize flag throughout.
    . Remove last of allocations taking place during layout.
    . Implement measureChild() etc.
    . Added LinearLayout alignment compatibility mode, and made it the default.

    Change-Id: I6a4ffa022d97d68138d1903d3830a20278815435
    https://android-git.corp.google.com/g/#change,109891