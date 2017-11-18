commit 94e3330e639e1b96f5bd000f4c0f743b3cc174fc
Author: Yohei Yukawa <yukawa@google.com>
Date:   Fri Feb 12 19:37:03 2016 -0800

    Remove redundant arguments.

    The "list" and "map" arguments of IMMS#buildInputMethodListLocked() are
    nothing more than synonyms of IMMS#mMethodList and IMMS#mMethodMap,
    respectively.  There is no reason to pass them as parameters.  We can
    access them directly as we have done there for other member fields.

    This is kind of a mechanical refactoring.  No behavior change is
    intended.

    Bug: 26279466
    Change-Id: Ia27e19f9358ba33abbb1e5a27cebe7c9953c998f