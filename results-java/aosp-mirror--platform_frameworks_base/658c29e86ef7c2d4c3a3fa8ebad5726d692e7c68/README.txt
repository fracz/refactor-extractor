commit 658c29e86ef7c2d4c3a3fa8ebad5726d692e7c68
Author: Yohei Yukawa <yukawa@google.com>
Date:   Fri Dec 4 14:43:01 2015 -0800

    retry: Add @hide SpellCheckerSubtype#getLocaleObject().

    This is the 2nd try of I39dc0c310158ad23ba6c987efce07deaf30ce693.

    This is a mechanical refactoring with no behavior change.

    With this CL, InputMethodSubtype and SpellCheckerSubtype have the same
    getLocaleObject() hidden API, which makes it easy to share the logic in
    subsequent CLs.

    No behavior change is intended.

    Bug: 11736916
    Bug: 20696126
    Bug: 22858221
    Change-Id: I51be014c752b736a808e2b0d56e664941a218a2f