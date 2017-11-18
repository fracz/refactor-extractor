commit e3c761c87efdee7c28461559015fa76602d4605a
Author: Yohei Yukawa <yukawa@google.com>
Date:   Thu Dec 3 18:12:40 2015 -0800

    Add @hide SpellCheckerSubtype#getLocaleObject().

    This is a mechanical refactoring with no behavior change.

    With this CL, InputMethodSubtype and SpellCheckerSubtype have the same
    getLocaleObject() hidden API, which makes it easy to share the logic in
    subsequent CLs.

    Bug: 11736916
    Bug: 20696126
    Bug: 22858221
    Change-Id: I39dc0c310158ad23ba6c987efce07deaf30ce693